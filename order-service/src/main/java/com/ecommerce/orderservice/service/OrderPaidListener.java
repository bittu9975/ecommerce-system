// NEW FILE: order-service/.../service/OrderPaidListener.java

package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaidListener {

    private final OrderService orderService;

    @RabbitListener(queues = "${rabbitmq.queue.order-paid}")
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("Received OrderPaidEvent for order ID: {}", event.getOrderId());
        try {
            orderService.updateOrderStatus(event.getOrderId(), OrderStatus.PAID);
            log.info("Order {} status updated to PAID", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to update order {} status: {}", event.getOrderId(), e.getMessage(), e);
        }
    }
}