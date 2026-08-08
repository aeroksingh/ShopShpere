package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.product.CategoryRequest;
import com.shopsphere.shopsphere.dto.product.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse getById(Long id);
    List<CategoryResponse> getAll();
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
}
