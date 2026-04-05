package com.bakery.bakeryapi.purchasess;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.purchasess.dto.PurchaseRequest;
import com.bakery.bakeryapi.purchasess.dto.PurchaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchases")
@Tag(name = "Purchases", description = "Create and manage purchases (stock is updated transactionally)")
public class PurchaseController {

    private final PurchaseService service;

    public PurchaseController(PurchaseService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get purchase by id", description = "Users can only access their own purchases; admins can access any.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<PurchaseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List purchases", description = "Admins can optionally filter by userId. Non-admins always see their own purchases.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of purchases"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<PurchaseResponse>> getAll(
            Pageable pageable,
            @Parameter(description = "Admin-only filter") @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(service.getAll(pageable, userId));
    }

    @PostMapping
    @Operation(summary = "Create purchase", description = "Decreases stock, optionally applies a percentage promotion (one-time per user), and creates a purchase.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Purchase created"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase / promotion"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
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
            @ApiResponse(responseCode = "400", description = "Invalid state"),
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
            @ApiResponse(responseCode = "400", description = "Invalid state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> pay(@PathVariable Long id) {
        service.pay(id);
        return ResponseEntity.noContent().build();
    }
}


