package com.betha.streamvault.notification.repository;

import com.betha.streamvault.notification.model.Notification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface NotificationRepository extends ReactiveCrudRepository<Notification, UUID> {

    Flux<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Flux<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    Mono<Long> countByUserIdAndIsReadFalse(UUID userId);

    Mono<Long> countByUserIdAndIsReadFalseAndUserIdIsNotNull(UUID userId);
}
