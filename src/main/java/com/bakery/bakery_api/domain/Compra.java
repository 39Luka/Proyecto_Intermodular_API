package com.bakery.bakery_api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "compra")
@Getter
@Setter
@NoArgsConstructor
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.PENDIENTE;

    @ManyToOne
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;

    @OneToMany(mappedBy = "compra")
    private List<DetalleCompra> detalles;

    public enum Estado { PENDIENTE, LISTO_EN_TIENDA, CANCELADO }

    public Compra(Usuario usuario, LocalDate fecha, Estado estado, Promocion promocion) {
        this.usuario = usuario;
        this.fecha = fecha;
        this.estado = estado;
        this.promocion = promocion;
    }
}
