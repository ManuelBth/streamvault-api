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
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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
        when(adminUserService.getAllUsers(0, 20)).thenReturn(Mono.just(testUserListResponse));

        Mono<ResponseEntity<AdminUserListResponse>> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        ResponseEntity<AdminUserListResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(1, entity.getBody().getUsers().size());
        assertEquals(1L, entity.getBody().getTotal());
    }

    @Test
    @DisplayName("GET /admin/users - Should use default pagination")
    void getAllUsers_DefaultPagination() {
        when(adminUserService.getAllUsers(0, 20)).thenReturn(Mono.just(testUserListResponse));

        Mono<ResponseEntity<AdminUserListResponse>> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        ResponseEntity<AdminUserListResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
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

        when(adminUserService.getAllUsers(5, 10)).thenReturn(Mono.just(paginatedResponse));

        Mono<ResponseEntity<AdminUserListResponse>> result = adminUserController.getAllUsers("admin@test.com", 5, 10);

        ResponseEntity<AdminUserListResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(5, entity.getBody().getPage());
        assertEquals(10, entity.getBody().getSize());
        assertEquals(100L, entity.getBody().getTotal());
    }

    @Test
    @DisplayName("GET /admin/users/{id} - Should return user by ID")
    void getUserById_Success() {
        UUID userId = testUserResponse.getId();
        when(adminUserService.getUserById(userId)).thenReturn(Mono.just(testUserResponse));

        Mono<ResponseEntity<AdminUserResponse>> result = adminUserController.getUserById("admin@test.com", userId);

        ResponseEntity<AdminUserResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(userId, entity.getBody().getId());
        assertEquals("test@streamvault.com", entity.getBody().getEmail());
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

        when(adminUserService.getAllUsers(0, 20)).thenReturn(Mono.just(emptyResponse));

        Mono<ResponseEntity<AdminUserListResponse>> result = adminUserController.getAllUsers("admin@test.com", 0, 20);

        ResponseEntity<AdminUserListResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertTrue(entity.getBody().getUsers().isEmpty());
        assertEquals(0L, entity.getBody().getTotal());
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

        when(adminUserService.getUserById(adminId)).thenReturn(Mono.just(adminResponse));

        Mono<ResponseEntity<AdminUserResponse>> result = adminUserController.getUserById("admin@test.com", adminId);

        ResponseEntity<AdminUserResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("ROLE_ADMIN", entity.getBody().getRole());
        assertEquals("Admin User", entity.getBody().getName());
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

        when(adminUserService.getUserById(userId)).thenReturn(Mono.just(fullUserResponse));

        Mono<ResponseEntity<AdminUserResponse>> result = adminUserController.getUserById("admin@test.com", userId);

        ResponseEntity<AdminUserResponse> entity = result.block();
        assertNotNull(entity);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals("full@streamvault.com", entity.getBody().getEmail());
        assertEquals("Full Name", entity.getBody().getName());
        assertEquals("ROLE_USER", entity.getBody().getRole());
        assertFalse(entity.getBody().getIsVerified());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 30, 0), entity.getBody().getCreatedAt());
    }
}
