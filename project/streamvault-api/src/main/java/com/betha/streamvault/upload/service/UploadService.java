package com.betha.streamvault.upload.service;

import com.betha.streamvault.streaming.service.MinioService;
import com.betha.streamvault.upload.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final MinioService minioService;

    @Value("${minio.thumbnail-presigned-url-expiry:300}")
    private int thumbnailPresignedUrlExpirySeconds;

    public Mono<UploadResponse> uploadThumbnail(MultipartFile file, String folder) {
        return Mono.fromCallable(() -> {
            validateFile(
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    file.getSize()
            );
            return generateKey(file.getOriginalFilename(), folder);
        }).flatMap(key -> {
            try {
                byte[] data = file.getBytes();
                String contentType = file.getContentType() != null
                        ? file.getContentType()
                        : "application/octet-stream";
                minioService.uploadThumbnail(key, data, contentType);
                String url = minioService.getThumbnailPresignedUrl(key, Duration.ofSeconds(thumbnailPresignedUrlExpirySeconds));
                return Mono.just(UploadResponse.builder()
                        .key(key)
                        .url(url)
                        .filename(file.getOriginalFilename())
                        .contentType(contentType)
                        .size(file.getSize())
                        .uploadedAt(Instant.now())
                        .build());
            } catch (Exception e) {
                log.error("Failed to upload thumbnail: {}", file.getOriginalFilename(), e);
                return Mono.error(new RuntimeException("Failed to upload thumbnail", e));
            }
        });
    }

    private void validateFile(String contentType, long size) {
        if (size > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + ALLOWED_CONTENT_TYPES);
        }
    }

    private String generateKey(String filename, String folder) {
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex);
        }
        return String.format("thumbnails/%s/%s%s", folder, UUID.randomUUID(), extension);
    }
}
