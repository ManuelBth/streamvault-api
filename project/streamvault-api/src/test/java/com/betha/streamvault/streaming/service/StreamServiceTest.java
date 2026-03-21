package com.betha.streamvault.streaming.service;

import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.repository.ContentRepository;
import com.betha.streamvault.catalog.repository.EpisodeRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.SubscriptionRepository;
import com.betha.streamvault.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreamService Tests")
class StreamServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MinioService minioService;

    private StreamService streamService;

    private User testUser;
    private Content testContent;
    private Episode testEpisode;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        streamService = new StreamService(
                contentRepository,
                episodeRepository,
                subscriptionRepository,
                userRepository,
                minioService
        );
        ReflectionTestUtils.setField(streamService, "presignedExpirySeconds", 7200);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role("ROLE_USER")
                .isVerified(true)
                .createdAt(Instant.now())
                .build();

        testContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .type("MOVIE")
                .minioBaseKey("videos/test.mp4")
                .status("PUBLISHED")
                .createdAt(Instant.now())
                .build();

        testEpisode = Episode.builder()
                .id(UUID.randomUUID())
                .seasonId(UUID.randomUUID())
                .episodeNumber(1)
                .title("Episode 1")
                .minioKey("videos/episode1.mp4")
                .status("READY")
                .createdAt(Instant.now())
                .build();

        testSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .plan("PREMIUM")
                .startedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getStreamUrl - Should return stream URL with active subscription")
    void getStreamUrl_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(testSubscription));
        when(contentRepository.findById(testContent.getId())).thenReturn(Mono.just(testContent));
        when(minioService.getPresignedUrl(anyString(), any())).thenReturn("https://minio.example.com/presigned-url");

        Mono<StreamResponse> result = streamService.getStreamUrl(testContent.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getUrl()).isEqualTo("https://minio.example.com/presigned-url");
                    assertThat(response.getExpiresAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw exception when no subscription")
    void getStreamUrl_NoSubscription() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.empty());

        Mono<StreamResponse> result = streamService.getStreamUrl(testContent.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof StreamService.SubscriptionNotActiveException)
                .verify();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw exception when expired subscription")
    void getStreamUrl_ExpiredSubscription() {
        Subscription expiredSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .userId(testUser.getId())
                .plan("PREMIUM")
                .startedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .active(true)
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(expiredSubscription));

        Mono<StreamResponse> result = streamService.getStreamUrl(testContent.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof StreamService.SubscriptionNotActiveException)
                .verify();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when content not found")
    void getStreamUrl_ContentNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(testSubscription));
        when(contentRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        Mono<StreamResponse> result = streamService.getStreamUrl(UUID.randomUUID(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when user not found")
    void getStreamUrl_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        Mono<StreamResponse> result = streamService.getStreamUrl(testContent.getId(), "notfound@test.com");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when no video available")
    void getStreamUrl_NoVideoAvailable() {
        Content contentWithoutVideo = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .type("MOVIE")
                .minioBaseKey(null)
                .status("PUBLISHED")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(testSubscription));
        when(contentRepository.findById(contentWithoutVideo.getId())).thenReturn(Mono.just(contentWithoutVideo));

        Mono<StreamResponse> result = streamService.getStreamUrl(contentWithoutVideo.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should return stream URL for episode")
    void getEpisodeStreamUrl_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(testSubscription));
        when(episodeRepository.findById(testEpisode.getId())).thenReturn(Mono.just(testEpisode));
        when(minioService.getPresignedUrl(anyString(), any())).thenReturn("https://minio.example.com/presigned-url");

        Mono<StreamResponse> result = streamService.getEpisodeStreamUrl(testContent.getId(), testEpisode.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getUrl()).isEqualTo("https://minio.example.com/presigned-url");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should throw exception when no subscription")
    void getEpisodeStreamUrl_NoSubscription() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.empty());

        Mono<StreamResponse> result = streamService.getEpisodeStreamUrl(testContent.getId(), testEpisode.getId(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof StreamService.SubscriptionNotActiveException)
                .verify();
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should throw ResourceNotFoundException when episode not found")
    void getEpisodeStreamUrl_EpisodeNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(subscriptionRepository.findByUserIdAndActiveTrue(testUser.getId())).thenReturn(Mono.just(testSubscription));
        when(episodeRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        Mono<StreamResponse> result = streamService.getEpisodeStreamUrl(testContent.getId(), UUID.randomUUID(), testUser.getEmail());

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException)
                .verify();
    }
}
