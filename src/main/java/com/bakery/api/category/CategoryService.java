package com.bakery.api.category;

import com.bakery.api.category.dto.CategoryMapper;
import com.bakery.api.category.dto.request.CategoryRequest;
import com.bakery.api.category.dto.response.CategoryResponse;
import com.bakery.api.category.exception.CategoryAlreadyExistsException;
import com.bakery.api.category.exception.CategoryNotFoundException;
import com.bakery.api.common.pagination.PageableUtils;
import com.bakery.api.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES_BY_ID, CacheConfig.CATEGORIES_LIST}, allEntries = true)
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

    @Cacheable(cacheNames = CacheConfig.CATEGORIES_BY_ID, key = "#id")
    public CategoryResponse getById(Long id) {
        return mapper.toResponse(getEntityById(id));
    }

    @Cacheable(
            cacheNames = CacheConfig.CATEGORIES_LIST,
            key = "new org.springframework.cache.interceptor.SimpleKey(" +
                    "T(com.bakery.api.common.pagination.PageableUtils).safe(#pageable).pageNumber," +
                    "T(com.bakery.api.common.pagination.PageableUtils).safe(#pageable).pageSize," +
                    "T(com.bakery.api.common.pagination.PageableUtils).safe(#pageable).sort.toString()" +
                    ")"
    )
    public Page<CategoryResponse> getAll(Pageable pageable) {
        Pageable safePageable = PageableUtils.safe(pageable);
        return repository.findAll(safePageable)
                .map(mapper::toResponse);
    }

    @Transactional
    @CacheEvict(cacheNames = {CacheConfig.CATEGORIES_BY_ID, CacheConfig.CATEGORIES_LIST}, allEntries = true)
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

