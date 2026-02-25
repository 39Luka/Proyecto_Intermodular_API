package org.example.bakeryapi.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.example.bakeryapi.product.Product;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("BUY_X_PAY_Y")
public class BuyXPayYPromotion extends Promotion {

    @Column(nullable = false)
    private int buyQuantity;

    @Column(nullable = false)
    private int payQuantity;

    protected BuyXPayYPromotion() {
        // Constructor for JPA
    }

    public BuyXPayYPromotion(
            String description,
            int buyQuantity,
            int payQuantity,
            LocalDate startDate,
            LocalDate endDate,
            Product product
    ) {
        super(description, startDate, endDate, product);
        this.buyQuantity = buyQuantity;
        this.payQuantity = payQuantity;
    }

    @Override
    public BigDecimal calculateDiscountAmount(BigDecimal unitPrice, int quantity) {
        if (quantity < buyQuantity || unitPrice == null || buyQuantity <= payQuantity) {
            return BigDecimal.ZERO;
        }

        int groups = quantity / buyQuantity;
        int freeUnits = groups * (buyQuantity - payQuantity);

        return unitPrice.multiply(BigDecimal.valueOf(freeUnits));
    }

    @Override
    public String getType() {
        return "BUY_X_PAY_Y";
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public int getPayQuantity() {
        return payQuantity;
    }
}


