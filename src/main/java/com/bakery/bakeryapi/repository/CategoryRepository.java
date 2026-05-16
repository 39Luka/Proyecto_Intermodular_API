package com.bakery.bakeryapi.repository;

import com.bakery.bakeryapi.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso de persistencia para categorías de productos.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);
}


