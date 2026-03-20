package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Genre;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface GenreRepository extends ReactiveCrudRepository<Genre, UUID> {

    Flux<Genre> findAllByOrderByNameAsc();

    Flux<Genre> findByNameContainingIgnoreCase(String name);
}
