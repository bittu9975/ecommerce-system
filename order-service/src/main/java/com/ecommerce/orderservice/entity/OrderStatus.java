package com.ecommerce.orderservice.entity;

public enum OrderStatus {
    PENDING,        // Order created, waiting for payment
    PAID,           // Payment successful
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order shipped
    DELIVERED,      // Order delivered
    CANCELLED       // Order cancelled
}
