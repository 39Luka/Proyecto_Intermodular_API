package com.bakery.api.auth;

import com.bakery.api.auth.dto.LoginResponse;
import com.bakery.api.auth.exception.InvalidCredentialsException;
import com.bakery.api.auth.refresh.RefreshTokenService;
import com.bakery.api.security.JwtTokenService;
import com.bakery.api.user.UserService;
import com.bakery.api.user.domain.Role;
import com.bakery.api.user.domain.User;
import com.bakery.api.user.exception.UserDisabledException;
import com.bakery.api.user.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserService userService,
            JwtTokenService jwtTokenService,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse register(String email, String password) {
        User user = userService.createInternal(email, password, Role.USER);
        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.issueFor(user);

        return new LoginResponse(token, refreshToken);
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
        String refreshToken = refreshTokenService.issueFor(user);

        return new LoginResponse(token, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(refreshToken);
        User user = rotation.user();
        String token = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, rotation.refreshToken());
    }
}



