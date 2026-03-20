package com.betha.streamvault.streaming.controller;

import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.streaming.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @GetMapping("/{contentId}")
    public Mono<ResponseEntity<StreamResponse>> getStreamUrl(
            @PathVariable UUID contentId,
            @AuthenticationPrincipal String email) {
        return streamService.getStreamUrl(contentId, email)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> {
                    log.warn("Resource not found: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .onErrorResume(StreamService.SubscriptionNotActiveException.class, e -> {
                    log.warn("Subscription not active for user: {}", email);
                    return Mono.just(ResponseEntity.status(403).build());
                });
    }

    @GetMapping("/{contentId}/episode/{episodeId}")
    public Mono<ResponseEntity<StreamResponse>> getEpisodeStreamUrl(
            @PathVariable UUID contentId,
            @PathVariable UUID episodeId,
            @AuthenticationPrincipal String email) {
        return streamService.getEpisodeStreamUrl(contentId, episodeId, email)
                .map(ResponseEntity::ok)
                .onErrorResume(ResourceNotFoundException.class, e -> {
                    log.warn("Resource not found: {}", e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .onErrorResume(StreamService.SubscriptionNotActiveException.class, e -> {
                    log.warn("Subscription not active for user: {}", email);
                    return Mono.just(ResponseEntity.status(403).build());
                });
    }
}
