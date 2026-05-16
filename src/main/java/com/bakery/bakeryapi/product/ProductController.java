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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Puntos finales REST para el catálogo de productos.
 */
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
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Top selling products", description = "Returns the most sold products (from PAID purchases).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    public ResponseEntity<Page<ProductSalesResponse>> getTopSelling(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getTopSelling(pageable));
    }

    @GetMapping
    @Operation(summary = "List products", description = """
            Optional filters by categoryId and/or name. Supports sorting. Non-admin users only see active products.
            
            Query parameters:
            - page: Page number (0-indexed, default: 0)
            - size: Items per page (default: 20, max: 100)
            - sortBy: Field to sort by (name, price, category, createdAt - default: name)
            - order: Sort direction (asc, desc - default: asc)
            - categoryId: Optional category filter
            - name: Optional name filter (partial match, case-insensitive)
            
            Example: /products?page=0&size=10&sortBy=price&order=desc&categoryId=1&name=pan
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<ProductResponse>> getAll(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by (name, price, category, createdAt)") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction (asc, desc)") @RequestParam(required = false) String order,
            @Parameter(description = "Optional category filter") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Optional name filter (partial match)") @RequestParam(required = false) String name
    ) {
        Sort sort = buildSort(sortBy, order, "name");
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.getAll(pageable, categoryId, name));
    }

    private Sort buildSort(String sortBy, String order, String defaultField) {
        String field = (sortBy == null || sortBy.isBlank()) ? defaultField : sortBy;
        Sort.Direction direction = (order != null && order.equalsIgnoreCase("desc")) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    @PostMapping
    @Operation(summary = "Create product", description = "Admin-only.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Category not found")
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
            @ApiResponse(responseCode = "200", description = "Updated"),
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
    @SecurityRequirement(name = "bearerAuth")
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Admin-only. If the product has purchases, it will be deactivated instead of deleted.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted or deactivated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
