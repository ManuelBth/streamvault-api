package com.betha.streamvault.catalog.repository;

import com.betha.streamvault.catalog.model.Content;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ContentRepository extends ReactiveCrudRepository<Content, UUID> {

    Flux<Content> findByStatus(String status);

    Flux<Content> findByType(String type);

    Flux<Content> findByStatusAndType(String status, String type);

    Flux<Content> findAllBy(Pageable pageable);

    Flux<Content> findByTitleContainingIgnoreCase(String title);

    Mono<Boolean> existsByTitle(String title);
}
