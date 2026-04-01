package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.BroadcastNotificationResponse;
import com.betha.streamvault.notification.model.BroadcastNotification;
import com.betha.streamvault.notification.model.BroadcastNotificationType;
import com.betha.streamvault.notification.repository.BroadcastNotificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BroadcastNotificationService {

    private final BroadcastNotificationJpaRepository broadcastNotificationJpaRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    @Transactional
    public BroadcastNotificationResponse createBroadcastNotification(BroadcastNotificationType type,
                                                                      String title, String message, UUID relatedId) {
        BroadcastNotification notification = BroadcastNotification.builder()
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build();

        BroadcastNotification saved = broadcastNotificationJpaRepository.save(notification);
        BroadcastNotificationResponse response = toResponse(saved);
        webSocketHandler.broadcastNotification(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<BroadcastNotificationResponse> getAllBroadcastNotifications() {
        return broadcastNotificationJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private BroadcastNotificationResponse toResponse(BroadcastNotification notification) {
        return BroadcastNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedId(notification.getRelatedId())
                .createdAt(notification.getCreatedAt() != null
                        ? notification.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}