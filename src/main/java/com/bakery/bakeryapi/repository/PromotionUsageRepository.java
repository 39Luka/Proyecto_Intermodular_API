package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso de persistencia para registros de uso de promoción.
 */
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByPromotionIdAndUserId(Long promotionId, Long userId);

    void deleteByPromotionIdAndUserId(Long promotionId, Long userId);
}


