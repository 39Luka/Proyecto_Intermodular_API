package com.bakery.api.product.dto.response;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


