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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryController Tests")
class HistoryControllerTest {

    @Mock
    private HistoryService historyService;

    private HistoryController historyController;

    private WatchHistoryResponse testHistoryResponse;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        historyController = new HistoryController(historyService);
        profileId = UUID.randomUUID();

        testHistoryResponse = WatchHistoryResponse.builder()
                .id(UUID.randomUUID())
                .profileId(profileId)
                .episodeId(UUID.randomUUID())
                .progressSec(120)
                .completed(false)
                .watchedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /history - Should return watch history")
    void getHistory_Success() {
        when(historyService.getHistory(eq("test@streamvault.com"), any())).thenReturn(List.of(testHistoryResponse));

        ResponseEntity<List<WatchHistoryResponse>> result = historyController.getHistory("test@streamvault.com", profileId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("GET /history - Should return empty list when no history")
    void getHistory_Empty() {
        when(historyService.getHistory(eq("test@streamvault.com"), any())).thenReturn(List.of());

        ResponseEntity<List<WatchHistoryResponse>> result = historyController.getHistory("test@streamvault.com", profileId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    @DisplayName("GET /history/{id} - Should return history by ID")
    void getHistoryById_Success() {
        UUID historyId = testHistoryResponse.getId();
        when(historyService.getHistoryById(eq("test@streamvault.com"), any(), eq(historyId))).thenReturn(testHistoryResponse);

        ResponseEntity<WatchHistoryResponse> result = historyController.getHistoryById("test@streamvault.com", profileId, historyId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(historyId);
    }

    @Test
    @DisplayName("POST /history - Should start tracking")
    void startTracking_Success() {
        WatchHistoryRequest request = WatchHistoryRequest.builder()
                .episodeId(UUID.randomUUID())
                .build();

        when(historyService.startTracking(eq("test@streamvault.com"), any(), any())).thenReturn(testHistoryResponse);

        ResponseEntity<WatchHistoryResponse> result = historyController.startTracking("test@streamvault.com", profileId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isNotNull();
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
                .profileId(profileId)
                .episodeId(testHistoryResponse.getEpisodeId())
                .progressSec(300)
                .completed(false)
                .watchedAt(LocalDateTime.now())
                .build();

        when(historyService.updateProgress(eq("test@streamvault.com"), any(), eq(historyId), any())).thenReturn(updatedResponse);

        ResponseEntity<WatchHistoryResponse> result = historyController.updateProgress("test@streamvault.com", profileId, historyId, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getProgressSec()).isEqualTo(300);
    }

    @Test
    @DisplayName("PUT /history/{id}/completed - Should mark as completed")
    void markAsCompleted_Success() {
        UUID historyId = testHistoryResponse.getId();

        WatchHistoryResponse completedResponse = WatchHistoryResponse.builder()
                .id(historyId)
                .profileId(profileId)
                .episodeId(testHistoryResponse.getEpisodeId())
                .progressSec(-1)
                .completed(true)
                .watchedAt(LocalDateTime.now())
                .build();

        when(historyService.markAsCompleted(eq("test@streamvault.com"), any(), eq(historyId))).thenReturn(completedResponse);

        ResponseEntity<WatchHistoryResponse> result = historyController.markAsCompleted("test@streamvault.com", profileId, historyId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCompleted()).isTrue();
    }
}
