package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.model.MailUser;
import com.betha.streamvault.notification.repository.MailUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Log4j2
@Service
@RequiredArgsConstructor
public class MailUserService {

    private final MailUserRepository mailUserRepository;
    private final DatabaseClient databaseClient;

    /**
     * Creates a mail account for a user.
     * Password is hashed using SHA256 (Dovecot compatible).
     */
    public Mono<MailUser> createMailAccount(String email, String password) {
        log.info("Creating mail account for: {}", email);

        String hashedPassword = hashPasswordSha256(password);
        Instant now = Instant.now();
        String maildir = "streamvault.com/" + email.split("@")[0] + "/";

        // Use raw SQL to avoid RETURNING clause issues with R2DBC
        String sql = "INSERT INTO mail_users (email, password, maildir, quota, active, created_at, updated_at) " +
                     "VALUES ($1, $2, $3, $4, $5, $6, $7)";

        return databaseClient.sql(sql)
                .bind("$1", email)
                .bind("$2", hashedPassword)
                .bind("$3", maildir)
                .bind("$4", 0L)
                .bind("$5", true)
                .bind("$6", now)
                .bind("$7", now)
                .then()
                .doOnSuccess(v -> log.info("Mail account created for: {}", email))
                .doOnError(e -> log.error("Failed to create mail account for {}: {}", email, e.getMessage()))
                .thenReturn(MailUser.builder()
                        .email(email)
                        .password(hashedPassword)
                        .maildir(maildir)
                        .quota(0L)
                        .active(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
    }

    /**
     * Hashes password using SHA256 - compatible with Dovecot SHA256-CRYPT.
     */
    private String hashPasswordSha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);
            // Dovecot expects format: {SHA256}base64hash
            return "{SHA256}" + encoded;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
