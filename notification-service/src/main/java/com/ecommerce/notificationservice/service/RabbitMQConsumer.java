package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.event.OrderCreatedEvent;
import com.ecommerce.notificationservice.event.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final EmailService emailService;
    private final SMSService smsService;

    @RabbitListener(queues = "${rabbitmq.queue.order-created}")
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("📦 Received OrderCreatedEvent for order ID: {}", event.getOrderId());

        try {
            // Format price
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedPrice = currencyFormatter.format(event.getTotalPrice());

            // Send email notification
            String emailSubject = "Order Confirmation - Order #" + event.getOrderId();
            String emailMessage = String.format(
                    "Dear Customer,\n\n" +
                    "Your order #%d has been successfully placed!\n\n" +
                    "Order Details:\n" +
                    "- Order ID: %d\n" +
                    "- Total Amount: %s\n" +
                    "- Shipping Address: %s\n" +
                    "- Order Date: %s\n\n" +
                    "Your order is being processed and payment is pending.\n\n" +
                    "Thank you for shopping with us!\n\n" +
                    "Best regards,\n" +
                    "E-Commerce Team",
                    event.getOrderId(),
                    event.getOrderId(),
                    formattedPrice,
                    event.getShippingAddress(),
                    event.getCreatedAt()
            );

            emailService.sendEmail(
                    event.getUserId(),
                    emailSubject,
                    emailMessage,
                    "ORDER_CREATED",
                    event.getOrderId()
            );

            // Send SMS notification
            String smsMessage = String.format(
                    "Order #%d confirmed! Total: %s. Payment pending. Track at ecommerce.com/orders/%d",
                    event.getOrderId(),
                    formattedPrice,
                    event.getOrderId()
            );

            smsService.sendSMS(
                    event.getUserId(),
                    smsMessage,
                    "ORDER_CREATED",
                    event.getOrderId()
            );

            log.info("✅ Notifications sent successfully for order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ Error processing OrderCreatedEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment-success}")
    public void consumePaymentSuccessEvent(PaymentResultEvent event) {
        log.info("💳 Received PaymentSuccessEvent for order ID: {}", event.getOrderId());

        try {
            // Format amount
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedAmount = currencyFormatter.format(event.getAmount());

            // Send email notification
            String emailSubject = "Payment Successful - Order #" + event.getOrderId();
            String emailMessage = String.format(
                    "Dear Customer,\n\n" +
                    "Your payment has been successfully processed!\n\n" +
                    "Payment Details:\n" +
                    "- Order ID: %d\n" +
                    "- Payment ID: %d\n" +
                    "- Amount Paid: %s\n" +
                    "- Transaction ID: %s\n" +
                    "- Status: PAID\n\n" +
                    "Your order is now being prepared for shipment.\n\n" +
                    "Thank you for your payment!\n\n" +
                    "Best regards,\n" +
                    "E-Commerce Team",
                    event.getOrderId(),
                    event.getPaymentId(),
                    formattedAmount,
                    event.getTransactionId()
            );

            emailService.sendEmail(
                    event.getUserId(),
                    emailSubject,
                    emailMessage,
                    "PAYMENT_SUCCESS",
                    event.getOrderId()
            );

            // Send SMS notification
            String smsMessage = String.format(
                    "Payment successful! Order #%d paid: %s. Transaction: %s",
                    event.getOrderId(),
                    formattedAmount,
                    event.getTransactionId()
            );

            smsService.sendSMS(
                    event.getUserId(),
                    smsMessage,
                    "PAYMENT_SUCCESS",
                    event.getOrderId()
            );

            log.info("✅ Payment success notifications sent for order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ Error processing PaymentSuccessEvent: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.payment-failed}")
    public void consumePaymentFailedEvent(PaymentResultEvent event) {
        log.info("❌ Received PaymentFailedEvent for order ID: {}", event.getOrderId());

        try {
            // Format amount
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
            String formattedAmount = currencyFormatter.format(event.getAmount());

            // Send email notification
            String emailSubject = "Payment Failed - Order #" + event.getOrderId();
            String emailMessage = String.format(
                    "Dear Customer,\n\n" +
                    "Unfortunately, your payment could not be processed.\n\n" +
                    "Payment Details:\n" +
                    "- Order ID: %d\n" +
                    "- Payment ID: %d\n" +
                    "- Amount: %s\n" +
                    "- Reason: %s\n\n" +
                    "Please try again or use a different payment method.\n" +
                    "Visit: ecommerce.com/orders/%d to retry payment.\n\n" +
                    "If you need assistance, please contact our support team.\n\n" +
                    "Best regards,\n" +
                    "E-Commerce Team",
                    event.getOrderId(),
                    event.getPaymentId(),
                    formattedAmount,
                    event.getFailureReason(),
                    event.getOrderId()
            );

            emailService.sendEmail(
                    event.getUserId(),
                    emailSubject,
                    emailMessage,
                    "PAYMENT_FAILED",
                    event.getOrderId()
            );

            // Send SMS notification
            String smsMessage = String.format(
                    "Payment failed for Order #%d. Reason: %s. Please retry at ecommerce.com",
                    event.getOrderId(),
                    event.getFailureReason()
            );

            smsService.sendSMS(
                    event.getUserId(),
                    smsMessage,
                    "PAYMENT_FAILED",
                    event.getOrderId()
            );

            log.info("✅ Payment failure notifications sent for order ID: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("❌ Error processing PaymentFailedEvent: {}", e.getMessage(), e);
        }
    }
}
