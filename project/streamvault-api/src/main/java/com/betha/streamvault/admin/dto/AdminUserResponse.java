package com.betha.streamvault.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse {

    private UUID id;
    private String email;
    private String name;
    private String role;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
