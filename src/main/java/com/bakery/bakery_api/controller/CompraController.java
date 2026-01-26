package com.bakery.bakery_api.controller;

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
        return ResponseEntity.ok(
                service.findAll().stream()
                        .map(CompraDTO::fromEntity)
                        .toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(CompraDTO.fromEntity(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<CompraDTO> create(@RequestBody CreateCompraDTO dto) {
        return new ResponseEntity<>(CompraDTO.fromEntity(service.create(dto)), HttpStatus.CREATED);
    }
}
