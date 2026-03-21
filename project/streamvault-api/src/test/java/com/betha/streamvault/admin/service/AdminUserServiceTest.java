package com.betha.streamvault.admin.service;

import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService Tests")
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DatabaseClient databaseClient;

    private AdminUserService adminUserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, databaseClient);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role("ROLE_USER")
                .isVerified(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getUserById - Should return user by ID")
    void getUserById_Success() {
        when(userRepository.findById(testUser.getId())).thenReturn(Mono.just(testUser));

        Mono<AdminUserResponse> result = adminUserService.getUserById(testUser.getId());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(testUser.getId());
                    assertThat(response.getEmail()).isEqualTo("test@streamvault.com");
                    assertThat(response.getName()).isEqualTo("Test User");
                    assertThat(response.getRole()).isEqualTo("ROLE_USER");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getUserById - Should throw ResourceNotFoundException when user not found")
    void getUserById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Mono.empty());

        Mono<AdminUserResponse> result = adminUserService.getUserById(nonExistentId);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResourceNotFoundException
                        && throwable.getMessage().equals("Usuario no encontrado"))
                .verify();
    }

    @Test
    @DisplayName("getUserById - Should map user fields correctly")
    void getUserById_FieldsMapped() {
        User userWithAllFields = User.builder()
                .id(UUID.randomUUID())
                .email("admin@streamvault.com")
                .name("Admin User")
                .role("ROLE_ADMIN")
                .isVerified(true)
                .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
                .build();

        when(userRepository.findById(userWithAllFields.getId())).thenReturn(Mono.just(userWithAllFields));

        Mono<AdminUserResponse> result = adminUserService.getUserById(userWithAllFields.getId());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getEmail()).isEqualTo("admin@streamvault.com");
                    assertThat(response.getName()).isEqualTo("Admin User");
                    assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
                    assertThat(response.getIsVerified()).isTrue();
                    assertThat(response.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }
}
