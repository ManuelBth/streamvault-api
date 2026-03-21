package com.betha.streamvault.history.repository;

import com.betha.streamvault.history.model.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchHistoryJpaRepository extends JpaRepository<WatchHistory, UUID> {
    
    Optional<WatchHistory> findByProfileIdAndEpisodeId(UUID profileId, UUID episodeId);
    
    java.util.List<WatchHistory> findByProfileIdOrderByWatchedAtDesc(UUID profileId);
}
