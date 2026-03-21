package com.betha.streamvault.upload.service;

import com.betha.streamvault.streaming.service.MinioService;
import com.betha.streamvault.upload.dto.UploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadService Tests")
class UploadServiceTest {

    @Mock
    private MinioService minioService;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadService(minioService);
        ReflectionTestUtils.setField(uploadService, "thumbnailPresignedUrlExpirySeconds", 300);
    }

    @Test
    @DisplayName("uploadThumbnail - Should upload valid JPEG image successfully")
    void uploadThumbnail_Success_Jpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(minioService.uploadThumbnail(anyString(), any(byte[].class), eq("image/jpeg"))).thenReturn("thumbnails/content/test.jpg");
        when(minioService.getThumbnailPresignedUrl(anyString(), any(Duration.class))).thenReturn("https://minio.example.com/thumbnails/test.jpg?token=xyz");

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getFilename()).isEqualTo("test.jpg");
                    assertThat(response.getContentType()).isEqualTo("image/jpeg");
                    assertThat(response.getKey()).startsWith("thumbnails/content/");
                    assertThat(response.getUrl()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("uploadThumbnail - Should upload valid PNG image successfully")
    void uploadThumbnail_Success_Png() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(minioService.uploadThumbnail(anyString(), any(byte[].class), eq("image/png"))).thenReturn("thumbnails/content/test.png");
        when(minioService.getThumbnailPresignedUrl(anyString(), any(Duration.class))).thenReturn("https://minio.example.com/thumbnails/test.png?token=xyz");

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getContentType()).isEqualTo("image/png");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("uploadThumbnail - Should upload valid WebP image successfully")
    void uploadThumbnail_Success_Webp() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(minioService.uploadThumbnail(anyString(), any(byte[].class), eq("image/webp"))).thenReturn("thumbnails/content/test.webp");
        when(minioService.getThumbnailPresignedUrl(anyString(), any(Duration.class))).thenReturn("https://minio.example.com/thumbnails/test.webp?token=xyz");

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getContentType()).isEqualTo("image/webp");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("uploadThumbnail - Should upload valid GIF image successfully")
    void uploadThumbnail_Success_Gif() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(minioService.uploadThumbnail(anyString(), any(byte[].class), eq("image/gif"))).thenReturn("thumbnails/content/test.gif");
        when(minioService.getThumbnailPresignedUrl(anyString(), any(Duration.class))).thenReturn("https://minio.example.com/thumbnails/test.gif?token=xyz");

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getContentType()).isEqualTo("image/gif");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("uploadThumbnail - Should reject invalid file type")
    void uploadThumbnail_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new byte[]{1, 2, 3, 4, 5}
        );

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("Invalid file type"))
                .verify();
    }

    @Test
    @DisplayName("uploadThumbnail - Should reject file exceeding 5MB")
    void uploadThumbnail_FileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("exceeds maximum"))
                .verify();
    }

    @Test
    @DisplayName("uploadThumbnail - Should reject file with null content type")
    void uploadThumbnail_NullContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.unknown",
                null,
                new byte[]{1, 2, 3, 4, 5}
        );

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException
                        && throwable.getMessage().contains("Invalid file type"))
                .verify();
    }

    @Test
    @DisplayName("uploadThumbnail - Should reject invalid content type")
    void uploadThumbnail_InvalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[]{1, 2, 3, 4, 5}
        );

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    @DisplayName("uploadThumbnail - Should reject video file")
    void uploadThumbnail_VideoFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "video.mp4",
                "video/mp4",
                new byte[]{1, 2, 3, 4, 5}
        );

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    @DisplayName("uploadThumbnail - Should throw error when MinIO upload fails")
    void uploadThumbnail_MinioError() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        when(minioService.uploadThumbnail(anyString(), any(byte[].class), eq("image/jpeg")))
                .thenThrow(new RuntimeException("MinIO upload failed"));

        var result = uploadService.uploadThumbnail(file, "content");

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException
                        && throwable.getMessage().contains("Failed to upload thumbnail"))
                .verify();
    }
}
