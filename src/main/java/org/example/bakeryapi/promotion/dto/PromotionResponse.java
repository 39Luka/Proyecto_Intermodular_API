package org.example.bakeryapi.promotion.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.bakeryapi.promotion.domain.BuyXPayYPromotion;
import org.example.bakeryapi.promotion.domain.PercentagePromotion;
import org.example.bakeryapi.promotion.domain.Promotion;

import java.time.LocalDate;

// @JsonTypeInfo: configura cómo Jackson serializa/deserializa subclases usando el campo "type"
// @JsonSubTypes: mapea valores de "type" a clases concretas (PERCENTAGE -> PercentagePromotionResponse)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PercentagePromotionResponse.class, name = "PERCENTAGE"),
        @JsonSubTypes.Type(value = BuyXPayYPromotionResponse.class, name = "BUY_X_PAY_Y")
})
public abstract class PromotionResponse {

    protected Long id;
    protected String description;
    protected String type;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected boolean active;
    protected Long productId;
    protected String productName;

    protected PromotionResponse() {
    }

    protected PromotionResponse(Long id, String description, String type, LocalDate startDate,
                                 LocalDate endDate, boolean active, Long productId, String productName) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.productId = productId;
        this.productName = productName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public static PromotionResponse from(Promotion promotion) {
        if (promotion instanceof PercentagePromotion percentagePromotion) {
            return PercentagePromotionResponse.from(percentagePromotion);
        } else if (promotion instanceof BuyXPayYPromotion buyXPayYPromotion) {
            return BuyXPayYPromotionResponse.from(buyXPayYPromotion);
        }
        throw new IllegalArgumentException("Unknown promotion type: " + promotion.getClass());
    }
}
