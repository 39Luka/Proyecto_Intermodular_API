package com.bakery.bakeryapi.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.refresh-token")
public record RefreshTokenProperties(
        long expirationMs
) {
}
