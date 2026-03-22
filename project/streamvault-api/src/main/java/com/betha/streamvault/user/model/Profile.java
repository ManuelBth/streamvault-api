package com.betha.streamvault.user.model;

import com.betha.streamvault.history.model.WatchHistory;
import com.betha.streamvault.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Profile entity for user profiles within a subscription.
 * Each user can have up to MAX_PROFILES_PER_USER profiles.
 * 
 * Uses @Getter @Setter instead of @Data to prevent Hibernate proxy issues.
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "avatar_url")
    private String avatarUrl;

    // JPA Relationships
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    @Builder.Default
    private List<WatchHistory> watchHistories = new ArrayList<>();

    public static final int MAX_PROFILES_PER_USER = 3;
}
