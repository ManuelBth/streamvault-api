package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.service.NotificationService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @InjectMocks
    private NotificationController notificationController;

    private UUID userId;
    private UUID notificationId;
    private UserResponse testUserResponse;
    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testUserResponse = UserResponse.builder()
                .id(userId)
                .email("test@streamvault.com")
                .name("Test User")
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
        // Given
        List<NotificationResponse> notifications = Arrays.asList(
                testNotificationResponse,
                NotificationResponse.builder()
                        .id(UUID.randomUUID())
                        .type(Notification.NotificationType.NEW_EPISODE)
                        .title("Another Notification")
                        .message("Another Message")
                        .isRead(true)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        when(userService.getCurrentUser(anyString())).thenReturn(Mono.just(testUserResponse));
        when(notificationService.getNotifications(userId)).thenReturn(Flux.fromIterable(notifications));

        // When
        Mono<ResponseEntity<List<NotificationResponse>>> result = notificationController
                .getNotifications("test@streamvault.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getUnreadNotifications - Should return only unread notifications")
    void getUnreadNotifications_Success() {
        // Given
        when(userService.getCurrentUser(anyString())).thenReturn(Mono.just(testUserResponse));
        when(notificationService.getUnreadNotifications(userId))
                .thenReturn(Flux.just(testNotificationResponse));

        // When
        Mono<ResponseEntity<List<NotificationResponse>>> result = notificationController
                .getUnreadNotifications("test@streamvault.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).hasSize(1);
                    assertThat(response.getBody().get(0).getIsRead()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getUnreadCount - Should return count of unread notifications")
    void getUnreadCount_Success() {
        // Given
        when(userService.getCurrentUser(anyString())).thenReturn(Mono.just(testUserResponse));
        when(notificationService.getUnreadCount(userId)).thenReturn(Mono.just(5L));

        // When
        Mono<ResponseEntity<Map<String, Long>>> result = notificationController
                .getUnreadCount("test@streamvault.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).containsEntry("count", 5L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("markAsRead - Should mark notification as read")
    void markAsRead_Success() {
        // Given
        when(userService.getCurrentUser(anyString())).thenReturn(Mono.just(testUserResponse));
        when(notificationService.markAsRead(notificationId, userId)).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Void>> result = notificationController
                .markAsRead("test@streamvault.com", notificationId);

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("markAllAsRead - Should mark all notifications as read")
    void markAllAsRead_Success() {
        // Given
        when(userService.getCurrentUser(anyString())).thenReturn(Mono.just(testUserResponse));
        when(notificationService.markAllAsRead(userId)).thenReturn(Mono.empty());

        // When
        Mono<ResponseEntity<Void>> result = notificationController
                .markAllAsRead("test@streamvault.com");

        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                })
                .verifyComplete();
    }
}
