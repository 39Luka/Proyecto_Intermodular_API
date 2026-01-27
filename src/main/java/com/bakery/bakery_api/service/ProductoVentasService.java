package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.Producto;
import com.bakery.bakery_api.domain.ProductoVentas;
import com.bakery.bakery_api.repository.ProductoVentasRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoVentasService {

    private final ProductoVentasRepository repo;

    public ProductoVentasService(ProductoVentasRepository repo) {
        this.repo = repo;
    }

    // CAMBIO IMPORTANTE AQUÍ
    public ProductoVentas getOrCreateByProducto(Producto producto) {
        return repo.findByProducto(producto)
                .orElseGet(() -> repo.save(new ProductoVentas(producto)));
    }

    public void save(ProductoVentas pv) {
        repo.save(pv);
    }

    public List<ProductoVentas> getTopVentas(int top) {
        Pageable pageable = PageRequest.of(0, top, Sort.by("cantidadVendida").descending());
        return repo.findAll(pageable).getContent();
    }
}

