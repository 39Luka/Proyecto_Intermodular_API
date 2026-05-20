package com.bakery.bakeryapi.domain;

import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.product.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    /**
     * CP-DOM.01: decreaseStock_withInsufficientStock_throws
     * Valida que la entidad Producto impida reducciones de stock por debajo de cero, lanzando la excepción correspondiente.
     */
    @Test
    void decreaseStock_withInsufficientStock_throws() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 1, new Category("Bread"));
        assertThrows(InsufficientStockException.class, () -> product.decreaseStock(2));
    }

    /**
     * CP-DOM.02: decreaseStock_reducesStock
     * Verifica la lógica interna de decremento de inventario tras una venta.
     */
    @Test
    void decreaseStock_reducesStock() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        product.decreaseStock(3);
        assertEquals(7, product.getStock());
    }

    /**
     * CP-DOM.03: increaseStock_increasesStock
     * Verifica la lógica interna de incremento de inventario tras una cancelación o reposición.
     */
    @Test
    void increaseStock_increasesStock() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        product.increaseStock(5);
        assertEquals(15, product.getStock());
    }
}

