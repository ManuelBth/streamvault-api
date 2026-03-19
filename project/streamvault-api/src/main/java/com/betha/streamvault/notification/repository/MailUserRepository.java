package com.betha.streamvault.notification.repository;

import com.betha.streamvault.notification.model.MailUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MailUserRepository extends ReactiveCrudRepository<MailUser, Long> {

    Mono<MailUser> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}
