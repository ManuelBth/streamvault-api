package com.betha.streamvault.user.controller;

import com.betha.streamvault.user.dto.ChangePasswordRequest;
import com.betha.streamvault.user.dto.UpdateUserRequest;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Log4j2
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpServletRequest request) {
        String email = (String) request.getAttribute("CURRENT_USER_EMAIL");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        UserResponse user = userService.getCurrentUser(email);
        if (user == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            HttpServletRequest request,
            @Valid @RequestBody UpdateUserRequest updateRequest) {
        String email = (String) request.getAttribute("CURRENT_USER_EMAIL");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        UserResponse user = userService.updateUser(email, updateRequest);
        if (user == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePasswordRequest changeRequest) {
        String email = (String) request.getAttribute("CURRENT_USER_EMAIL");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        userService.changePassword(email, changeRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }
}
