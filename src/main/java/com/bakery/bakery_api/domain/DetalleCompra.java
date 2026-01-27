package com.bakery.bakery_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalle_compra")
@Getter
@Setter
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double subtotal;

    public DetalleCompra(Compra compra, Producto producto, Integer cantidad) {
        if (compra == null) throw new IllegalArgumentException("Compra es obligatoria");
        if (producto == null) throw new IllegalArgumentException("Producto es obligatorio");
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");

        this.compra = compra;
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = producto.getPrecio() * cantidad;
    }

    public void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        this.cantidad = cantidad;
        this.subtotal = this.producto.getPrecio() * cantidad;
    }

}
