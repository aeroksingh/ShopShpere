package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.order.CheckoutRequest;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import com.shopsphere.shopsphere.entity.*;
import com.shopsphere.shopsphere.exception.BadRequestException;
import com.shopsphere.shopsphere.exception.InsufficientStockException;
import com.shopsphere.shopsphere.repository.CartRepository;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.util.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl checkout()")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Cart cart;

    // SecurityUtils.getCurrentUser() reads from Spring Security's SecurityContextHolder,
    // which doesn't exist in a plain Mockito unit test. mockStatic lets us stub the static
    // method itself instead of standing up a whole security context just for this test.
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("jane@example.com").role(Role.ROLE_CUSTOMER).build();
        product = Product.builder().id(100L).name("Mechanical Keyboard").price(new BigDecimal("89.99")).stockQuantity(5).build();

        cart = Cart.builder().id(1L).user(user).build();
        cart.addItem(CartItem.builder().id(1L).product(product).quantity(2).build());

        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close(); // always close a MockedStatic, or it leaks into other tests
    }

    @Test
    @DisplayName("checkout() decrements stock, snapshots price, and empties the cart")
    void checkout_happyPath() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(500L);
            return o;
        });

        CheckoutRequest request = CheckoutRequest.builder().shippingAddress("123 Main St").build();
        OrderResponse response = orderService.checkout(request);

        assertThat(response.getOrderId()).isEqualTo(500L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("179.98"); // 89.99 * 2
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getPriceAtPurchase()).isEqualByComparingTo("89.99");

        // Stock must be decremented: 5 - 2 = 3
        assertThat(product.getStockQuantity()).isEqualTo(3);

        // Cart must be emptied after a successful checkout
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("checkout() throws InsufficientStockException when requested quantity exceeds current stock")
    void checkout_throwsWhenStockInsufficient() {
        product.setStockQuantity(1); // less than the 2 units in the cart -> should fail

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        CheckoutRequest request = CheckoutRequest.builder().shippingAddress("123 Main St").build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(InsufficientStockException.class);

        // Nothing should be persisted if stock validation fails
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkout() rejects an empty cart before touching the repository")
    void checkout_rejectsEmptyCart() {
        Cart emptyCart = Cart.builder().id(2L).user(user).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(emptyCart));

        CheckoutRequest request = CheckoutRequest.builder().shippingAddress("123 Main St").build();

        assertThatThrownBy(() -> orderService.checkout(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("empty cart");

        verify(orderRepository, never()).save(any());
    }
}
