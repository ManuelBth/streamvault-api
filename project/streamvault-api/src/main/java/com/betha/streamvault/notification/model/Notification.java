package com.betha.streamvault.notification.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("notifications")
public class Notification {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("type")
    private NotificationType type;

    @Column("title")
    private String title;

    @Column("message")
    private String message;

    @Column("related_id")
    private UUID relatedId;

    @Column("is_read")
    private Boolean isRead;

    @Column("created_at")
    private Instant createdAt;

    public enum NotificationType {
        NEW_CONTENT,
        NEW_EPISODE,
        USER_NOTIFICATION,
        SYSTEM
    }
}
