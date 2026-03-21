package com.betha.streamvault.auth.repository;

import com.betha.streamvault.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID> {
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    Optional<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);
}
