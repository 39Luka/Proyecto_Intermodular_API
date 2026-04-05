package com.bakery.bakeryapi.promotionss;

import com.bakery.bakeryapi.promotionss.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByPromotionIdAndUserId(Long promotionId, Long userId);

    void deleteByPromotionIdAndUserId(Long promotionId, Long userId);
}


