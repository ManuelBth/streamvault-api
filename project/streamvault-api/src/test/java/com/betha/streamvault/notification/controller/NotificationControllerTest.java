package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.service.NotificationService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    private NotificationController notificationController;

    private UUID userId;
    private UUID notificationId;
    private UserResponse testUserResponse;
    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        notificationController = new NotificationController(notificationService, userService);
        
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testUserResponse = UserResponse.builder()
                .id(userId)
                .email("test@streamvault.com")
                .name("Test User")
                .role("ROLE_USER")
                .build();

        testNotificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .type(Notification.NotificationType.NEW_CONTENT)
                .title("Test Notification")
                .message("Test Message")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getNotifications - Should return all notifications for authenticated user")
    void getNotifications_Success() {
        List<NotificationResponse> notifications = List.of(testNotificationResponse);

        when(userService.getCurrentUser(anyString())).thenReturn(testUserResponse);
        when(notificationService.getNotifications(userId)).thenReturn(notifications);

        ResponseEntity<List<NotificationResponse>> response = notificationController
                .getNotifications("test@streamvault.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("getUnreadNotifications - Should return only unread notifications")
    void getUnreadNotifications_Success() {
        when(userService.getCurrentUser(anyString())).thenReturn(testUserResponse);
        when(notificationService.getUnreadNotifications(userId))
                .thenReturn(List.of(testNotificationResponse));

        ResponseEntity<List<NotificationResponse>> response = notificationController
                .getUnreadNotifications("test@streamvault.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getIsRead()).isFalse();
    }

    @Test
    @DisplayName("getUnreadCount - Should return count of unread notifications")
    void getUnreadCount_Success() {
        when(userService.getCurrentUser(anyString())).thenReturn(testUserResponse);
        when(notificationService.getUnreadCount(userId)).thenReturn(5L);

        ResponseEntity<Map<String, Long>> response = notificationController
                .getUnreadCount("test@streamvault.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("count", 5L);
    }

    @Test
    @DisplayName("markAsRead - Should mark notification as read")
    void markAsRead_Success() {
        when(userService.getCurrentUser(anyString())).thenReturn(testUserResponse);

        ResponseEntity<Void> response = notificationController
                .markAsRead("test@streamvault.com", notificationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("markAllAsRead - Should mark all notifications as read")
    void markAllAsRead_Success() {
        when(userService.getCurrentUser(anyString())).thenReturn(testUserResponse);

        ResponseEntity<Void> response = notificationController
                .markAllAsRead("test@streamvault.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
