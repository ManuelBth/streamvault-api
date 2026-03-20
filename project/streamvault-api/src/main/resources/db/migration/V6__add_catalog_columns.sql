-- V6: Add catalog columns and indexes
-- Status columns for content and episodes
ALTER TABLE content ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT';
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT';
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS thumbnail_key VARCHAR(255);
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_content_status ON content(status);
CREATE INDEX IF NOT EXISTS idx_episodes_status ON episodes(status);
