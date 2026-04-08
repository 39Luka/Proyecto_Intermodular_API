package com.bakery.bakeryapi.auth;

import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.infra.security.JwtTokenService;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.user.exception.UserDisabledException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
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



