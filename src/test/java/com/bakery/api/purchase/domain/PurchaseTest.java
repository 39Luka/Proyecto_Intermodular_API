package com.bakery.api.purchase.domain;

import com.bakery.api.category.Category;
import com.bakery.api.product.Product;
import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseTest {

    @Test
    void addItem_accumulatesTotal() {
        User user = new User("u@example.com", "pass", Role.USER);
        Purchase purchase = new Purchase(user, LocalDateTime.now(), PurchaseStatus.CREATED);

        Product product = new Product("Baguette", null, new BigDecimal("2.00"), 10, new Category("Bread"));
        PurchaseItem item1 = new PurchaseItem(product, null, 1, product.getPrice(), BigDecimal.ZERO, new BigDecimal("2.00"));
        PurchaseItem item2 = new PurchaseItem(product, null, 2, product.getPrice(), BigDecimal.ZERO, new BigDecimal("4.00"));

        purchase.addItem(item1);
        purchase.addItem(item2);

        assertEquals(new BigDecimal("6.00"), purchase.getTotal());
        assertEquals(2, purchase.getItems().size());
    }
}

