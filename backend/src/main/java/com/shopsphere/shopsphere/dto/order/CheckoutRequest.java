package com.shopsphere.shopsphere.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 300, message = "Shipping address must be under 300 characters")
    private String shippingAddress;
}
