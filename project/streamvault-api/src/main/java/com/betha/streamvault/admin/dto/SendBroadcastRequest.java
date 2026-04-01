package com.betha.streamvault.admin.dto;

import com.betha.streamvault.notification.model.BroadcastNotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendBroadcastRequest {

    @NotNull(message = "El tipo de notificación es requerido")
    private BroadcastNotificationType type;

    @NotNull(message = "El título es requerido")
    private String title;

    @NotNull(message = "El mensaje es requerido")
    private String message;

    private UUID relatedId;
}