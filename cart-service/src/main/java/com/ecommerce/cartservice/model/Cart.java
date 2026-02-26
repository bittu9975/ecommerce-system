package com.ecommerce.cartservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)  // Ignore any unknown fields from Redis
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;  // Email from JWT

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Calculated fields - these are computed, not stored
    public Integer getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        return items.size();
    }
}