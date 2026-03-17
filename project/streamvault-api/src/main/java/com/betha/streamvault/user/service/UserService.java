package com.betha.streamvault.user.service;

import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponse> getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .map(this::toResponse);
    }

    public Mono<UserResponse> getUserById(java.util.UUID id) {
        return userRepository.findById(id)
                .map(this::toResponse);
    }

    public Mono<UserResponse> updateUser(String email, UpdateUserRequest request) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getEmail() != null) {
                        user.setEmail(request.getEmail());
                    }
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    public Mono<Void> changePassword(String email, ChangePasswordRequest request) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
                        return Mono.error(new IllegalArgumentException("Contraseña actual incorrecta"));
                    }
                    user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user)
                            .then();
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
