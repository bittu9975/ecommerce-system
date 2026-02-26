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
public class EmailService {

    private final NotificationRepository notificationRepository;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean enabled;

    public void sendEmail(String to, String subject, String message, String eventType, Long relatedId) {
        if (!enabled) {
            log.info("Email sending is disabled");
            return;
        }

        log.info("=".repeat(50));
        log.info("📧 SENDING EMAIL");
        log.info("From: {}", fromEmail);
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Message: {}", message);
        log.info("=".repeat(50));

        // Save notification to database
        Notification notification = Notification.builder()
                .userId(to)
                .type(NotificationType.EMAIL)
                .recipient(to)
                .subject(subject)
                .message(message)
                .eventType(eventType)
                .relatedId(relatedId)
                .build();

        notificationRepository.save(notification);
        log.info("✅ Email notification logged to database");
    }
}
