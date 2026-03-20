package com.betha.streamvault.catalog.controller;

import com.betha.streamvault.catalog.dto.*;
import com.betha.streamvault.catalog.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping
    public Mono<ResponseEntity<PagedResponse<ContentResponse>>> getAllContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogService.getAllContent(page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ContentResponse>> getContentById(@PathVariable UUID id) {
        return catalogService.getContentById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<PagedResponse<ContentResponse>>> searchContent(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalogService.searchContent(q, page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/seasons")
    public Mono<ResponseEntity<Flux<SeasonResponse>>> getSeasonsByContentId(@PathVariable UUID id) {
        return catalogService.getSeasonsByContentId(id)
                .collectList()
                .map(content -> ResponseEntity.ok(Flux.fromIterable(content)));
    }

    @GetMapping("/seasons/{seasonId}/episodes")
    public Mono<ResponseEntity<Flux<EpisodeResponse>>> getEpisodesBySeasonId(@PathVariable UUID seasonId) {
        return catalogService.getEpisodesBySeasonId(seasonId)
                .collectList()
                .map(content -> ResponseEntity.ok(Flux.fromIterable(content)));
    }

    @GetMapping("/genres")
    public ResponseEntity<Flux<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(catalogService.getAllGenres());
    }

    @PostMapping
    public Mono<ResponseEntity<ContentResponse>> createContent(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ContentRequest request) {
        return catalogService.createContent(request, email)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ContentResponse>> updateContent(
            @PathVariable UUID id,
            @Valid @RequestBody ContentRequest request) {
        return catalogService.updateContent(id, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteContent(@PathVariable UUID id) {
        return catalogService.deleteContent(id)
                .thenReturn(ResponseEntity.noContent().<Void>build());
    }
}
