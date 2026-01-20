package com.bakery.bakery_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "producto_ventas")
@Getter
@Setter
@NoArgsConstructor
public class ProductoVentas {

    @Id
    @Column(name = "id_producto")
    private Long idProducto;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "cantidad_vendida", nullable = false)
    private Long cantidadVendida = 0L;

    public ProductoVentas(Producto producto) {
        this.producto = producto;
        this.cantidadVendida = 0L;
    }

    public ProductoVentas(Producto producto, long l) {
        this.producto = producto;
        this.cantidadVendida = l;
    }
}
