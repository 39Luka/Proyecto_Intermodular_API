package com.bakery.bakeryapi.product.dto;

/**
 * Proyección para el ranking de ventas de productos.
 */
public record ProductSalesResponse(
        Long productId,
        String productName,
        Long totalQuantity
) {
}


