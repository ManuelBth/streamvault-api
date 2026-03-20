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
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public Mono<ResponseEntity<?>> getHistory(@AuthenticationPrincipal String email) {
        return historyService.getHistory(email)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<WatchHistoryResponse>> getHistoryById(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        return historyService.getHistoryById(email, id)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<WatchHistoryResponse>> startTracking(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody WatchHistoryRequest request) {
        return historyService.startTracking(email, request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/progress")
    public Mono<ResponseEntity<WatchHistoryResponse>> updateProgress(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id,
            @Valid @RequestBody ProgressUpdateRequest request) {
        return historyService.updateProgress(email, id, request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/completed")
    public Mono<ResponseEntity<WatchHistoryResponse>> markAsCompleted(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        return historyService.markAsCompleted(email, id)
                .map(ResponseEntity::ok);
    }
}
