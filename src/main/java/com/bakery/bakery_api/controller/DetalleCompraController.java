package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.dto.request.CreateDetalleCompraDTO;
import com.bakery.bakery_api.dto.request.UpdateDetalleCompraDTO;
import com.bakery.bakery_api.dto.response.DetalleCompraDTO;
import com.bakery.bakery_api.service.DetalleCompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/detalle-compras")
public class DetalleCompraController {

    private final DetalleCompraService service;

    public DetalleCompraController(DetalleCompraService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DetalleCompraDTO>> getAll() {
        List<DetalleCompraDTO> dtos = service.findAll().stream()
                .map(DetalleCompraDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompraDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(DetalleCompraDTO.fromEntity(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<DetalleCompraDTO> create(@RequestBody CreateDetalleCompraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DetalleCompraDTO.fromEntity(service.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleCompraDTO> update(@PathVariable Long id, @RequestBody UpdateDetalleCompraDTO dto) {
        return ResponseEntity.ok(DetalleCompraDTO.fromEntity(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
