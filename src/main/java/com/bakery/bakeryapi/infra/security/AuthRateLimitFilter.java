package com.bakery.bakeryapi.infra.security;

import com.bakery.bakeryapi.infra.config.RateLimitProperties;
import com.bakery.bakeryapi.shared.exception.TooManyRequestsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final HandlerExceptionResolver exceptionResolver;
    private final int requestsPerMinute;
    private final Map<String, CounterWindow> counters = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            RateLimitProperties rateLimitProperties
    ) {
        this.exceptionResolver = exceptionResolver;
        this.requestsPerMinute = rateLimitProperties.requestsPerMinute();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !"/auth/login".equals(path) && !"/auth/register".equals(path) && !"/auth/refresh".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        String key = clientIp(request) + ":" + path;
        CounterWindow window = counters.computeIfAbsent(key, ignored -> new CounterWindow());
        long retryAfterSeconds;

        synchronized (window) {
            Instant now = Instant.now();
            if (window.windowStart.plus(WINDOW).isBefore(now) || window.windowStart.plus(WINDOW).equals(now)) {
                window.windowStart = now;
                window.count = 0;
            }

            if (window.count >= requestsPerMinute) {
                retryAfterSeconds = Duration.between(now, window.windowStart.plus(WINDOW)).toSeconds() + 1;
                response.setHeader("Retry-After", String.valueOf(Math.max(retryAfterSeconds, 1)));
                exceptionResolver.resolveException(
                        request,
                        response,
                        null,
                        new TooManyRequestsException("Too many authentication requests, please retry later")
                );
                return;
            }

            window.count++;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class CounterWindow {
        private Instant windowStart = Instant.now();
        private int count = 0;
    }
}
