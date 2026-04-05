package com.bakery.bakeryapi.catalog.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @Schema(description = "Product name", example = "Baguette")
        @NotBlank String name,
        @Schema(description = "Optional description", example = "Classic French bread")
        String description,
        @Schema(description = "Unit price", example = "1.50")
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Schema(description = "Current stock", example = "50")
        @PositiveOrZero int stock,
        @Schema(description = "Category id", example = "1")
        @NotNull Long categoryId
) {
}


