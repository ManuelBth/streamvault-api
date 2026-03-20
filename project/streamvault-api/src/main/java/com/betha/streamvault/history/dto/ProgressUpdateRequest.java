package com.betha.streamvault.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateRequest {

    @NotNull(message = "progressSec es requerido")
    @PositiveOrZero(message = "progressSec debe ser cero o positivo")
    private Integer progressSec;

    private Boolean completed;
}
