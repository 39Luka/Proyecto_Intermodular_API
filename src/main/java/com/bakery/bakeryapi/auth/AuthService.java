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

/**
 * Coordina flujos de autenticación: registro, inicio de sesión y rotación de token de actualización.
 */
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

    /**
     * Registra una nueva cuenta pública con el rol {@link Role#USER} y devuelve los tokens de inicio de sesión.
     *
     * @param email correo electrónico único de la cuenta
     * @param password contraseña sin procesar para codificar
     * @return tokens de acceso y refresco para la nueva cuenta
     */
    public LoginResponse register(String email, String password) {
        log.info("Intento de registro de usuario para el correo: {}", email);
        User user = userService.rotateRefreshToken(userService.createInternal(email, password, Role.USER));
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("Usuario registrado con éxito: {}", email);
        return new LoginResponse(accessToken, refreshToken, jwtTokenService.getExpirationMs());
    }


    /**
     * Autentica a un usuario por correo/contraseña y rota su token de refresco.
     *
     * @param email correo de la cuenta
     * @param password contraseña sin procesar proporcionada por el cliente
     * @return nuevos tokens de acceso y refresco
     * @throws InvalidCredentialsException cuando el usuario no existe o la contraseña es incorrecta
     * @throws UserDisabledException cuando la cuenta está deshabilitada
     */
    public LoginResponse login(String email, String password) {
        log.info("Intento de inicio de sesión para el correo: {}", email);
        User user;
        try {
            user = userService.getEntityByEmail(email);
        } catch (UserNotFoundException e){
            log.warn("Inicio de sesión fallido: usuario no encontrado para el correo: {}", email);
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Inicio de sesión fallido: contraseña inválida para el correo: {}", email);
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            log.warn("Inicio de sesión fallido: usuario deshabilitado para el correo: {}", email);
            throw new UserDisabledException();
        }

        user = userService.rotateRefreshToken(user);
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("Inicio de sesión exitoso para el correo: {}", email);
        return new LoginResponse(accessToken, refreshToken, jwtTokenService.getExpirationMs());
    }

    /**
     * Valida un token de refresco y devuelve un nuevo par de tokens.
     *
     * Los tokens de refresco son de un solo uso porque la versión del token almacenada se rota después
     * de cada refresco exitoso.
     *
     * @param refreshToken token de refresco proporcionado por el cliente
     * @return nuevos tokens de acceso y refresco
     * @throws InvalidCredentialsException cuando el token es inválido, ha expirado o ya ha sido rotado
     * @throws UserDisabledException cuando la cuenta está deshabilitada
     */
    public LoginResponse refreshAccessToken(String refreshToken) {
        log.info("Solicitud de refresco de token");
        JwtTokenService.RefreshTokenPayload payload = jwtTokenService.readRefreshToken(refreshToken);
        if (payload == null) {
            log.warn("Fallo en la validación del token de refresco");
            throw new InvalidCredentialsException();
        }

        String email = payload.subject();
        if (email == null || email.isBlank()) {
            log.warn("No se pudo extraer el correo del token de refresco");
            throw new InvalidCredentialsException();
        }

        User user;
        try {
            user = userService.getEntityByEmail(email);
        } catch (UserNotFoundException e) {
            log.warn("Token de refresco rechazado: usuario no encontrado para el correo: {}", email);
            throw new InvalidCredentialsException();
        }
        if (!user.isEnabled()) {
            log.warn("Usuario deshabilitado: {}", email);
            throw new UserDisabledException();
        }
        if (payload.refreshTokenVersion() != user.getRefreshTokenVersion()) {
            log.warn("Token de refresco rechazado: reutilización de token rotado para el correo: {}", email);
            throw new InvalidCredentialsException();
        }

        user = userService.rotateRefreshToken(user);
        String accessToken = jwtTokenService.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getEmail(), user.getRefreshTokenVersion());
        log.info("Token de refresco validado y nuevo token de acceso generado para: {}", email);
        return new LoginResponse(accessToken, newRefreshToken, jwtTokenService.getExpirationMs());
    }
}



