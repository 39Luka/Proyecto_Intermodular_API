package com.bakery.bakeryapi.purchase;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates purchase line monetary amounts.
 */
@Service
public class PurchasePricingService {

    /**
     * Calculates a line subtotal after discount and normalizes it to two decimals.
     *
     * @param unitPrice product unit price
     * @param quantity requested quantity
     * @param discountAmount discount amount to subtract
     * @return subtotal, never below zero
     */
    public BigDecimal calculateSubtotal(BigDecimal unitPrice, int quantity, BigDecimal discountAmount) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal subtotal = gross.subtract(discountAmount);
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
}
