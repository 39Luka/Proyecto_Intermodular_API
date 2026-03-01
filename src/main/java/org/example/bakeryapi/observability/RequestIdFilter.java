package org.example.bakeryapi.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a request id to every request for log correlation.
 * - If the client sends X-Request-Id, we keep it.
 * - Otherwise we generate a UUID.
 *
 * The id is stored in MDC under "requestId" and echoed back as X-Request-Id.
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    static final String MDC_KEY = "requestId";

    @Value("${app.request-id.header:X-Request-Id}")
    private String headerName;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String incoming = request.getHeader(headerName);
        String requestId = incoming == null || incoming.isBlank()
                ? UUID.randomUUID().toString()
                : incoming.trim();

        MDC.put(MDC_KEY, requestId);
        response.setHeader(headerName, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}

