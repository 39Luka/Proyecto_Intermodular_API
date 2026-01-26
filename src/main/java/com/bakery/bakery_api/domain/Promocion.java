package com.bakery.bakery_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "promocion")
@Getter
@Setter
@NoArgsConstructor
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_promocion")
    private Long id;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Double descuento;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @ManyToMany(mappedBy = "promocionesAsignadas")
    private List<Usuario> usuarios;

    @OneToMany(mappedBy = "promocion")
    private List<Compra> compras;

    public Promocion(String descripcion, Double descuento, LocalDate fechaInicio, LocalDate fechaFin) {
        this.descripcion = descripcion;
        this.descuento = descuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }
}
