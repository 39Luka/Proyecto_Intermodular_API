package com.bakery.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.bakery.api.common.exception.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiting for public auth endpoints.
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.equals("/auth/login") && !path.equals("/auth/register");
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
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // If multiple IPs are present, the first one is the original client.
            int comma = forwardedFor.indexOf(',');
            return (comma >= 0 ? forwardedFor.substring(0, comma) : forwardedFor).trim();
        }
        return request.getRemoteAddr();
    }

    private record WindowCounter(long windowStartSeconds, AtomicInteger count) {
    }
}
