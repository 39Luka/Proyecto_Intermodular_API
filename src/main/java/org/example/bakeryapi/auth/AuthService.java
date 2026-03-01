package org.example.bakeryapi.auth;

import org.example.bakeryapi.auth.dto.response.LoginResponse;
import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.auth.exception.InvalidCredentialsException;
import org.example.bakeryapi.auth.refresh.RefreshTokenService;
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

    public LoginResponse register(String email, String password, Role role) {
        return register(email, password, role, null, null);
    }

    public LoginResponse register(String email, String password, Role role, String ip, String userAgent) {
        if (role == Role.ADMIN) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream()
                    .noneMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"))) {
                throw new ForbiddenOperationException("Only an ADMIN can create another ADMIN");
            }
        }

        User user = userService.createInternal(email, password, role);
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.issueFor(user, ip, userAgent);

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
        String refreshToken = refreshTokenService.issueFor(user, ip, userAgent);

        return new LoginResponse(token, refreshToken);
    }

    public LoginResponse refresh(String refreshToken) {
        return refresh(refreshToken, null, null);
    }

    public LoginResponse refresh(String refreshToken, String ip, String userAgent) {
        RefreshTokenService.RotationResult rotation = refreshTokenService.rotate(refreshToken, ip, userAgent);
        User user = rotation.user();
        String token = jwtProvider.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, rotation.refreshToken());
    }
}



