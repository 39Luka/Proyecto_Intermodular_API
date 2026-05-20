package com.bakery.bakeryapi.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Cuerpo de solicitud para una línea de compra.
 */
public record PurchaseItemRequest(
        @Schema(description = "ID del producto", example = "1")
        @NotNull Long productId,
        @Schema(description = "Unidades a comprar", example = "2")
        @Positive int quantity,
        @Schema(description = "ID opcional de promoción (solo promociones de porcentaje)", example = "10")
        Long promotionId
) {
}


