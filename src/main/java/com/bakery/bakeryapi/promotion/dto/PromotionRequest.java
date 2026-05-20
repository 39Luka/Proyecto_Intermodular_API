package com.bakery.bakeryapi.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Campos de solicitud base compartidos por las solicitudes de creación de promociones.
 */
public abstract class PromotionRequest {

    @NotBlank(message = "La descripción es requerida")
    protected String description;

    @NotNull(message = "La fecha de inicio es requerida")
    protected LocalDate startDate;

    protected LocalDate endDate;

    @NotNull(message = "El ID del producto es requerido")
    protected Long productId;

    protected PromotionRequest() {
    }

    protected PromotionRequest(String description, LocalDate startDate, LocalDate endDate, Long productId) {
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
