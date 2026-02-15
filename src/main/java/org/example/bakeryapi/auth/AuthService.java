package org.example.bakeryapi.auth;

import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.auth.exception.InvalidCredentialsException;
import org.example.bakeryapi.security.JwtProvider;
import org.example.bakeryapi.user.User;
import org.example.bakeryapi.user.Role;
import org.example.bakeryapi.user.UserService;
import org.example.bakeryapi.user.exception.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtProvider jwtProvider, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(String email, String password, Role role) {
        if (role == Role.ADMIN) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream()
                    .noneMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"))) {
                throw new ForbiddenOperationException("Solo un ADMIN puede crear otro ADMIN");
            }
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.create(email, hashedPassword, role);

        return jwtProvider.generateToken(user.getEmail(), user.getRole().name());
    }


    public String login(String email, String password) {
        User user;
        try {
            user = userService.getByEmail(email);
        } catch (UserNotFoundException e){
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return jwtProvider.generateToken(user.getEmail(), user.getRole().name());
    }
}

