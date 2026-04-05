package com.bakery.bakeryapi.catalog.product;

import com.bakery.bakeryapi.catalog.category.Category;
import com.bakery.bakeryapi.catalog.category.CategoryService;
import com.bakery.bakeryapi.common.pagination.PageableUtils;
import com.bakery.bakeryapi.common.security.SecurityUtils;
import com.bakery.bakeryapi.catalog.product.dto.ProductMapper;
import com.bakery.bakeryapi.catalog.product.dto.ProductRequest;
import com.bakery.bakeryapi.catalog.product.dto.ProductResponse;
import com.bakery.bakeryapi.catalog.product.dto.ProductSalesResponse;
import com.bakery.bakeryapi.catalog.product.exception.ProductInactiveException;
import com.bakery.bakeryapi.catalog.product.exception.ProductNotFoundException;
import com.bakery.bakeryapi.purchasess.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;
    private final CategoryService categoryService;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, CategoryService categoryService, ProductMapper mapper) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.mapper = mapper;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryService.getEntityById(request.categoryId());
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                category
        );
        return mapper.toResponse(repository.save(product));
    }

    public Product getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
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
            return mapper.toResponse(product);
        }
        if (!product.isActive()) {
            // Do not leak inactive products to non-admins (including anonymous users).
            throw new ProductNotFoundException(id);
        }
        return mapper.toResponse(product);
    }

    public ProductResponse getActiveByIdCached(Long id) {
        return mapper.toResponse(getActiveEntityById(id));
    }

    public Page<ProductResponse> getAllActiveCached(Pageable pageable, Long categoryId) {
        Pageable safePageable = PageableUtils.safe(pageable);
        if (categoryId == null) {
            return repository.findAllByActiveTrue(safePageable).map(mapper::toResponse);
        }
        categoryService.getEntityById(categoryId);
        return repository.findAllByCategoryIdAndActiveTrue(categoryId, safePageable).map(mapper::toResponse);
    }

    public Page<ProductResponse> getAll(Pageable pageable, Long categoryId) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.optionalAuthentication();
        boolean admin = auth != null && SecurityUtils.isAdmin(auth);

        if (categoryId == null) {
            if (admin) {
                return repository.findAll(safePageable).map(mapper::toResponse);
            }
            return getAllActiveCached(safePageable, null);
        }
        categoryService.getEntityById(categoryId);
        if (admin) {
            return repository.findAllByCategoryId(categoryId, safePageable).map(mapper::toResponse);
        }
        return getAllActiveCached(safePageable, categoryId);
    }

    public Page<ProductSalesResponse> getTopSelling(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.optionalAuthentication();
        return auth != null && SecurityUtils.isAdmin(auth)
                ? repository.findTopSellingByStatus(PurchaseStatus.PAID, safePageable)
                : repository.findTopSellingByStatusAndActiveTrue(PurchaseStatus.PAID, safePageable);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntityById(id);
        Category category = categoryService.getEntityById(request.categoryId());
        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                category
        );
        return mapper.toResponse(repository.save(product));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Product product = getEntityById(id);
        if (active) {
            product.enable();
        } else {
            product.disable();
        }
        repository.save(product);
    }

    @Transactional
    @Deprecated(forRemoval = true)
    public void disable(Long id) {
        setActive(id, false);
    }

    @Transactional
    @Deprecated(forRemoval = true)
    public void enable(Long id) {
        setActive(id, true);
    }
}
