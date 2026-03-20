package com.betha.streamvault.streaming.service;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Log4j2
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-videos:streamvault-videos}")
    private String bucketVideos;

    @Value("${minio.bucket-thumbnails:streamvault-thumbnails}")
    private String bucketThumbnails;

    public String getPresignedUrl(String objectKey, Duration expiry) {
        return getPresignedUrl(bucketVideos, objectKey, expiry);
    }

    public String getThumbnailPresignedUrl(String objectKey, Duration expiry) {
        return getPresignedUrl(bucketThumbnails, objectKey, expiry);
    }

    private String getPresignedUrl(String bucket, String objectKey, Duration expiry) {
        try {
            return minioClient.getPresignedObjectUrl(
                    io.minio.GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry((int) expiry.toSeconds())
                            .build());
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for object: {}", objectKey, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    public String uploadThumbnail(String key, byte[] data, String contentType) {
        return uploadToBucket(bucketThumbnails, key, data, contentType);
    }

    private String uploadToBucket(String bucket, String key, byte[] data, String contentType) {
        try {
            var putObjectArgs = io.minio.PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(new java.io.ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build();
            minioClient.putObject(putObjectArgs);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload to bucket: {} key: {}", bucket, key, e);
            throw new RuntimeException("Failed to upload", e);
        }
    }
}
