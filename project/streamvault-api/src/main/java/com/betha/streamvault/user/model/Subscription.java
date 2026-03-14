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
@Table("subscriptions")
public class Subscription {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("plan")
    private String plan;

    @Column("started_at")
    private Instant startedAt;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("active")
    private Boolean active;

    public static final String PLAN_BASIC = "BASIC";
    public static final String PLAN_STANDARD = "STANDARD";
    public static final String PLAN_PREMIUM = "PREMIUM";
}
