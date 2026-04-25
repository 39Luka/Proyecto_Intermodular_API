package com.bakery.bakeryapi.auth;

import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.auth.exception.InvalidCredentialsException;
import com.bakery.bakeryapi.infra.security.JwtTokenService;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.domain.Role;
import com.bakery.bakeryapi.domain.User;
import com.bakery.bakeryapi.user.exception.UserDisabledException;
import com.bakery.bakeryapi.user.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        log.info("User registration attempt for email: {}", email);
        User user = userService.rotateRefreshToken(userService.createInternal(email, password, Role.USER));
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("User registered successfully: {}", email);
        return new LoginResponse(accessToken, refreshToken, jwtTokenService.getExpirationMs());
    }


    public LoginResponse login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        User user;
        try {
            user = userService.getEntityByEmail(email);
        } catch (UserNotFoundException e){
            log.warn("Login failed: user not found for email: {}", email);
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed: invalid password for email: {}", email);
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            log.warn("Login failed: user disabled for email: {}", email);
            throw new UserDisabledException();
        }

        user = userService.rotateRefreshToken(user);
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("Login successful for email: {}", email);
        return new LoginResponse(accessToken, refreshToken, jwtTokenService.getExpirationMs());
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        log.info("Refresh token request");
        JwtTokenService.RefreshTokenPayload payload = jwtTokenService.readRefreshToken(refreshToken);
        if (payload == null) {
            log.warn("Refresh token validation failed");
            throw new InvalidCredentialsException();
        }

        String email = payload.subject();
        if (email == null || email.isBlank()) {
            log.warn("Could not extract email from refresh token");
            throw new InvalidCredentialsException();
        }

        User user;
        try {
            user = userService.getEntityByEmail(email);
        } catch (UserNotFoundException e) {
            log.warn("Refresh token rejected: user not found for email: {}", email);
            throw new InvalidCredentialsException();
        }
        if (!user.isEnabled()) {
            log.warn("User disabled: {}", email);
            throw new UserDisabledException();
        }
        if (payload.refreshTokenVersion() != user.getRefreshTokenVersion()) {
            log.warn("Refresh token rejected: rotated token reuse for email: {}", email);
            throw new InvalidCredentialsException();
        }

        user = userService.rotateRefreshToken(user);
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("Refresh token validated and new access token generated for: {}", email);
        return new LoginResponse(accessToken, newRefreshToken, jwtTokenService.getExpirationMs());
    }
}



