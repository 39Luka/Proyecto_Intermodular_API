package org.example.bakeryapi.product.dto;

public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


