package com.ecommerce.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;
}
