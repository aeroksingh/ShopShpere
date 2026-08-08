package com.shopsphere.shopsphere.service.impl;

import com.shopsphere.shopsphere.dto.cart.AddToCartRequest;
import com.shopsphere.shopsphere.dto.cart.CartItemResponse;
import com.shopsphere.shopsphere.dto.cart.CartResponse;
import com.shopsphere.shopsphere.entity.Cart;
import com.shopsphere.shopsphere.entity.CartItem;
import com.shopsphere.shopsphere.entity.Product;
import com.shopsphere.shopsphere.entity.User;
import com.shopsphere.shopsphere.exception.BadRequestException;
import com.shopsphere.shopsphere.exception.InsufficientStockException;
import com.shopsphere.shopsphere.exception.ResourceNotFoundException;
import com.shopsphere.shopsphere.repository.CartRepository;
import com.shopsphere.shopsphere.repository.ProductRepository;
import com.shopsphere.shopsphere.service.CartService;
import com.shopsphere.shopsphere.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartResponse getMyCart() {
        return toResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse addItem(AddToCartRequest request) {
        Cart cart = getOrCreateCart();

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.of("Product", request.getProductId()));

        // Check if the product is already in the cart -> bump quantity instead of duplicate row
        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity = (existing != null ? existing.getQuantity() : 0) + request.getQuantity();

        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(
                    "Only " + product.getStockQuantity() + " units of '" + product.getName() + "' are in stock");
        }

        if (existing != null) {
            existing.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1. Use removeItem to delete it instead.");
        }

        Cart cart = getOrCreateCart();
        CartItem item = findItemInCart(cart, cartItemId);

        if (quantity > item.getProduct().getStockQuantity()) {
            throw new InsufficientStockException(
                    "Only " + item.getProduct().getStockQuantity() + " units of '"
                            + item.getProduct().getName() + "' are in stock");
        }

        item.setQuantity(quantity);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long cartItemId) {
        Cart cart = getOrCreateCart();
        CartItem item = findItemInCart(cart, cartItemId);
        cart.removeItem(item);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear(); // orphanRemoval = true on Cart.items -> DB rows deleted too
    }

    private CartItem findItemInCart(Cart cart, Long cartItemId) {
        return cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Cart item", cartItemId));
    }

    private Cart getOrCreateCart() {
        User user = SecurityUtils.getCurrentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(i -> CartItemResponse.builder()
                        .cartItemId(i.getId())
                        .productId(i.getProduct().getId())
                        .productName(i.getProduct().getName())
                        .unitPrice(i.getProduct().getPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build())
                .toList();

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .grandTotal(grandTotal)
                .build();
    }
}
