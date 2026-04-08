package com.bakery.bakeryapi.purchase.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal subtotal,
        Long promotionId
) {
}


