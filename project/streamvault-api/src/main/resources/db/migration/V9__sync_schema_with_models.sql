-- V9: Sync database schema with Java models
-- Agrega columnas faltantes que los modelos Java esperan

-- ============================================
-- CONTENT: Agregar status y updated_at
-- ============================================
ALTER TABLE content ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'DRAFT';
ALTER TABLE content ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- ============================================
-- EPISODES: Agregar status, thumbnail_key, created_at
-- ============================================
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS thumbnail_key VARCHAR(255);
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();

-- ============================================
-- WATCH_HISTORY: Agregar completed
-- ============================================
ALTER TABLE watch_history ADD COLUMN IF NOT EXISTS completed BOOLEAN DEFAULT FALSE;

-- ============================================
-- PROFILES: Limpiar columna is_kids si existe
-- ============================================
ALTER TABLE profiles DROP COLUMN IF EXISTS is_kids;

-- ============================================
-- Crear tabla content_genres si no existe
-- ============================================
CREATE TABLE IF NOT EXISTS content_genres (
    content_id UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (content_id, genre_id)
);

-- ============================================
-- Verificar estructura final
-- ============================================
DO $$
BEGIN
    RAISE NOTICE 'Schema sync completed. Tables structure:';
END $$;
