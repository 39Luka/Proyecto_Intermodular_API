package com.bakery.bakery_api.Service;

import com.bakery.bakery_api.domain.Compra;
import com.bakery.bakery_api.dto.request.CreateCompraDTO;
import com.bakery.bakery_api.dto.request.UpdateCompraDTO;
import com.bakery.bakery_api.repository.CompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CompraService {

    private final CompraRepository repo;
    private final UsuarioService usuarioService;
    private final PromocionService promocionService;

    public CompraService(CompraRepository repo, UsuarioService usuarioService, PromocionService promocionService) {
        this.repo = repo;
        this.usuarioService = usuarioService;
        this.promocionService = promocionService;
    }

    public List<Compra> findAll() {
        return repo.findAll();
    }

    public Compra findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
    }

    // Creamos desde DTO
    public Compra create(CreateCompraDTO dto) {
        Compra compra = new Compra();
        compra.setUsuario(usuarioService.findById(dto.usuarioId())); // validación incluida
        if (dto.promocionId() != null) {
            compra.setPromocion(promocionService.findById(dto.promocionId()));
        }
        compra.setFecha(dto.fecha());
        compra.setEstado(dto.estado() != null ? dto.estado() : Compra.Estado.PENDIENTE);
        return repo.save(compra);
    }

    // Actualización parcial desde DTO
    public Compra update(Long id, UpdateCompraDTO dto) {
        Compra existing = findById(id);

        if (dto.usuarioId() != null) existing.setUsuario(usuarioService.findById(dto.usuarioId()));
        if (dto.promocionId() != null) existing.setPromocion(promocionService.findById(dto.promocionId()));
        if (dto.fecha() != null) existing.setFecha(dto.fecha());
        if (dto.estado() != null) existing.setEstado(dto.estado());

        return repo.save(existing);
    }

    public void delete(Long id) {
        Compra existing = findById(id);
        repo.delete(existing);
    }
}
