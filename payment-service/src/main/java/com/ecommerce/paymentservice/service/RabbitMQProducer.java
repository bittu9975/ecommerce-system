package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.event.OrderPaidEvent;
import com.ecommerce.paymentservice.event.PaymentResultEvent;
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

    @Value("${rabbitmq.routing-key.order-paid}")
    private String orderPaidRoutingKey;

    public void publishOrderPaidEvent(OrderPaidEvent event) {
        log.info("Publishing OrderPaidEvent for order ID: {}", event.getOrderId());
        
        rabbitTemplate.convertAndSend(exchange, orderPaidRoutingKey, event);
        
        log.info("OrderPaidEvent published successfully");
    }

    @Value("${rabbitmq.routing-key.payment-success}")
    private String paymentSuccessRoutingKey;

    @Value("${rabbitmq.routing-key.payment-failed}")
    private String paymentFailedRoutingKey;

    public void publishPaymentResultEvent(PaymentResultEvent event, boolean success) {
        String routingKey = success ? paymentSuccessRoutingKey : paymentFailedRoutingKey;
        log.info("Publishing PaymentResultEvent for order ID: {} (success={})", event.getOrderId(), success);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
