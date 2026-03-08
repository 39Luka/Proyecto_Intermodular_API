package com.bakery.api.purchase.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseItemRequest(
        @Schema(description = "Product id", example = "1")
        @NotNull Long productId,
        @Schema(description = "Units to buy", example = "2")
        @Positive int quantity,
        @Schema(description = "Optional promotion id (percentage promotions only)", example = "10")
        Long promotionId
) {
}


