package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationService(NotificationRepository notificationRepository,
                              NotificationWebSocketHandler webSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public Flux<NotificationResponse> getNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .map(this::toResponse);
    }

    public Flux<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .map(this::toResponse);
    }

    public Mono<Long> getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public Mono<NotificationResponse> createNotification(UUID userId, Notification.NotificationType type,
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

        return notificationRepository.save(notification)
                .map(this::toResponse)
                .doOnSuccess(response -> webSocketHandler.sendNotificationToUser(userId.toString(), response));
    }

    public Mono<Void> markAsRead(UUID notificationId, UUID userId) {
        return notificationRepository.findById(notificationId)
                .flatMap(notification -> {
                    if (!notification.getUserId().equals(userId)) {
                        return Mono.error(new IllegalArgumentException("Notificación no autorizada"));
                    }
                    notification.setIsRead(true);
                    return notificationRepository.save(notification);
                })
                .then();
    }

    public Mono<Void> markAllAsRead(UUID userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    public Mono<NotificationResponse> createBroadcastNotification(Notification.NotificationType type,
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

        return notificationRepository.save(notification)
                .map(this::toResponse)
                .doOnSuccess(response -> webSocketHandler.broadcastNotification(response));
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
