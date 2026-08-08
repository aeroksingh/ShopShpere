package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.product.ProductRequest;
import com.shopsphere.shopsphere.dto.product.ProductResponse;
import com.shopsphere.shopsphere.entity.Category;
import com.shopsphere.shopsphere.entity.Product;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.CategoryRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(10L).name("Electronics").build();
        product = Product.builder()
                .id(1L)
                .name("Wireless Mouse")
                .price(new BigDecimal("19.99"))
                .stockQuantity(50)
                .category(category)
                .build();
    }

    @Test
    @DisplayName("create() links the product to its category and saves it")
    void create_savesProductWithCategory() {
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("19.99"))
                .stockQuantity(50)
                .categoryId(10L)
                .build();

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.create(request);

        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getCategoryName()).isEqualTo("Electronics");
        assertThat(response.getPrice()).isEqualByComparingTo("19.99");
    }

    @Test
    @DisplayName("create() throws ResourceNotFoundException when categoryId doesn't exist")
    void create_throwsWhenCategoryMissing() {
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse")
                .price(new BigDecimal("19.99"))
                .stockQuantity(50)
                .categoryId(999L)
                .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById() returns the mapped product when found")
    void getById_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1L);

        assertThat(response.getName()).isEqualTo("Wireless Mouse");
        assertThat(response.getStockQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("update() swaps the category only when categoryId actually changes")
    void update_changesCategoryOnlyWhenDifferent() {
        Category newCategory = Category.builder().id(20L).name("Accessories").build();
        ProductRequest request = ProductRequest.builder()
                .name("Wireless Mouse v2")
                .price(new BigDecimal("24.99"))
                .stockQuantity(40)
                .categoryId(20L) // different from current (10L)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(20L)).thenReturn(Optional.of(newCategory));

        ProductResponse response = productService.update(1L, request);

        assertThat(response.getCategoryId()).isEqualTo(20L);
        assertThat(response.getName()).isEqualTo("Wireless Mouse v2");
        assertThat(response.getPrice()).isEqualByComparingTo("24.99");
    }
}
