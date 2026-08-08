package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.cart.AddToCartRequest;
import com.shopsphere.shopsphere.dto.cart.CartResponse;

public interface CartService {
    CartResponse getMyCart();
    CartResponse addItem(AddToCartRequest request);
    CartResponse updateItemQuantity(Long cartItemId, int quantity);
    CartResponse removeItem(Long cartItemId);
    void clearCart();
}
