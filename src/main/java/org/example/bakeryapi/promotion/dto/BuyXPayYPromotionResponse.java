package org.example.bakeryapi.promotion.dto;

import org.example.bakeryapi.promotion.domain.BuyXPayYPromotion;

import java.time.LocalDate;

public class BuyXPayYPromotionResponse extends PromotionResponse {

    private Integer buyQuantity;
    private Integer payQuantity;

    public BuyXPayYPromotionResponse() {
    }

    public BuyXPayYPromotionResponse(Long id, String description, String type, LocalDate startDate,
                                      LocalDate endDate, boolean active, Long productId,
                                      Integer buyQuantity, Integer payQuantity) {
        super(id, description, type, startDate, endDate, active, productId);
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

    public static BuyXPayYPromotionResponse from(BuyXPayYPromotion promotion) {
        Long productId = promotion.getProduct() != null ? promotion.getProduct().getId() : null;
        return new BuyXPayYPromotionResponse(
                promotion.getId(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive(),
                productId,
                promotion.getBuyQuantity(),
                promotion.getPayQuantity()
        );
    }
}
