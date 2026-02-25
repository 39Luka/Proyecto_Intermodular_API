package org.example.bakeryapi.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.example.bakeryapi.product.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("PERCENTAGE")
public class PercentagePromotion extends Promotion {

    @Column(precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    protected PercentagePromotion() {
        // Constructor for JPA
    }

    public PercentagePromotion(
            String description,
            BigDecimal discountPercentage,
            LocalDate startDate,
            LocalDate endDate,
            Product product
    ) {
        super(description, startDate, endDate, product);
        this.discountPercentage = discountPercentage;
    }

    @Override
    public BigDecimal calculateDiscountAmount(BigDecimal unitPrice, int quantity) {
        if (quantity <= 0 || unitPrice == null || discountPercentage == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = discountPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).multiply(rate);
    }

    @Override
    public String getType() {
        return "PERCENTAGE";
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
}


