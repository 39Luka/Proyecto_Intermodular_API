package com.bakery.bakeryapi.purchase;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.purchase.dto.PurchaseRequest;
import com.bakery.bakeryapi.purchase.dto.PurchaseResponse;
import com.bakery.bakeryapi.purchase.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Puntos finales REST para crear compras, búsqueda y cambios de estado.
 */
@RestController
@RequestMapping("/purchases")
@Tag(name = "Compras", description = "Crear y gestionar compras (el stock se actualiza transaccionalmente)")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener compra por ID", description = "Los usuarios solo pueden acceder a sus propias compras; los administradores pueden acceder a cualquiera.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<PurchaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Listar compras", description = """
            Los administradores pueden filtrar opcionalmente por userId y/o rango de fechas. Los no administradores siempre ven sus propias compras. Soporta ordenación.
            
            Parámetros de consulta:
            - page: Número de página (empezando en 0, por defecto: 0)
            - size: Elementos por página (por defecto: 20, máx: 100)
            - sortBy: Campo por el que ordenar (createdAt, total, status - por defecto: createdAt)
            - order: Dirección de ordenación (asc, desc - por defecto: desc)
            - userId: Filtro solo para administradores para un usuario específico
            - startDate: Fecha de inicio (formato ISO 8601, ej. 2024-01-01T00:00:00)
            - endDate: Fecha de fin (formato ISO 8601, ej. 2024-12-31T23:59:59)
            
            Ejemplo: /purchases?page=0&size=10&sortBy=createdAt&order=desc&startDate=2024-01-01T00:00:00
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Page<PurchaseResponse>> getAll(
            @Parameter(description = "Número de página (empezando en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Elementos por página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el que ordenar (createdAt, total, status)") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Dirección de ordenación (asc, desc)") @RequestParam(required = false) String order,
            @Parameter(description = "Filtro solo para administradores") @RequestParam(required = false) Long userId,
            @Parameter(description = "Fecha de inicio (formato ISO 8601)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "Fecha de fin (formato ISO 8601)") @RequestParam(required = false) LocalDateTime endDate
    ) {
        Sort sort = buildSort(sortBy, order, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.getAll(pageable, userId, startDate, endDate));
    }

    private Sort buildSort(String sortBy, String order, String defaultField) {
        String field = (sortBy == null || sortBy.isBlank()) ? defaultField : sortBy;
        Sort.Direction direction = (order != null && order.equalsIgnoreCase("asc")) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    @PostMapping
    @Operation(summary = "Crear compra", description = "Disminuye el stock, aplica opcionalmente una promoción porcentual (una sola vez por usuario) y crea una compra.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compra creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Producto, promoción o usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Actualización concurrente / conflicto")
    })
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse purchase = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchase);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar compra", description = "Solo el propietario (o administrador) puede cancelar una compra CREADA. Restaura el stock y libera el uso de la promoción.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cancelada"),
            @ApiResponse(responseCode = "400", description = "Estado de compra inválido"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Marcar compra como pagada", description = "Solo el propietario (o administrador) puede pagar una compra CREADA.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pagada"),
            @ApiResponse(responseCode = "400", description = "Estado de compra inválido"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrada")
    })
    public ResponseEntity<Void> pay(@PathVariable Long id) {
        service.pay(id);
        return ResponseEntity.noContent().build();
    }
}


