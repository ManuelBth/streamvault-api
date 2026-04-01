package com.betha.streamvault.admin.controller;

import com.betha.streamvault.admin.dto.SendBroadcastRequest;
import com.betha.streamvault.admin.dto.SendNotificationRequest;
import com.betha.streamvault.notification.dto.BroadcastNotificationResponse;
import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.service.BroadcastNotificationService;
import com.betha.streamvault.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final BroadcastNotificationService broadcastNotificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody SendNotificationRequest request) {
        log.info("Admin {} sending notification to user: {}", email, request.getUserId());
        NotificationResponse response = notificationService.createNotification(
                request.getUserId(),
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getRelatedId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/broadcast")
    public ResponseEntity<BroadcastNotificationResponse> sendBroadcast(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody SendBroadcastRequest request) {
        log.info("Admin {} sending broadcast notification: {}", email, request.getType());
        BroadcastNotificationResponse response = broadcastNotificationService.createBroadcastNotification(
                request.getType(),
                request.getTitle(),
                request.getMessage(),
                request.getRelatedId()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/broadcast")
    public ResponseEntity<List<BroadcastNotificationResponse>> getAllBroadcasts(
            @AuthenticationPrincipal String email) {
        log.info("Admin {} requesting all broadcast notifications", email);
        return ResponseEntity.ok(broadcastNotificationService.getAllBroadcastNotifications());
    }
}