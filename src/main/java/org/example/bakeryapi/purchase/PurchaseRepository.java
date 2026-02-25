package org.example.bakeryapi.purchase;

import org.example.bakeryapi.purchase.domain.Purchase;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p where p.id = :id")
    Optional<Purchase> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p")
    Page<Purchase> findAllDetailed(Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "items.promotion"})
    @Query("select p from Purchase p where p.user.id = :userId")
    Page<Purchase> findAllDetailedByUserId(@Param("userId") Long userId, Pageable pageable);
}


