package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS configuration properties.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        String allowedOrigins
) {
}

