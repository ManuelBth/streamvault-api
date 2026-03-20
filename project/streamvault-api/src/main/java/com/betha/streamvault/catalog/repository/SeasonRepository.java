package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Season;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface SeasonRepository extends ReactiveCrudRepository<Season, UUID> {

    Flux<Season> findByContentIdOrderBySeasonNumberAsc(UUID contentId);

    Flux<Season> findByContentId(UUID contentId);
}
