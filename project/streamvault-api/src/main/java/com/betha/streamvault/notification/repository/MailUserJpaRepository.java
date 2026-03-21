package com.betha.streamvault.notification.repository;

import com.betha.streamvault.notification.model.MailUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailUserJpaRepository extends JpaRepository<MailUser, Long> {
    
    Optional<MailUser> findByEmail(String email);
}
