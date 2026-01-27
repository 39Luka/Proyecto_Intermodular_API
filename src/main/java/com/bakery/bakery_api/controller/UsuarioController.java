package com.bakery.bakery_api.controller;

import com.bakery.bakery_api.service.UsuarioService;
import com.bakery.bakery_api.dto.request.CreateUsuarioDTO;
import com.bakery.bakery_api.dto.request.UpdateUsuarioDTO;
import com.bakery.bakery_api.dto.response.UsuarioDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<UsuarioDTO> dtos = usuarioService.findAll().stream()
                .map(UsuarioDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(
                UsuarioDTO.fromEntity(usuarioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> createUsuario(
            @RequestBody CreateUsuarioDTO dto) {

        return new ResponseEntity<>(
                UsuarioDTO.fromEntity(usuarioService.create(dto)),
                HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> updateUsuario(
            @PathVariable Long id,
            @RequestBody UpdateUsuarioDTO dto) {

        return ResponseEntity.ok(
                UsuarioDTO.fromEntity(usuarioService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
