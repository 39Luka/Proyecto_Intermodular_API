package com.bakery.bakery_api.Service;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    // Crear usuario desde DTO
    public Usuario create(CreateUsuarioDTO dto) {
        if (repo.existsByEmail(dto.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.nombre());
        usuario.setEmail(dto.email());
        usuario.setContrasena(dto.contrasena());
        usuario.setRol(dto.rol());

        return repo.save(usuario);
    }

    // Actualización parcial desde DTO
    public Usuario update(Long id, UpdateUsuarioDTO dto) {
        Usuario existing = findById(id);

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
