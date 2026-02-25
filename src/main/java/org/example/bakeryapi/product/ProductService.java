package org.example.bakeryapi.product;

import org.example.bakeryapi.category.Category;
import org.example.bakeryapi.category.CategoryService;
import org.example.bakeryapi.product.dto.ProductSalesResponse;
import org.example.bakeryapi.product.dto.ProductRequest;
import org.example.bakeryapi.product.dto.ProductResponse;
import org.example.bakeryapi.product.exception.ProductInactiveException;
import org.example.bakeryapi.product.exception.ProductNotFoundException;
import org.example.bakeryapi.purchase.domain.PurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        return ProductResponse.from(getEntityById(id));
    }

    public Page<ProductResponse> getAll(Pageable pageable, Long categoryId) {
        Pageable safePageable = createSafePageable(pageable);
        if (categoryId == null) {
            return repository.findAll(safePageable)
                    .map(ProductResponse::from);
        }
        categoryService.getEntityById(categoryId);
        return repository.findAllByCategoryId(categoryId, safePageable)
                .map(ProductResponse::from);
    }

    public Page<ProductSalesResponse> getTopSelling(Pageable pageable) {
        Pageable safePageable = createSafePageable(pageable);
        return repository.findTopSellingByStatus(PurchaseStatus.PAID, safePageable);
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
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public void disable(Long id) {
        Product product = getActiveEntityById(id);
        product.disable();
        repository.save(product);
    }

    @Transactional
    public void enable(Long id) {
        Product product = getEntityById(id);
        product.enable();
        repository.save(product);
    }

    private Pageable createSafePageable(Pageable pageable) {
        return PageRequest.of(
                Math.max(0, pageable.getPageNumber()),
                Math.max(1, pageable.getPageSize()),
                pageable.getSort()
        );
    }
}


