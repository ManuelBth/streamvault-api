package com.betha.streamvault.notification.dto;

import com.betha.streamvault.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private UUID relatedId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
