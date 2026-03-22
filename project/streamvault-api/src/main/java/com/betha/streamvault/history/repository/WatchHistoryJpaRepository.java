package com.betha.streamvault.history.repository;

import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.history.model.WatchHistory;
import com.betha.streamvault.user.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchHistoryJpaRepository extends JpaRepository<WatchHistory, UUID> {
    
    Optional<WatchHistory> findByProfileAndEpisode(Profile profile, Episode episode);
    
    List<WatchHistory> findByProfileOrderByWatchedAtDesc(Profile profile);
}
