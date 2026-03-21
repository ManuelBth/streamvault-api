package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GenreJpaRepository extends JpaRepository<Genre, UUID> {

    List<Genre> findAllByOrderByNameAsc();

    List<Genre> findByNameContainingIgnoreCase(String name);
}
