package com.bakery.bakeryapi.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        boolean enabled,
        int windowSeconds,
        int maxRequests,
        int maxEntries,
        String excludedPathPrefixes
) {
}

