package com.betha.streamvault.admin.service;

import com.betha.streamvault.admin.dto.AdminUserListResponse;
import com.betha.streamvault.admin.dto.AdminUserResponse;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserJpaRepository userJpaRepository;

    @Transactional(readOnly = true)
    public AdminUserListResponse getAllUsers(int page, int size) {
        Page<User> userPage = userJpaRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<AdminUserResponse> users = userPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        AdminUserListResponse response = AdminUserListResponse.builder()
                .users(users)
                .total(userPage.getTotalElements())
                .page(page)
                .size(size)
                .build();

        log.info("Admin retrieved {} users (page={}, size={})", response.getUsers().size(), page, size);
        return response;
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID userId) {
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        log.info("Admin retrieved user: {}", userId);
        return toResponse(user);
    }

    private AdminUserResponse toResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt() != null
                        ? user.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null)
                .build();
    }
}
