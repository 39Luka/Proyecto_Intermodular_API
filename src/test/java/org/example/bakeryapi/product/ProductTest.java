package org.example.bakeryapi.product;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.product.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void decreaseStock_withInsufficientStock_throws() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 1, new Category("Bread"));
        assertThrows(InsufficientStockException.class, () -> product.decreaseStock(2));
    }

    @Test
    void decreaseStock_reducesStock() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        product.decreaseStock(3);
        assertEquals(7, product.getStock());
    }

    @Test
    void increaseStock_increasesStock() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        product.increaseStock(5);
        assertEquals(15, product.getStock());
    }
}

