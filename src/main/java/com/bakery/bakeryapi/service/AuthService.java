package com.bakery.bakeryapi.service;

import com.bakery.bakeryapi.dto.auth.LoginResponse;
import com.bakery.bakeryapi.exception.auth.InvalidCredentialsException;
import com.bakery.bakeryapi.security.JwtTokenService;
import com.bakery.bakeryapi.service.UserService;
import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.exception.user.UserDisabledException;
import com.bakery.bakeryapi.exception.user.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserService userService,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse register(String email, String password) {
        User user = userService.createInternal(email, password, Role.USER);
        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
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

        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token);
    }
}



