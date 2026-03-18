package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;
    private Notification testNotification;
    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testNotification = new Notification(
                notificationId,
                userId,
                Notification.NotificationType.NEW_CONTENT,
                "Test Title",
                "Test Message",
                UUID.randomUUID(),
                false,
                Instant.now()
        );

        testNotificationResponse = NotificationResponse.builder()
                .id(notificationId)
                .type(Notification.NotificationType.NEW_CONTENT)
                .title("Test Title")
                .message("Test Message")
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("getNotifications - Should return all notifications for user")
    void getNotifications_Success() {
        // Given
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(Flux.just(testNotification));

        // When & Then
        StepVerifier.create(notificationService.getNotifications(userId))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(notificationId);
                    assertThat(response.getTitle()).isEqualTo("Test Title");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getUnreadNotifications - Should return only unread notifications")
    void getUnreadNotifications_Success() {
        // Given
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(Flux.just(testNotification));

        // When & Then
        StepVerifier.create(notificationService.getUnreadNotifications(userId))
                .assertNext(response -> {
                    assertThat(response.getIsRead()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getUnreadCount - Should return count of unread notifications")
    void getUnreadCount_Success() {
        // Given
        when(notificationRepository.countByUserIdAndIsReadFalse(userId))
                .thenReturn(Mono.just(5L));

        // When & Then
        StepVerifier.create(notificationService.getUnreadCount(userId))
                .assertNext(count -> assertThat(count).isEqualTo(5L))
                .verifyComplete();
    }

    @Test
    @DisplayName("createNotification - Should create and send notification via WebSocket")
    void createNotification_Success() {
        // Given
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Mono.just(testNotification));
        doNothing().when(webSocketHandler).sendNotificationToUser(anyString(), any());

        // When & Then
        StepVerifier.create(notificationService.createNotification(
                userId,
                Notification.NotificationType.NEW_CONTENT,
                "Test Title",
                "Test Message",
                UUID.randomUUID()
        ))
                .assertNext(response -> {
                    assertThat(response.getTitle()).isEqualTo("Test Title");
                })
                .verifyComplete();

        verify(webSocketHandler).sendNotificationToUser(eq(userId.toString()), any());
    }

    @Test
    @DisplayName("markAsRead - Should mark notification as read")
    void markAsRead_Success() {
        // Given
        when(notificationRepository.findById(notificationId))
                .thenReturn(Mono.just(testNotification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Mono.just(testNotification));

        // When & Then
        StepVerifier.create(notificationService.markAsRead(notificationId, userId))
                .verifyComplete();

        verify(notificationRepository).save(argThat(notification ->
                notification.getIsRead() == true));
    }

    @Test
    @DisplayName("markAsRead - Should fail when user tries to mark another user's notification")
    void markAsRead_Unauthorized() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId))
                .thenReturn(Mono.just(testNotification));

        // When & Then
        StepVerifier.create(notificationService.markAsRead(notificationId, differentUserId))
                .expectErrorMatches(throwable ->
                        throwable.getMessage().equals("Notificación no autorizada"))
                .verify();
    }

    @Test
    @DisplayName("markAllAsRead - Should mark all notifications as read")
    void markAllAsRead_Success() {
        // Given
        Notification notification1 = new Notification(
                UUID.randomUUID(), userId, Notification.NotificationType.NEW_CONTENT,
                "Title 1", "Message 1", null, false, Instant.now()
        );
        Notification notification2 = new Notification(
                UUID.randomUUID(), userId, Notification.NotificationType.NEW_EPISODE,
                "Title 2", "Message 2", null, false, Instant.now()
        );

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId))
                .thenReturn(Flux.just(notification1, notification2));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When & Then
        StepVerifier.create(notificationService.markAllAsRead(userId))
                .verifyComplete();

        verify(notificationRepository, times(2)).save(argThat(n -> n.getIsRead() == true));
    }

    @Test
    @DisplayName("createBroadcastNotification - Should create and broadcast notification")
    void createBroadcastNotification_Success() {
        // Given
        Notification broadcastNotification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(null)
                .type(Notification.NotificationType.SYSTEM)
                .title("System Message")
                .message("Important announcement")
                .relatedId(null)
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(Mono.just(broadcastNotification));
        doNothing().when(webSocketHandler).broadcastNotification(any());

        // When & Then
        StepVerifier.create(notificationService.createBroadcastNotification(
                Notification.NotificationType.SYSTEM,
                "System Message",
                "Important announcement",
                null
        ))
                .assertNext(response -> {
                    assertThat(response.getTitle()).isEqualTo("System Message");
                })
                .verifyComplete();

        verify(webSocketHandler).broadcastNotification(any());
    }
}
