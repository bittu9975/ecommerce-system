package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running!");
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("GET /api/payments/order/{} - Fetching payment", orderId);
        
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }
        
        PaymentResponse response = convertToResponse(payment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId:.+}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable String userId) {
        log.info("GET /api/payments/user/{} - Fetching payments", userId);

        List<Payment> payments = paymentService.getPaymentsByUserId(userId);

        if (payments.isEmpty()) {
            return ResponseEntity.ok(List.of()); // Return empty list instead of 404
        }

        List<PaymentResponse> responses = payments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private PaymentResponse convertToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
