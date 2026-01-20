package com.bakery.bakery_api.Service;

import com.bakery.bakery_api.domain.Promocion;
import com.bakery.bakery_api.dto.request.CreatePromocionDTO;
import com.bakery.bakery_api.dto.request.UpdatePromocionDTO;
import com.bakery.bakery_api.repository.PromocionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class PromocionService {

    private final PromocionRepository repo;

    public PromocionService(PromocionRepository repo) {
        this.repo = repo;
    }

    public List<Promocion> findAll() {
        return repo.findAll();
    }

    public Promocion findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promoción no encontrada"));
    }

    // Crear desde DTO
    public Promocion create(CreatePromocionDTO dto) {
        Promocion promocion = new Promocion();
        promocion.setDescripcion(dto.descripcion());
        promocion.setDescuento(dto.descuento());
        promocion.setFechaInicio(dto.fechaInicio());
        promocion.setFechaFin(dto.fechaFin());
        return repo.save(promocion);
    }

    // Actualización parcial desde DTO
    public Promocion update(Long id, UpdatePromocionDTO dto) {
        Promocion existing = findById(id);

        if (dto.descripcion() != null) existing.setDescripcion(dto.descripcion());
        if (dto.descuento() != null) existing.setDescuento(dto.descuento());
        if (dto.fechaInicio() != null) existing.setFechaInicio(dto.fechaInicio());
        if (dto.fechaFin() != null) existing.setFechaFin(dto.fechaFin());

        return repo.save(existing);
    }

    public void delete(Long id) {
        Promocion existing = findById(id);
        repo.delete(existing);
    }
}
