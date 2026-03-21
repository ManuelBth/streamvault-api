package com.betha.streamvault.notification.controller;

import com.betha.streamvault.auth.service.JwtService;
import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.service.EmailService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mail")
public class MailController {

    private final EmailService emailService;
    private final UserService userService;
    private final JwtService jwtService;

    public MailController(EmailService emailService, UserService userService, JwtService jwtService) {
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = authHeader.substring(7);
        UUID userId = jwtService.getUserIdFromToken(token);
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserResponse user = userService.getUserById(userId);
        if (user != null) {
            request.setFrom(user.getEmail());
            emailService.sendEmail(request);
        }
        
        return ResponseEntity.ok().build();
    }
}
