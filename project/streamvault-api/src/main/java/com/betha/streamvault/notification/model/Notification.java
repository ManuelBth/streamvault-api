package com.betha.streamvault.notification.model;

import com.betha.streamvault.shared.model.BaseEntity;
import com.betha.streamvault.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    public enum NotificationType {
        NEW_CONTENT,
        NEW_EPISODE,
        USER_NOTIFICATION,
        SYSTEM
    }
}
