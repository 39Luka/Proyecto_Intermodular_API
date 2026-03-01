package org.example.bakeryapi.product;

import org.example.bakeryapi.product.dto.response.ProductSalesResponse;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Load category in the same query because ProductResponse includes it (avoids N+1).
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    @Query("""
            select new org.example.bakeryapi.product.dto.response.ProductSalesResponse(
                p.id,
                p.name,
                sum(pi.quantity)
            )
            from PurchaseItem pi
            join pi.purchase purchase
            join pi.product p
            where purchase.status = :status
            group by p.id, p.name
            order by sum(pi.quantity) desc
            """)
    Page<ProductSalesResponse> findTopSellingByStatus(
            @Param("status") PurchaseStatus status,
            Pageable pageable
    );

    @Query("""
            select new org.example.bakeryapi.product.dto.response.ProductSalesResponse(
                p.id,
                p.name,
                sum(pi.quantity)
            )
            from PurchaseItem pi
            join pi.purchase purchase
            join pi.product p
            where purchase.status = :status
              and p.active = true
            group by p.id, p.name
            order by sum(pi.quantity) desc
            """)
    Page<ProductSalesResponse> findTopSellingByStatusAndActiveTrue(
            @Param("status") PurchaseStatus status,
            Pageable pageable
    );
}

