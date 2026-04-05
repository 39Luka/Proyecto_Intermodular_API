package com.bakery.bakeryapi.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @Schema(description = "Category name", example = "Bread")
        @NotBlank String name
) {
}


