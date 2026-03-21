package com.bakery.api.purchase.dto;

import com.bakery.api.purchase.PurchaseStatus;

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
}


