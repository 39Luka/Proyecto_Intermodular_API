package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate-limit settings for authentication endpoints.
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
