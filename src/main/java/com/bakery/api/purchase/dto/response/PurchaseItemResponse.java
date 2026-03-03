package com.bakery.api.purchase.dto.response;

import com.bakery.api.purchase.domain.PurchaseItem;

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
        Long promotionId = item.getPromotion() != null ? item.getPromotion().getId() : null;
        return new PurchaseItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountAmount(),
                item.getSubtotal(),
                promotionId
        );
    }
}


