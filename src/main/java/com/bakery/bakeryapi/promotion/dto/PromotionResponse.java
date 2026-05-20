package com.bakery.bakeryapi.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Las promociones están restringidas intencionadamente a descuentos basados en porcentajes en este proyecto.
 * Por este motivo, la API siempre expone el porcentaje de descuento.
 */
public record PromotionResponse(
        Long id,
        String description,
        String type,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        Long productId,
        String productName,
        BigDecimal discountPercentage
) {
    public static PromotionResponse from(com.bakery.bakeryapi.domain.Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive(),
                promotion.getProduct() != null ? promotion.getProduct().getId() : null,
                promotion.getProduct() != null ? promotion.getProduct().getName() : null,
                promotion.getDiscountPercentage()
        );
    }
}
