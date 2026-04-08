package com.bakery.bakeryapi.product;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.shared.dto.ActiveUpdateRequest;
import com.bakery.bakeryapi.product.dto.ProductRequest;
import com.bakery.bakeryapi.product.dto.ProductResponse;
import com.bakery.bakeryapi.product.dto.ProductSalesResponse;
import com.bakery.bakeryapi.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id", description = "Non-admin users cannot access disabled products.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Top selling products", description = "Returns the most sold products (from PAID purchases).")
    public ResponseEntity<Page<ProductSalesResponse>> getTopSelling(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getTopSelling(pageable));
    }

    @GetMapping
    @Operation(summary = "List products", description = "Optional filter by categoryId. Non-admin users only see active products.")
    public ResponseEntity<Page<ProductResponse>> getAll(
            Pageable pageable,
            @Parameter(description = "Optional category filter") @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(service.getAll(pageable, categoryId));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Admin-only.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Admin-only.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update product flags", description = "Admin-only. Currently supports updating { active }.")
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
