package com.bakery.bakeryapi.product.dto;

/**
 * Projection for product sales ranking.
 */
public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


