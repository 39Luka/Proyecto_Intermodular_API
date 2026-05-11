package com.bakery.bakeryapi.promotion;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.Promotion;
import com.bakery.bakeryapi.promotion.exception.InvalidPromotionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Validation and normalization rules for promotions.
 */
@Component
public class PromotionRules {

    /**
     * Validates the configured promotion date window.
     *
     * @param startDate first active date
     * @param endDate optional last active date
     */
    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidPromotionException("Promotion start date cannot be in the past");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidPromotionException("Promotion end date cannot be before start date");
        }
    }

    /**
     * Validates a percentage discount.
     *
     * @param discountPercentage discount percentage from 0 to 100
     */
    public void validatePercentage(BigDecimal discountPercentage) {
        if (discountPercentage == null) {
            throw new InvalidPromotionException("Percentage promotion requires discountPercentage");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPromotionException("discountPercentage must be between 0 and 100");
        }
    }

    /**
     * Validates that a promotion can be applied to a product and quantity.
     *
     * @param promotion promotion to apply
     * @param product purchased product
     * @param quantity requested quantity
     */
    public void validateApplicable(Promotion promotion, Product product, int quantity) {
        if (!promotion.getProduct().getId().equals(product.getId())) {
            throw new InvalidPromotionException("Promotion does not apply to this product");
        }

        if (!promotion.isActiveOn(LocalDate.now())) {
            throw new InvalidPromotionException("Promotion is not active");
        }

        if (promotion.calculateDiscountAmount(product.getPrice(), quantity).compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPromotionException("Promotion does not apply to the requested quantity");
        }
    }

    /**
     * Normalizes money and percentage amounts to two decimals.
     *
     * @param value value to normalize
     * @return normalized value, or zero when absent
     */
    public BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
