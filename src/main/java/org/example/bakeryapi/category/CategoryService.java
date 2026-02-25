package org.example.bakeryapi.category;

import org.example.bakeryapi.category.dto.CategoryRequest;
import org.example.bakeryapi.category.dto.CategoryResponse;
import org.example.bakeryapi.category.exception.CategoryAlreadyExistsException;
import org.example.bakeryapi.category.exception.CategoryNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
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
        return repository.findAll(pageable)
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


