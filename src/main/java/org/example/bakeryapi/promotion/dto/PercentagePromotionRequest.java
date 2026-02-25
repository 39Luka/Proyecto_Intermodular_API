package org.example.bakeryapi.promotion.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class PercentagePromotionRequest extends PromotionRequest {

    @NotNull(message = "Discount percentage is required")
    @DecimalMin(value = "0.0", message = "Discount percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100")
    private BigDecimal discountPercentage;

    public PercentagePromotionRequest() {
    }

    public PercentagePromotionRequest(String description, LocalDate startDate, LocalDate endDate,
                                       Long productId, BigDecimal discountPercentage) {
        super(description, startDate, endDate, productId);
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PercentagePromotionRequest that = (PercentagePromotionRequest) o;
        return Objects.equals(discountPercentage, that.discountPercentage) &&
               Objects.equals(description, that.description) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, startDate, endDate, productId, discountPercentage);
    }
}
