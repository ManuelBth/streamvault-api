package com.betha.streamvault.user.repository;

import com.betha.streamvault.user.model.Subscription;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface SubscriptionRepository extends ReactiveCrudRepository<Subscription, UUID> {
    
    Mono<Subscription> findByUserId(UUID userId);
    
    Mono<Subscription> findByUserIdAndActiveTrue(UUID userId);
}
