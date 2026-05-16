package com.bakery.bakeryapi.product;

import com.bakery.bakeryapi.category.CategoryService;
import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import com.bakery.bakeryapi.shared.PageableUtils;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.shared.ImageValidator;
import com.bakery.bakeryapi.product.dto.ProductRequest;
import com.bakery.bakeryapi.product.dto.ProductResponse;
import com.bakery.bakeryapi.product.dto.ProductSalesResponse;
import com.bakery.bakeryapi.product.exception.ProductInactiveException;
import com.bakery.bakeryapi.product.exception.ProductNotFoundException;
import com.bakery.bakeryapi.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * Servicio de aplicación para productos.
 *
 * Aplica reglas de visibilidad para productos inactivos, valida imágenes de productos opcionales
 * y previene eliminación forzada cuando existe historial de compras.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;
    private final CategoryService categoryService;
    private final PaginationProperties paginationProperties;

    public ProductService(
            ProductRepository repository,
            CategoryService categoryService,
            PaginationProperties paginationProperties
    ) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.paginationProperties = paginationProperties;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creating new product: {}", request.name());
        
        // Validar imagen si se proporciona
        if (request.imageBase64() != null && !request.imageBase64().isBlank()) {
            ImageValidator.validateImageBase64(request.imageBase64());
        }
        
        Category category = categoryService.getEntityById(request.categoryId());
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                category
        );
        if (request.imageBase64() != null && !request.imageBase64().isBlank()) {
            product.setImage(decodeImageBase64(request.imageBase64()));
        }
        Product saved = repository.save(product);
        log.info("Product created successfully with ID: {}", saved.getId());
        return ProductResponse.from(saved);
    }

    public Product getEntityById(Long id) {
        log.debug("Fetching product entity by ID: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: {}", id);
                    return new ProductNotFoundException(id);
                });
    }

    public Product getActiveEntityById(Long id) {
        Product product = getEntityById(id);
        if (!product.isActive()) {
            throw new ProductInactiveException(id);
        }
        return product;
    }

    public ProductResponse getById(Long id) {
        Authentication auth = SecurityUtils.optionalAuthentication();
        Product product = getEntityById(id);
        if (auth != null && SecurityUtils.isAdmin(auth)) {
            return ProductResponse.from(product);
        }
        if (!product.isActive()) {
            // No filtrar productos inactivos para no-administradores (incluyendo usuarios anónimos).
            throw new ProductNotFoundException(id);
        }
        return ProductResponse.from(product);
    }

    public ProductResponse getActiveByIdCached(Long id) {
        return ProductResponse.from(getActiveEntityById(id));
    }

    public Page<ProductResponse> getAllActiveCached(Pageable pageable, Long categoryId, String name) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        
        // Si ambos filtros son null, devolver todos
        if (categoryId == null && (name == null || name.isBlank())) {
            return repository.findAllByActiveTrue(safePageable).map(ProductResponse::from);
        }
        
        // Solo categoría
        if (categoryId != null && (name == null || name.isBlank())) {
            categoryService.getEntityById(categoryId);
            return repository.findAllByCategoryIdAndActiveTrue(categoryId, safePageable).map(ProductResponse::from);
        }
        
        // Solo nombre
        if (categoryId == null) {
            return repository.findByNameContainsIgnoreCaseAndActiveTrue(name, safePageable).map(ProductResponse::from);
        }
        
        // Ambos filtros
        categoryService.getEntityById(categoryId);
        return repository.findByCategoryIdAndNameContainsIgnoreCaseAndActiveTrue(categoryId, name, safePageable).map(ProductResponse::from);
    }

    public Page<ProductResponse> getAll(Pageable pageable, Long categoryId, String name) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        Authentication auth = SecurityUtils.optionalAuthentication();
        boolean admin = auth != null && SecurityUtils.isAdmin(auth);

        // Si ambos filtros son null, devolver todos
        if (categoryId == null && (name == null || name.isBlank())) {
            if (admin) {
                return repository.findAll(safePageable).map(ProductResponse::from);
            }
            return getAllActiveCached(safePageable, null, null);
        }

        // Validar categoría si se proporciona
        if (categoryId != null) {
            categoryService.getEntityById(categoryId);
        }

        // Solo nombre
        if (categoryId == null) {
            if (admin) {
                return repository.findByNameContainsIgnoreCase(name, safePageable).map(ProductResponse::from);
            }
            return getAllActiveCached(safePageable, null, name);
        }

        // Solo categoría
        if (name == null || name.isBlank()) {
            if (admin) {
                return repository.findAllByCategoryId(categoryId, safePageable).map(ProductResponse::from);
            }
            return getAllActiveCached(safePageable, categoryId, null);
        }

        // Ambos filtros
        if (admin) {
            return repository.findByCategoryIdAndNameContainsIgnoreCase(categoryId, name, safePageable).map(ProductResponse::from);
        }
        return getAllActiveCached(safePageable, categoryId, name);
    }

    public Page<ProductSalesResponse> getTopSelling(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        Authentication auth = SecurityUtils.optionalAuthentication();
        return auth != null && SecurityUtils.isAdmin(auth)
                ? repository.findTopSellingByStatus(PurchaseStatus.PAID, safePageable)
                : repository.findTopSellingByStatusAndActiveTrue(PurchaseStatus.PAID, safePageable);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product ID: {}", id);
        
        // Validar imagen si se proporciona
        if (request.imageBase64() != null && !request.imageBase64().isBlank()) {
            ImageValidator.validateImageBase64(request.imageBase64());
        }
        
        Product product = getEntityById(id);
        Category category = categoryService.getEntityById(request.categoryId());
        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                category
        );
        if (request.imageBase64() != null && !request.imageBase64().isBlank()) {
            product.setImage(decodeImageBase64(request.imageBase64()));
        }
        Product updated = repository.save(product);
        log.info("Product updated successfully: {}", id);
        return ProductResponse.from(updated);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        log.info("Setting product {} active status to: {}", id, active);
        Product product = getEntityById(id);
        if (active) {
            product.enable();
        } else {
            product.disable();
        }
        repository.save(product);
        log.info("Product {} active status updated", id);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Attempting to delete product ID: {}", id);
        Product product = getEntityById(id);
        
        if (repository.existsPurchasesByProductId(id)) {
            log.info("Product {} has purchases, deactivating instead of deleting", id);
            product.disable();
            repository.save(product);
        } else {
            log.info("Product {} has no purchases, performing hard delete", id);
            repository.delete(product);
        }
    }

    private byte[] decodeImageBase64(String imageBase64) {
        try {
            return Base64.getDecoder().decode(imageBase64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 image encoding", e);
        }
    }
}
