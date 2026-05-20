package com.bakery.bakeryapi.auth;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.auth.dto.LoginRequest;
import com.bakery.bakeryapi.auth.dto.RefreshTokenRequest;
import com.bakery.bakeryapi.auth.dto.RegisterRequest;
import com.bakery.bakeryapi.auth.dto.LoginResponse;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.user.UserService;
import com.bakery.bakeryapi.user.dto.PasswordUpdateRequest;
import com.bakery.bakeryapi.user.dto.ProfileImageUpdateRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Puntos finales REST para autenticación y acciones de cuenta autenticada.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Inicio de sesión, registro y refresco de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica a un usuario y devuelve tokens de acceso y refresco.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(
                request.email(),
                request.password()
        ));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrarse", description = "Crea una nueva cuenta de USUARIO y devuelve tokens de acceso y refresco.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "409", description = "El correo electrónico ya existe")
    })
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(
                        request.email(),
                        request.password()
                ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Utiliza un token de refresco para obtener un nuevo token de acceso.", security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuevo token de acceso generado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Token de refresco inválido o expirado")
    })
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshAccessToken(request.refreshToken()));
    }

    /**
     * Devuelve los datos de la cuenta del usuario autenticado.
     *
     * @return datos del usuario actual
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Obtener mi usuario", description = "Devuelve los datos de la cuenta del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<UserResponse> getMe() {
        String email = SecurityUtils.requireAuthentication().getName();
        return ResponseEntity.ok(userService.getByEmail(email));
    }

    /**
     * Actualiza o elimina la imagen de perfil del usuario autenticado.
     *
     * @param request solicitud que contiene la imagen en Base64, o un valor vacío para eliminarla
     * @return datos del usuario actualizados
     */
    @PatchMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualizar mi imagen de perfil", description = "Actualiza o elimina la imagen de perfil del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Imagen inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<UserResponse> updateMyProfileImage(
            @RequestBody ProfileImageUpdateRequest request
    ) {
        String email = SecurityUtils.requireAuthentication().getName();
        return ResponseEntity.ok(userService.updateProfileImage(email, request.profileImageBase64()));
    }

    /**
     * Cambia la contraseña del usuario autenticado tras comprobar la contraseña actual.
     *
     * @param request solicitud que contiene la contraseña actual y la nueva contraseña
     * @return respuesta vacía cuando se cambia la contraseña
     */
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cambiar mi contraseña", description = "Cambia la contraseña del usuario autenticado tras validar la contraseña actual.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Contraseña actual inválida o no autorizado")
    })
    public ResponseEntity<Void> changeMyPassword(
            @Valid @RequestBody PasswordUpdateRequest request
    ) {
        String email = SecurityUtils.requireAuthentication().getName();
        userService.changePassword(email, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

}

