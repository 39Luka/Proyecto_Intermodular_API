package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.Producto;
import com.bakery.bakery_api.dto.request.CreateProductoDTO;
import com.bakery.bakery_api.dto.request.UpdateProductoDTO;
import com.bakery.bakery_api.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    public List<Producto> findAll() {
        return repo.findAll();
    }

    public Producto findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    // Crear producto desde DTO
    public Producto create(CreateProductoDTO dto) {
        if (repo.findAll().stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(dto.nombre()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
        }

        Producto producto = new Producto();
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());

        return repo.save(producto);
    }

    // Actualización parcial desde DTO
    public Producto update(Long id, UpdateProductoDTO dto) {
        Producto existing = findById(id);

        if (dto.nombre() != null) existing.setNombre(dto.nombre());
        if (dto.descripcion() != null) existing.setDescripcion(dto.descripcion());
        if (dto.precio() != null) existing.setPrecio(dto.precio());
        if (dto.stock() != null) existing.setStock(dto.stock());

        return repo.save(existing);
    }

    public void delete(Long id) {
        Producto existing = findById(id);
        repo.delete(existing);
    }
}
