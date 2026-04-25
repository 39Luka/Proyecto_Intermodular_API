package com.bakery.bakeryapi.category;

import com.bakery.bakeryapi.infra.config.PaginationProperties;
import com.bakery.bakeryapi.category.dto.CategoryRequest;
import com.bakery.bakeryapi.category.dto.CategoryResponse;
import com.bakery.bakeryapi.domain.Category;
import com.bakery.bakeryapi.category.exception.CategoryAlreadyExistsException;
import com.bakery.bakeryapi.category.exception.CategoryNotFoundException;
import com.bakery.bakeryapi.shared.PageableUtils;
import com.bakery.bakeryapi.repository.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository repository;
    private final PaginationProperties paginationProperties;

    public CategoryService(CategoryRepository repository, PaginationProperties paginationProperties) {
        this.repository = repository;
        this.paginationProperties = paginationProperties;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        Category category = new Category(request.name());
        return CategoryResponse.from(repository.save(category));
    }

    public Category getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public CategoryResponse getById(Long id) {
        return CategoryResponse.from(getEntityById(id));
    }

    public Page<CategoryResponse> getAll(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable, paginationProperties.maxPageSize());
        return repository.findAll(safePageable)
                .map(CategoryResponse::from);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntityById(id);
        if (!category.getName().equalsIgnoreCase(request.name())
                && repository.existsByNameIgnoreCase(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }
        category.update(request.name());
        return CategoryResponse.from(repository.save(category));
    }
}

