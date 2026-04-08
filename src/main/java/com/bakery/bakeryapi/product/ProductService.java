package com.bakery.bakeryapi.product;

import com.bakery.bakeryapi.category.CategoryService;
import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.domain.Product;
import com.bakery.bakeryapi.domain.PurchaseStatus;
import com.bakery.bakeryapi.shared.PageableUtils;
import com.bakery.bakeryapi.shared.SecurityUtils;
import com.bakery.bakeryapi.product.dto.ProductMapper;
import com.bakery.bakeryapi.product.dto.ProductRequest;
import com.bakery.bakeryapi.product.dto.ProductResponse;
import com.bakery.bakeryapi.product.dto.ProductSalesResponse;
import com.bakery.bakeryapi.product.exception.ProductInactiveException;
import com.bakery.bakeryapi.product.exception.ProductNotFoundException;
import com.bakery.bakeryapi.repository.ProductRepository;
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
    private final PaginationProperties paginationProperties;

    public ProductService(
            ProductRepository repository,
            CategoryService categoryService,
            ProductMapper mapper,
            PaginationProperties paginationProperties
    ) {
        this.repository = repository;
        this.categoryService = categoryService;
        this.mapper = mapper;
        this.paginationProperties = paginationProperties;
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
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        if (categoryId == null) {
            return repository.findAllByActiveTrue(safePageable).map(mapper::toResponse);
        }
        categoryService.getEntityById(categoryId);
        return repository.findAllByCategoryIdAndActiveTrue(categoryId, safePageable).map(mapper::toResponse);
    }

    public Page<ProductResponse> getAll(Pageable pageable, Long categoryId) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
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
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
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
}
