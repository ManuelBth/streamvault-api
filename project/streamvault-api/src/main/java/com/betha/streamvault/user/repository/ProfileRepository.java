package com.betha.streamvault.user.repository;

import com.betha.streamvault.user.model.Profile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProfileRepository extends ReactiveCrudRepository<Profile, UUID> {
    
    Flux<Profile> findByUserId(UUID userId);
    
    Mono<Long> countByUserId(UUID userId);
    
    Mono<Void> deleteByUserId(UUID userId);
}
