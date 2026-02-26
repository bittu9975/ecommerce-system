package com.ecommerce.notificationservice.controller;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running!");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String userId) {
        log.info("GET /api/notifications/user/{} - Fetching notifications", userId);
        
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        log.info("GET /api/notifications - Fetching all notifications");
        
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

//    @GetMapping("/user/{userId:.+}")  // Add :.+ here
//    public ResponseEntity<List<NotificationDTO>> getNotificationsByUserId(@PathVariable String userId) {
//        log.info("GET /api/notifications/user/{} - Fetching notifications", userId);
//        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(userId);
//        return ResponseEntity.ok(notifications);
//    }
}
