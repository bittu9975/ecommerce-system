package com.ecommerce.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private String userId;
    private List<CartItemDTO> items;
    private Integer totalItems;
    private Integer itemCount;
    private BigDecimal totalPrice;
}
