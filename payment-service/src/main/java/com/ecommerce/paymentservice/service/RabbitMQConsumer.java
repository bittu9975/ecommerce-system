package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.event.PaymentRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${rabbitmq.queue.payment-request}")
    public void consumePaymentRequestEvent(PaymentRequestEvent event) {
        log.info("Received PaymentRequestEvent from RabbitMQ for order ID: {}", event.getOrderId());
        
        try {
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Error processing payment for order ID: {} - Error: {}", 
                    event.getOrderId(), e.getMessage(), e);
            // In production, you might want to send to a dead letter queue
        }
    }
}
