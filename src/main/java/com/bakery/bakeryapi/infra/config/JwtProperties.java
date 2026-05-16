package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de configuración de JWT.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long expiration,
        Long refreshExpiration
) {
}

