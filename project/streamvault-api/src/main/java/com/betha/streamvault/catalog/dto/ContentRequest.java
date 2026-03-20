package com.betha.streamvault.catalog.dto;

import com.betha.streamvault.catalog.model.ContentStatus;
import com.betha.streamvault.catalog.model.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ContentRequest {

    @NotBlank(message = "El título es requerido")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;

    private String description;

    @NotNull(message = "El tipo de contenido es requerido")
    private ContentType type;

    private Integer releaseYear;

    @Size(max = 10, message = "El rating no puede exceder 10 caracteres")
    private String rating;

    private String thumbnailKey;

    private String minioKey;

    private List<UUID> genreIds;

    private ContentStatus status;
}
