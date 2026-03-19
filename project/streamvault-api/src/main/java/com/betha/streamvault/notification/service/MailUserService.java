package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.model.MailUser;
import com.betha.streamvault.notification.repository.MailUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    /**
     * Creates a mail account for a user.
     * Password is hashed using SHA256 (Dovecot compatible).
     */
    public Mono<MailUser> createMailAccount(String email, String password) {
        log.info("Creating mail account for: {}", email);

        String hashedPassword = hashPasswordSha256(password);
        Instant now = Instant.now();

        MailUser mailUser = MailUser.builder()
                .email(email)
                .password(hashedPassword)
                .maildir("streamvault.com/" + email.split("@")[0] + "/")
                .quota(0L)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return mailUserRepository.save(mailUser)
                .doOnSuccess(user -> log.info("Mail account created for: {}", email))
                .doOnError(e -> log.error("Failed to create mail account for {}: {}", email, e.getMessage()));
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
