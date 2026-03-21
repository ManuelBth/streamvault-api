package com.betha.streamvault.catalog.service;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.model.*;
import com.betha.streamvault.catalog.repository.*;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService Tests")
class CatalogServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private GenreRepository genreRepository;

    private CatalogService catalogService;

    private Content testContent;
    private Season testSeason;
    private Episode testEpisode;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        catalogService = new CatalogService(contentRepository, seasonRepository, episodeRepository, genreRepository);

        testContent = Content.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .description("A test movie description")
                .type("MOVIE")
                .releaseYear(2024)
                .rating("PG-13")
                .thumbnailKey("thumbnails/test.jpg")
                .minioBaseKey("videos/test.mp4")
                .status("PUBLISHED")
                .createdAt(Instant.now())
                .build();

        testSeason = Season.builder()
                .id(UUID.randomUUID())
                .contentId(testContent.getId())
                .seasonNumber(1)
                .build();

        testEpisode = Episode.builder()
                .id(UUID.randomUUID())
                .seasonId(testSeason.getId())
                .episodeNumber(1)
                .title("Episode 1")
                .description("First episode")
                .minioKey("videos/episode1.mp4")
                .thumbnailKey("thumbnails/ep1.jpg")
                .durationSec(3600)
                .status("READY")
                .createdAt(Instant.now())
                .build();

        testGenre = Genre.builder()
                .id(UUID.randomUUID())
                .name("Action")
                .build();
    }

    @Test
    @DisplayName("getContentById - Should return content by ID")
    void getContentById_Success() {
        when(contentRepository.findById(any(UUID.class))).thenReturn(Mono.just(testContent));

        Mono<ContentResponse> result = catalogService.getContentById(testContent.getId());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(testContent.getId());
                    assertThat(response.getTitle()).isEqualTo("Test Movie");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getContentById - Should throw ResourceNotFoundException when not found")
    void getContentById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(contentRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<ContentResponse> result = catalogService.getContentById(nonExistentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Contenido no encontrado"))
                .verify();
    }

    @Test
    @DisplayName("createContent - Should create new content")
    void createContent_Success() {
        ContentRequest request = new ContentRequest();
        request.setTitle("New Movie");
        request.setDescription("New description");
        request.setType(ContentType.MOVIE);
        request.setReleaseYear(2024);

        when(contentRepository.save(any(Content.class))).thenReturn(Mono.just(testContent));

        Mono<ContentResponse> result = catalogService.createContent(request, "admin@test.com");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateContent - Should update existing content")
    void updateContent_Success() {
        ContentRequest request = new ContentRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description");

        when(contentRepository.findById(testContent.getId())).thenReturn(Mono.just(testContent));
        when(contentRepository.save(any(Content.class))).thenReturn(Mono.just(testContent));

        Mono<ContentResponse> result = catalogService.updateContent(testContent.getId(), request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateContent - Should throw ResourceNotFoundException when content not found")
    void updateContent_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        ContentRequest request = new ContentRequest();
        request.setTitle("Updated Title");

        when(contentRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<ContentResponse> result = catalogService.updateContent(nonExistentId, request);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Contenido no encontrado"))
                .verify();
    }

    @Test
    @DisplayName("getSeasonsByContentId - Should return seasons for content")
    void getSeasonsByContentId_Success() {
        when(contentRepository.existsById(testContent.getId())).thenReturn(Mono.just(true));
        when(seasonRepository.findByContentIdOrderBySeasonNumberAsc(testContent.getId()))
                .thenReturn(Flux.just(testSeason));

        Flux<SeasonResponse> result = catalogService.getSeasonsByContentId(testContent.getId());

        StepVerifier.create(result)
                .assertNext(season -> {
                    assertThat(season.getId()).isEqualTo(testSeason.getId());
                    assertThat(season.getSeasonNumber()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getEpisodesBySeasonId - Should return episodes for season")
    void getEpisodesBySeasonId_Success() {
        when(seasonRepository.existsById(testSeason.getId())).thenReturn(Mono.just(true));
        when(episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(testSeason.getId()))
                .thenReturn(Flux.just(testEpisode));

        Flux<EpisodeResponse> result = catalogService.getEpisodesBySeasonId(testSeason.getId());

        StepVerifier.create(result)
                .assertNext(episode -> {
                    assertThat(episode.getId()).isEqualTo(testEpisode.getId());
                    assertThat(episode.getTitle()).isEqualTo("Episode 1");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getAllGenres - Should return all genres")
    void getAllGenres_Success() {
        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(Flux.just(testGenre));

        Flux<GenreResponse> result = catalogService.getAllGenres();

        StepVerifier.create(result)
                .assertNext(genre -> {
                    assertThat(genre.getName()).isEqualTo("Action");
                })
                .verifyComplete();
    }
}
