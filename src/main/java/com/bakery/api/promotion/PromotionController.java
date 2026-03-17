package com.bakery.api.promotion;

import jakarta.validation.Valid;
import com.bakery.api.promotion.dto.request.PercentagePromotionRequest;
import com.bakery.api.promotion.dto.response.PromotionResponse;
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
@RequestMapping("/promotions")
@Tag(name = "Promotions", description = "Percentage promotions for products")
public class PromotionController {

    private final PromotionService service;

    public PromotionController(PromotionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get promotion by id", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promotion found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<PromotionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List promotions", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of promotions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PromotionResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    @Operation(
            summary = "List active promotions for a product",
            description = "Returns active percentage promotions for the given product. If userId is provided, filters out promotions already used by that user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of active promotions"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<PromotionResponse>> getActiveByProduct(
            @Parameter(description = "Product id") @RequestParam Long productId,
            @Parameter(description = "Optional filter. Admins can query any userId; users can only query their own.") @RequestParam(required = false) Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getActiveByProduct(productId, userId, pageable));
    }

    @PostMapping("/percentage")
    @Operation(summary = "Create percentage promotion", description = "Admin-only. Creates an active percentage discount for a product.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promotion created"),
            @ApiResponse(responseCode = "400", description = "Invalid promotion"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<PromotionResponse> createPercentage(@Valid @RequestBody PercentagePromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPercentage(request));
    }

    @PatchMapping("/{id}/disable")
    @Operation(summary = "Disable promotion", description = "Admin-only. Disables a promotion without deleting it.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disabled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        service.disable(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    @Operation(summary = "Enable promotion", description = "Admin-only. Re-enables a previously disabled promotion.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Enabled"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        service.enable(id);
        return ResponseEntity.noContent().build();
    }
}
