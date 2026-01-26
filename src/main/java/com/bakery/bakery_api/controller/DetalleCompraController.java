package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.service.DetalleCompraService;
import com.bakery.bakery_api.domain.DetalleCompra;
import com.bakery.bakery_api.dto.request.CreateDetalleCompraDTO;
import com.bakery.bakery_api.dto.request.UpdateDetalleCompraDTO;
import com.bakery.bakery_api.dto.response.DetalleCompraDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalleCompraDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(DetalleCompraDTO.fromEntity(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<DetalleCompraDTO> create(@RequestBody CreateDetalleCompraDTO dto) {
        DetalleCompra creado = service.create(dto);
        return new ResponseEntity<>(DetalleCompraDTO.fromEntity(creado), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalleCompraDTO> update(@PathVariable Long id, @RequestBody UpdateDetalleCompraDTO dto) {
        DetalleCompra actualizado = service.update(id, dto);
        return ResponseEntity.ok(DetalleCompraDTO.fromEntity(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
