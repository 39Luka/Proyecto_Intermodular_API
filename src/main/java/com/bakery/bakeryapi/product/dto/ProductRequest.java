package com.bakery.bakeryapi.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Cuerpo de solicitud para crear o actualizar un producto.
 */
public record ProductRequest(
        @Schema(description = "Nombre del producto", example = "Baguette")
        @NotBlank String name,
        @Schema(description = "Descripción opcional", example = "Pan francés clásico")
        String description,
        @Schema(description = "Precio unitario", example = "1.50")
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Schema(description = "Stock actual", example = "50")
        @PositiveOrZero int stock,
        @Schema(description = "ID de la categoría", example = "1")
        @NotNull Long categoryId,
        @Schema(description = "Imagen del producto en formato base64 (opcional)", type = "string")
        String imageBase64
) {
}


