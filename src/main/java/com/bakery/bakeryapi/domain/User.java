package com.bakery.bakeryapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cuenta de usuario de la aplicación.
 *
 * Almacena datos de autenticación, rol de autorización, estado habilitado,
 * versión de token de actualización e imagen de perfil opcional.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private long refreshTokenVersion = 0;

    /**
     * Imagen de perfil opcional almacenada como bytes sin procesar.
     *
     * Los clientes de API envían y reciben este valor como Base64 a través de los DTOs de usuario.
     */
    @Column(columnDefinition = "LONGBLOB")
    private byte[] profileImage;

    protected User(){
        // Constructor for JPA
    }

    public User(String email, String password, Role role){
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Reemplaza la contraseña ya codificada.
     *
     * Los llamadores deben codificar la contraseña sin procesar antes de invocar este método.
     *
     * @param password contraseña codificada a persistir
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public long getRefreshTokenVersion() {
        return refreshTokenVersion;
    }

    public void rotateRefreshToken() {
        this.refreshTokenVersion++;
    }

    /**
     * Devuelve los bytes de la imagen de perfil sin procesar.
     *
     * @return bytes de la imagen, o {@code null} cuando no se establece ninguna imagen de perfil
     */
    public byte[] getProfileImage() {
        return profileImage;
    }

    /**
     * Actualiza o elimina la imagen de perfil.
     *
     * @param profileImage bytes de imagen, o {@code null} para eliminar la imagen
     */
    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
}


