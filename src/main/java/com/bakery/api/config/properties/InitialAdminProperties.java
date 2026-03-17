package com.bakery.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "initial.admin")
public record InitialAdminProperties(
        String email,
        String password
) {
}

