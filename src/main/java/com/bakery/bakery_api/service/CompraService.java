package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.Compra;
import com.bakery.bakery_api.domain.DetalleCompra;
import com.bakery.bakery_api.domain.Producto;
import com.bakery.bakery_api.dto.request.CreateCompraDTO;
import com.bakery.bakery_api.repository.CompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Service
@Transactional
public class CompraService {

    private final CompraRepository compraRepo;
    private final UsuarioService usuarioService;
    private final PromocionService promocionService;
    private final ProductoService productoService;

    public CompraService(
            CompraRepository compraRepo,
            UsuarioService usuarioService,
            PromocionService promocionService,
            ProductoService productoService
    ) {
        this.compraRepo = compraRepo;
        this.usuarioService = usuarioService;
        this.promocionService = promocionService;
        this.productoService = productoService;
    }

    public List<Compra> findAll() {
        return compraRepo.findAll();
    }

    public Compra findById(Long id) {
        return compraRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
    }

    public Compra create(CreateCompraDTO dto) {

        if (dto.usuarioId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario es obligatorio");
        if (dto.fecha() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha es obligatoria");
        if (dto.productos() == null || dto.productos().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe incluir al menos un producto");

        var usuario = usuarioService.findById(dto.usuarioId());
        if (usuario == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");


        var promocion = dto.promocionId() != null ? promocionService.findById(dto.promocionId()) : null;
        if (dto.promocionId() != null && promocion == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promoción no encontrada");


        var estado = dto.estado() != null ? Compra.Estado.valueOf(dto.estado()) : Compra.Estado.PENDIENTE;
        Compra compra = new Compra(usuario, dto.fecha(), estado, promocion);

        for (CreateCompraDTO.ProductoCantidad pc : dto.productos()) {
            Producto producto = productoService.findById(pc.productoId());
            if (producto == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto con ID " + pc.productoId() + " no encontrado");

            if (pc.cantidad() <= 0)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad debe ser mayor que 0 para el producto " + producto.getNombre());

            DetalleCompra detalle = new DetalleCompra(compra, producto, pc.cantidad());
            compra.addDetalle(detalle);
        }

        return compraRepo.save(compra);
    }
}
