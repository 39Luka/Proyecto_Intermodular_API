package com.bakery.bakeryapi.product.dto;

import com.bakery.bakeryapi.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Base64;

/**
 * Datos del producto devueltos por la API.
 */
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stock,
        boolean active,
        Long categoryId,
        @Schema(description = "Imagen del producto en formato base64 (null si no está establecida)", type = "string")
        String imageBase64
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.isActive(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getImage() == null ? null : Base64.getEncoder().encodeToString(product.getImage())
        );
    }
}


