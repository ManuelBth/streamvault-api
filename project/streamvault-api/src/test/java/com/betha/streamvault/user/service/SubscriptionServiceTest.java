package com.betha.streamvault.user.service;

import com.betha.streamvault.shared.exception.AlreadyHasSubscriptionException;
import com.betha.streamvault.shared.exception.ResourceNotFoundException;
import com.betha.streamvault.shared.exception.SubscriptionNotFoundException;
import com.betha.streamvault.user.dto.SubscriptionResponse;
import com.betha.streamvault.user.model.Subscription;
import com.betha.streamvault.user.model.SubscriptionPlan;
import com.betha.streamvault.user.model.User;
import com.betha.streamvault.user.model.UserRole;
import com.betha.streamvault.user.repository.SubscriptionJpaRepository;
import com.betha.streamvault.user.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService Tests")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionJpaRepository subscriptionJpaRepository;

    @Mock
    private UserJpaRepository userJpaRepository;

    private SubscriptionService subscriptionService;

    private User testUser;
    private Subscription activeSubscription;
    private Subscription expiredSubscription;

    private static final String TEST_EMAIL = "test@streamvault.com";

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(subscriptionJpaRepository, userJpaRepository);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email(TEST_EMAIL)
                .name("Test User")
                .passwordHash("hashedPassword")
                .role(UserRole.ROLE_USER)
                .isVerified(true)
                .build();

        // Active subscription
        activeSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .active(true)
                .build();

        // Expired subscription
        expiredSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .expiresAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("purchaseSubscription - Success - Creates new subscription")
    void purchaseSubscription_Success() {
        // Given
        UUID userId = testUser.getId();
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        Subscription savedSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
                .active(true)
                .build();
        when(subscriptionJpaRepository.save(any(Subscription.class))).thenReturn(savedSubscription);

        // When
        SubscriptionResponse response = subscriptionService.purchaseSubscription(TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("DEFAULT", response.getPlan());
        assertTrue(response.getActive());
        assertNotNull(response.getStartedAt());
        assertNotNull(response.getExpiresAt());

        verify(subscriptionJpaRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("purchaseSubscription - Fails - User already has active subscription")
    void purchaseSubscription_Fails_AlreadyHasActiveSubscription() {
        // Given
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(activeSubscription));

        // When/Then
        AlreadyHasSubscriptionException exception = assertThrows(
                AlreadyHasSubscriptionException.class,
                () -> subscriptionService.purchaseSubscription(TEST_EMAIL)
        );

        assertTrue(exception.getMessage().contains("ya tiene una suscripción activa"));
        verify(subscriptionJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("purchaseSubscription - Success - Can purchase after expired subscription")
    void purchaseSubscription_Success_CanPurchaseAfterExpired() {
        // Given - user has expired subscription
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(expiredSubscription));
        when(subscriptionJpaRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SubscriptionResponse response = subscriptionService.purchaseSubscription(TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertTrue(response.getActive());
        verify(subscriptionJpaRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("purchaseSubscription - Fails - User not found")
    void purchaseSubscription_Fails_UserNotFound() {
        // Given
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> subscriptionService.purchaseSubscription(TEST_EMAIL)
        );
    }

    @Test
    @DisplayName("getCurrentSubscription - Success - Returns active subscription")
    void getCurrentSubscription_Success() {
        // Given
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.of(activeSubscription));

        // When
        SubscriptionResponse response = subscriptionService.getCurrentSubscription(TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(activeSubscription.getId(), response.getId());
        assertEquals("DEFAULT", response.getPlan());
        assertTrue(response.getActive());
    }

    @Test
    @DisplayName("getCurrentSubscription - Fails - No subscription found")
    void getCurrentSubscription_Fails_NotFound() {
        // Given
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(subscriptionJpaRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());

        // When/Then
        assertThrows(
                SubscriptionNotFoundException.class,
                () -> subscriptionService.getCurrentSubscription(TEST_EMAIL)
        );
    }

    @Test
    @DisplayName("getCurrentSubscription - Fails - User not found")
    void getCurrentSubscription_Fails_UserNotFound() {
        // Given
        when(userJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> subscriptionService.getCurrentSubscription(TEST_EMAIL)
        );
    }

    @Test
    @DisplayName("hasActiveSubscription - Returns true when active subscription exists")
    void hasActiveSubscription_ReturnsTrue_WhenActive() {
        // Given
        when(subscriptionJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(activeSubscription));

        // When
        boolean result = subscriptionService.hasActiveSubscription(TEST_EMAIL);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("hasActiveSubscription - Returns false when no subscription")
    void hasActiveSubscription_ReturnsFalse_WhenNoSubscription() {
        // Given
        when(subscriptionJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // When
        boolean result = subscriptionService.hasActiveSubscription(TEST_EMAIL);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("hasActiveSubscription - Returns false when expired")
    void hasActiveSubscription_ReturnsFalse_WhenExpired() {
        // Given
        when(subscriptionJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(expiredSubscription));

        // When
        boolean result = subscriptionService.hasActiveSubscription(TEST_EMAIL);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("hasActiveSubscription - Returns false when active is false")
    void hasActiveSubscription_ReturnsFalse_WhenInactive() {
        // Given
        Subscription inactiveSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .plan(SubscriptionPlan.DEFAULT)
                .startedAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .expiresAt(Instant.now().plus(15, ChronoUnit.DAYS))
                .active(false)
                .build();

        when(subscriptionJpaRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(inactiveSubscription));

        // When
        boolean result = subscriptionService.hasActiveSubscription(TEST_EMAIL);

        // Then
        assertFalse(result);
    }
}
