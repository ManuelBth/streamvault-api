package com.betha.streamvault.history.service;

import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.model.EpisodeStatus;
import com.betha.streamvault.catalog.model.Season;
import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.ContentType;
import com.betha.streamvault.catalog.model.ContentStatus;
import com.betha.streamvault.history.dto.ProgressUpdateRequest;
import com.betha.streamvault.history.dto.WatchHistoryRequest;
import com.betha.streamvault.history.dto.WatchHistoryResponse;
import com.betha.streamvault.history.model.WatchHistory;
import com.betha.streamvault.history.repository.WatchHistoryJpaRepository;
import com.betha.streamvault.user.model.Profile;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.ProfileJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import com.betha.streamvault.catalog.repository.EpisodeJpaRepository;
import com.betha.streamvault.catalog.repository.SeasonJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryService Tests")
class HistoryServiceTest {

    @Mock
    private WatchHistoryJpaRepository watchHistoryJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private ProfileJpaRepository profileJpaRepository;

    @Mock
    private EpisodeJpaRepository episodeJpaRepository;

    @Mock
    private SeasonJpaRepository seasonJpaRepository;

    private HistoryService historyService;

    private User testUser;
    private Profile testProfile;
    private Content testContent;
    private Season testSeason;
    private Episode testEpisode;
    private WatchHistory testHistory;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(
                watchHistoryJpaRepository, 
                userJpaRepository, 
                profileJpaRepository, 
                episodeJpaRepository
        );

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();

        testProfile = Profile.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Test Profile")
                .build();

        testContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Series")
                .type(ContentType.SERIES)
                .status(ContentStatus.PUBLISHED)
                .build();

        testSeason = Season.builder()
                .id(UUID.randomUUID())
                .content(testContent)
                .seasonNumber(1)
                .build();

        testEpisode = Episode.builder()
                .id(UUID.randomUUID())
                .season(testSeason)
                .episodeNumber(1)
                .title("Episode 1")
                .minioKey("videos/episode1.mp4")
                .status(EpisodeStatus.READY)
                .build();

        testHistory = WatchHistory.builder()
                .id(UUID.randomUUID())
                .profile(testProfile)
                .episode(testEpisode)
                .progressSec(120)
                .watchedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getActiveProfileById - Should return profile for user")
    void getActiveProfileById_Success() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(userJpaRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testProfile));

        Profile result = historyService.getActiveProfileById(testProfile.getId(), testUser.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testProfile.getId());
        assertThat(result.getName()).isEqualTo("Test Profile");
    }

    @Test
    @DisplayName("getHistory - Should return watch history for user")
    void getHistory_Success() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(userJpaRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testProfile));
        when(watchHistoryJpaRepository.findByProfileOrderByWatchedAtDesc(testProfile)).thenReturn(List.of(testHistory));

        List<WatchHistoryResponse> result = historyService.getHistory(testUser.getEmail(), testProfile.getId());

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(testHistory.getId());
    }

    @Test
    @DisplayName("startTracking - Should create new history entry")
    void startTracking_NewEntry() {
        UUID newEpisodeId = UUID.randomUUID();
        Episode newEpisode = Episode.builder()
                .id(newEpisodeId)
                .season(testSeason)
                .episodeNumber(1)
                .title("New Episode")
                .minioKey("videos/new.mp4")
                .status(EpisodeStatus.READY)
                .build();

        WatchHistoryRequest request = WatchHistoryRequest.builder()
                .episodeId(newEpisodeId)
                .progressSec(0)
                .build();

        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(userJpaRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testProfile));
        when(episodeJpaRepository.findById(newEpisodeId)).thenReturn(java.util.Optional.of(newEpisode));
        when(watchHistoryJpaRepository.findByProfileAndEpisode(testProfile, newEpisode)).thenReturn(java.util.Optional.empty());

        WatchHistory newHistory = WatchHistory.builder()
                .id(UUID.randomUUID())
                .profile(testProfile)
                .episode(newEpisode)
                .progressSec(0)
                .watchedAt(Instant.now())
                .build();
        when(watchHistoryJpaRepository.save(any(WatchHistory.class))).thenReturn(newHistory);

        WatchHistoryResponse result = historyService.startTracking(testUser.getEmail(), testProfile.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.getProgressSec()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateProgress - Should update progress successfully")
    void updateProgress_Success() {
        ProgressUpdateRequest request = new ProgressUpdateRequest();
        request.setProgressSec(300);

        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(userJpaRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testProfile));
        when(watchHistoryJpaRepository.findById(testHistory.getId())).thenReturn(java.util.Optional.of(testHistory));
        when(watchHistoryJpaRepository.save(any(WatchHistory.class))).thenReturn(testHistory);

        WatchHistoryResponse result = historyService.updateProgress(
                testUser.getEmail(), 
                testProfile.getId(), 
                testHistory.getId(), 
                request
        );

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("markAsCompleted - Should mark history as completed")
    void markAsCompleted_Success() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(testUser));
        when(userJpaRepository.findById(testUser.getId())).thenReturn(java.util.Optional.of(testUser));
        when(profileJpaRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of(testProfile));
        when(watchHistoryJpaRepository.findById(testHistory.getId())).thenReturn(java.util.Optional.of(testHistory));
        when(watchHistoryJpaRepository.save(any(WatchHistory.class))).thenReturn(testHistory);

        WatchHistoryResponse result = historyService.markAsCompleted(
                testUser.getEmail(), 
                testProfile.getId(), 
                testHistory.getId()
        );

        assertThat(result).isNotNull();
    }
}
