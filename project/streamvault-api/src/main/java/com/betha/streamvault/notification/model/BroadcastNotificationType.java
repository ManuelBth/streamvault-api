package com.betha.streamvault.notification.model;

/**
 * Enum for broadcast notification types.
 * These are notifications that go to all users.
 */
public enum BroadcastNotificationType {
    NEW_CONTENT,    // New movie/series published
    NEW_EPISODE,   // New episode of a series
    SYSTEM         // System-wide announcement
}
