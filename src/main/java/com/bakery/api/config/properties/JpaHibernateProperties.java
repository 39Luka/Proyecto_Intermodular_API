package com.bakery.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jpa.hibernate")
public record JpaHibernateProperties(
        String ddlAuto
) {
}

