package com.bakery.bakeryapi.purchasess.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseRequest(
        @Schema(description = "User id that owns the purchase", example = "1")
        @NotNull Long userId,
        @Schema(description = "Items in the purchase")
        @NotEmpty @Valid List<PurchaseItemRequest> items
) {
}


