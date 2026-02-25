package org.example.bakeryapi.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.bakeryapi.product.Product;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
// @Index acelera búsquedas por product_id, fechas y active
// @Inheritance SINGLE_TABLE: todas las subclases (PercentagePromotion, BuyXPayYPromotion) en una sola tabla
// @DiscriminatorColumn type: columna que identifica qué tipo de promoción es cada fila (PERCENTAGE, BUY_X_PAY_Y)
@Table(name = "promotions", indexes = {
    @Index(name = "idx_promotion_product", columnList = "product_id"),
    @Index(name = "idx_promotion_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_promotion_active", columnList = "active")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected Promotion() {
        // Constructor for JPA
    }

    protected Promotion(String description, LocalDate startDate, LocalDate endDate, Product product) {
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.product = product;
    }

    public abstract BigDecimal calculateDiscountAmount(BigDecimal unitPrice, int quantity);

    public abstract String getType();

    public boolean isActiveOn(LocalDate date) {
        return active && !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate));
    }

    public void disable() {
        this.active = false;
    }

    public void enable() {
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public Product getProduct() {
        return product;
    }
}


