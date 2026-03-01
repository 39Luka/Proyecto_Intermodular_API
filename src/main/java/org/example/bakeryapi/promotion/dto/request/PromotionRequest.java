package org.example.bakeryapi.promotion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public abstract class PromotionRequest {

    @NotBlank(message = "Description is required")
    protected String description;

    @NotNull(message = "Start date is required")
    protected LocalDate startDate;

    protected LocalDate endDate;

    @NotNull(message = "Product ID is required")
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
