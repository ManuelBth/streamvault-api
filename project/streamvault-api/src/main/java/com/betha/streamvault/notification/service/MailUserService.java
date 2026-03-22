package com.betha.streamvault.notification.service;

import com.betha.streamvault.notification.model.MailUser;
import com.betha.streamvault.notification.repository.MailUserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@Log4j2
@Service
@RequiredArgsConstructor
public class MailUserService {

    private final MailUserJpaRepository mailUserJpaRepository;

    @Transactional
    public MailUser createMailAccount(String email, String password) {
        log.info("Creating mail account for: {}", email);

        String hashedPassword = hashPasswordSha256(password);
        String maildir = "streamvault.com/" + email.split("@")[0] + "/";

        MailUser mailUser = MailUser.builder()
                .email(email)
                .password(hashedPassword)
                .maildir(maildir)
                .quota(0L)
                .active(true)
                .build();

        try {
            MailUser saved = mailUserJpaRepository.save(mailUser);
            log.info("Mail account created for: {}", email);
            return saved;
        } catch (Exception e) {
            log.error("Failed to create mail account for {}: {}", email, e.getMessage());
            return mailUser;
        }
    }

    private String hashPasswordSha256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);
            return "{SHA256}" + encoded;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
