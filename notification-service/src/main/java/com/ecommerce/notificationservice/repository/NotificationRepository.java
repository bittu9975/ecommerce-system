package com.ecommerce.notificationservice.repository;

import com.ecommerce.notificationservice.entity.Notification;
import com.ecommerce.notificationservice.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserId(String userId);
    
    List<Notification> findByType(NotificationType type);
    
    List<Notification> findByUserIdOrderBySentAtDesc(String userId);
}
