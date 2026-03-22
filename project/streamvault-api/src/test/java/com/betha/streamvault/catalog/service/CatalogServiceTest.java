package com.betha.streamvault.catalog.service;

import com.betha.streamvault.catalog.dto.ContentRequest;
import com.betha.streamvault.catalog.dto.ContentResponse;
import com.betha.streamvault.catalog.model.ContentStatus;
import com.betha.streamvault.catalog.model.ContentType;
import com.betha.streamvault.catalog.model.EpisodeStatus;
import com.betha.streamvault.catalog.dto.EpisodeResponse;
import com.betha.streamvault.catalog.dto.GenreResponse;
import com.betha.streamvault.catalog.dto.PagedResponse;
import com.betha.streamvault.catalog.dto.SeasonResponse;
import com.betha.streamvault.catalog.model.Content;
import com.betha.streamvault.catalog.model.Episode;
import com.betha.streamvault.catalog.model.Genre;
import com.betha.streamvault.catalog.model.Season;
import com.betha.streamvault.catalog.repository.ContentJpaRepository;
import com.betha.streamvault.catalog.repository.EpisodeJpaRepository;
import com.betha.streamvault.catalog.repository.GenreJpaRepository;
import com.betha.streamvault.catalog.repository.SeasonJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService Tests")
class CatalogServiceTest {

    @Mock
    private ContentJpaRepository contentJpaRepository;

    @Mock
    private SeasonJpaRepository seasonJpaRepository;

    @Mock
    private EpisodeJpaRepository episodeJpaRepository;

    @Mock
    private GenreJpaRepository genreJpaRepository;

    private CatalogService catalogService;

    private Content testContent;
    private Season testSeason;
    private Episode testEpisode;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(contentJpaRepository, seasonJpaRepository, episodeJpaRepository, genreJpaRepository);

        testContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .description("A test movie description")
                .type(ContentType.MOVIE)
                .releaseYear(2024)
                .rating("PG-13")
                .thumbnailKey("thumbnails/test.jpg")
                .minioBaseKey("videos/test.mp4")
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
                .description("First episode")
                .minioKey("videos/episode1.mp4")
                .thumbnailKey("thumbnails/ep1.jpg")
                .durationSec(3600)
                .status(EpisodeStatus.READY)
                .build();

        testGenre = Genre.builder()
                .id(UUID.randomUUID())
                .name("Action")
                .build();
    }

    @Test
    @DisplayName("getContentById - Should return content by ID")
    void getContentById_Success() {
        when(contentJpaRepository.findById(testContent.getId())).thenReturn(Optional.of(testContent));

        ContentResponse response = catalogService.getContentById(testContent.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testContent.getId());
        assertThat(response.getTitle()).isEqualTo("Test Movie");
    }

    @Test
    @DisplayName("getSeasonsByContentId - Should return seasons for content")
    void getSeasonsByContentId_Success() {
        when(contentJpaRepository.existsById(testContent.getId())).thenReturn(true);
        when(seasonJpaRepository.findByContentOrderBySeasonNumberAsc(testContent))
                .thenReturn(List.of(testSeason));

        List<SeasonResponse> seasons = catalogService.getSeasonsByContentId(testContent.getId());

        assertThat(seasons).hasSize(1);
        assertThat(seasons.get(0).getId()).isEqualTo(testSeason.getId());
        assertThat(seasons.get(0).getSeasonNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("getEpisodesBySeasonId - Should return episodes for season")
    void getEpisodesBySeasonId_Success() {
        when(seasonJpaRepository.existsById(testSeason.getId())).thenReturn(true);
        when(episodeJpaRepository.findBySeasonOrderByEpisodeNumberAsc(testSeason))
                .thenReturn(List.of(testEpisode));

        List<EpisodeResponse> episodes = catalogService.getEpisodesBySeasonId(testSeason.getId());

        assertThat(episodes).hasSize(1);
        assertThat(episodes.get(0).getId()).isEqualTo(testEpisode.getId());
        assertThat(episodes.get(0).getTitle()).isEqualTo("Episode 1");
    }

    @Test
    @DisplayName("getAllGenres - Should return all genres")
    void getAllGenres_Success() {
        when(genreJpaRepository.findAllByOrderByNameAsc()).thenReturn(List.of(testGenre));

        List<GenreResponse> genres = catalogService.getAllGenres();

        assertThat(genres).hasSize(1);
        assertThat(genres.get(0).getName()).isEqualTo("Action");
    }
}
