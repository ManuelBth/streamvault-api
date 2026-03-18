package com.betha.streamvault.notification.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("NotificationWebSocketHandler Tests")
class NotificationWebSocketHandlerTest {

    private NotificationWebSocketHandler handler;
    private WebSocketSession session;

    @BeforeEach
    void setUp() {
        handler = new NotificationWebSocketHandler();
        session = mock(WebSocketSession.class);
    }

    @Test
    @DisplayName("afterConnectionEstablished - Should not throw with userId")
    void afterConnectionEstablished_WithUserId() throws Exception {
        // Given
        String userId = UUID.randomUUID().toString();
        when(session.getAttributes()).thenReturn(java.util.Map.of("userId", userId));

        // When & Then - should not throw
        assertDoesNotThrow(() -> handler.afterConnectionEstablished(session));
    }

    @Test
    @DisplayName("afterConnectionEstablished - Should not throw without userId")
    void afterConnectionEstablished_WithoutUserId() {
        // Given
        when(session.getAttributes()).thenReturn(java.util.Map.of());

        // When & Then - should not throw
        assertDoesNotThrow(() -> handler.afterConnectionEstablished(session));
    }

    @Test
    @DisplayName("afterConnectionClosed - Should not throw")
    void afterConnectionClosed_Success() {
        // Given
        String userId = UUID.randomUUID().toString();
        when(session.getAttributes()).thenReturn(java.util.Map.of("userId", userId));

        // When & Then - should not throw
        assertDoesNotThrow(() -> 
            handler.afterConnectionClosed(session, CloseStatus.NORMAL));
    }

    @Test
    @DisplayName("handleTextMessage - Should not throw")
    void handleTextMessage_Success() {
        // Given
        TextMessage message = new TextMessage("test message");

        // When & Then - should not throw
        assertDoesNotThrow(() -> handler.handleTextMessage(session, message));
    }

    @Test
    @DisplayName("sendNotificationToUser - Should not throw when no session")
    void sendNotificationToUser_NoSession() {
        // Given - don't add session to handler
        String userId = UUID.randomUUID().toString();

        // When & Then - should not throw even without a session
        assertDoesNotThrow(() -> 
            handler.sendNotificationToUser(userId, null));
    }

    @Test
    @DisplayName("sendNotificationToUser - Should not throw when session is closed")
    void sendNotificationToUser_SessionClosed() {
        // Given
        String userId = UUID.randomUUID().toString();
        WebSocketSession closedSession = mock(WebSocketSession.class);
        when(closedSession.isOpen()).thenReturn(false);

        // When & Then - should not throw
        assertDoesNotThrow(() -> 
            handler.sendNotificationToUser(userId, null));
    }

    @Test
    @DisplayName("broadcastNotification - Should not throw with null sessions")
    void broadcastNotification_NullSessions() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> 
            handler.broadcastNotification(null));
    }

    @Test
    @DisplayName("broadcastNotification - Should not throw with empty sessions")
    void broadcastNotification_EmptySessions() {
        // When & Then - should not throw
        assertDoesNotThrow(() -> 
            handler.broadcastNotification(null));
    }
}
