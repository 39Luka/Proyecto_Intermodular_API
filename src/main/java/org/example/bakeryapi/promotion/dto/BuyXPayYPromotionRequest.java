package org.example.bakeryapi.promotion.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Objects;

public class BuyXPayYPromotionRequest extends PromotionRequest {

    @NotNull(message = "Buy quantity is required")
    @Positive(message = "Buy quantity must be positive")
    private Integer buyQuantity;

    @NotNull(message = "Pay quantity is required")
    @Positive(message = "Pay quantity must be positive")
    private Integer payQuantity;

    public BuyXPayYPromotionRequest() {
    }

    public BuyXPayYPromotionRequest(String description, LocalDate startDate, LocalDate endDate,
                                     Long productId, Integer buyQuantity, Integer payQuantity) {
        super(description, startDate, endDate, productId);
        this.buyQuantity = buyQuantity;
        this.payQuantity = payQuantity;
    }

    public Integer getBuyQuantity() {
        return buyQuantity;
    }

    public void setBuyQuantity(Integer buyQuantity) {
        this.buyQuantity = buyQuantity;
    }

    public Integer getPayQuantity() {
        return payQuantity;
    }

    public void setPayQuantity(Integer payQuantity) {
        this.payQuantity = payQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyXPayYPromotionRequest that = (BuyXPayYPromotionRequest) o;
        return Objects.equals(buyQuantity, that.buyQuantity) &&
               Objects.equals(payQuantity, that.payQuantity) &&
               Objects.equals(description, that.description) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, startDate, endDate, productId, buyQuantity, payQuantity);
    }
}
