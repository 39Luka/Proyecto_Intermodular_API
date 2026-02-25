package org.example.bakeryapi.promotion;

import org.example.bakeryapi.promotion.domain.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Override
    @EntityGraph(attributePaths = "product")
    Page<Promotion> findAll(Pageable pageable);

    @Query("""
            select p from Promotion p
            where p.active = true
              and p.product.id = :productId
              and p.startDate <= :date
              and (p.endDate is null or p.endDate >= :date)
            """)
    @EntityGraph(attributePaths = "product")
    Page<Promotion> findActiveByProductId(
            @Param("productId") Long productId,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    @Query("""
            select distinct p from Promotion p
            left join PromotionUsage pu on pu.promotion = p and pu.user.id = :userId
            where p.active = true
              and p.product.id = :productId
              and p.startDate <= :date
              and (p.endDate is null or p.endDate >= :date)
              and pu.id is null
            """)
    @EntityGraph(attributePaths = "product")
    Page<Promotion> findActiveByProductIdAndUserId(
            @Param("productId") Long productId,
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            Pageable pageable
    );

}

