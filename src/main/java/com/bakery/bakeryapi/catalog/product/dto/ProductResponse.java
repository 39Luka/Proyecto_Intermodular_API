package com.bakery.bakeryapi.catalog.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active,
        Long categoryId
) {
}


