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

@RestController
@RequestMapping("/promotions")
@Tag(name = "Promotions", description = "Percentage promotions for products")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promotion by id", description = "Admin-only.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List promotions", description = "Admin-only.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PromotionResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    @Operation(
            summary = "List active promotions for a product",
            description = "Returns active percentage promotions for the given product. If userId is provided, filters out promotions already used by that user."
    )
    public ResponseEntity<Page<PromotionResponse>> getActiveByProduct(
            @Parameter(description = "Product id") @RequestParam Long productId,
            @Parameter(description = "Optional filter. Admins can query any userId; users can only query their own.") @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getActiveByProduct(productId, userId, pageable));
    }

    @PostMapping("/percentage")
    @Operation(summary = "Create percentage promotion", description = "Admin-only. Creates an active percentage discount for a product.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promotion created")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromotionResponse> createPercentage(@Valid @RequestBody PercentagePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPercentage(request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update promotion flags", description = "Admin-only. Currently supports updating { active }.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patch(@PathVariable Long id, @Valid @RequestBody ActiveUpdateRequest request) {
        service.setActive(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
