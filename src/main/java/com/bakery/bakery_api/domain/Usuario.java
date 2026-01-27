package com.bakery.bakery_api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.CLIENTE;

    // Compras del usuario
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Compra> compras;

    // Promociones asignadas (no se serializa)
    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "Usuario_Promocion",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_promocion")
    )
    private List<Promocion> promocionesAsignadas;

    public enum Rol { ADMIN, CLIENTE }

    public Usuario(String nombre, String email, String contrasena, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = rol;
    }
}
