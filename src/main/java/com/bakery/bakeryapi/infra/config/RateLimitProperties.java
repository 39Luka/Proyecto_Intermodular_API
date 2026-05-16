package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de límite de velocidad para puntos finales de autenticación.
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        Integer requestsPerMinute
) {
    public RateLimitProperties {
        if (requestsPerMinute == null) {
            requestsPerMinute = 100;
        }
    }
}
