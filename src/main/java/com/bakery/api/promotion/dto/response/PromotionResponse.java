package com.bakery.api.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Promotions are intentionally restricted to percentage-based discounts in this project.
 *
 * For that reason the API always exposes the discount percentage.
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
}

