package org.example.bakeryapi.purchase.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.example.bakeryapi.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases", indexes = {
    @Index(name = "idx_purchase_user", columnList = "user_id"),
    @Index(name = "idx_purchase_status", columnList = "status")
})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurchaseStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PurchaseItem> items = new ArrayList<>();

    protected Purchase() {
        // Constructor for JPA
    }

    public Purchase(User user, LocalDateTime createdAt, PurchaseStatus status) {
        this.user = user;
        this.createdAt = createdAt;
        this.status = status;
    }

    public void addItem(PurchaseItem item) {
        items.add(item);
        item.assignPurchase(this);
        total = total.add(item.getSubtotal());
    }

    public void cancel() {
        this.status = PurchaseStatus.CANCELLED;
    }

    public void pay() {
        this.status = PurchaseStatus.PAID;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }
}


