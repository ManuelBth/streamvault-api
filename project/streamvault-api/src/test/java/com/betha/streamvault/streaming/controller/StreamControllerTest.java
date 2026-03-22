package com.betha.streamvault.streaming.controller;

import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.streaming.service.StreamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreamController Tests")
class StreamControllerTest {

    @Mock
    private StreamService streamService;

    @InjectMocks
    private StreamController streamController;

    @Test
    @DisplayName("GET /stream/{contentId} - Should return stream URL")
    void getStreamUrl_Success() {
        UUID contentId = UUID.randomUUID();
        StreamResponse streamResponse = StreamResponse.builder()
                .url("https://minio.example.com/presigned-url")
                .expiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();
        when(streamService.getStreamUrl(contentId, "test@streamvault.com")).thenReturn(streamResponse);

        ResponseEntity<StreamResponse> result = streamController.getStreamUrl(contentId, "test@streamvault.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("https://minio.example.com/presigned-url", result.getBody().getUrl());
    }

    @Test
    @DisplayName("GET /stream/{contentId} - Should return 404 when content not found")
    void getStreamUrl_NotFound() {
        UUID contentId = UUID.randomUUID();
        when(streamService.getStreamUrl(contentId, "test@streamvault.com"))
                .thenThrow(new ResourceNotFoundException("Contenido no encontrado"));

        ResponseEntity<StreamResponse> result = streamController.getStreamUrl(contentId, "test@streamvault.com");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("GET /stream/{contentId} - Should return 403 when no subscription")
    void getStreamUrl_NoSubscription() {
        UUID contentId = UUID.randomUUID();
        when(streamService.getStreamUrl(contentId, "test@streamvault.com"))
                .thenThrow(new StreamService.SubscriptionNotActiveException("Suscripción no activa"));

        ResponseEntity<StreamResponse> result = streamController.getStreamUrl(contentId, "test@streamvault.com");

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    @DisplayName("GET /stream/{contentId}/episode/{episodeId} - Should return episode stream URL")
    void getEpisodeStreamUrl_Success() {
        UUID contentId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        StreamResponse streamResponse = StreamResponse.builder()
                .url("https://minio.example.com/presigned-url")
                .expiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                .build();
        when(streamService.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com"))
                .thenReturn(streamResponse);

        ResponseEntity<StreamResponse> result = streamController.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("https://minio.example.com/presigned-url", result.getBody().getUrl());
    }

    @Test
    @DisplayName("GET /stream/{contentId}/episode/{episodeId} - Should return 404 when episode not found")
    void getEpisodeStreamUrl_NotFound() {
        UUID contentId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        when(streamService.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com"))
                .thenThrow(new ResourceNotFoundException("Episodio no encontrado"));

        ResponseEntity<StreamResponse> result = streamController.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("GET /stream/{contentId}/episode/{episodeId} - Should return 403 when no subscription")
    void getEpisodeStreamUrl_NoSubscription() {
        UUID contentId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        when(streamService.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com"))
                .thenThrow(new StreamService.SubscriptionNotActiveException("Suscripción no activa"));

        ResponseEntity<StreamResponse> result = streamController.getEpisodeStreamUrl(contentId, episodeId, "test@streamvault.com");

        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }
}
