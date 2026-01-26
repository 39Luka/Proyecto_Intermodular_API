package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.Compra;
import com.bakery.bakery_api.domain.DetalleCompra;
import com.bakery.bakery_api.domain.Producto;
import com.bakery.bakery_api.dto.request.CreateCompraDTO;
import com.bakery.bakery_api.repository.CompraRepository;
import com.bakery.bakery_api.repository.DetalleCompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class CompraService {

    private final CompraRepository compraRepo;
    private final DetalleCompraRepository detalleRepo;
    private final UsuarioService usuarioService;
    private final PromocionService promocionService;
    private final ProductoService productoService;

    public CompraService(
            CompraRepository compraRepo,
            DetalleCompraRepository detalleRepo,
            UsuarioService usuarioService,
            PromocionService promocionService,
            ProductoService productoService
    ) {
        this.compraRepo = compraRepo;
        this.detalleRepo = detalleRepo;
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
        Compra compra = new Compra();
        compra.setUsuario(usuarioService.findById(dto.usuarioId()));
        compra.setFecha(dto.fecha());
        compra.setEstado(dto.estado() != null ? Compra.Estado.valueOf(dto.estado()) : Compra.Estado.PENDIENTE);

        if (dto.promocionId() != null) {
            compra.setPromocion(promocionService.findById(dto.promocionId()));
        }

        Compra compraGuardada = compraRepo.save(compra);

        // Guardar los detalles de compra
        if (dto.productos() != null) {
            for (CreateCompraDTO.ProductoCantidad pc : dto.productos()) {
                Producto producto = productoService.findById(pc.productoId());
                DetalleCompra detalle = new DetalleCompra();
                detalle.setCompra(compraGuardada);
                detalle.setProducto(producto);
                detalle.setCantidad(pc.cantidad());
                detalle.setSubtotal(producto.getPrecio() * pc.cantidad());
                detalleRepo.save(detalle);
            }
        }

        return compraGuardada;
    }
}
