package com.bakery.bakeryapi.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active,
        Long categoryId,
        @Schema(description = "Product image as base64 (null if not set)", type = "string")
        String imageBase64
) {
}


