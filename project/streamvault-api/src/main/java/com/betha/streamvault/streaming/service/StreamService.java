package com.betha.streamvault.streaming.service;

import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.repository.ContentRepository;
import com.betha.streamvault.catalog.repository.EpisodeRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.repository.SubscriptionRepository;
import com.betha.streamvault.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class StreamService {

    private final ContentRepository contentRepository;
    private final EpisodeRepository episodeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Value("${minio.presigned-expiry:7200}")
    private int presignedExpirySeconds;

    public Mono<StreamResponse> getStreamUrl(UUID contentId, String userEmail) {
        return verifyActiveSubscription(userEmail)
                .flatMap(hasSubscription -> {
                    if (!hasSubscription) {
                        return Mono.error(new SubscriptionNotActiveException("Suscripción no activa"));
                    }
                    return getContentAndGenerateUrl(contentId);
                });
    }

    public Mono<StreamResponse> getEpisodeStreamUrl(UUID contentId, UUID episodeId, String userEmail) {
        return verifyActiveSubscription(userEmail)
                .flatMap(hasSubscription -> {
                    if (!hasSubscription) {
                        return Mono.error(new SubscriptionNotActiveException("Suscripción no activa"));
                    }
                    return getEpisodeAndGenerateUrl(episodeId);
                });
    }

    private Mono<Boolean> verifyActiveSubscription(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado")))
                .flatMap(user -> subscriptionRepository.findByUserIdAndActiveTrue(user.getId())
                        .map(subscription -> {
                            boolean isActive = subscription.getActive() != null &&
                                    subscription.getActive() &&
                                    subscription.getExpiresAt() != null &&
                                    subscription.getExpiresAt().isAfter(Instant.now());
                            log.debug("Subscription check for user {}: active={}, expiresAt={}",
                                    user.getEmail(), isActive, subscription.getExpiresAt());
                            return isActive;
                        })
                        .defaultIfEmpty(false));
    }

    private Mono<StreamResponse> getContentAndGenerateUrl(UUID contentId) {
        return contentRepository.findById(contentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contenido no encontrado")))
                .flatMap(content -> {
                    String minioKey = content.getMinioBaseKey();
                    if (minioKey == null || minioKey.isBlank()) {
                        return Mono.error(new ResourceNotFoundException("Video no disponible para este contenido"));
                    }
                    return generateStreamResponse(minioKey);
                });
    }

    private Mono<StreamResponse> getEpisodeAndGenerateUrl(UUID episodeId) {
        return episodeRepository.findById(episodeId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Episodio no encontrado")))
                .flatMap(episode -> {
                    String minioKey = episode.getMinioKey();
                    if (minioKey == null || minioKey.isBlank()) {
                        return Mono.error(new ResourceNotFoundException("Video no disponible para este episodio"));
                    }
                    return generateStreamResponse(minioKey);
                });
    }

    private Mono<StreamResponse> generateStreamResponse(String minioKey) {
        Duration expiry = Duration.ofSeconds(presignedExpirySeconds);
        String url = minioService.getPresignedUrl(minioKey, expiry);
        Instant expiresAt = Instant.now().plus(expiry);
        
        return Mono.just(StreamResponse.builder()
                .url(url)
                .expiresAt(expiresAt)
                .build());
    }

    public static class SubscriptionNotActiveException extends RuntimeException {
        public SubscriptionNotActiveException(String message) {
            super(message);
        }
    }
}
