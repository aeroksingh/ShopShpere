package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.product.CategoryRequest;
import com.shopsphere.shopsphere.dto.product.CategoryResponse;
import com.shopsphere.shopsphere.entity.Category;
import com.shopsphere.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.CategoryRepository;
import com.shopsphere.shopsphere.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // Lombok generates a constructor for all `final` fields -> clean constructor injection
@Transactional(readOnly = true) // default: most methods just read; writers override below
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.getName()).ifPresent(c -> {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        });

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findEntity(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toResponse(category); // no explicit save() needed -> dirty checking inside @Transactional
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = findEntity(id);
        categoryRepository.delete(category);
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", id));
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
    }
}
