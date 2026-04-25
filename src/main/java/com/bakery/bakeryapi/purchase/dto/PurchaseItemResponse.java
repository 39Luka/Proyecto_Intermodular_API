package com.bakery.bakeryapi.purchase.dto;

import com.bakery.bakeryapi.domain.PurchaseItem;

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
    public static PurchaseItemResponse from(PurchaseItem item) {
        return new PurchaseItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getSubtotal(),
                item.getPromotion() == null ? null : item.getPromotion().getId()
        );
    }
}


