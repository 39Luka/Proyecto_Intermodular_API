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
 * Application user account.
 *
 * Stores authentication data, authorization role, enabled state, refresh-token
 * version and optional profile image.
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
     * Optional profile image stored as raw bytes.
     *
     * API clients send and receive this value as Base64 through the user DTOs.
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
     * Replaces the already encoded password.
     *
     * Callers must encode the raw password before invoking this method.
     *
     * @param password encoded password to persist
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
     * Returns the raw profile image bytes.
     *
     * @return image bytes, or {@code null} when no profile image is set
     */
    public byte[] getProfileImage() {
        return profileImage;
    }

    /**
     * Updates or removes the profile image.
     *
     * @param profileImage image bytes, or {@code null} to remove the image
     */
    public void setProfileImage(byte[] profileImage) {
        this.profileImage = profileImage;
    }
}


