package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.service.ProductoService;
import com.bakery.bakery_api.dto.request.CreateProductoDTO;
import com.bakery.bakery_api.dto.request.UpdateProductoDTO;
import com.bakery.bakery_api.dto.response.ProductoDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> getAll() {
        List<ProductoDTO> dtos = service.findAll().stream()
                .map(ProductoDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoDTO.fromEntity(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> create(@RequestBody CreateProductoDTO dto) {
        return new ResponseEntity<>(ProductoDTO.fromEntity(service.create(dto)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> update(@PathVariable Long id, @RequestBody UpdateProductoDTO dto) {
        return ResponseEntity.ok(ProductoDTO.fromEntity(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
