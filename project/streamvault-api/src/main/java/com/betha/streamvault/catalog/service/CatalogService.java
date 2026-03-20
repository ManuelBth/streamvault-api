package com.betha.streamvault.catalog.service;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.model.*;
import com.betha.streamvault.catalog.repository.*;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ContentRepository contentRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final GenreRepository genreRepository;

    public Mono<PagedResponse<ContentResponse>> getAllContent(int page, int size) {
        return contentRepository.findByStatus(ContentStatus.PUBLISHED.name())
                .count()
                .flatMap(total -> contentRepository.findByStatus(ContentStatus.PUBLISHED.name())
                        .sort(Comparator.comparing(Content::getCreatedAt).reversed())
                        .skip((long) page * size)
                        .limitRequest(size)
                        .flatMap(this::toContentResponseMono)
                        .collectList()
                        .map(contentList -> PagedResponse.<ContentResponse>builder()
                                .content(contentList)
                                .page(page)
                                .size(size)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / size))
                                .first(page == 0)
                                .last(page >= Math.ceil((double) total / size) - 1)
                                .build()));
    }

    public Mono<ContentResponse> getContentById(UUID id) {
        return contentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contenido no encontrado")))
                .flatMap(this::toContentResponseMono);
    }

    public Mono<PagedResponse<ContentResponse>> searchContent(String query, int page, int size) {
        return contentRepository.findByTitleContainingIgnoreCase(query)
                .filter(c -> ContentStatus.PUBLISHED.name().equals(c.getStatus()))
                .count()
                .flatMap(total -> contentRepository.findByTitleContainingIgnoreCase(query)
                        .filter(c -> ContentStatus.PUBLISHED.name().equals(c.getStatus()))
                        .skip((long) page * size)
                        .limitRequest(size)
                        .flatMap(this::toContentResponseMono)
                        .collectList()
                        .map(contentList -> PagedResponse.<ContentResponse>builder()
                                .content(contentList)
                                .page(page)
                                .size(size)
                                .totalElements(total)
                                .totalPages((int) Math.ceil((double) total / size))
                                .first(page == 0)
                                .last(page >= Math.ceil((double) total / size) - 1)
                                .build()));
    }

    public Flux<SeasonResponse> getSeasonsByContentId(UUID contentId) {
        return contentRepository.existsById(contentId)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new ResourceNotFoundException("Contenido no encontrado"));
                    }
                    return seasonRepository.findByContentIdOrderBySeasonNumberAsc(contentId);
                })
                .map(this::toSeasonResponse);
    }

    public Flux<EpisodeResponse> getEpisodesBySeasonId(UUID seasonId) {
        return seasonRepository.existsById(seasonId)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new ResourceNotFoundException("Temporada no encontrada"));
                    }
                    return episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId);
                })
                .map(this::toEpisodeResponse);
    }

    public Flux<GenreResponse> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc()
                .map(this::toGenreResponse);
    }

    public Mono<ContentResponse> createContent(ContentRequest request, String userEmail) {
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setType(request.getType().name());
        content.setReleaseYear(request.getReleaseYear());
        content.setRating(request.getRating());
        content.setThumbnailKey(request.getThumbnailKey());
        content.setMinioBaseKey(request.getMinioKey());
        content.setStatus(request.getStatus() != null ? request.getStatus().name() : ContentStatus.DRAFT.name());
        content.setCreatedAt(Instant.now());

        return contentRepository.save(content)
                .flatMap(this::toContentResponseMono)
                .doOnSuccess(c -> log.info("Content created: {}", c.getId()));
    }

    public Mono<ContentResponse> updateContent(UUID id, ContentRequest request) {
        return contentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contenido no encontrado")))
                .flatMap(content -> {
                    if (request.getTitle() != null) {
                        content.setTitle(request.getTitle());
                    }
                    if (request.getDescription() != null) {
                        content.setDescription(request.getDescription());
                    }
                    if (request.getType() != null) {
                        content.setType(request.getType().name());
                    }
                    if (request.getReleaseYear() != null) {
                        content.setReleaseYear(request.getReleaseYear());
                    }
                    if (request.getRating() != null) {
                        content.setRating(request.getRating());
                    }
                    if (request.getThumbnailKey() != null) {
                        content.setThumbnailKey(request.getThumbnailKey());
                    }
                    if (request.getMinioKey() != null) {
                        content.setMinioBaseKey(request.getMinioKey());
                    }
                    if (request.getStatus() != null) {
                        content.setStatus(request.getStatus().name());
                    }
                    content.setUpdatedAt(Instant.now());
                    return contentRepository.save(content);
                })
                .flatMap(this::toContentResponseMono)
                .doOnSuccess(c -> log.info("Content updated: {}", c.getId()));
    }

    public Mono<Void> deleteContent(UUID id) {
        return contentRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Contenido no encontrado")))
                .flatMap(contentRepository::delete)
                .doOnSuccess(v -> log.info("Content deleted: {}", id));
    }

    private Mono<ContentResponse> toContentResponseMono(Content content) {
        return Mono.just(toContentResponse(content));
    }

    private ContentResponse toContentResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .type(content.getType() != null ? ContentType.valueOf(content.getType()) : null)
                .releaseYear(content.getReleaseYear())
                .rating(content.getRating())
                .thumbnailKey(content.getThumbnailKey())
                .minioKey(content.getMinioBaseKey())
                .status(content.getStatus() != null ? ContentStatus.valueOf(content.getStatus()) : null)
                .createdAt(content.getCreatedAt() != null ? content.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(content.getUpdatedAt() != null ? content.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private SeasonResponse toSeasonResponse(Season season) {
        return SeasonResponse.builder()
                .id(season.getId())
                .contentId(season.getContentId())
                .seasonNumber(season.getSeasonNumber())
                .build();
    }

    private EpisodeResponse toEpisodeResponse(Episode episode) {
        return EpisodeResponse.builder()
                .id(episode.getId())
                .seasonId(episode.getSeasonId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .description(episode.getDescription())
                .minioKey(episode.getMinioKey())
                .thumbnailKey(episode.getThumbnailKey())
                .durationSec(episode.getDurationSec())
                .status(episode.getStatus() != null ? EpisodeStatus.valueOf(episode.getStatus()) : null)
                .createdAt(episode.getCreatedAt() != null ? episode.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private GenreResponse toGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}
