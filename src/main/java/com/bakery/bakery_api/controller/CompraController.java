package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.domain.Compra;
import com.bakery.bakery_api.dto.request.CreateCompraDTO;
import com.bakery.bakery_api.dto.response.CompraDTO;
import com.bakery.bakery_api.service.CompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/compras")
public class CompraController {

    private final CompraService service;

    public CompraController(CompraService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CompraDTO>> getAll() {
        List<CompraDTO> compras = service.findAll().stream()
                .map(CompraDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(compras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> getById(@PathVariable Long id) {
        Compra compra = service.findById(id);
        return ResponseEntity.ok(CompraDTO.fromEntity(compra));
    }

    @PostMapping
    public ResponseEntity<CompraDTO> create(@RequestBody CreateCompraDTO dto) {
        Compra compra = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CompraDTO.fromEntity(compra));
    }
}
