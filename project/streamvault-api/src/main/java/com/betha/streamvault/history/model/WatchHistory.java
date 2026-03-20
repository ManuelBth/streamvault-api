package com.betha.streamvault.history.model;

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
@Table("watch_history")
public class WatchHistory {

    @Id
    private UUID id;

    @Column("profile_id")
    private UUID profileId;

    @Column("episode_id")
    private UUID episodeId;

    @Column("progress_sec")
    private Integer progressSec;

    @Column("watched_at")
    private Instant watchedAt;
}
