package com.betha.streamvault.user.model;


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
@Table("profiles")
public class Profile {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("name")
    private String name;

    @Column("avatar_url")
    private String avatarUrl;

    @Column("created_at")
    private Instant createdAt;

    public static final int MAX_PROFILES_PER_USER = 3;
}
