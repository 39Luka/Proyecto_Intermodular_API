package com.bakery.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.bakery.api.common.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiting.
 *
 * This is not meant to be perfect across multiple replicas. For that, use a shared store (Redis) or gateway rate limiting.
 * It is still useful to protect against accidental abuse and basic brute-force attempts.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private volatile long lastCleanupSeconds = 0;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${app.rate-limit.max-requests:30}")
    private int maxRequests;

    @Value("${app.rate-limit.max-entries:10000}")
    private int maxEntries;

    @Value("${app.rate-limit.cleanup-interval-seconds:30}")
    private int cleanupIntervalSeconds;

    /**
     * Comma-separated list of URI prefixes to exclude from rate limiting.
     * Example: /swagger-ui,/v3/api-docs,/actuator/health,/actuator/info
     */
    @Value("${app.rate-limit.excluded-path-prefixes:}")
    private String excludedPathPrefixes;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
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

        long nowSeconds = Instant.now().getEpochSecond();
        cleanupIfNeeded(nowSeconds);

        WindowCounter counter = counters.compute(clientKey, (k, current) -> {
            if (current == null || (nowSeconds - current.windowStartSeconds) >= windowSeconds) {
                return new WindowCounter(nowSeconds, new AtomicInteger(1));
            }
            current.count.incrementAndGet();
            return current;
        });

        if (counter.count.get() > maxRequests) {
            int retryAfter = (int) Math.max(1, (counter.windowStartSeconds + windowSeconds) - nowSeconds);

            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ErrorResponse(429, "Too many requests, please retry later")
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private List<String> excludedPrefixes() {
        if (excludedPathPrefixes == null || excludedPathPrefixes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(excludedPathPrefixes.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private void cleanupIfNeeded(long nowSeconds) {
        int interval = cleanupIntervalSeconds <= 0 ? 30 : cleanupIntervalSeconds;
        if ((nowSeconds - lastCleanupSeconds) < interval) {
            return;
        }

        // Best-effort cleanup. If multiple threads run cleanup concurrently it's fine.
        lastCleanupSeconds = nowSeconds;

        // Remove expired windows so the map doesn't grow forever with unique IPs.
        counters.entrySet().removeIf(e -> (nowSeconds - e.getValue().windowStartSeconds) >= windowSeconds);

        // Hard cap: if still too large, evict arbitrary keys until we're below the limit.
        // This isn't perfect, but it keeps memory bounded without disabling rate limiting completely.
        if (maxEntries > 0 && counters.size() > maxEntries) {
            int toRemove = counters.size() - maxEntries;
            for (String key : counters.keySet()) {
                counters.remove(key);
                toRemove--;
                if (toRemove <= 0) {
                    break;
                }
            }
        }
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
            // If multiple IPs are present, the first one is the original client.
            int comma = forwardedFor.indexOf(',');
            return "ip:" + (comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor).trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private record WindowCounter(long windowStartSeconds, AtomicInteger count) {
    }
}
