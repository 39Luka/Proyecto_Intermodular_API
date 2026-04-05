package com.bakery.bakeryapi.purchasess.dto;

import com.bakery.bakeryapi.purchasess.PurchaseStatus;

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


