package com.bakery.bakeryapi.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication optionalAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth;
    }

    public static Authentication requireAuthentication() {
        Authentication auth = optionalAuthentication();
        if (auth == null) {
            throw new AuthenticationCredentialsNotFoundException("Unauthorized");
        }
        return auth;
    }

    public static boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}

