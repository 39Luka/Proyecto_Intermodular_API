package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración de CORS.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        String allowedOrigins
) {
}

