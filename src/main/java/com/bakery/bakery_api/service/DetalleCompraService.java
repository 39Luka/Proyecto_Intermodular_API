package com.bakery.bakery_api.service;

import com.bakery.bakery_api.domain.DetalleCompra;
import com.bakery.bakery_api.domain.ProductoVentas;
import com.bakery.bakery_api.dto.request.CreateDetalleCompraDTO;
import com.bakery.bakery_api.dto.request.UpdateDetalleCompraDTO;
import com.bakery.bakery_api.repository.DetalleCompraRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class DetalleCompraService {

    private final DetalleCompraRepository repo;
    private final CompraService compraService;
    private final ProductoService productoService;
    private final ProductoVentasService ventasService;

    public DetalleCompraService(DetalleCompraRepository repo,
                                CompraService compraService,
                                ProductoService productoService,
                                ProductoVentasService ventasService) {
        this.repo = repo;
        this.compraService = compraService;
        this.productoService = productoService;
        this.ventasService = ventasService;
    }

    public List<DetalleCompra> findAll() {
        return repo.findAll();
    }

    public DetalleCompra findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DetalleCompra no encontrado"));
    }

    public DetalleCompra create(CreateDetalleCompraDTO dto) {
        DetalleCompra detalle = new DetalleCompra();
        detalle.setCompra(compraService.findById(dto.compraId()));
        detalle.setProducto(productoService.findById(dto.productoId()));
        detalle.setCantidad(dto.cantidad());
        detalle.setSubtotal(dto.subtotal());

        DetalleCompra saved = repo.save(detalle);

        ProductoVentas pv = ventasService.getOrCreateByProducto(detalle.getProducto());
        pv.setCantidadVendida(pv.getCantidadVendida() + detalle.getCantidad());
        ventasService.save(pv);

        return saved;
    }
    public DetalleCompra update(Long id, UpdateDetalleCompraDTO dto) {
        DetalleCompra existing = findById(id);

        if (dto.compraId() != null) existing.setCompra(compraService.findById(dto.compraId()));
        if (dto.productoId() != null) existing.setProducto(productoService.findById(dto.productoId()));

        if (dto.cantidad() != null) {
            long diferencia = dto.cantidad() - existing.getCantidad();
            existing.setCantidad(dto.cantidad());

            ProductoVentas pv = ventasService.getOrCreateByProducto(existing.getProducto());
            pv.setCantidadVendida(pv.getCantidadVendida() + diferencia);
            ventasService.save(pv);
        }

        if (dto.subtotal() != null) existing.setSubtotal(dto.subtotal());

        return repo.save(existing);
    }


    public void delete(Long id) {
        DetalleCompra existing = findById(id);
        repo.delete(existing);
    }
}
