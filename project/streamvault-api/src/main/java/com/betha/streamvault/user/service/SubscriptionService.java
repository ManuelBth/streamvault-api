package com.betha.streamvault.user.service;

import com.betha.streamvault.shared.exception.AlreadyHasSubscriptionException;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.shared.exception.SubscriptionNotFoundException;
import com.betha.streamvault.user.dto.SubscriptionResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.model.SubscriptionPlan;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.repository.SubscriptionJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for managing user subscriptions.
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final int SUBSCRIPTION_DURATION_DAYS = 30;
    private static final double SUBSCRIPTION_PRICE_USD = 10.0;

    private final SubscriptionJpaRepository subscriptionJpaRepository;
    private final UserJpaRepository userJpaRepository;

    /**
     * Purchases a subscription for the given user.
     * Creates a new active subscription with 30-day duration.
     *
     * @param userEmail the email of the user
     * @return the created subscription response
     * @throws AlreadyHasSubscriptionException if user already has active subscription
     */
    @Transactional
    public SubscriptionResponse purchaseSubscription(String userEmail) {
        log.info("Purchasing subscription for user: {}", userEmail);

        User user = userJpaRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Check if user already has active subscription
        subscriptionJpaRepository.findByUserId(user.getId())
                .ifPresent(existing -> {
                    if (existing.getActive() != null && existing.getActive()
                            && existing.getExpiresAt() != null
                            && existing.getExpiresAt().isAfter(Instant.now())) {
                        throw new AlreadyHasSubscriptionException("El usuario ya tiene una suscripción activa");
                    }
                });

        // Create new subscription
        Instant now = Instant.now();
        Instant expiresAt = now.plus(SUBSCRIPTION_DURATION_DAYS, ChronoUnit.DAYS);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(now)
                .expiresAt(expiresAt)
                .active(true)
                .build();

        subscription = subscriptionJpaRepository.save(subscription);

        log.info("Subscription created for user {} with expiresAt {}",
                userEmail, expiresAt);

        return toResponse(subscription);
    }

    /**
     * Gets the current subscription for the given user.
     *
     * @param userEmail the email of the user
     * @return the subscription response
     * @throws SubscriptionNotFoundException if user has no subscription
     */
    public SubscriptionResponse getCurrentSubscription(String userEmail) {
        log.info("Getting subscription for user: {}", userEmail);

        User user = userJpaRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Subscription subscription = subscriptionJpaRepository.findByUserId(user.getId())
                .orElseThrow(() -> new SubscriptionNotFoundException("Suscripción no encontrada"));

        return toResponse(subscription);
    }

    /**
     * Checks if user has an active, non-expired subscription.
     * Used by StreamService for access control.
     *
     * @param userEmail the email of the user
     * @return true if user has active subscription
     */
    public boolean hasActiveSubscription(String userEmail) {
        return subscriptionJpaRepository.findByEmail(userEmail)
                .map(subscription -> {
                    boolean isActive = subscription.getActive() != null
                            && subscription.getActive()
                            && subscription.getExpiresAt() != null
                            && subscription.getExpiresAt().isAfter(Instant.now());
                    log.debug("Subscription check for {}: active={}", userEmail, isActive);
                    return isActive;
                })
                .orElse(false);
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .plan(subscription.getPlan().name())
                .startedAt(subscription.getStartedAt())
                .expiresAt(subscription.getExpiresAt())
                .active(subscription.getActive())
                .build();
    }
}
