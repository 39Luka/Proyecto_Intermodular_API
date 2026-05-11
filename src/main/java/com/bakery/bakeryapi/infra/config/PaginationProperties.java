package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Pagination limits used to clamp client-provided page sizes.
 */
@ConfigurationProperties(prefix = "app.pagination")
public record PaginationProperties(
        Integer maxPageSize
) {
    public PaginationProperties {
        if (maxPageSize == null) {
            maxPageSize = 100;
        }
        if (maxPageSize < 1) {
            maxPageSize = 1;
        }
    }
}

