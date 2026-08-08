package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.ApiResponse;
import com.shopsphere.shopsphere.dto.order.CheckoutRequest;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import com.shopsphere.shopsphere.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Orders", description = "Checkout and order history")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Convert my current cart into an order")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        OrderResponse order = orderService.checkout(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Order placed", order));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by id (owner or admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getById(id)));
    }

    @GetMapping
    @Operation(summary = "List my past orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(pageable)));
    }
}
