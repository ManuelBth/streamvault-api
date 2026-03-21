package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        return userJpaRepository.findByEmail(email)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userJpaRepository.findById(id)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public UserResponse updateUser(String email, UpdateUserRequest request) {
        return userJpaRepository.findByEmail(email)
                .map(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getEmail() != null) {
                        user.setEmail(request.getEmail());
                    }
                    user.setUpdatedAt(Instant.now());
                    return userJpaRepository.save(user);
                })
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        userJpaRepository.findByEmail(email).ifPresent(user -> {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Contraseña actual incorrecta");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(Instant.now());
            userJpaRepository.save(user);
        });
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }
}
