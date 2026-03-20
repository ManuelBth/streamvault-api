package com.betha.streamvault.admin.service;

import com.betha.streamvault.admin.dto.AdminUserListResponse;
import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneId;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final DatabaseClient databaseClient;

    public Mono<AdminUserListResponse> getAllUsers(int page, int size) {
        int offset = page * size;

        Mono<Long> countMono = databaseClient.sql("SELECT COUNT(*) FROM users")
                .map((row, metadata) -> row.get(0, Long.class))
                .one();

        Mono<AdminUserListResponse> usersMono = databaseClient.sql("SELECT id, email, name, role, is_verified, created_at FROM users ORDER BY created_at DESC LIMIT :size OFFSET :offset")
                .bind("size", size)
                .bind("offset", offset)
                .map((row, metadata) -> AdminUserResponse.builder()
                        .id(row.get("id", java.util.UUID.class))
                        .email(row.get("email", String.class))
                        .name(row.get("name", String.class))
                        .role(row.get("role", String.class))
                        .isVerified(row.get("is_verified", Boolean.class))
                        .createdAt(row.get("created_at", java.time.Instant.class) != null
                                ? row.get("created_at", java.time.Instant.class).atZone(ZoneId.systemDefault()).toLocalDateTime()
                                : null)
                        .build())
                .all()
                .collectList()
                .map(users -> AdminUserListResponse.builder()
                        .users(users)
                        .total(0L)
                        .page(page)
                        .size(size)
                        .build());

        return Mono.zip(countMono, usersMono)
                .map(tuple -> {
                    AdminUserListResponse response = tuple.getT2();
                    response.setTotal(tuple.getT1());
                    log.info("Admin retrieved {} users (page={}, size={})", response.getUsers().size(), page, size);
                    return response;
                });
    }

    public Mono<AdminUserResponse> getUserById(java.util.UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Usuario no encontrado")))
                .map(this::toResponse)
                .doOnSuccess(user -> log.info("Admin retrieved user: {}", userId));
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}
