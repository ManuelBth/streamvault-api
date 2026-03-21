package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EpisodeJpaRepository extends JpaRepository<Episode, UUID> {

    List<Episode> findBySeasonIdOrderByEpisodeNumberAsc(UUID seasonId);

    List<Episode> findBySeasonId(UUID seasonId);
}
