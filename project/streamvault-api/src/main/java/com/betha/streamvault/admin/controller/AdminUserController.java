package com.betha.streamvault.admin.controller;

import com.betha.streamvault.admin.dto.AdminUserListResponse;
import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Mono<ResponseEntity<AdminUserListResponse>> getAllUsers(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Admin {} requesting user list (page={}, size={})", email, page, size);
        return adminUserService.getAllUsers(page, size)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AdminUserResponse>> getUserById(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {
        log.info("Admin {} requesting user details: {}", email, id);
        return adminUserService.getUserById(id)
                .map(ResponseEntity::ok);
    }
}
