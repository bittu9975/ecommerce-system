package com.ecommerce.paymentservice.listener;

import com.ecommerce.paymentservice.event.PaymentRequestEvent;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestListener {

    private final PaymentService paymentService;

    @RabbitListener(queues = "${rabbitmq.queue.payment-request}")
    public void handlePaymentRequest(PaymentRequestEvent event) {
        log.info("Received PaymentRequestEvent for order: {}", event.getOrderId());
        
        try {
            paymentService.processPayment(event);
        } catch (Exception e) {
            log.error("Error processing payment for order {}: {}", event.getOrderId(), e.getMessage(), e);
        }
    }
}
