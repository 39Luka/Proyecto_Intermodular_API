package com.bakery.api.promotion.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PercentagePromotionRequest extends PromotionRequest {

    @Schema(description = "Discount percentage (0-100)", example = "10.00")
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
}
