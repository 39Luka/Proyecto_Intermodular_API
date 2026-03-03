package com.bakery.api.purchase.dto.response;

import com.bakery.api.purchase.domain.Purchase;
import com.bakery.api.purchase.domain.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseResponse(
        Long id,
        Long userId,
        LocalDateTime createdAt,
        PurchaseStatus status,
        BigDecimal total,
        List<PurchaseItemResponse> items
) {
    public static PurchaseResponse from(Purchase purchase) {
        List<PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                .map(PurchaseItemResponse::from)
                .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUser().getId(),
                purchase.getCreatedAt(),
                purchase.getStatus(),
                purchase.getTotal(),
                itemResponses
        );
    }
}


