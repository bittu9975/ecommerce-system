package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by user
    Page<Order> findByUserId(String userId, Pageable pageable);
    
    // Find orders by user and status
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(OrderStatus status);
    
    // Count orders by user
    long countByUserId(String userId);
}
