package com.betha.streamvault.admin.controller;

import com.betha.streamvault.admin.dto.AdminUserListResponse;
import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.admin.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserController Tests")
class AdminUserControllerTest {

    @Mock
    private AdminUserService adminUserService;

    private AdminUserController adminUserController;

    private AdminUserResponse testUserResponse;
    private AdminUserListResponse testUserListResponse;

    @BeforeEach
    void setUp() {
        adminUserController = new AdminUserController(adminUserService);

        testUserResponse = AdminUserResponse.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .role("ROLE_USER")
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        testUserListResponse = AdminUserListResponse.builder()
                .users(List.of(testUserResponse))
                .total(1L)
                .page(0)
                .size(20)
                .build();
    }

    @Test
    @DisplayName("GET /admin/users - Should return user list")
    void getAllUsers_Success() {
        when(adminUserService.getAllUsers(0, 20)).thenReturn(testUserListResponse);

        ResponseEntity<AdminUserListResponse> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUsers()).hasSize(1);
        assertThat(result.getBody().getTotal()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /admin/users - Should use default pagination")
    void getAllUsers_DefaultPagination() {
        when(adminUserService.getAllUsers(0, 20)).thenReturn(testUserListResponse);

        ResponseEntity<AdminUserListResponse> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
    }

    @Test
    @DisplayName("GET /admin/users - Should handle custom pagination")
    void getAllUsers_CustomPagination() {
        AdminUserListResponse paginatedResponse = AdminUserListResponse.builder()
                .users(List.of())
                .total(100L)
                .page(5)
                .size(10)
                .build();

        when(adminUserService.getAllUsers(5, 10)).thenReturn(paginatedResponse);

        ResponseEntity<AdminUserListResponse> result = adminUserController.getAllUsers("admin@test.com", 5, 10);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getPage()).isEqualTo(5);
        assertThat(result.getBody().getSize()).isEqualTo(10);
        assertThat(result.getBody().getTotal()).isEqualTo(100L);
    }

    @Test
    @DisplayName("GET /admin/users/{id} - Should return user by ID")
    void getUserById_Success() {
        UUID userId = testUserResponse.getId();
        when(adminUserService.getUserById(userId)).thenReturn(testUserResponse);

        ResponseEntity<AdminUserResponse> result = adminUserController.getUserById("admin@test.com", userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(userId);
        assertThat(result.getBody().getEmail()).isEqualTo("test@streamvault.com");
    }

    @Test
    @DisplayName("GET /admin/users - Should return empty list when no users")
    void getAllUsers_Empty() {
        AdminUserListResponse emptyResponse = AdminUserListResponse.builder()
                .users(List.of())
                .total(0L)
                .page(0)
                .size(20)
                .build();

        when(adminUserService.getAllUsers(0, 20)).thenReturn(emptyResponse);

        ResponseEntity<AdminUserListResponse> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getUsers()).isEmpty();
        assertThat(result.getBody().getTotal()).isEqualTo(0L);
    }

    @Test
    @DisplayName("GET /admin/users/{id} - Should return admin user details")
    void getUserById_AdminUser() {
        UUID adminId = UUID.randomUUID();
        AdminUserResponse adminResponse = AdminUserResponse.builder()
                .id(adminId)
                .email("admin@streamvault.com")
                .name("Admin User")
                .role("ROLE_ADMIN")
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(adminUserService.getUserById(adminId)).thenReturn(adminResponse);

        ResponseEntity<AdminUserResponse> result = adminUserController.getUserById("admin@test.com", adminId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(result.getBody().getName()).isEqualTo("Admin User");
    }

    @Test
    @DisplayName("GET /admin/users/{id} - Should return user with all fields")
    void getUserById_AllFields() {
        UUID userId = UUID.randomUUID();
        AdminUserResponse fullUserResponse = AdminUserResponse.builder()
                .id(userId)
                .email("full@streamvault.com")
                .name("Full Name")
                .role("ROLE_USER")
                .isVerified(false)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build();

        when(adminUserService.getUserById(userId)).thenReturn(fullUserResponse);

        ResponseEntity<AdminUserResponse> result = adminUserController.getUserById("admin@test.com", userId);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getEmail()).isEqualTo("full@streamvault.com");
        assertThat(result.getBody().getName()).isEqualTo("Full Name");
        assertThat(result.getBody().getRole()).isEqualTo("ROLE_USER");
        assertThat(result.getBody().getIsVerified()).isFalse();
        assertThat(result.getBody().getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }
}
