package com.betha.streamvault.upload.controller;

import com.betha.streamvault.upload.dto.UploadResponse;
import com.betha.streamvault.upload.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadController Tests")
class UploadControllerTest {

    @Mock
    private UploadService uploadService;

    private UploadController uploadController;

    private UploadResponse testUploadResponse;

    @BeforeEach
    void setUp() {
        uploadController = new UploadController(uploadService);

        testUploadResponse = UploadResponse.builder()
                .key("thumbnails/content/test.jpg")
                .url("https://minio.example.com/thumbnails/test.jpg?token=xyz")
                .filename("test.jpg")
                .contentType("image/jpeg")
                .size(5L)
                .uploadedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("POST /admin/upload/thumbnail - Should upload thumbnail successfully")
    void uploadThumbnail_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(uploadService.uploadThumbnail(any(), any())).thenReturn(Mono.just(testUploadResponse));

        Mono<ResponseEntity<UploadResponse>> result = uploadController.uploadThumbnail("admin@test.com", file);

        ResponseEntity<UploadResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("thumbnails/content/test.jpg", entity.getBody().getKey());
        assertEquals("test.jpg", entity.getBody().getFilename());
    }

    @Test
    @DisplayName("POST /admin/upload/thumbnail - Should return bad request on upload failure")
    void uploadThumbnail_BadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(uploadService.uploadThumbnail(any(), any())).thenReturn(Mono.error(new RuntimeException("Upload failed")));

        Mono<ResponseEntity<UploadResponse>> result = uploadController.uploadThumbnail("admin@test.com", file);

        ResponseEntity<UploadResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    @DisplayName("POST /admin/upload/thumbnail - Should return bad request on invalid file type")
    void uploadThumbnail_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(uploadService.uploadThumbnail(any(), any())).thenReturn(Mono.error(new IllegalArgumentException("Invalid file type")));

        Mono<ResponseEntity<UploadResponse>> result = uploadController.uploadThumbnail("admin@test.com", file);

        ResponseEntity<UploadResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    @DisplayName("POST /admin/upload/thumbnail - Should return bad request when file too large")
    void uploadThumbnail_FileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        when(uploadService.uploadThumbnail(any(), any())).thenReturn(Mono.error(new IllegalArgumentException("File too large")));

        Mono<ResponseEntity<UploadResponse>> result = uploadController.uploadThumbnail("admin@test.com", file);

        ResponseEntity<UploadResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    }

    @Test
    @DisplayName("POST /admin/upload/thumbnail - Should handle PNG upload")
    void uploadThumbnail_PngSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3, 4, 5}
        );

        UploadResponse pngResponse = UploadResponse.builder()
                .key("thumbnails/content/test.png")
                .url("https://minio.example.com/thumbnails/test.png?token=xyz")
                .filename("test.png")
                .contentType("image/png")
                .size(5L)
                .uploadedAt(Instant.now())
                .build();

        when(uploadService.uploadThumbnail(any(), any())).thenReturn(Mono.just(pngResponse));

        Mono<ResponseEntity<UploadResponse>> result = uploadController.uploadThumbnail("admin@test.com", file);

        ResponseEntity<UploadResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("image/png", entity.getBody().getContentType());
    }
}
