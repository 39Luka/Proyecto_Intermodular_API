package org.example.bakeryapi.product.dto.response;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


