package com.bakery.bakeryapi.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de solicitud para crear o actualizar una categoría de producto.
 */
public record CategoryRequest(
        @Schema(description = "Nombre de la categoría", example = "Pan")
        @NotBlank String name
) {
}


