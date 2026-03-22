package com.betha.streamvault.catalog.controller;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<PagedResponse<ContentResponse>> getAllContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(catalogService.getAllContent(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentResponse> getContentById(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getContentById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ContentResponse>> searchContent(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(catalogService.searchContent(q, page, size));
    }

    @GetMapping("/{id}/seasons")
    public ResponseEntity<List<SeasonResponse>> getSeasonsByContentId(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getSeasonsByContentId(id));
    }

    @GetMapping("/seasons/{seasonId}/episodes")
    public ResponseEntity<List<EpisodeResponse>> getEpisodesBySeasonId(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(catalogService.getEpisodesBySeasonId(seasonId));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(catalogService.getAllGenres());
    }

    @PostMapping
    public ResponseEntity<ContentResponse> createContent(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createContent(request, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentResponse> updateContent(
            @PathVariable UUID id,
            @Valid @RequestBody ContentRequest request) {
        return ResponseEntity.ok(catalogService.updateContent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID id) {
        catalogService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
