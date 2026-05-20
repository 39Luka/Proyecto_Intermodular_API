package com.bakery.bakeryapi.shared.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de solicitud para alternar estado activo/habilitado.
 */
public record ActiveUpdateRequest(
        @NotNull(message = "el estado activo es requerido") Boolean active
) {
}

