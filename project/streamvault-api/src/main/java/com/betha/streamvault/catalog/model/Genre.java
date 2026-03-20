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
@Table("genres")
public class Genre {

    @Id
    private UUID id;

    @Column("name")
    private String name;
}
