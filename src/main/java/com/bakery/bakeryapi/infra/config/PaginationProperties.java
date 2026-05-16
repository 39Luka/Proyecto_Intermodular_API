package com.bakery.bakeryapi.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Límites de paginación utilizados para fijar los tamaños de página proporcionados por el cliente.
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

