package com.bakery.bakery_api.repository;

import com.bakery.bakery_api.domain.ProductoVentas;
import com.bakery.bakery_api.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ProductoVentasRepository extends JpaRepository<ProductoVentas, Long> {
    Optional<ProductoVentas> findByProducto(Producto producto);

    List<ProductoVentas> findTop10ByOrderByCantidadVendidaDesc();
}
