package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.repository.NotificationJpaRepository;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationJpaRepository notificationJpaRepository;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @Mock
    private UserJpaRepository userJpaRepository;

    private NotificationService notificationService;

    private UUID userId;
    private UUID notificationId;
    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationJpaRepository, webSocketHandler, userJpaRepository);

        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .email("test@streamvault.com")
                .name("Test User")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();

        testNotification = Notification.builder()
                .id(notificationId)
                .user(testUser)
                .type(Notification.NotificationType.NEW_CONTENT)
                .title("Test Title")
                .message("Test Message")
                .relatedId(UUID.randomUUID())
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("getNotifications - Should return all notifications for user")
    void getNotifications_Success() {
        when(notificationJpaRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getNotifications(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(notificationId);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Title");
    }

    @Test
    @DisplayName("getUnreadNotifications - Should return only unread notifications")
    void getUnreadNotifications_Success() {
        when(notificationJpaRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> responses = notificationService.getUnreadNotifications(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getIsRead()).isFalse();
    }

    @Test
    @DisplayName("getUnreadCount - Should return count of unread notifications")
    void getUnreadCount_Success() {
        when(notificationJpaRepository.countByUserAndIsReadFalse(testUser))
                .thenReturn(5L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("createNotification - Should create and send notification via WebSocket")
    void createNotification_Success() {
        when(notificationJpaRepository.save(any(Notification.class)))
                .thenReturn(testNotification);
        doNothing().when(webSocketHandler).sendNotificationToUser(anyString(), any());

        NotificationResponse response = notificationService.createNotification(
                userId,
                Notification.NotificationType.NEW_CONTENT,
                "Test Title",
                "Test Message",
                UUID.randomUUID()
        );

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Title");

        verify(webSocketHandler).sendNotificationToUser(eq(userId.toString()), any());
    }

    @Test
    @DisplayName("markAsRead - Should mark notification as read")
    void markAsRead_Success() {
        when(notificationJpaRepository.findById(notificationId))
                .thenReturn(java.util.Optional.of(testNotification));
        when(notificationJpaRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        notificationService.markAsRead(notificationId, userId);

        verify(notificationJpaRepository).save(argThat(n -> n.getIsRead() == true));
    }

    @Test
    @DisplayName("createBroadcastNotification - Should create and broadcast notification")
    void createBroadcastNotification_Success() {
        Notification broadcastNotification = Notification.builder()
                .id(UUID.randomUUID())
                .user(null)
                .type(Notification.NotificationType.SYSTEM)
                .title("System Message")
                .message("Important announcement")
                .relatedId(null)
                .isRead(false)
                .build();

        when(notificationJpaRepository.save(any(Notification.class)))
                .thenReturn(broadcastNotification);
        doNothing().when(webSocketHandler).broadcastNotification(any());

        NotificationResponse response = notificationService.createBroadcastNotification(
                Notification.NotificationType.SYSTEM,
                "System Message",
                "Important announcement",
                null
        );

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("System Message");

        verify(webSocketHandler).broadcastNotification(any());
    }
}
