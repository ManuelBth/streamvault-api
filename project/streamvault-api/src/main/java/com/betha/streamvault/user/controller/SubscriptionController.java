package com.betha.streamvault.user.controller;

import com.betha.streamvault.user.dto.SubscriptionResponse;
import com.betha.streamvault.user.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for subscription management.
 */
@Log4j2
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Purchase a new subscription for the authenticated user.
     * Creates an active subscription with 30-day duration.
     */
    @PostMapping("/purchase")
    public ResponseEntity<SubscriptionResponse> purchaseSubscription(
            @AuthenticationPrincipal String username) {
        
        log.info("Purchase subscription request from user: {}", username);
        SubscriptionResponse subscription = subscriptionService.purchaseSubscription(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
    }

    /**
     * Get the current subscription for the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponse> getMySubscription(
            @AuthenticationPrincipal String username) {
        
        log.info("Get subscription request from user: {}", username);
        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription(username);
        return ResponseEntity.ok(subscription);
    }
}
