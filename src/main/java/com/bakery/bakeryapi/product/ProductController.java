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
@Tag(name = "Productos", description = "Catálogo de productos")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Los usuarios no administradores no pueden acceder a productos deshabilitados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Productos más vendidos", description = "Devuelve los productos más vendidos (basado en compras PAGADAS).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto")
    })
    public ResponseEntity<Page<ProductSalesResponse>> getTopSelling(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(service.getTopSelling(pageable));
    }

    @GetMapping
    @Operation(summary = "Listar productos", description = """
            Filtros opcionales por categoryId y/o nombre. Soporta ordenación. Los usuarios no administradores solo ven productos activos.
            
            Parámetros de consulta:
            - page: Número de página (empezando en 0, por defecto: 0)
            - size: Elementos por página (por defecto: 20, máx: 100)
            - sortBy: Campo por el que ordenar (name, price, category, createdAt - por defecto: name)
            - order: Dirección de ordenación (asc, desc - por defecto: asc)
            - categoryId: Filtro opcional por categoría
            - name: Filtro opcional por nombre (coincidencia parcial, insensible a mayúsculas)
            
            Ejemplo: /products?page=0&size=10&sortBy=price&order=desc&categoryId=1&name=pan
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correcto"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Page<ProductResponse>> getAll(
            @Parameter(description = "Número de página (empezando en 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Elementos por página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo por el que ordenar (name, price, category, createdAt)") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Dirección de ordenación (asc, desc)") @RequestParam(required = false) String order,
            @Parameter(description = "Filtro opcional por categoría") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filtro opcional por nombre (coincidencia parcial)") @RequestParam(required = false) String name
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
    @Operation(summary = "Crear producto", description = "Solo para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(product);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Solo para administradores.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar banderas de producto", description = "Solo para administradores. Actualmente soporta la actualización de { active }.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patch(@PathVariable Long id, @Valid @RequestBody ActiveUpdateRequest request) {
        service.setActive(id, request.active());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Solo para administradores. Si el producto tiene compras, será desactivado en lugar de eliminado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado o desactivado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Prohibido"),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
