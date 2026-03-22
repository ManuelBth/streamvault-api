package com.betha.streamvault.catalog.service;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.model.*;
import com.betha.streamvault.catalog.repository.*;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ContentJpaRepository contentJpaRepository;
    private final SeasonJpaRepository seasonJpaRepository;
    private final EpisodeJpaRepository episodeJpaRepository;
    private final GenreJpaRepository genreJpaRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ContentResponse> getAllContent(int page, int size) {
        Page<Content> contentPage = contentJpaRepository.findAll(
                PageRequest.of(page, size)
        );

        List<ContentResponse> contentList = contentPage.getContent().stream()
                .filter(c -> c.getStatus() == ContentStatus.PUBLISHED)
                .sorted(Comparator.comparing(Content::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toContentResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ContentResponse>builder()
                .content(contentList)
                .page(page)
                .size(size)
                .totalElements(contentPage.getTotalElements())
                .totalPages(contentPage.getTotalPages())
                .first(page == 0)
                .last(page >= contentPage.getTotalPages() - 1)
                .build();
    }

    @Transactional(readOnly = true)
    public ContentResponse getContentById(UUID id) {
        Content content = contentJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));
        return toContentResponse(content);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ContentResponse> searchContent(String query, int page, int size) {
        Page<Content> contentPage = contentJpaRepository.findByTitleContainingIgnoreCase(
                query, PageRequest.of(page, size)
        );

        List<ContentResponse> contentList = contentPage.getContent().stream()
                .filter(c -> c.getStatus() == ContentStatus.PUBLISHED)
                .map(this::toContentResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ContentResponse>builder()
                .content(contentList)
                .page(page)
                .size(size)
                .totalElements(contentPage.getTotalElements())
                .totalPages(contentPage.getTotalPages())
                .first(page == 0)
                .last(page >= contentPage.getTotalPages() - 1)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SeasonResponse> getSeasonsByContentId(UUID contentId) {
        Content content = contentJpaRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));
        return seasonJpaRepository.findByContentOrderBySeasonNumberAsc(content).stream()
                .map(this::toSeasonResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EpisodeResponse> getEpisodesBySeasonId(UUID seasonId) {
        Season season = seasonJpaRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Temporada no encontrada"));
        return episodeJpaRepository.findBySeasonOrderByEpisodeNumberAsc(season).stream()
                .map(this::toEpisodeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreJpaRepository.findAllByOrderByNameAsc().stream()
                .map(this::toGenreResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContentResponse createContent(ContentRequest request, String userEmail) {
        Content content = new Content();
        content.setTitle(request.getTitle());
        content.setDescription(request.getDescription());
        content.setType(request.getType());
        content.setReleaseYear(request.getReleaseYear());
        content.setRating(request.getRating());
        content.setThumbnailKey(request.getThumbnailKey());
        content.setMinioBaseKey(request.getMinioKey());
        content.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        content.setCreatedAt(Instant.now());

        Content saved = contentJpaRepository.save(content);
        log.info("Content created: {}", saved.getId());
        return toContentResponse(saved);
    }

    @Transactional
    public ContentResponse updateContent(UUID id, ContentRequest request) {
        Content content = contentJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));

        if (request.getTitle() != null) content.setTitle(request.getTitle());
        if (request.getDescription() != null) content.setDescription(request.getDescription());
        if (request.getType() != null) content.setType(request.getType());
        if (request.getReleaseYear() != null) content.setReleaseYear(request.getReleaseYear());
        if (request.getRating() != null) content.setRating(request.getRating());
        if (request.getThumbnailKey() != null) content.setThumbnailKey(request.getThumbnailKey());
        if (request.getMinioKey() != null) content.setMinioBaseKey(request.getMinioKey());
        if (request.getStatus() != null) content.setStatus(request.getStatus());
        content.setUpdatedAt(Instant.now());

        Content saved = contentJpaRepository.save(content);
        log.info("Content updated: {}", saved.getId());
        return toContentResponse(saved);
    }

    @Transactional
    public void deleteContent(UUID id) {
        Content content = contentJpaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado"));
        contentJpaRepository.delete(content);
        log.info("Content deleted: {}", id);
    }

    private ContentResponse toContentResponse(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .type(content.getType())
                .releaseYear(content.getReleaseYear())
                .rating(content.getRating())
                .thumbnailKey(content.getThumbnailKey())
                .minioKey(content.getMinioBaseKey())
                .status(content.getStatus())
                .createdAt(content.getCreatedAt() != null ? content.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(content.getUpdatedAt() != null ? content.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private SeasonResponse toSeasonResponse(Season season) {
        return SeasonResponse.builder()
                .id(season.getId())
                .contentId(season.getContent().getId())
                .seasonNumber(season.getSeasonNumber())
                .build();
    }

    private EpisodeResponse toEpisodeResponse(Episode episode) {
        return EpisodeResponse.builder()
                .id(episode.getId())
                .seasonId(episode.getSeason().getId())
                .episodeNumber(episode.getEpisodeNumber())
                .title(episode.getTitle())
                .description(episode.getDescription())
                .minioKey(episode.getMinioKey())
                .thumbnailKey(episode.getThumbnailKey())
                .durationSec(episode.getDurationSec())
                .status(episode.getStatus())
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
