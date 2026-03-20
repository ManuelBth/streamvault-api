package com.betha.streamvault.history.dto;

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
public class WatchHistoryResponse {

    private UUID id;
    private UUID profileId;
    private UUID episodeId;
    private Integer progressSec;
    private Boolean completed;
    private LocalDateTime watchedAt;
}
