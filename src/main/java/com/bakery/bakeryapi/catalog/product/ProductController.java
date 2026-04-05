package com.bakery.bakeryapi.catalog.product;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.common.dto.ActiveUpdateRequest;
import com.bakery.bakeryapi.catalog.product.dto.ProductRequest;
import com.bakery.bakeryapi.catalog.product.dto.ProductResponse;
import com.bakery.bakeryapi.catalog.product.dto.ProductSalesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Top selling products", description = "Returns the most sold products (from PAID purchases).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of products"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProductSalesResponse>> getTopSelling(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getTopSelling(pageable));
    }

    @GetMapping
    @Operation(summary = "List products", description = "Optional filter by categoryId. Non-admin users only see active products.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of products"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ProductResponse>> getAll(
            Pageable pageable,
            @Parameter(description = "Optional category filter") @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(service.getAll(pageable, categoryId));
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
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
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patch(@PathVariable Long id, @Valid @RequestBody ActiveUpdateRequest request) {
        service.setActive(id, request.active());
        return ResponseEntity.noContent().build();
    }
}
