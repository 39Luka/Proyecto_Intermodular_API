package org.example.bakeryapi.auth;

import org.example.bakeryapi.auth.dto.LoginResponse;
import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.auth.exception.InvalidCredentialsException;
import org.example.bakeryapi.security.JwtProvider;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.domain.Role;
import org.example.bakeryapi.user.domain.User;
import org.example.bakeryapi.user.exception.UserDisabledException;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse register(String email, String password, Role role) {
        if (role == Role.ADMIN) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream()
                    .noneMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"))) {
                throw new ForbiddenOperationException("Only an ADMIN can create another ADMIN");
            }
        }

        User user = userService.createInternal(email, password, role);
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token);
    }


    public LoginResponse login(String email, String password) {
        User user;
        try {
            user = userService.getEntityByEmail(email);
        } catch (UserNotFoundException e){
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new UserDisabledException();
        }

        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(token);
    }
}



