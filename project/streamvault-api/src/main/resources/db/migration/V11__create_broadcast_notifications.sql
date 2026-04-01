-- V11: Create broadcast_notifications table
-- This table stores general notifications that go to all users (NEW_CONTENT, NEW_EPISODE, SYSTEM)

CREATE TABLE IF NOT EXISTS broadcast_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    related_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_broadcast_notifications_created_at ON broadcast_notifications(created_at DESC);
