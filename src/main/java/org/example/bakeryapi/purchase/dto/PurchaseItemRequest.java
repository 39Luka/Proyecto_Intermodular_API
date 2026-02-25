package org.example.bakeryapi.purchase.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseItemRequest(
        @NotNull Long productId,
        @Positive int quantity,
        Long promotionId
) {
}


