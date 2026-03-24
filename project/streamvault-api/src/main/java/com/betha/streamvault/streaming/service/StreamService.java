package com.betha.streamvault.streaming.service;

import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.ContentType;
import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.repository.ContentJpaRepository;
import com.betha.streamvault.catalog.repository.EpisodeJpaRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.repository.SubscriptionJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StreamService {

    private final ContentJpaRepository contentJpaRepository;
    private final EpisodeJpaRepository episodeJpaRepository;
    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final MinioService minioService;

    @Value("${minio.presigned-expiry:7200}")
    private int presignedExpirySeconds;

    public StreamResponse getStreamUrl(UUID contentId, String userEmail) {
        verifyActiveSubscription(userEmail);
        
        Content content = contentJpaRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));
        
        if (content.getType() == ContentType.MOVIE) {
            String minioKey = content.getMinioBaseKey();
            if (minioKey == null || minioKey.isBlank()) {
                throw new ResourceNotFoundException("Video no disponible para esta película");
            }
            return generateStreamResponse(minioKey);
        }
        
        throw new ResourceNotFoundException("Para series debe especificar el episodio");
    }

    public StreamResponse getEpisodeStreamUrl(UUID contentId, UUID episodeId, String userEmail) {
        verifyActiveSubscription(userEmail);
        
        Episode episode = episodeJpaRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episodio no encontrado"));
        
        if (episode.getSeason() == null || episode.getSeason().getContent() == null ||
            !episode.getSeason().getContent().getId().equals(contentId)) {
            throw new ResourceNotFoundException("Episodio no encontrado para este contenido");
        }
        
        String minioKey = episode.getMinioKey();
        if (minioKey == null || minioKey.isBlank()) {
            throw new ResourceNotFoundException("Video no disponible para este episodio");
        }
        
        return generateStreamResponse(minioKey);
    }

    private void verifyActiveSubscription(String userEmail) {
        var userOpt = userJpaRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        
        var subscriptionOpt = subscriptionJpaRepository.findByUserId(userOpt.get().getId());
        if (subscriptionOpt.isEmpty()) {
            throw new SubscriptionNotActiveException("Suscripción no activa");
        }
        
        Subscription subscription = subscriptionOpt.get();
        boolean isActive = subscription.getActive() != null &&
                subscription.getActive() &&
                subscription.getExpiresAt() != null &&
                subscription.getExpiresAt().isAfter(Instant.now());
        
        log.debug("Subscription check for user {}: active={}, expiresAt={}",
                userOpt.get().getEmail(), isActive, subscription.getExpiresAt());
        
        if (!isActive) {
            throw new SubscriptionNotActiveException("Suscripción no activa");
        }
    }

    private StreamResponse getEpisodeAndGenerateUrl(UUID episodeId) {
        Episode episode = episodeJpaRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episodio no encontrado"));
        
        String minioKey = episode.getMinioKey();
        if (minioKey == null || minioKey.isBlank()) {
            throw new ResourceNotFoundException("Video no disponible para este episodio");
        }
        
        return generateStreamResponse(minioKey);
    }

    private StreamResponse generateStreamResponse(String minioKey) {
        Duration expiry = Duration.ofSeconds(presignedExpirySeconds);
        String url = minioService.getPresignedUrl(minioKey, expiry);
        Instant expiresAt = Instant.now().plus(expiry);
        
        return StreamResponse.builder()
                .url(url)
                .expiresAt(expiresAt)
                .build();
    }

    public static class SubscriptionNotActiveException extends RuntimeException {
        public SubscriptionNotActiveException(String message) {
            super(message);
        }
    }
}
