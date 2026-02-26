package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.CartItemDTO;
import com.ecommerce.orderservice.dto.CartResponse;
import com.ecommerce.orderservice.dto.CreateOrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.event.PaymentRequestEvent;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final RabbitMQProducer rabbitMQProducer;

    @Transactional
    public OrderResponse createOrder(String userId, String token, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);
        
        // 1. Get cart from Cart Service
        CartResponse cart = cartService.getCart(token);
        
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new OrderException("Cannot create order from empty cart");
        }
        
        // 2. Create order entity
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalPrice(cart.getTotalPrice())
                .shippingAddress(request.getShippingAddress())
                .build();
        
        // 3. Add order items
        for (CartItemDTO cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .price(cartItem.getPrice())
                    .quantity(cartItem.getQuantity())
                    .imageUrl(cartItem.getImageUrl())
                    .build();
            
            order.addItem(orderItem);
        }
        
        // 4. Save order to database
        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}", savedOrder.getId());
        
        // 5. Publish OrderCreatedEvent to RabbitMQ
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .totalPrice(savedOrder.getTotalPrice())
                .shippingAddress(savedOrder.getShippingAddress())
                .createdAt(savedOrder.getCreatedAt())
                .build();
        
        rabbitMQProducer.publishOrderCreatedEvent(orderCreatedEvent);
        
        // 6. Publish PaymentRequestEvent to RabbitMQ
        PaymentRequestEvent paymentRequestEvent = PaymentRequestEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .amount(savedOrder.getTotalPrice())
                .build();
        
        rabbitMQProducer.publishPaymentRequestEvent(paymentRequestEvent);
        
        // 7. Clear cart
        cartService.clearCart(token);
        
        return convertToResponse(savedOrder);
    }

    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        log.info("Fetching orders for user: {}", userId);
        
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::convertToResponse);
    }

    public OrderResponse getOrderById(String userId, Long orderId) {
        log.info("Fetching order {} for user: {}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        
        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new OrderException("You don't have permission to view this order");
        }
        
        return convertToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("Order not found with ID: " + orderId));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order status updated successfully");
        return convertToResponse(updatedOrder);
    }

    private OrderResponse convertToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .items(order.getItems())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
