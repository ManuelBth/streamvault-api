package com.betha.streamvault.admin.controller;

import com.betha.streamvault.admin.dto.SendBroadcastRequest;
import com.betha.streamvault.admin.dto.SendNotificationRequest;
import com.betha.streamvault.notification.dto.BroadcastNotificationResponse;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.BroadcastNotificationType;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.service.BroadcastNotificationService;
import com.betha.streamvault.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminNotificationController Tests")
class AdminNotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private BroadcastNotificationService broadcastNotificationService;

    private AdminNotificationController adminNotificationController;

    private NotificationResponse testNotificationResponse;
    private BroadcastNotificationResponse testBroadcastResponse;

    @BeforeEach
    void setUp() {
        adminNotificationController = new AdminNotificationController(
                notificationService,
                broadcastNotificationService
        );

        testNotificationResponse = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .type(Notification.NotificationType.SYSTEM)
                .title("Test Notification")
                .message("Test message content")
                .relatedId(UUID.randomUUID())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        testBroadcastResponse = BroadcastNotificationResponse.builder()
                .id(UUID.randomUUID())
                .type(BroadcastNotificationType.SYSTEM)
                .title("System Update")
                .message("Important system update message")
                .relatedId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/admin/notifications - Should create notification for specific user")
    void sendNotificationToUser_Success() {
        UUID userId = UUID.randomUUID();
        Notification.NotificationType type = Notification.NotificationType.SYSTEM;
        String title = "Test Notification";
        String message = "Test message content";
        UUID relatedId = UUID.randomUUID();

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build();

        when(notificationService.createNotification(
                any(UUID.class),
                any(Notification.NotificationType.class),
                any(String.class),
                any(String.class),
                any(UUID.class)
        )).thenReturn(testNotificationResponse);

        ResponseEntity<NotificationResponse> result = adminNotificationController.sendNotification(
                "admin@streamvault.com",
                request
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(testNotificationResponse.getId());
        assertThat(result.getBody().getTitle()).isEqualTo(title);
        assertThat(result.getBody().getMessage()).isEqualTo(message);

        verify(notificationService).createNotification(userId, type, title, message, relatedId);
    }

    @Test
    @DisplayName("POST /api/v1/admin/notifications/broadcast - Should create broadcast notification")
    void sendBroadcast_Success() {
        BroadcastNotificationType type = BroadcastNotificationType.SYSTEM;
        String title = "System Update";
        String message = "Important system update message";
        UUID relatedId = UUID.randomUUID();

        SendBroadcastRequest request = SendBroadcastRequest.builder()
                .type(type)
                .title(title)
                .message(message)
                .relatedId(relatedId)
                .build();

        when(broadcastNotificationService.createBroadcastNotification(
                any(BroadcastNotificationType.class),
                any(String.class),
                any(String.class),
                any(UUID.class)
        )).thenReturn(testBroadcastResponse);

        ResponseEntity<BroadcastNotificationResponse> result = adminNotificationController.sendBroadcast(
                "admin@streamvault.com",
                request
        );

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(testBroadcastResponse.getId());
        assertThat(result.getBody().getTitle()).isEqualTo(title);
        assertThat(result.getBody().getMessage()).isEqualTo(message);
        assertThat(result.getBody().getType()).isEqualTo(type);

        verify(broadcastNotificationService).createBroadcastNotification(type, title, message, relatedId);
    }
}