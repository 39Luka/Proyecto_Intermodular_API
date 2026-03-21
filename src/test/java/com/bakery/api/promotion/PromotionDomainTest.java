package com.bakery.api.promotion;

import com.bakery.api.category.Category;
import com.bakery.api.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionDomainTest {

    @Test
    void isActiveOn_withNullEndDate_neverExpiresWhileActive() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        Promotion promo = new PercentagePromotion("10% OFF", new BigDecimal("10.00"), LocalDate.now(), null, product);

        assertTrue(promo.isActiveOn(LocalDate.now()));
        assertTrue(promo.isActiveOn(LocalDate.now().plusYears(10)));
    }

    @Test
    void isActiveOn_beforeStartDate_isFalse() {
        Product product = new Product("Baguette", null, new BigDecimal("1.00"), 10, new Category("Bread"));
        Promotion promo = new PercentagePromotion("10% OFF", new BigDecimal("10.00"), LocalDate.now().plusDays(1), null, product);
        assertFalse(promo.isActiveOn(LocalDate.now()));
    }

    @Test
    void percentagePromotion_calculatesDiscount() {
        Product product = new Product("Baguette", null, new BigDecimal("2.00"), 10, new Category("Bread"));
        PercentagePromotion promo = new PercentagePromotion("10% OFF", new BigDecimal("10.00"), LocalDate.now(), null, product);
        assertEquals(new BigDecimal("0.40000"), promo.calculateDiscountAmount(product.getPrice(), 2).setScale(5));
    }
}

