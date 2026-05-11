package com.bakery.bakeryapi.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating or updating a product category.
 */
public record CategoryRequest(
        @Schema(description = "Category name", example = "Bread")
        @NotBlank String name
) {
}


