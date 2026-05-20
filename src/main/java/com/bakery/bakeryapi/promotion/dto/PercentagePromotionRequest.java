package com.bakery.bakeryapi.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuerpo de solicitud para crear una promoción basada en porcentaje.
 */
public class PercentagePromotionRequest extends PromotionRequest {

    @Schema(description = "Porcentaje de descuento (0-100)", example = "10.00")
    @NotNull(message = "El porcentaje de descuento es requerido")
    @DecimalMin(value = "0.0", message = "El porcentaje de descuento debe ser al menos 0")
    @DecimalMax(value = "100.0", message = "El porcentaje de descuento no debe exceder 100")
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
