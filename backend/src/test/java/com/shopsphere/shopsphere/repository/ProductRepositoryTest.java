package com.shopsphere.shopsphere.repository;

import com.shopsphere.shopsphere.entity.Category;
import com.shopsphere.shopsphere.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @DataJpaTest loads ONLY JPA-related components (repositories, entities, the
 * H2 in-memory DB from src/test/resources/application.yml) -- no web layer,
 * no security, no service beans. Each test method runs in its own transaction
 * that's rolled back afterward, so tests never see each other's data.
 *
 * This is the layer where you actually verify your custom query methods
 * (findByCategoryId, findByNameContainingIgnoreCase) work as intended --
 * Mockito tests can't catch a typo in a derived query method name, but a real
 * database hitting a real query will.
 */
@DataJpaTest
@DisplayName("ProductRepository")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = categoryRepository.save(Category.builder().name("Electronics").build());

        productRepository.save(Product.builder()
                .name("Wireless Mouse").price(new BigDecimal("19.99")).stockQuantity(50).category(electronics).build());
        productRepository.save(Product.builder()
                .name("Mechanical Keyboard").price(new BigDecimal("89.99")).stockQuantity(20).category(electronics).build());
        productRepository.save(Product.builder()
                .name("USB-C Cable").price(new BigDecimal("9.99")).stockQuantity(100).category(electronics).build());
    }

    @Test
    @DisplayName("findByCategoryId() returns only products in that category")
    void findByCategoryId_returnsMatchingProducts() {
        Page<Product> result = productRepository.findByCategoryId(electronics.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Wireless Mouse", "Mechanical Keyboard", "USB-C Cable");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase() matches regardless of case")
    void findByNameContainingIgnoreCase_isCaseInsensitive() {
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("MOUSE", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Wireless Mouse");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase() returns an empty page when nothing matches")
    void findByNameContainingIgnoreCase_noMatch_returnsEmptyPage() {
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("nonexistent", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}
