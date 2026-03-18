package com.betha.streamvault.notification.controller;

import com.betha.streamvault.notification.dto.SendEmailRequest;
import com.betha.streamvault.notification.service.EmailService;
import com.betha.streamvault.user.dto.UserResponse;
import com.betha.streamvault.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/mail")
public class MailController {

    private final EmailService emailService;
    private final UserService userService;

    public MailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public Mono<ResponseEntity<Void>> sendEmail(
            @Valid @RequestBody SendEmailRequest request,
            @AuthenticationPrincipal String username) {
        
        // Obtener el usuario autenticado para usar su email como remitente
        return userService.getCurrentUser(username)
                .flatMap(user -> {
                    // Usar el email del usuario autenticado como remitente
                    request.setFrom(user.getEmail());
                    return emailService.sendEmail(request);
                })
                .thenReturn(ResponseEntity.ok().<Void>build());
    }
}
