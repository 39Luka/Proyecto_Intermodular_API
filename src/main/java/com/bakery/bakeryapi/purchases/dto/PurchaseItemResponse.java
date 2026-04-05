package com.bakery.bakeryapi.purchasess.dto;

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


