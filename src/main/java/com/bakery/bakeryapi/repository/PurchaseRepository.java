package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.Purchase;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    interface PurchaseTotalRow {
        LocalDateTime getCreatedAt();
        BigDecimal getTotal();
    }

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p where p.id = :id")
    Optional<Purchase> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p")
    Page<Purchase> findAllDetailed(Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p where p.user.id = :userId")
    Page<Purchase> findAllDetailedByUserId(@Param("userId") Long userId, Pageable pageable);

    long countByStatus(PurchaseStatus status);

    @Query("select coalesce(sum(p.total), 0) from Purchase p where p.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") PurchaseStatus status);

    @Query("""
            select p.createdAt as createdAt, p.total as total
            from Purchase p
            where p.status = :status
              and p.createdAt >= :from
              and p.createdAt < :to
            """)
    List<PurchaseTotalRow> findTotalsByStatusBetween(
            @Param("status") PurchaseStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}


