package com.bakery.bakeryapi.purchase;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calcula montos monetarios de línea de compra.
 */
@Service
public class PurchasePricingService {

    /**
     * Calcula un subtotal de línea después del descuento y lo normaliza a dos decimales.
     *
     * @param unitPrice precio unitario del producto
     * @param quantity cantidad solicitada
     * @param discountAmount cantidad de descuento a restar
     * @return subtotal, nunca por debajo de cero
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
