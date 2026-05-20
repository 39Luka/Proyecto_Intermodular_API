package com.bakery.bakeryapi.user;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.shared.dto.ActiveUpdateRequest;
import com.bakery.bakeryapi.user.dto.UserRequest;
import com.bakery.bakeryapi.user.dto.UserResponse;
import com.bakery.bakeryapi.user.UserService;
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
 * Puntos finales REST solo para administradores para la gestión de usuarios.
 */
@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios solo para administradores")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID", description = "Solo para administradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Obtener usuario por correo electrónico", description = "Solo para administradores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<UserResponse> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }


    @PostMapping
    @Operation(summary = "Crear usuario", description = "Solo para administradores. Crea un usuario con el rol/banderas según la carga de solicitud.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "409", description = "Conflicto")
    })
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest request) {
        UserResponse user = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(user);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar banderas de usuario", description = "Solo para administradores. Actualmente soporta la actualización de { active } que se mapea a habilitado/deshabilitado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<Void> patchUser(@PathVariable Long id, @RequestBody @Valid ActiveUpdateRequest request) {
        // La API usa un nombre de bandera unificado: { "active": true|false }. Para usuarios se mapea al estado "enabled".
        service.setEnabled(id, request.active());
        return ResponseEntity.noContent().build();
    }

}
