package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.PaymentStatus;
import com.ecommerce.paymentservice.event.OrderPaidEvent;
import com.ecommerce.paymentservice.event.PaymentRequestEvent;
import com.ecommerce.paymentservice.event.PaymentResultEvent;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RabbitMQProducer rabbitMQProducer;
    private final Random random = new Random();

    @Value("${payment.mock.enabled}")
    private boolean mockEnabled;

    @Value("${payment.mock.success-rate}")
    private int successRate;

    @Async
    @Transactional
    public void processPayment(PaymentRequestEvent event) {
        log.info("Processing payment for order: {}, amount: {}", event.getOrderId(), event.getAmount());
        
        // Check if payment already exists for this order
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment already processed for order: {}", event.getOrderId());
            return;
        }
        
        // Create payment record
        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod("MOCK_PAYMENT")
                .build();
        
        payment = paymentRepository.save(payment);
        log.info("Payment record created with ID: {}", payment.getId());
        
        // Simulate payment processing delay
        try {
            Thread.sleep(2000);  // 2 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mock payment processing
        boolean paymentSuccess = processPaymentMock();
        
        if (paymentSuccess) {
            // Payment successful
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId());
            paymentRepository.save(payment);
            
            log.info("Payment successful for order: {}, transaction ID: {}", 
                    event.getOrderId(), payment.getTransactionId());
            
            // Publish OrderPaidEvent
            OrderPaidEvent orderPaidEvent = OrderPaidEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .transactionId(payment.getTransactionId())
                    .paidAt(LocalDateTime.now())
                    .build();
            
            rabbitMQProducer.publishOrderPaidEvent(orderPaidEvent);

            //publish PaymentResultEvent so notification-service fires
            PaymentResultEvent resultEvent = PaymentResultEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .status(PaymentStatus.SUCCESS)
                    .transactionId(payment.getTransactionId())
                    .build();
            rabbitMQProducer.publishPaymentResultEvent(resultEvent, true);
        } else {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Insufficient funds");
            paymentRepository.save(payment);
            
            log.error("Payment failed for order: {}", event.getOrderId());
//            publish failure event
            PaymentResultEvent resultEvent = PaymentResultEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .status(PaymentStatus.FAILED)
                    .failureReason("Insufficient funds")
                    .build();
            rabbitMQProducer.publishPaymentResultEvent(resultEvent, false);
        }
    }

    private boolean processPaymentMock() {
        if (!mockEnabled) {
            return true;  // If mock disabled, always succeed
        }
        
        // Random success based on success rate (default 90%)
        int randomValue = random.nextInt(100);
        return randomValue < successRate;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElse(null);
    }

    public List<Payment> getPaymentsByUserId(String userId) {
        log.info("Fetching payments for user: {}", userId);
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
