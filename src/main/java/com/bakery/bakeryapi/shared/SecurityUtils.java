package com.bakery.bakeryapi.shared;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helpers for reading the current Spring Security authentication.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Returns the current non-anonymous authentication when present.
     *
     * @return current authentication, or {@code null} when unauthenticated
     */
    public static Authentication optionalAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    /**
     * Returns the current authentication or raises Spring Security's unauthorized exception.
     *
     * @return current authentication
     */
    public static Authentication requireAuthentication() {
        Authentication auth = optionalAuthentication();
        if (auth == null) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }
        return auth;
    }

    /**
     * Checks whether an authentication has the admin role.
     *
     * @param auth authentication to inspect
     * @return {@code true} when the user has {@code ROLE_ADMIN}
     */
    public static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

