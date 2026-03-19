package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .passwordHash("hashedPassword")
                .role("ROLE_USER")
                .isVerified(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("getCurrentUser - Should return user by email")
    void getCurrentUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getCurrentUser("test@streamvault.com"))
                .assertNext(response -> {
                    assertEquals(testUser.getEmail(), response.getEmail());
                    assertEquals(testUser.getName(), response.getName());
                    assertEquals(testUser.getRole(), response.getRole());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getCurrentUser - Should return empty when user not found")
    void getCurrentUser_NotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(userService.getCurrentUser("notfound@streamvault.com"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getUserById - Should return user by ID")
    void getUserById_Success() {
        when(userRepository.findById(any(UUID.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(testUser.getId()))
                .assertNext(response -> {
                    assertEquals(testUser.getId(), response.getId());
                    assertEquals(testUser.getEmail(), response.getEmail());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("updateUser - Should update user name and email")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("updated@streamvault.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.updateUser("test@streamvault.com", request))
                .assertNext(response -> {
                    assertNotNull(response);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("changePassword - Should change password when current password is correct")
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.changePassword("test@streamvault.com", request))
                .verifyComplete();
    }

    @Test
    @DisplayName("changePassword - Should fail when current password is incorrect")
    void changePassword_WrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        StepVerifier.create(userService.changePassword("test@streamvault.com", request))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
