package com.betha.streamvault.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendEmailRequest {

    @NotBlank(message = "El destinatario es obligatorio")
    @Email(message = "Debe ser un email válido")
    private String to;

    @NotBlank(message = "El asunto es obligatorio")
    private String subject;

    @NotBlank(message = "El cuerpo del email es obligatorio")
    private String body;
}
