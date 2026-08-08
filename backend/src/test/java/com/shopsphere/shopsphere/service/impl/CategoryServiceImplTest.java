package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.product.CategoryRequest;
import com.shopsphere.shopsphere.dto.product.CategoryResponse;
import com.shopsphere.shopsphere.entity.Category;
import com.shopsphere.shopsphere.exception.DuplicateResourceException;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @ExtendWith(MockitoExtension.class) boots Mockito WITHOUT starting the whole
 * Spring context -> these tests run in milliseconds. This is what "unit test"
 * means: test CategoryServiceImpl's logic in complete isolation, with the
 * repository replaced by a mock we fully control.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(1L).name("Electronics").description("Gadgets").build();
    }

    @Test
    @DisplayName("create() saves and returns a new category when the name is unique")
    void create_savesNewCategory() {
        // given
        CategoryRequest request = CategoryRequest.builder().name("Electronics").description("Gadgets").build();
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // when
        CategoryResponse response = categoryService.create(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("create() throws DuplicateResourceException when the name already exists")
    void create_throwsOnDuplicateName() {
        CategoryRequest request = CategoryRequest.builder().name("Electronics").build();
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        // Critical assertion: on the duplicate path, save() must NEVER be called
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById() throws ResourceNotFoundException when the id doesn't exist")
    void getById_throwsWhenNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getAll() maps every entity to a response DTO")
    void getAll_returnsAllMapped() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("delete() removes the category when it exists")
    void delete_removesExistingCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        categoryService.delete(1L);

        verify(categoryRepository).delete(category);
    }
}
