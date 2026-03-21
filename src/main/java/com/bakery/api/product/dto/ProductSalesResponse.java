package com.bakery.api.product.dto;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


