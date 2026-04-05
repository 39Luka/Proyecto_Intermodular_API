package com.bakery.bakeryapi.dto.purchase;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PurchaseRequest(
        @Schema(description = "Optional. Owner user id. Required for admins; ignored/resolved from JWT for regular users.", example = "1")
        Long userId,
        @Schema(description = "Items in the purchase")
        @NotEmpty @Valid List<PurchaseItemRequest> items
) {
}


