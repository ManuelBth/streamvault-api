package com.betha.streamvault.catalog.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("seasons")
public class Season {

    @Id
    private UUID id;

    @Column("content_id")
    private UUID contentId;

    @Column("season_number")
    private Integer seasonNumber;
}
