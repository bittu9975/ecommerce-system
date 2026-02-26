package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.event.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.order}")
    private String exchange;

    @Value("${rabbitmq.routing-key.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.payment-request}")
    private String paymentRequestRoutingKey;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent for order ID: {}", event.getOrderId());
        
        rabbitTemplate.convertAndSend(exchange, orderCreatedRoutingKey, event);
        
        log.info("OrderCreatedEvent published successfully");
    }

    public void publishPaymentRequestEvent(PaymentRequestEvent event) {
        log.info("Publishing PaymentRequestEvent for order ID: {}", event.getOrderId());
        
        rabbitTemplate.convertAndSend(exchange, paymentRequestRoutingKey, event);
        
        log.info("PaymentRequestEvent published successfully");
    }
}
