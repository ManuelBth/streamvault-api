package com.betha.streamvault.catalog.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("content")
public class Content {

    @Id
    private UUID id;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("type")
    private String type;

    @Column("release_year")
    private Integer releaseYear;

    @Column("rating")
    private String rating;

    @Column("thumbnail_key")
    private String thumbnailKey;

    @Column("minio_base_key")
    private String minioBaseKey;

    @Column("status")
    private String status;

    @Column("created_by")
    private UUID createdBy;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Transient
    private List<Genre> genres;
}
