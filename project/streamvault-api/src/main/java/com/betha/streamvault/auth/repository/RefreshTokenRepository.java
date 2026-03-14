package com.betha.streamvault.auth.repository;

import com.betha.streamvault.auth.model.RefreshToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, UUID> {
    
    Mono<RefreshToken> findByTokenHash(String tokenHash);
    
    Mono<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
    
    Mono<Void> deleteByUserId(UUID userId);
}
