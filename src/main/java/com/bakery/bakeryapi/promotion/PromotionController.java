package com.bakery.bakeryapi.promotion;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.shared.dto.ActiveUpdateRequest;
import com.bakery.bakeryapi.promotion.dto.PercentagePromotionRequest;
import com.bakery.bakeryapi.promotion.dto.PromotionResponse;
import com.bakery.bakeryapi.promotion.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Puntos finales REST para promociones de porcentaje.
 */
@RestController
@RequestMapping("/promotions")
@Tag(name = "Promociones", description = "Promociones porcentuales para productos")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener promoción por ID", description = "Solo para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Listar promociones", description = "Solo para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PromotionResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Listar promociones activas para un producto",
            description = "Devuelve las promociones de porcentaje activas para el producto dado. Si se proporciona userId, filtra las promociones ya utilizadas por ese usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación para el filtrado por usuario"),
            @ApiResponse(responseCode = "403", description = "No se pueden consultar las promociones de otro usuario"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Page<PromotionResponse>> getActiveByProduct(
            @Parameter(description = "ID del producto") @RequestParam Long productId,
            @Parameter(description = "Filtro opcional. Los administradores pueden consultar cualquier userId; los usuarios solo pueden consultar el suyo propio.") @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getActiveByProduct(productId, userId, pageable));
    }

    @GetMapping("/available")
    @Operation(
            summary = "Listar todas las promociones disponibles para un usuario",
            description = "Devuelve las promociones de porcentaje activas que el usuario aún no ha utilizado en todos los productos."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "No se pueden consultar las promociones de otro usuario")
    })
    public ResponseEntity<Page<PromotionResponse>> getAvailablePromotions(
            @Parameter(description = "ID de usuario opcional. Los administradores pueden consultar cualquiera; los usuarios solo el suyo propio.") @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getAvailablePromotions(userId, pageable));
    }

    @PostMapping("/percentage")
    @Operation(summary = "Crear promoción de porcentaje", description = "Solo para administradores. Crea un descuento de porcentaje activo para un producto.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promoción creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> createPercentage(@Valid @RequestBody PercentagePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPercentage(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar banderas de promoción", description = "Solo para administradores. Actualmente soporta la actualización de { active }.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patch(@PathVariable Long id, @Valid @RequestBody ActiveUpdateRequest request) {
        service.setActive(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
