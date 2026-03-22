-- V10: Add JPA Auditing columns to all entities
-- Agrega updated_at a las tablas que ahora extienden BaseEntity

-- ============================================
-- EPISODES: Agregar updated_at (ya tiene created_at de V9)
-- ============================================
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- SEASONS: Agregar created_at y updated_at
-- ============================================
ALTER TABLE seasons ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
ALTER TABLE seasons ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- GENRES: Agregar created_at y updated_at
-- ============================================
ALTER TABLE genres ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
ALTER TABLE genres ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- MAIL_USERS: Agregar updated_at (ya tiene created_at)
-- ============================================
ALTER TABLE mail_users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- REFRESH_TOKENS: Agregar updated_at
-- ============================================
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- NOTIFICATIONS: Agregar updated_at (ya tiene created_at)
-- ============================================
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- Verificar columnas agregadas
-- ============================================
DO $$
BEGIN
    -- Verificar que todas las columnas existen
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'episodes' AND column_name = 'updated_at') THEN
        RAISE WARNING 'episodes.updated_at not found';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'seasons' AND column_name = 'created_at') THEN
        RAISE WARNING 'seasons.created_at not found';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'seasons' AND column_name = 'updated_at') THEN
        RAISE WARNING 'seasons.updated_at not found';
    END IF;
    
    RAISE NOTICE 'JPA Auditing columns added successfully';
END $$;
