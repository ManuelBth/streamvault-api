package com.betha.streamvault.admin.service;

import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService Tests")
class AdminUserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    private AdminUserService adminUserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userJpaRepository);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();
    }

    @Test
    @DisplayName("getUserById - Should return user by ID")
    void getUserById_Success() {
        when(userJpaRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        AdminUserResponse response = adminUserService.getUserById(testUser.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testUser.getId());
        assertThat(response.getEmail()).isEqualTo("test@streamvault.com");
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("getUserById - Should map user fields correctly")
    void getUserById_FieldsMapped() {
        User userWithAllFields = User.builder()
                .id(UUID.randomUUID())
                .email("admin@streamvault.com")
                .name("Admin User")
                .role(UserRole.ROLE_ADMIN)
                .isVerified(true)
                .build();

        when(userJpaRepository.findById(userWithAllFields.getId())).thenReturn(Optional.of(userWithAllFields));

        AdminUserResponse response = adminUserService.getUserById(userWithAllFields.getId());

        assertThat(response.getEmail()).isEqualTo("admin@streamvault.com");
        assertThat(response.getName()).isEqualTo("Admin User");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(response.getIsVerified()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
    }
}
