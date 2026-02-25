package org.example.bakeryapi.promotion.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.example.bakeryapi.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "promotion_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"promotion_id", "user_id"})
)
public class PromotionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime usedAt;

    protected PromotionUsage() {
        // Constructor for JPA
    }

    public PromotionUsage(Promotion promotion, User user, LocalDateTime usedAt) {
        this.promotion = promotion;
        this.user = user;
        this.usedAt = usedAt;
    }

    public Long getId() {
        return id;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }
}


