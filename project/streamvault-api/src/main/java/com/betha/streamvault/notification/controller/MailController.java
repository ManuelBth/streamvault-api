package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.model.Notification;
import com.betha.streamvault.notification.service.EmailService;
import com.betha.streamvault.notification.service.NotificationService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.UserJpaRepository;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
@Log4j2
public class MailController {

    private final EmailService emailService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final UserJpaRepository userJpaRepository;

    @PostMapping("/send")
    public ResponseEntity<Void> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @AuthenticationPrincipal String email) {
        
        log.info("POST /api/v1/mail/send - from: {}", email);
        
        UserResponse user = userService.getCurrentUser(email);
        if (user != null) {
            request.setFrom(user.getEmail());
            emailService.sendEmail(request);

            try {
                User receiver = userJpaRepository.findByEmail(request.getTo()).orElse(null);
                if (receiver != null) {
                    notificationService.createNotification(
                        receiver.getId(),
                        Notification.NotificationType.USER_NOTIFICATION,
                        "Nuevo correo electrónico",
                        "Has recibido un correo de " + user.getEmail(),
                        null
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to send notification to receiver: {}", e.getMessage());
            }
        }
        
        return ResponseEntity.ok().build();
    }
}
