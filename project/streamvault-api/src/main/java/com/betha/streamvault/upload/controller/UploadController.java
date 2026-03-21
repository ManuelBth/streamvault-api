package com.betha.streamvault.upload.controller;

import com.betha.streamvault.upload.dto.UploadResponse;
import com.betha.streamvault.upload.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@RestController
@RequestMapping("/api/v1/admin/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/thumbnail")
    public ResponseEntity<UploadResponse> uploadThumbnail(
            @AuthenticationPrincipal String email,
            @RequestParam("file") MultipartFile file) {
        log.info("Thumbnail upload requested by user: {}", email);
        try {
            UploadResponse response = uploadService.uploadThumbnail(file, "content");
            log.info("Thumbnail uploaded successfully: {}", response.getKey());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Thumbnail upload failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
