package com.bakery.bakeryapi.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Cuerpo de solicitud para crear una compra.
 */
public record PurchaseRequest(
        @Schema(description = "Opcional. ID del usuario propietario. Requerido para administradores; ignorado/resuelto desde el JWT para usuarios regulares.", example = "1")
        Long userId,
        @Schema(description = "Artículos en la compra")
        @NotEmpty @Valid List<PurchaseItemRequest> items
) {
}


