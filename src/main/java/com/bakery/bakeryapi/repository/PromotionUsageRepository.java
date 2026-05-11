package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence access for promotion usage records.
 */
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByPromotionIdAndUserId(Long promotionId, Long userId);

    void deleteByPromotionIdAndUserId(Long promotionId, Long userId);
}


