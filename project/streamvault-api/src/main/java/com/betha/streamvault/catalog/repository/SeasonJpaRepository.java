package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeasonJpaRepository extends JpaRepository<Season, UUID> {

    List<Season> findByContentIdOrderBySeasonNumberAsc(UUID contentId);

    List<Season> findByContentId(UUID contentId);
}
