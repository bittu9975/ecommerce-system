package com.ecommerce.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long productId;
    
    private String productName;
    
    private BigDecimal price;
    
    private Integer quantity;
    
    private String imageUrl;

    private BigDecimal subtotal;
    
    // Calculated field
    public BigDecimal getSubtotal() {
        if (subtotal==null&&price != null && quantity != null) {
            subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotal;
    }

    // Update subtotal when quantity changes
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        if (price != null && quantity != null) {
            this.subtotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
