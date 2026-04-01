package com.betha.streamvault.notification.dto;

import com.betha.streamvault.notification.model.BroadcastNotificationType;
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
public class BroadcastNotificationResponse {

    private UUID id;
    private BroadcastNotificationType type;
    private String title;
    private String message;
    private UUID relatedId;
    private LocalDateTime createdAt;
}