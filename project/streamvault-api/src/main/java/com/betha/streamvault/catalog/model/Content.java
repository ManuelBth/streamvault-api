package com.betha.streamvault.catalog.model;

import com.betha.streamvault.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ContentType type;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "rating")
    private String rating;

    @Column(name = "thumbnail_key")
    private String thumbnailKey;

    @Column(name = "minio_base_key")
    private String minioBaseKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ContentStatus status;

    @Column(name = "created_by")
    private UUID createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "content_genres",
        joinColumns = @JoinColumn(name = "content_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "content", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Season> seasons = new ArrayList<>();
}
