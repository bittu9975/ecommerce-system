package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationType;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SMSService {

    private final NotificationRepository notificationRepository;

    @Value("${notification.sms.enabled:true}")
    private boolean enabled;

    public void sendSMS(String to, String message, String eventType, Long relatedId) {
        if (!enabled) {
            log.info("SMS sending is disabled");
            return;
        }

        log.info("=".repeat(50));
        log.info("📱 SENDING SMS");
        log.info("To: {}", to);
        log.info("Message: {}", message);
        log.info("=".repeat(50));

        // Save notification to database
        Notification notification = Notification.builder()
                .userId(to)
                .type(NotificationType.SMS)
                .recipient(to)
                .subject("SMS Notification")
                .message(message)
                .eventType(eventType)
                .relatedId(relatedId)
                .build();

        notificationRepository.save(notification);
        log.info("✅ SMS notification logged to database");
    }
}
