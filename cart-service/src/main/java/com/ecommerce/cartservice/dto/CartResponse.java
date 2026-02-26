package com.ecommerce.cartservice.dto;

import com.ecommerce.cartservice.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private String userId;
    
    private List<CartItem> items;
    
    private Integer totalItems;
    
    private Integer itemCount;
    
    private BigDecimal totalPrice;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
