package com.betha.streamvault.auth.model;

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
@Table("refresh_tokens")
public class RefreshToken {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("token_hash")
    private String tokenHash;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("revoked")
    private Boolean revoked;

    @Column("created_at")
    private Instant createdAt;
}
