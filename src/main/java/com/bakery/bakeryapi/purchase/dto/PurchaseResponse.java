package com.bakery.bakeryapi.purchase.dto;

import com.bakery.bakeryapi.domain.Purchase;
import com.bakery.bakeryapi.domain.PurchaseStatus;

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
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getUser().getId(),
                purchase.getCreatedAt(),
                purchase.getStatus(),
                purchase.getTotal(),
                purchase.getItems().stream().map(PurchaseItemResponse::from).toList()
        );
    }
}


