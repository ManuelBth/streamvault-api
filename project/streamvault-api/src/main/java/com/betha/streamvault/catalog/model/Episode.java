package com.betha.streamvault.catalog.model;

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
@Table("episodes")
public class Episode {

    @Id
    private UUID id;

    @Column("season_id")
    private UUID seasonId;

    @Column("episode_number")
    private Integer episodeNumber;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("minio_key")
    private String minioKey;

    @Column("thumbnail_key")
    private String thumbnailKey;

    @Column("duration_sec")
    private Integer durationSec;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;
}
