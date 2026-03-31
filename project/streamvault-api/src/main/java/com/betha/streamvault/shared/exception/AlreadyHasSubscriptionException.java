package com.betha.streamvault.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user already has an active subscription
 * and tries to purchase another one.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyHasSubscriptionException extends RuntimeException {
    
    public AlreadyHasSubscriptionException(String message) {
        super(message);
    }
}
