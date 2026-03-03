package com.bakery.api.auth;

import com.bakery.api.auth.dto.response.LoginResponse;
import com.bakery.api.auth.exception.InvalidCredentialsException;
import com.bakery.api.auth.refresh.RefreshTokenService;
import com.bakery.api.security.JwtProvider;
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
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserService userService,
            JwtProvider jwtProvider,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse register(String email, String password) {
        return register(email, password, null, null);
    }

    public LoginResponse register(String email, String password, String ip, String userAgent) {
        User user = userService.createInternal(email, password, Role.USER);
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.issueFor(user);

        return new LoginResponse(token, refreshToken);
    }


    public LoginResponse login(String email, String password) {
        return login(email, password, null, null);
    }

    public LoginResponse login(String email, String password, String ip, String userAgent) {
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
        String refreshToken = refreshTokenService.issueFor(user);

        return new LoginResponse(token, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        return refresh(refreshToken, null, null);
    }

    public LoginResponse refresh(String refreshToken, String ip, String userAgent) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(refreshToken);
        User user = rotation.user();
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, rotation.refreshToken());
    }
}



