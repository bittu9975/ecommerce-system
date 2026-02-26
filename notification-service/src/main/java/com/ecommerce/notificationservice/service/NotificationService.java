package com.ecommerce.notificationservice.service;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getUserNotifications(String userId) {
        log.info("Fetching notifications for user: {}", userId);
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public List<Notification> getAllNotifications() {
        log.info("Fetching all notifications");
        return notificationRepository.findAll();
    }
}
