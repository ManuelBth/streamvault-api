package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.repository.NotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID userId) {
        return notificationJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationJpaRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationJpaRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public NotificationResponse createNotification(UUID userId, Notification.NotificationType type,
                                                         String title, String message, UUID relatedId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationJpaRepository.save(notification);
        NotificationResponse response = toResponse(saved);
        webSocketHandler.sendNotificationToUser(userId.toString(), response);
        return response;
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        notificationJpaRepository.findById(notificationId).ifPresent(notification -> {
            if (!notification.getUserId().equals(userId)) {
                throw new IllegalArgumentException("Notificación no autorizada");
            }
            notification.setIsRead(true);
            notificationJpaRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationJpaRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notificationJpaRepository.save(notification);
                });
    }

    @Transactional
    public NotificationResponse createBroadcastNotification(Notification.NotificationType type,
                                                          String title, String message, UUID relatedId) {
        Notification notification = Notification.builder()
                .userId(null)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationJpaRepository.save(notification);
        NotificationResponse response = toResponse(saved);
        webSocketHandler.broadcastNotification(response);
        return response;
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt() != null
                        ? notification.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}