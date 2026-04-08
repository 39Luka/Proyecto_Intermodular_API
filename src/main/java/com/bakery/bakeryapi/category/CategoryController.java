package com.bakery.bakeryapi.category;

import jakarta.validation.Valid;
import com.bakery.bakeryapi.category.dto.CategoryRequest;
import com.bakery.bakeryapi.category.dto.CategoryResponse;
import com.bakery.bakeryapi.category.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Product categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "List categories")
    public ResponseEntity<Page<CategoryResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Create category", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "409", description = "Already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Admin-only.")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Already exists")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
