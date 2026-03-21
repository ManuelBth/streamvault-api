package com.betha.streamvault.user.repository;

import com.betha.streamvault.user.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProfileJpaRepository extends JpaRepository<Profile, UUID> {
    
    List<Profile> findByUserId(UUID userId);
    
    long countByUserId(UUID userId);
}
