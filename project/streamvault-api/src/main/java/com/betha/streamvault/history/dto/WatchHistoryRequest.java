package com.betha.streamvault.history.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchHistoryRequest {

    @NotNull(message = "episodeId es requerido para episodios")
    private UUID episodeId;

    @PositiveOrZero(message = "progressSec debe ser cero o positivo")
    private Integer progressSec;

    private Boolean completed;
}
