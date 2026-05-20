package com.bakery.bakeryapi.shared;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Ayudantes para leer la autenticación actual de Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Devuelve la autenticación actual no anónima cuando está presente.
     *
     * @return autenticación actual, o {@code null} cuando no se autentica
     */
    public static Authentication optionalAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    /**
     * Devuelve la autenticación actual o lanza la excepción no autorizada de Spring Security.
     *
     * @return autenticación actual
     */
    public static Authentication requireAuthentication() {
        Authentication auth = optionalAuthentication();
        if (auth == null) {
            throw new AuthenticationCredentialsNotFoundException("No autorizado");
        }
        return auth;
    }

    /**
     * Comprueba si una autenticación tiene el rol de administrador.
     *
     * @param auth autenticación a inspeccionar
     * @return {@code true} cuando el usuario tiene {@code ROLE_ADMIN}
     */
    public static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

