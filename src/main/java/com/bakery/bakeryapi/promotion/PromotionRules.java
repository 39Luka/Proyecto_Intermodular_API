package com.bakery.bakeryapi.promotion;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.Promotion;
import com.bakery.bakeryapi.promotion.exception.InvalidPromotionException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Reglas de validación y normalización para promociones.
 */
@Component
public class PromotionRules {

    /**
     * Valida la ventana de fecha de promoción configurada.
     *
     * @param startDate primer fecha activa
     * @param endDate última fecha activa opcional
     */
    public void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidPromotionException("La fecha de inicio de la promoción no puede estar en el pasado");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidPromotionException("La fecha de finalización de la promoción no puede ser anterior a la fecha de inicio");
        }
    }

    /**
     * Valida un descuento de porcentaje.
     *
     * @param discountPercentage porcentaje de descuento del 0 al 100
     */
    public void validatePercentage(BigDecimal discountPercentage) {
        if (discountPercentage == null) {
            throw new InvalidPromotionException("La promoción de porcentaje requiere discountPercentage");
        }
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0
                || discountPercentage.compareTo(new BigDecimal("100")) > 0) {
            throw new InvalidPromotionException("discountPercentage debe estar entre 0 y 100");
        }
    }

    /**
     * Valida que una promoción pueda aplicarse a un producto y cantidad.
     *
     * @param promotion promoción a aplicar
     * @param product producto comprado
     * @param quantity cantidad solicitada
     */
    public void validateApplicable(Promotion promotion, Product product, int quantity) {
        if (!promotion.getProduct().getId().equals(product.getId())) {
            throw new InvalidPromotionException("La promoción no se aplica a este producto");
        }

        if (!promotion.isActiveOn(LocalDate.now())) {
            throw new InvalidPromotionException("La promoción no está activa");
        }

        if (promotion.calculateDiscountAmount(product.getPrice(), quantity).compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPromotionException("La promoción no se aplica a la cantidad solicitada");
        }
    }

    /**
     * Normaliza los importes monetarios y porcentuales a dos decimales.
     *
     * @param value valor a normalizar
     * @return valor normalizado, o cero cuando está ausente
     */
    public BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
