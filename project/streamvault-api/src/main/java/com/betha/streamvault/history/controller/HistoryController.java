package com.betha.streamvault.history.controller;

import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.service.HistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<WatchHistoryResponse>> getHistory(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) UUID profileId) {
        return ResponseEntity.ok(historyService.getHistory(email, profileId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WatchHistoryResponse> getHistoryById(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) UUID profileId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(historyService.getHistoryById(email, profileId, id));
    }

    @PostMapping
    public ResponseEntity<WatchHistoryResponse> startTracking(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) UUID profileId,
            @Valid @RequestBody WatchHistoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historyService.startTracking(email, profileId, request));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<WatchHistoryResponse> updateProgress(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) UUID profileId,
            @PathVariable UUID id,
            @Valid @RequestBody ProgressUpdateRequest request) {
        return ResponseEntity.ok(historyService.updateProgress(email, profileId, id, request));
    }

    @PutMapping("/{id}/completed")
    public ResponseEntity<WatchHistoryResponse> markAsCompleted(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) UUID profileId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(historyService.markAsCompleted(email, profileId, id));
    }
}
