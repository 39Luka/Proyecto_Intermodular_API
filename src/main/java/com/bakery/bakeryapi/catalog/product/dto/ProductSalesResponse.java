package com.bakery.bakeryapi.catalog.product.dto;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


