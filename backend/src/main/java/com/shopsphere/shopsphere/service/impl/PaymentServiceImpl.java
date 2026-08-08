package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.order.OrderItemResponse;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import com.shopsphere.shopsphere.entity.*;
import com.shopsphere.shopsphere.exception.BadRequestException;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.OrderRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.service.PaymentService;
import com.shopsphere.shopsphere.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderResponse processPayment(Long orderId, boolean success) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        ResourceNotFoundException.of("Order", orderId));

        User currentUser = SecurityUtils.getCurrentUser();

        boolean isOwner =
                order.getUser().getId().equals(currentUser.getId());

        boolean isAdmin =
                currentUser.getRole() == Role.ROLE_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot pay for this order"
            );
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException(
                    "Payment has already been processed"
            );
        }

        if (!success) {

            order.setPaymentStatus(PaymentStatus.FAILED);

            return toResponse(order);
        }

        /*
         * Payment succeeded.
         *
         * Now reduce stock.
         */
        for (OrderItem item : order.getItems()) {

            Product product = productRepository
                    .findById(item.getProduct().getId())
                    .orElseThrow(() ->
                            ResourceNotFoundException.of(
                                    "Product",
                                    item.getProduct().getId()
                            ));

            if (item.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException(
                        "Product '" +
                        product.getName() +
                        "' is no longer available in the requested quantity"
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity()
                            - item.getQuantity()
            );
        }

        order.setPaymentStatus(
                PaymentStatus.SUCCESS
        );

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(
                        order.getItems()
                                .stream()
                                .map(item ->
                                        OrderItemResponse.builder()
                                                .productId(
                                                        item.getProduct().getId()
                                                )
                                                .productName(
                                                        item.getProduct().getName()
                                                )
                                                .quantity(
                                                        item.getQuantity()
                                                )
                                                .priceAtPurchase(
                                                        item.getPriceAtPurchase()
                                                )
                                                .subtotal(
                                                        item.getPriceAtPurchase()
                                                                .multiply(
                                                                        BigDecimal.valueOf(
                                                                                item.getQuantity()
                                                                        )
                                                                )
                                                )
                                                .build()
                                )
                                .toList()
                )
                .build();
    }
}