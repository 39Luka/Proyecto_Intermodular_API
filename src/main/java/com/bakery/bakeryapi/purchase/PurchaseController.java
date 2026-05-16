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
@Tag(name = "Purchases", description = "Create and manage purchases (stock is updated transactionally)")
@SecurityRequirement(name = "bearerAuth")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase by id", description = "Users can only access their own purchases; admins can access any.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<PurchaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List purchases", description = """
            Admins can optionally filter by userId and/or date range. Non-admins always see their own purchases. Supports sorting.
            
            Query parameters:
            - page: Page number (0-indexed, default: 0)
            - size: Items per page (default: 20, max: 100)
            - sortBy: Field to sort by (createdAt, total, status - default: createdAt)
            - order: Sort direction (asc, desc - default: desc)
            - userId: Admin-only filter for specific user
            - startDate: Start date (ISO 8601 format, e.g. 2024-01-01T00:00:00)
            - endDate: End date (ISO 8601 format, e.g. 2024-12-31T23:59:59)
            
            Example: /purchases?page=0&size=10&sortBy=createdAt&order=desc&startDate=2024-01-01T00:00:00
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Page<PurchaseResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by (createdAt, total, status)") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc, desc)") @RequestParam(required = false) String order,
            @Parameter(description = "Admin-only filter") @RequestParam(required = false) Long userId,
            @Parameter(description = "Start date (ISO 8601 format)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (ISO 8601 format)") @RequestParam(required = false) LocalDateTime endDate
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
    @Operation(summary = "Create purchase", description = "Decreases stock, optionally applies a percentage promotion (one-time per user), and creates a purchase.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Purchase created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Product, promotion or user not found"),
            @ApiResponse(responseCode = "409", description = "Concurrent update / conflict")
    })
    public ResponseEntity<PurchaseResponse> create(@Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse purchase = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchase);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel purchase", description = "Only the owner (or admin) can cancel a CREATED purchase. Restores stock and releases promotion usage.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pay")
    @Operation(summary = "Mark purchase as paid", description = "Only the owner (or admin) can pay a CREATED purchase.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paid"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> pay(@PathVariable Long id) {
        service.pay(id);
        return ResponseEntity.noContent().build();
    }
}


