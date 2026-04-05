package com.bakery.bakeryapi.catalog.category;

import com.bakery.bakeryapi.catalog.category.dto.CategoryMapper;
import com.bakery.bakeryapi.catalog.category.dto.CategoryRequest;
import com.bakery.bakeryapi.catalog.category.dto.CategoryResponse;
import com.bakery.bakeryapi.catalog.category.exception.CategoryAlreadyExistsException;
import com.bakery.bakeryapi.catalog.category.exception.CategoryNotFoundException;
import com.bakery.bakeryapi.common.pagination.PageableUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }

        Category category = new Category(request.name());
        return mapper.toResponse(repository.save(category));
    }

    public Category getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    public CategoryResponse getById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    public Page<CategoryResponse> getAll(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        return repository.findAll(safePageable)
                .map(mapper::toResponse);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntityById(id);
        if (!category.getName().equalsIgnoreCase(request.name())
                && repository.existsByNameIgnoreCase(request.name())) {
            throw new CategoryAlreadyExistsException(request.name());
        }
        category.update(request.name());
        return mapper.toResponse(repository.save(category));
    }
}

