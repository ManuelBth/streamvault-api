package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.config.NotificationWebSocketHandler;
import com.betha.streamvault.notification.dto.BroadcastNotificationResponse;
import com.betha.streamvault.notification.model.BroadcastNotification;
import com.betha.streamvault.notification.model.BroadcastNotificationType;
import com.betha.streamvault.notification.repository.BroadcastNotificationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BroadcastNotificationService Tests")
class BroadcastNotificationServiceTest {

    @Mock
    private BroadcastNotificationJpaRepository broadcastNotificationJpaRepository;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    private BroadcastNotificationService broadcastNotificationService;

    private UUID notificationId;
    private UUID relatedId;
    private BroadcastNotification testBroadcastNotification;

    @BeforeEach
    void setUp() {
        broadcastNotificationService = new BroadcastNotificationService(broadcastNotificationJpaRepository, webSocketHandler);

        notificationId = UUID.randomUUID();
        relatedId = UUID.randomUUID();

        testBroadcastNotification = BroadcastNotification.builder()
                .id(notificationId)
                .type(BroadcastNotificationType.SYSTEM)
                .title("Test Broadcast")
                .message("Test Broadcast Message")
                .relatedId(relatedId)
                .build();
    }

    @Test
    @DisplayName("createBroadcastNotification - Should create and broadcast notification")
    void createBroadcastNotification_Success() {
        when(broadcastNotificationJpaRepository.save(any(BroadcastNotification.class)))
                .thenReturn(testBroadcastNotification);
        doNothing().when(webSocketHandler).broadcastNotification(any());

        BroadcastNotificationResponse response = broadcastNotificationService.createBroadcastNotification(
                BroadcastNotificationType.SYSTEM,
                "Test Broadcast",
                "Test Broadcast Message",
                relatedId
        );

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(notificationId);
        assertThat(response.getTitle()).isEqualTo("Test Broadcast");
        assertThat(response.getMessage()).isEqualTo("Test Broadcast Message");
        assertThat(response.getType()).isEqualTo(BroadcastNotificationType.SYSTEM);

        verify(broadcastNotificationJpaRepository).save(any(BroadcastNotification.class));
        verify(webSocketHandler).broadcastNotification(any(BroadcastNotificationResponse.class));
    }

    @Test
    @DisplayName("getAllBroadcastNotifications - Should return all broadcast notifications")
    void getAllBroadcastNotifications_Success() {
        when(broadcastNotificationJpaRepository.findAll())
                .thenReturn(List.of(testBroadcastNotification));

        List<BroadcastNotificationResponse> responses = broadcastNotificationService.getAllBroadcastNotifications();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(notificationId);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Broadcast");

        verify(broadcastNotificationJpaRepository).findAll();
    }
}
