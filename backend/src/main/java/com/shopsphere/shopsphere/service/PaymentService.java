package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.order.OrderResponse;

public interface PaymentService {

    OrderResponse processPayment(Long orderId, boolean success);
}