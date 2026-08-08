package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.order.CheckoutRequest;
import com.shopsphere.shopsphere.dto.order.OrderItemResponse;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import com.shopsphere.shopsphere.entity.*;
import com.shopsphere.shopsphere.exception.BadRequestException;
import com.shopsphere.shopsphere.exception.InsufficientStockException;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.CartRepository;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.service.OrderService;
import com.shopsphere.shopsphere.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional // write operation: everything below must succeed or nothing does
    public OrderResponse checkout(CheckoutRequest request) {
        User user = SecurityUtils.getCurrentUser();

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Re-validate stock at checkout time — it may have changed since items were added to cart
        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Product", cartItem.getProduct().getId()));

            if (cartItem.getQuantity() > product.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Only " + product.getStockQuantity() + " units of '" + product.getName() + "' are in stock");
            }

            // Decrement stock now that we know it's available
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice()) // snapshot the price NOW
                    .build();
            order.addItem(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        cart.getItems().clear(); // empty the cart after a successful order

        return toResponse(saved);
    }

    @Override
    public OrderResponse getById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));

        User current = SecurityUtils.getCurrentUser();
        boolean isOwner = order.getUser().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Not your order");
        }

        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(i -> OrderItemResponse.builder()
                                .productId(i.getProduct().getId())
                                .productName(i.getProduct().getName())
                                .quantity(i.getQuantity())
                                .priceAtPurchase(i.getPriceAtPurchase())
                                .subtotal(i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                                .build())
                        .toList())
                .build();
    }
}
