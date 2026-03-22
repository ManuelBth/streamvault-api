package com.betha.streamvault.catalog.model;

import com.betha.streamvault.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "seasons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Season extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber;

    @OneToMany(mappedBy = "season", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Episode> episodes = new ArrayList<>();
}
