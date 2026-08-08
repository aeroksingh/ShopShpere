package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.ApiResponse;
import com.shopsphere.shopsphere.dto.cart.AddToCartRequest;
import com.shopsphere.shopsphere.dto.cart.CartResponse;
import com.shopsphere.shopsphere.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cart", description = "Shopping cart for the logged-in user")
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get my cart")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        return ResponseEntity.ok(ApiResponse.success(cartService.getMyCart()));
    }

    @PostMapping("/items")
    @Operation(summary = "Add a product to my cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cartService.addItem(request)));
    }

    @PatchMapping("/items/{cartItemId}")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestParam @Min(1) int quantity) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateItemQuantity(cartItemId, quantity)));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove an item from my cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed", cartService.removeItem(cartItemId)));
    }

    @DeleteMapping
    @Operation(summary = "Clear my entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
