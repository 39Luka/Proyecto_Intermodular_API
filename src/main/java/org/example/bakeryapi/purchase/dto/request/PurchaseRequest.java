package org.example.bakeryapi.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseRequest(
        @NotNull Long userId,
        @NotEmpty @Valid List<PurchaseItemRequest> items
) {
}


