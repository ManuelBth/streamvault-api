package com.betha.streamvault.catalog.dto;

import com.betha.streamvault.catalog.model.ContentStatus;
import com.betha.streamvault.catalog.model.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {

    private UUID id;
    private String title;
    private String description;
    private ContentType type;
    private Integer releaseYear;
    private String rating;
    private String thumbnailKey;
    private String minioKey;
    private ContentStatus status;
    private List<GenreResponse> genres;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
