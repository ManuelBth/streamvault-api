package com.betha.streamvault.streaming.service;

import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.ContentType;
import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.model.EpisodeStatus;
import com.betha.streamvault.catalog.model.Season;
import com.betha.streamvault.catalog.repository.ContentJpaRepository;
import com.betha.streamvault.catalog.repository.EpisodeJpaRepository;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.streaming.dto.StreamResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.model.SubscriptionPlan;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.SubscriptionJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreamService Tests")
class StreamServiceTest {

    @Mock
    private ContentJpaRepository contentJpaRepository;

    @Mock
    private EpisodeJpaRepository episodeJpaRepository;

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private MinioService minioService;

    private StreamService streamService;

    private User testUser;
    private Content testMovie;
    private Episode testEpisode;
    private Subscription testSubscription;
    private Season testSeason;

    @BeforeEach
    void setUp() {
        streamService = new StreamService(
                contentJpaRepository,
                episodeJpaRepository,
                subscriptionJpaRepository,
                userJpaRepository,
                minioService
        );
        ReflectionTestUtils.setField(streamService, "presignedExpirySeconds", 7200);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();

        // Create a movie content
        testMovie = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .type(ContentType.MOVIE)
                .minioBaseKey("videos/movie.mp4")
                .build();

        // Create season with content
        testSeason = Season.builder()
                .id(UUID.randomUUID())
                .seasonNumber(1)
                .content(testMovie)
                .build();

        testEpisode = Episode.builder()
                .id(UUID.randomUUID())
                .season(testSeason)
                .episodeNumber(1)
                .title("Episode 1")
                .minioKey("videos/episode1.mp4")
                .status(EpisodeStatus.READY)
                .build();

        testSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getStreamUrl - Should return stream URL for movie with active subscription")
    void getStreamUrl_Success() {
        // Given - movie content exists
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(contentJpaRepository.findById(testMovie.getId())).thenReturn(Optional.of(testMovie));
        when(minioService.getPresignedUrl(anyString(), any())).thenReturn("https://minio.example.com/presigned-url");

        // When
        StreamResponse result = streamService.getStreamUrl(testMovie.getId(), testUser.getEmail());

        // Then
        assertThat(result.getUrl()).isEqualTo("https://minio.example.com/presigned-url");
        assertThat(result.getExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("getStreamUrl - Should throw exception when no subscription")
    void getStreamUrl_NoSubscription() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getStreamUrl(testMovie.getId(), testUser.getEmail()))
                .isInstanceOf(StreamService.SubscriptionNotActiveException.class);
    }

    @Test
    @DisplayName("getStreamUrl - Should throw exception when expired subscription")
    void getStreamUrl_ExpiredSubscription() {
        Subscription expiredSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .active(true)
                .build();

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(expiredSubscription));

        assertThatThrownBy(() -> streamService.getStreamUrl(testMovie.getId(), testUser.getEmail()))
                .isInstanceOf(StreamService.SubscriptionNotActiveException.class);
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when content not found")
    void getStreamUrl_ContentNotFound() {
        UUID randomId = UUID.randomUUID();
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(contentJpaRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getStreamUrl(randomId, testUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when user not found")
    void getStreamUrl_UserNotFound() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getStreamUrl(testMovie.getId(), "notfound@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException when no video available")
    void getStreamUrl_NoVideoAvailable() {
        Content movieWithoutVideo = Content.builder()
                .id(UUID.randomUUID())
                .title("Movie Without Video")
                .type(ContentType.MOVIE)
                .minioBaseKey(null)
                .build();

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(contentJpaRepository.findById(movieWithoutVideo.getId())).thenReturn(Optional.of(movieWithoutVideo));

        assertThatThrownBy(() -> streamService.getStreamUrl(movieWithoutVideo.getId(), testUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getStreamUrl - Should throw ResourceNotFoundException for series without episode specified")
    void getStreamUrl_SeriesRequiresEpisode() {
        Content testSeries = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Series")
                .type(ContentType.SERIES)
                .build();

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(contentJpaRepository.findById(testSeries.getId())).thenReturn(Optional.of(testSeries));

        assertThatThrownBy(() -> streamService.getStreamUrl(testSeries.getId(), testUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should return stream URL for episode")
    void getEpisodeStreamUrl_Success() {
        // Setup episode with proper season -> content relationship
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(episodeJpaRepository.findById(testEpisode.getId())).thenReturn(Optional.of(testEpisode));
        when(minioService.getPresignedUrl(anyString(), any())).thenReturn("https://minio.example.com/presigned-url");

        StreamResponse result = streamService.getEpisodeStreamUrl(testMovie.getId(), testEpisode.getId(), testUser.getEmail());

        assertThat(result.getUrl()).isEqualTo("https://minio.example.com/presigned-url");
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should throw exception when no subscription")
    void getEpisodeStreamUrl_NoSubscription() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getEpisodeStreamUrl(testMovie.getId(), testEpisode.getId(), testUser.getEmail()))
                .isInstanceOf(StreamService.SubscriptionNotActiveException.class);
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should throw ResourceNotFoundException when episode not found")
    void getEpisodeStreamUrl_EpisodeNotFound() {
        UUID randomId = UUID.randomUUID();
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(episodeJpaRepository.findById(randomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> streamService.getEpisodeStreamUrl(testMovie.getId(), randomId, testUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEpisodeStreamUrl - Should throw ResourceNotFoundException when episode doesn't belong to content")
    void getEpisodeStreamUrl_EpisodeNotBelongToContent() {
        // Create a different content and episode
        Content otherContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Other Series")
                .type(ContentType.SERIES)
                .build();
        
        Season otherSeason = Season.builder()
                .id(UUID.randomUUID())
                .seasonNumber(1)
                .content(otherContent)
                .build();
        
        Episode otherEpisode = Episode.builder()
                .id(UUID.randomUUID())
                .season(otherSeason)
                .episodeNumber(1)
                .title("Other Episode")
                .minioKey("videos/other.mp4")
                .status(EpisodeStatus.READY)
                .build();

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(testSubscription));
        when(episodeJpaRepository.findById(otherEpisode.getId())).thenReturn(Optional.of(otherEpisode));

        assertThatThrownBy(() -> streamService.getEpisodeStreamUrl(testMovie.getId(), otherEpisode.getId(), testUser.getEmail()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
