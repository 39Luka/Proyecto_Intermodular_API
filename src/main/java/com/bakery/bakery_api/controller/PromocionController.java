package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.Service.PromocionService;
import com.bakery.bakery_api.dto.request.CreatePromocionDTO;
import com.bakery.bakery_api.dto.request.UpdatePromocionDTO;
import com.bakery.bakery_api.dto.response.PromocionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/promociones")
public class PromocionController {

    private final PromocionService service;

    public PromocionController(PromocionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PromocionDTO>> getAll() {
        List<PromocionDTO> dtos = service.findAll().stream()
                .map(PromocionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(PromocionDTO.fromEntity(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PromocionDTO> create(@RequestBody CreatePromocionDTO dto) {
        return new ResponseEntity<>(PromocionDTO.fromEntity(service.create(dto)), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocionDTO> update(@PathVariable Long id, @RequestBody UpdatePromocionDTO dto) {
        return ResponseEntity.ok(PromocionDTO.fromEntity(service.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
