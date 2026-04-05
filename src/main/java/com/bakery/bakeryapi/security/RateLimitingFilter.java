package com.bakery.bakeryapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bakery.bakeryapi.config.properties.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.http.HttpStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ProblemDetail;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiting.
 *
 * This is not meant to be perfect across multiple replicas. For that, use a shared store (Redis) or gateway rate limiting.
 * It is still useful to protect against accidental abuse and basic brute-force attempts.
 *
 * ADR: docs/adr/0003-rate-limiting-bucket4j.md
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final RateLimitProperties properties;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(ObjectMapper objectMapper, RateLimitProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * Comma-separated list of URI prefixes to exclude from rate limiting.
     * Example: /swagger-ui,/v3/api-docs,/actuator/health,/actuator/info
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.enabled()) {
            return true;
        }

        String path = request.getRequestURI();
        for (String prefix : excludedPrefixes()) {
            if (!prefix.isBlank() && path.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String clientKey = clientKey(request);
        Bucket bucket = bucketFor(clientKey);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long nanos = probe.getNanosToWaitForRefill();
            int retryAfter = nanos <= 0 ? 1 : (int) Math.max(1, (nanos + 999_999_999L) / 1_000_000_000L);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please retry later");
            problem.setTitle(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
            // Keep a consistent "message" property with the rest of the API error format.
            problem.setProperty("message", "Too many requests, please retry later");
            problem.setProperty("timestamp", Instant.now());

            response.getWriter().write(objectMapper.writeValueAsString(problem));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private List<String> excludedPrefixes() {
        String raw = properties.excludedPathPrefixes();
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private Bucket bucketFor(String clientKey) {
        // Create a "fixed window" style token bucket (refill all tokens each windowSeconds).
        int window = properties.windowSeconds() <= 0 ? 60 : properties.windowSeconds();
        int capacity = properties.maxRequests() <= 0 ? 30 : properties.maxRequests();
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(capacity, Duration.ofSeconds(window))
                .build();

        Bucket bucket = buckets.computeIfAbsent(clientKey, ignored -> Bucket.builder().addLimit(limit).build());

        // Hard cap: keep memory bounded with best-effort eviction of arbitrary keys.
        if (properties.maxEntries() > 0 && buckets.size() > properties.maxEntries()) {
            int toRemove = buckets.size() - properties.maxEntries();
            for (String key : buckets.keySet()) {
                buckets.remove(key);
                toRemove--;
                if (toRemove <= 0) {
                    break;
                }
            }
        }

        return bucket;
    }

    private String clientKey(HttpServletRequest request) {
        // Prefer authenticated identity when available; fall back to client IP.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getName() != null
                && !authentication.getName().isBlank()) {
            return "user:" + authentication.getName();
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Note: only trust X-Forwarded-For when running behind a proxy/load balancer that sets it.
            // If multiple IPs are present, the first one is the original client.
            int comma = forwardedFor.indexOf(',');
            return "ip:" + (comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor).trim();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
