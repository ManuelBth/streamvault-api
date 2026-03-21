package com.betha.streamvault.history.controller;

import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.service.HistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<WatchHistoryResponse>> getHistory(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(historyService.getHistory(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchHistoryResponse> getHistoryById(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        return ResponseEntity.ok(historyService.getHistoryById(email, id));
    }

    @PostMapping
    public ResponseEntity<WatchHistoryResponse> startTracking(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody WatchHistoryRequest request) {
        return ResponseEntity.ok(historyService.startTracking(email, request));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<WatchHistoryResponse> updateProgress(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody ProgressUpdateRequest request) {
        return ResponseEntity.ok(historyService.updateProgress(email, id, request));
    }

    @PutMapping("/{id}/completed")
    public ResponseEntity<WatchHistoryResponse> markAsCompleted(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        return ResponseEntity.ok(historyService.markAsCompleted(email, id));
    }
}
