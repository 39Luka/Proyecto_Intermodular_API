package org.example.bakeryapi.promotion.dto;

import org.example.bakeryapi.promotion.domain.PercentagePromotion;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PercentagePromotionResponse extends PromotionResponse {

    private BigDecimal discountPercentage;

    public PercentagePromotionResponse() {
    }

    public PercentagePromotionResponse(Long id, String description, String type, LocalDate startDate,
                                        LocalDate endDate, boolean active, Long productId,
                                        BigDecimal discountPercentage) {
        super(id, description, type, startDate, endDate, active, productId);
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public static PercentagePromotionResponse from(PercentagePromotion promotion) {
        Long productId = promotion.getProduct() != null ? promotion.getProduct().getId() : null;
        return new PercentagePromotionResponse(
                promotion.getId(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive(),
                productId,
                promotion.getDiscountPercentage()
        );
    }
}
