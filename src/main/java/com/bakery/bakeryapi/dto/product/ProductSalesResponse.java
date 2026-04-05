package com.bakery.bakeryapi.dto.product;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


