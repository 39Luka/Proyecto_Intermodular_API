package com.bakery.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record AppDataSourceProperties(
        String url,
        String username,
        String password
) {
}

