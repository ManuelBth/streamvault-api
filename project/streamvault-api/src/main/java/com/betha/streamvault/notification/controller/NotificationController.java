package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.NotificationResponse;
import com.betha.streamvault.notification.service.NotificationService;
import com.betha.streamvault.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public Mono<ResponseEntity<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal String username) {
        return userService.getCurrentUser(username)
                .flatMap(user -> notificationService.getNotifications(user.getId()).collectList())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/unread")
    public Mono<ResponseEntity<List<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal String username) {
        return userService.getCurrentUser(username)
                .flatMap(user -> notificationService.getUnreadNotifications(user.getId()).collectList())
                .map(ResponseEntity::ok);
    }

    @GetMapping("/unread/count")
    public Mono<ResponseEntity<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal String username) {
        return userService.getCurrentUser(username)
                .flatMap(user -> notificationService.getUnreadCount(user.getId())
                        .map(count -> ResponseEntity.ok(Map.of("count", count))));
    }

    @PutMapping("/{id}/read")
    public Mono<ResponseEntity<Void>> markAsRead(
            @AuthenticationPrincipal String username,
            @PathVariable UUID id) {
        return userService.getCurrentUser(username)
                .flatMap(user -> notificationService.markAsRead(id, user.getId()))
                .thenReturn(ResponseEntity.ok().<Void>build());
    }

    @PutMapping("/read-all")
    public Mono<ResponseEntity<Void>> markAllAsRead(
            @AuthenticationPrincipal String username) {
        return userService.getCurrentUser(username)
                .flatMap(user -> notificationService.markAllAsRead(user.getId()))
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}
