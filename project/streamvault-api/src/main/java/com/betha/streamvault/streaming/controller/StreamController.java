package com.betha.streamvault.streaming.controller;

import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.streaming.service.StreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @GetMapping("/{contentId}")
    public ResponseEntity<StreamResponse> getStreamUrl(
            @PathVariable UUID contentId,
            @AuthenticationPrincipal String email) {
        try {
            StreamResponse response = streamService.getStreamUrl(contentId, email);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (StreamService.SubscriptionNotActiveException e) {
            log.warn("Subscription not active for user: {}", email);
            return ResponseEntity.status(403).build();
        }
    }

    @GetMapping("/{contentId}/episode/{episodeId}")
    public ResponseEntity<StreamResponse> getEpisodeStreamUrl(
            @PathVariable UUID contentId,
            @PathVariable UUID episodeId,
            @AuthenticationPrincipal String email) {
        try {
            StreamResponse response = streamService.getEpisodeStreamUrl(contentId, episodeId, email);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.warn("Resource not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (StreamService.SubscriptionNotActiveException e) {
            log.warn("Subscription not active for user: {}", email);
            return ResponseEntity.status(403).build();
        }
    }
}
