package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.Usuario;
import com.bakery.bakery_api.dto.request.CreateUsuarioDTO;
import com.bakery.bakery_api.dto.request.UpdateUsuarioDTO;
import com.bakery.bakery_api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public List<Usuario> findAll() {
        return repo.findAll();
    }

    public Usuario findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario create(CreateUsuarioDTO dto) {
        if (repo.existsByEmail(dto.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El email ya existe");
        }

        Usuario usuario = new Usuario(
                dto.nombre(),
                dto.email(),
                dto.contrasena(),
                dto.rol()
        );

        return repo.save(usuario);
    }

    public Usuario update(Long id, UpdateUsuarioDTO dto) {
        Usuario existing = findById(id);

        if (dto.email() != null &&
                repo.existsByEmail(dto.email()) &&
                !existing.getEmail().equals(dto.email())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El email ya existe");
        }

        if (dto.nombre() != null) existing.setNombre(dto.nombre());
        if (dto.email() != null) existing.setEmail(dto.email());
        if (dto.contrasena() != null) existing.setContrasena(dto.contrasena());
        if (dto.rol() != null) existing.setRol(dto.rol());

        return repo.save(existing);
    }

    public void delete(Long id) {
        Usuario existing = findById(id);
        repo.delete(existing);
    }
}
