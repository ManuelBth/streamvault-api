package com.betha.streamvault.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    private String key;
    private String url;
    private String filename;
    private String contentType;
    private Long size;
    private Instant uploadedAt;
}
