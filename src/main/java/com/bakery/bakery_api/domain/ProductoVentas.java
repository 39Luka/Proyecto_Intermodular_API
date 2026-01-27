package com.bakery.bakery_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto_ventas")
@Getter
@Setter
@NoArgsConstructor
public class ProductoVentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto_ventas")
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_producto", nullable = false, unique = true)
    private Producto producto;

    @Column(name = "cantidad_vendida", nullable = false)
    private Long cantidadVendida = 0L;

    public ProductoVentas(Producto producto) {
        this.producto = producto;
        this.cantidadVendida = 0L;
    }
}
