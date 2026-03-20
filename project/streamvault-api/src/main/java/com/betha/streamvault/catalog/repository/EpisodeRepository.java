package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Episode;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface EpisodeRepository extends ReactiveCrudRepository<Episode, UUID> {

    Flux<Episode> findBySeasonIdOrderByEpisodeNumberAsc(UUID seasonId);

    Flux<Episode> findBySeasonId(UUID seasonId);
}
