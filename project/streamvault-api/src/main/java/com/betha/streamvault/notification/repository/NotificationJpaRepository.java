package com.betha.streamvault.notification.repository;

import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    long countByUserAndIsReadFalse(User user);
}
