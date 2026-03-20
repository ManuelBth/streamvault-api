package com.betha.streamvault.catalog.dto;

import com.betha.streamvault.catalog.model.EpisodeStatus;
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
public class EpisodeResponse {

    private UUID id;
    private UUID seasonId;
    private Integer episodeNumber;
    private String title;
    private String description;
    private String minioKey;
    private String thumbnailKey;
    private Integer durationSec;
    private EpisodeStatus status;
    private LocalDateTime createdAt;
}
