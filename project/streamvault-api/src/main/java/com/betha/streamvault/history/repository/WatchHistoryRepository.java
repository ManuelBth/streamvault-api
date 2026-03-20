package com.betha.streamvault.history.repository;

import com.betha.streamvault.history.model.WatchHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface WatchHistoryRepository extends ReactiveCrudRepository<WatchHistory, UUID> {

    Flux<WatchHistory> findByProfileId(UUID profileId);

    Mono<WatchHistory> findByProfileIdAndEpisodeId(UUID profileId, UUID episodeId);

    Flux<WatchHistory> findByProfileIdOrderByWatchedAtDesc(UUID profileId);
}
