package com.betha.streamvault.notification.repository;

import com.betha.streamvault.notification.model.BroadcastNotification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BroadcastNotificationJpaRepository extends JpaRepository<BroadcastNotification, UUID> {
    
    default List<BroadcastNotification> findAll() {
        return findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}