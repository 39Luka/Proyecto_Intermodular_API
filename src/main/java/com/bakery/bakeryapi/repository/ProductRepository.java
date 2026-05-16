package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.product.dto.ProductSalesResponse;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso de persistencia para productos y proyecciones de ventas de productos.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Cargar categoría en la misma consulta porque ProductResponse la incluye (evita N+1).
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAllByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    @Query("select count(pi) > 0 from PurchaseItem pi where pi.product.id = :productId")
    boolean existsPurchasesByProductId(@Param("productId") Long productId);

    @Query("""
            select new com.bakery.bakeryapi.product.dto.ProductSalesResponse(
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
            select new com.bakery.bakeryapi.product.dto.ProductSalesResponse(
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

    @EntityGraph(attributePaths = {"category"})
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :name, '%'))")
    Page<Product> findByNameContainsIgnoreCase(@Param("name") String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("select p from Product p where p.active = true and lower(p.name) like lower(concat('%', :name, '%'))")
    Page<Product> findByNameContainsIgnoreCaseAndActiveTrue(@Param("name") String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    @Query("select p from Product p where p.category.id = :categoryId and lower(p.name) like lower(concat('%', :name, '%'))")
    Page<Product> findByCategoryIdAndNameContainsIgnoreCase(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("select p from Product p where p.category.id = :categoryId and p.active = true and lower(p.name) like lower(concat('%', :name, '%'))")
    Page<Product> findByCategoryIdAndNameContainsIgnoreCaseAndActiveTrue(
            @Param("categoryId") Long categoryId,
            @Param("name") String name,
            Pageable pageable
    );
}
