package org.example.bakeryapi.product;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryService;
import org.example.bakeryapi.auth.exception.ForbiddenOperationException;
import org.example.bakeryapi.common.pagination.PageableUtils;
import org.example.bakeryapi.common.security.SecurityUtils;
import org.example.bakeryapi.product.dto.ProductSalesResponse;
import org.example.bakeryapi.product.dto.ProductRequest;
import org.example.bakeryapi.product.dto.ProductResponse;
import org.example.bakeryapi.product.exception.ProductInactiveException;
import org.example.bakeryapi.product.exception.ProductNotFoundException;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
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

    public ProductService(ProductRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
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
        return ProductResponse.from(repository.save(product));
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
        Authentication auth = SecurityUtils.requireAuthentication();
        Product product = getEntityById(id);
        if (!SecurityUtils.isAdmin(auth) && !product.isActive()) {
            throw new ProductNotFoundException(id);
        }
        return ProductResponse.from(product);
    }

    public Page<ProductResponse> getAll(Pageable pageable, Long categoryId) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.requireAuthentication();
        boolean admin = SecurityUtils.isAdmin(auth);

        if (categoryId == null) {
            return (admin ? repository.findAll(safePageable) : repository.findAllByActiveTrue(safePageable))
                    .map(ProductResponse::from);
        }
        categoryService.getEntityById(categoryId);
        return (admin
                ? repository.findAllByCategoryId(categoryId, safePageable)
                : repository.findAllByCategoryIdAndActiveTrue(categoryId, safePageable))
                .map(ProductResponse::from);
    }

    public Page<ProductSalesResponse> getTopSelling(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        Authentication auth = SecurityUtils.requireAuthentication();
        return SecurityUtils.isAdmin(auth)
                ? repository.findTopSellingByStatus(PurchaseStatus.PAID, safePageable)
                : repository.findTopSellingByStatusAndActiveTrue(PurchaseStatus.PAID, safePageable);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getEntityById(id);
        if (product.isSystem()) {
            throw new ForbiddenOperationException("System products cannot be modified");
        }
        Category category = categoryService.getEntityById(request.categoryId());
        product.update(
                request.name(),
                request.description(),
                request.price(),
                request.stock(),
                category
        );
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public void disable(Long id) {
        Product product = getActiveEntityById(id);
        if (product.isSystem()) {
            throw new ForbiddenOperationException("System products cannot be modified");
        }
        product.disable();
        repository.save(product);
    }

    @Transactional
    public void enable(Long id) {
        Product product = getEntityById(id);
        if (product.isSystem()) {
            throw new ForbiddenOperationException("System products cannot be modified");
        }
        product.enable();
        repository.save(product);
    }
}

