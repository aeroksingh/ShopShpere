package com.shopsphere.shopsphere.controller;

import com.shopsphere.shopsphere.dto.ApiResponse;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import com.shopsphere.shopsphere.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Payments",
        description = "Mock payment processing for ShopSphere"
)
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    @Operation(
            summary = "Process a mock payment"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> processPayment(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "true") boolean success
    ) {

        OrderResponse response =
                paymentService.processPayment(
                        orderId,
                        success
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        success
                                ? "Payment successful"
                                : "Payment failed",
                        response
                )
        );
    }
}