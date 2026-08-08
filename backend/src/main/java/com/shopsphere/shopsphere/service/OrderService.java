package com.shopsphere.shopsphere.service;

import com.shopsphere.shopsphere.dto.order.CheckoutRequest;
import com.shopsphere.shopsphere.dto.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);
    OrderResponse getById(Long orderId);
    Page<OrderResponse> getMyOrders(Pageable pageable);
}
