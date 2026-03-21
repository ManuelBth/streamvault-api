package com.betha.streamvault.user.repository;

import com.betha.streamvault.user.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<Subscription, UUID> {
    
    Optional<Subscription> findByUserId(UUID userId);
}
