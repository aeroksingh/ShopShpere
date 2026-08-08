package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.product.ProductRequest;
import com.shopsphere.shopsphere.dto.product.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    Page<ProductResponse> getAll(Pageable pageable);
    Page<ProductResponse> search(String keyword, Pageable pageable);
    Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
