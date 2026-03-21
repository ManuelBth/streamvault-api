package com.betha.streamvault.catalog.controller;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.model.ContentStatus;
import com.betha.streamvault.catalog.model.ContentType;
import com.betha.streamvault.catalog.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogController Tests")
class CatalogControllerTest {

    @Mock
    private CatalogService catalogService;

    private CatalogController catalogController;

    private ContentResponse testContentResponse;
    private SeasonResponse testSeasonResponse;
    private EpisodeResponse testEpisodeResponse;
    private GenreResponse testGenreResponse;

    @BeforeEach
    void setUp() {
        catalogController = new CatalogController(catalogService);

        testContentResponse = ContentResponse.builder()
                .id(UUID.randomUUID())
                .title("Test Movie")
                .description("A test movie description")
                .type(ContentType.MOVIE)
                .releaseYear(2024)
                .rating("PG-13")
                .thumbnailKey("thumbnails/test.jpg")
                .minioKey("videos/test.mp4")
                .status(ContentStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .build();

        testSeasonResponse = SeasonResponse.builder()
                .id(UUID.randomUUID())
                .contentId(testContentResponse.getId())
                .seasonNumber(1)
                .build();

        testEpisodeResponse = EpisodeResponse.builder()
                .id(UUID.randomUUID())
                .seasonId(testSeasonResponse.getId())
                .episodeNumber(1)
                .title("Episode 1")
                .description("First episode")
                .minioKey("videos/episode1.mp4")
                .durationSec(3600)
                .build();

        testGenreResponse = GenreResponse.builder()
                .id(UUID.randomUUID())
                .name("Action")
                .build();
    }

    @Test
    @DisplayName("GET /catalog - Should return content list")
    void getAllContent_Success() {
        PagedResponse<ContentResponse> pagedResponse = PagedResponse.<ContentResponse>builder()
                .content(List.of(testContentResponse))
                .totalElements(1L)
                .page(0)
                .size(20)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(catalogService.getAllContent(0, 20)).thenReturn(Mono.just(pagedResponse));

        Mono<ResponseEntity<PagedResponse<ContentResponse>>> result = catalogController.getAllContent(0, 20);

        ResponseEntity<PagedResponse<ContentResponse>> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(1, entity.getBody().getContent().size());
        assertEquals("Test Movie", entity.getBody().getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("GET /catalog/{id} - Should return content by ID")
    void getContentById_Success() {
        UUID contentId = testContentResponse.getId();
        when(catalogService.getContentById(contentId)).thenReturn(Mono.just(testContentResponse));

        Mono<ResponseEntity<ContentResponse>> result = catalogController.getContentById(contentId);

        ResponseEntity<ContentResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("Test Movie", entity.getBody().getTitle());
    }

    @Test
    @DisplayName("GET /catalog/search - Should return search results")
    void searchContent_Success() {
        PagedResponse<ContentResponse> pagedResponse = PagedResponse.<ContentResponse>builder()
                .content(List.of(testContentResponse))
                .totalElements(1L)
                .page(0)
                .size(20)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(catalogService.searchContent("Test", 0, 20)).thenReturn(Mono.just(pagedResponse));

        Mono<ResponseEntity<PagedResponse<ContentResponse>>> result = catalogController.searchContent("Test", 0, 20);

        ResponseEntity<PagedResponse<ContentResponse>> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(1, entity.getBody().getContent().size());
    }

    @Test
    @DisplayName("GET /catalog/{id}/seasons - Should return seasons for content")
    void getSeasonsByContentId_Success() {
        UUID contentId = testContentResponse.getId();
        when(catalogService.getSeasonsByContentId(contentId)).thenReturn(Flux.just(testSeasonResponse));

        Mono<ResponseEntity<Flux<SeasonResponse>>> result = catalogController.getSeasonsByContentId(contentId);

        ResponseEntity<Flux<SeasonResponse>> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("GET /catalog/seasons/{seasonId}/episodes - Should return episodes for season")
    void getEpisodesBySeasonId_Success() {
        UUID seasonId = testSeasonResponse.getId();
        when(catalogService.getEpisodesBySeasonId(seasonId)).thenReturn(Flux.just(testEpisodeResponse));

        Mono<ResponseEntity<Flux<EpisodeResponse>>> result = catalogController.getEpisodesBySeasonId(seasonId);

        ResponseEntity<Flux<EpisodeResponse>> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("GET /catalog/genres - Should return all genres")
    void getAllGenres_Success() {
        when(catalogService.getAllGenres()).thenReturn(Flux.just(testGenreResponse));

        ResponseEntity<Flux<GenreResponse>> entity = catalogController.getAllGenres();

        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("POST /catalog - Should create content")
    void createContent_Success() {
        ContentRequest request = new ContentRequest();
        request.setTitle("New Movie");
        request.setDescription("New description");
        request.setType(ContentType.MOVIE);

        when(catalogService.createContent(request, "admin@test.com")).thenReturn(Mono.just(testContentResponse));

        Mono<ResponseEntity<ContentResponse>> result = catalogController.createContent("admin@test.com", request);

        ResponseEntity<ContentResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("PUT /catalog/{id} - Should update content")
    void updateContent_Success() {
        UUID contentId = testContentResponse.getId();
        ContentRequest request = new ContentRequest();
        request.setTitle("Updated Title");

        when(catalogService.updateContent(contentId, request)).thenReturn(Mono.just(testContentResponse));

        Mono<ResponseEntity<ContentResponse>> result = catalogController.updateContent(contentId, request);

        ResponseEntity<ContentResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /catalog/{id} - Should delete content")
    void deleteContent_Success() {
        UUID contentId = testContentResponse.getId();
        when(catalogService.deleteContent(contentId)).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> result = catalogController.deleteContent(contentId);

        ResponseEntity<Void> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    }
}
