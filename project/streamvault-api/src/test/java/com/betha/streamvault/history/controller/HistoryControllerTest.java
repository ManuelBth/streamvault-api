package com.betha.streamvault.history.controller;

import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryController Tests")
class HistoryControllerTest {

    @Mock
    private HistoryService historyService;

    private HistoryController historyController;

    private WatchHistoryResponse testHistoryResponse;

    @BeforeEach
    void setUp() {
        historyController = new HistoryController(historyService);

        testHistoryResponse = WatchHistoryResponse.builder()
                .id(UUID.randomUUID())
                .profileId(UUID.randomUUID())
                .episodeId(UUID.randomUUID())
                .progressSec(120)
                .completed(false)
                .watchedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /history - Should return watch history")
    void getHistory_Success() {
        when(historyService.getHistory("test@streamvault.com")).thenReturn(Flux.just(testHistoryResponse));

        Mono<ResponseEntity<?>> result = historyController.getHistory("test@streamvault.com");

        ResponseEntity<?> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("GET /history - Should return empty list when no history")
    void getHistory_Empty() {
        when(historyService.getHistory("test@streamvault.com")).thenReturn(Flux.empty());

        Mono<ResponseEntity<?>> result = historyController.getHistory("test@streamvault.com");

        ResponseEntity<?> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("GET /history/{id} - Should return history by ID")
    void getHistoryById_Success() {
        UUID historyId = testHistoryResponse.getId();
        when(historyService.getHistoryById("test@streamvault.com", historyId)).thenReturn(Mono.just(testHistoryResponse));

        Mono<ResponseEntity<WatchHistoryResponse>> result = historyController.getHistoryById("test@streamvault.com", historyId);

        ResponseEntity<WatchHistoryResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(historyId, entity.getBody().getId());
    }

    @Test
    @DisplayName("POST /history - Should start tracking")
    void startTracking_Success() {
        WatchHistoryRequest request = WatchHistoryRequest.builder()
                .episodeId(UUID.randomUUID())
                .build();

        when(historyService.startTracking("test@streamvault.com", request)).thenReturn(Mono.just(testHistoryResponse));

        Mono<ResponseEntity<WatchHistoryResponse>> result = historyController.startTracking("test@streamvault.com", request);

        ResponseEntity<WatchHistoryResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("PUT /history/{id}/progress - Should update progress")
    void updateProgress_Success() {
        UUID historyId = testHistoryResponse.getId();
        ProgressUpdateRequest request = ProgressUpdateRequest.builder()
                .progressSec(300)
                .build();

        WatchHistoryResponse updatedResponse = WatchHistoryResponse.builder()
                .id(historyId)
                .profileId(testHistoryResponse.getProfileId())
                .episodeId(testHistoryResponse.getEpisodeId())
                .progressSec(300)
                .completed(false)
                .watchedAt(LocalDateTime.now())
                .build();

        when(historyService.updateProgress("test@streamvault.com", historyId, request)).thenReturn(Mono.just(updatedResponse));

        Mono<ResponseEntity<WatchHistoryResponse>> result = historyController.updateProgress("test@streamvault.com", historyId, request);

        ResponseEntity<WatchHistoryResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(300, entity.getBody().getProgressSec());
    }

    @Test
    @DisplayName("PUT /history/{id}/completed - Should mark as completed")
    void markAsCompleted_Success() {
        UUID historyId = testHistoryResponse.getId();

        WatchHistoryResponse completedResponse = WatchHistoryResponse.builder()
                .id(historyId)
                .profileId(testHistoryResponse.getProfileId())
                .episodeId(testHistoryResponse.getEpisodeId())
                .progressSec(Integer.MAX_VALUE)
                .completed(true)
                .watchedAt(LocalDateTime.now())
                .build();

        when(historyService.markAsCompleted("test@streamvault.com", historyId)).thenReturn(Mono.just(completedResponse));

        Mono<ResponseEntity<WatchHistoryResponse>> result = historyController.markAsCompleted("test@streamvault.com", historyId);

        ResponseEntity<WatchHistoryResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertTrue(entity.getBody().getCompleted());
    }

    @Test
    @DisplayName("GET /history/{id} - Should return completed history")
    void getHistoryById_Completed() {
        UUID historyId = UUID.randomUUID();
        WatchHistoryResponse completedResponse = WatchHistoryResponse.builder()
                .id(historyId)
                .profileId(UUID.randomUUID())
                .episodeId(UUID.randomUUID())
                .progressSec(Integer.MAX_VALUE)
                .completed(true)
                .watchedAt(LocalDateTime.now())
                .build();

        when(historyService.getHistoryById("test@streamvault.com", historyId)).thenReturn(Mono.just(completedResponse));

        Mono<ResponseEntity<WatchHistoryResponse>> result = historyController.getHistoryById("test@streamvault.com", historyId);

        ResponseEntity<WatchHistoryResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertTrue(entity.getBody().getCompleted());
    }
}
