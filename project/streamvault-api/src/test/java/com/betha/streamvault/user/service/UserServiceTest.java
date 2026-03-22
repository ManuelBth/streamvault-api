package com.betha.streamvault.user.service;

import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService(userJpaRepository, passwordEncoder);
        
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@streamvault.com")
                .name("Test User")
                .passwordHash("hashedPassword")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();
    }

    @Test
    @DisplayName("getCurrentUser - Should return user by email")
    void getCurrentUser_Success() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getCurrentUser("test@streamvault.com");

        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        assertEquals(testUser.getRole().name(), result.getRole());
    }

    @Test
    @DisplayName("getCurrentUser - Should throw ResourceNotFoundException when user not found")
    void getCurrentUser_NotFound() {
        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getCurrentUser("notfound@streamvault.com"));
    }

    @Test
    @DisplayName("getUserById - Should return user by ID")
    void getUserById_Success() {
        when(userJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        UserResponse result = userService.getUserById(testUser.getId());

        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("getUserById - Should throw ResourceNotFoundException when user not found")
    void getUserById_NotFound() {
        when(userJpaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(UUID.randomUUID()));
    }

    @Test
    @DisplayName("updateUser - Should update user name and email")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setEmail("updated@streamvault.com");

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userJpaRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.updateUser("test@streamvault.com", request);

        assertNotNull(result);
    }

    @Test
    @DisplayName("changePassword - Should change password when current password is correct")
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");
        when(userJpaRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.changePassword("test@streamvault.com", request));
    }

    @Test
    @DisplayName("changePassword - Should fail when current password is incorrect")
    void changePassword_WrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userJpaRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("test@streamvault.com", request));
    }
}
