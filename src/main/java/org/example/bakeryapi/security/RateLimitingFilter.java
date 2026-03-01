package org.example.bakeryapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bakeryapi.common.exception.ErrorResponse;
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

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    @Value("${app.rate-limit.max-requests:30}")
    private int maxRequests;

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

