-- StreamVault Test Data
-- Datos de prueba para probar todas las APIs de StreamVault
-- Base URL: http://localhost:8080/api/v1

-- ============================================
-- USERS
-- ============================================
-- Password para todos: password123
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (id, email, name, password_hash, role, is_verified, created_at) VALUES
('11111111-1111-4111-8111-111111111111', 'admin@streamvault.com', 'Admin User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_ADMIN', true, NOW() - INTERVAL '90 days'),
('22222222-2222-4222-8222-222222222222', 'john.doe@streamvault.com', 'John Doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', true, NOW() - INTERVAL '60 days'),
('33333333-3333-4333-8333-333333333333', 'jane.smith@streamvault.com', 'Jane Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', true, NOW() - INTERVAL '45 days'),
('44444444-4444-4444-8444-444444444444', 'mike.wilson@streamvault.com', 'Mike Wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_USER', false, NOW() - INTERVAL '15 days');

-- ============================================
-- SUBSCRIPTIONS
-- ============================================

INSERT INTO subscriptions (id, user_id, plan, started_at, expires_at, active) VALUES
('aaaaaaa1-aaaa-4aaa-aaaa-aaaaaaaaaaaa', '11111111-1111-4111-8111-111111111111', 'PREMIUM', NOW() - INTERVAL '90 days', NOW() + INTERVAL '275 days', true),
('aaaaaaa2-aaaa-4aaa-aaaa-aaaaaaaaaaab', '22222222-2222-4222-8222-222222222222', 'BASIC', NOW() - INTERVAL '60 days', NOW() + INTERVAL '305 days', true),
('aaaaaaa3-aaaa-4aaa-aaaa-aaaaaaaaaaac', '33333333-3333-4333-8333-333333333333', 'STANDARD', NOW() - INTERVAL '45 days', NOW() + INTERVAL '320 days', true);

-- Actualizar users con subscriptions
UPDATE users SET subscription_id = 'aaaaaaa1-aaaa-4aaa-aaaa-aaaaaaaaaaaa' WHERE id = '11111111-1111-4111-8111-111111111111';
UPDATE users SET subscription_id = 'aaaaaaa2-aaaa-4aaa-aaaa-aaaaaaaaaaab' WHERE id = '22222222-2222-4222-8222-222222222222';
UPDATE users SET subscription_id = 'aaaaaaa3-aaaa-4aaa-aaaa-aaaaaaaaaaac' WHERE id = '33333333-3333-4333-8333-333333333333';

-- ============================================
-- PROFILES
-- ============================================
-- Schema: id, user_id, name, avatar_url (sin is_kids)

INSERT INTO profiles (id, user_id, name, avatar_url) VALUES
('bbbbbbb1-bbbb-4bbb-bbbb-bbbbbbbbbb01', '22222222-2222-4222-8222-222222222222', 'Main Profile', 'avatars/profile_main.jpg'),
('bbbbbbb2-bbbb-4bbb-bbbb-bbbbbbbbbb02', '22222222-2222-4222-8222-222222222222', 'Kids Profile', 'avatars/profile_kids.png'),
('bbbbbbb3-bbbb-4bbb-bbbb-bbbbbbbbbb03', '33333333-3333-4333-8333-333333333333', 'Default', 'avatars/profile_jane.jpg'),
('bbbbbbb4-bbbb-4bbb-bbbb-bbbbbbbbbb04', '44444444-4444-4444-8444-444444444444', 'My Profile', 'avatars/profile_mike.png');

-- ============================================
-- GENRES
-- ============================================

INSERT INTO genres (id, name) VALUES
('ddddddd1-dddd-4ddd-8ddd-ddddddddddd1', 'Action'),
('ddddddd2-dddd-4ddd-8ddd-ddddddddddd2', 'Drama'),
('ddddddd3-dddd-4ddd-8ddd-ddddddddddd3', 'Science Fiction'),
('ddddddd4-dddd-4ddd-8ddd-ddddddddddd4', 'Mystery'),
('ddddddd5-dddd-4ddd-8ddd-ddddddddddd5', 'Comedy'),
('ddddddd6-dddd-4ddd-8ddd-ddddddddddd6', 'Adventure'),
('ddddddd7-dddd-4ddd-8ddd-ddddddddddd7', 'Fantasy');

-- ============================================
-- CONTENT (MOVIES)
-- ============================================
-- Schema: id, title, description, type, release_year, rating, thumbnail_key, minio_base_key, status, created_by, created_at, updated_at

INSERT INTO content (id, title, description, type, release_year, rating, thumbnail_key, minio_base_key, status, created_by, created_at, updated_at) VALUES
('ccccccc1-cccc-4ccc-8ccc-ccccccccccc1', 'The Great Adventure', 'An epic journey through uncharted lands where heroes rise and destinies unfold. Join our protagonists as they face impossible odds and discover the true meaning of courage.', 'MOVIE', 2023, 'PG-13', 'thumbnails/movie_great_adventure.jpg', 'videos/movies/great_adventure', 'PUBLISHED', '11111111-1111-4111-8111-111111111111', NOW() - INTERVAL '30 days', NOW() - INTERVAL '5 days'),
('ccccccc2-cccc-4ccc-8ccc-ccccccccccc2', 'Midnight Mystery', 'When a renowned detective receives a cryptic letter, a chain of events is set in motion that will challenge everything she knows about truth and deception.', 'MOVIE', 2024, 'R', 'thumbnails/movie_midnight_mystery.jpg', 'videos/movies/midnight_mystery', 'PUBLISHED', '11111111-1111-4111-8111-111111111111', NOW() - INTERVAL '20 days', NOW() - INTERVAL '3 days'),
('ccccccc3-cccc-4ccc-8ccc-ccccccccccc3', 'Cosmic Warriors', 'In a distant galaxy, a band of unlikely heroes must unite to stop an empire that threatens to enslave billions. Action, drama, and the fight for freedom collide.', 'MOVIE', 2023, 'PG', 'thumbnails/movie_cosmic_warriors.jpg', 'videos/movies/cosmic_warriors', 'PUBLISHED', '11111111-1111-4111-8111-111111111111', NOW() - INTERVAL '15 days', NOW() - INTERVAL '1 day');

-- ============================================
-- CONTENT (SERIES)
-- ============================================

INSERT INTO content (id, title, description, type, release_year, rating, thumbnail_key, minio_base_key, status, created_by, created_at, updated_at) VALUES
('ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 'The Royal Kingdom', 'A sweeping tale of power, betrayal, and redemption set in a medieval kingdom. Watch as lords and ladies navigate the treacherous waters of court intrigue.', 'SERIES', 2022, 'TV-14', 'thumbnails/series_royal_kingdom.jpg', 'videos/series/royal_kingdom', 'PUBLISHED', '11111111-1111-4111-8111-111111111111', NOW() - INTERVAL '60 days', NOW() - INTERVAL '7 days'),
('ccccccc5-cccc-4ccc-8ccc-ccccccccccc5', 'Space Explorers', 'Follow the crew of the starship Aurora as they journey to the far reaches of the galaxy, encountering new civilizations and facing cosmic threats.', 'SERIES', 2023, 'TV-PG', 'thumbnails/series_space_explorers.jpg', 'videos/series/space_explorers', 'PUBLISHED', '11111111-1111-4111-8111-111111111111', NOW() - INTERVAL '45 days', NOW() - INTERVAL '2 days');

-- ============================================
-- SEASONS
-- ============================================

INSERT INTO seasons (id, content_id, season_number) VALUES
-- The Royal Kingdom (3 seasons)
('eeeeeeee1-eeee-4eee-8eee-eeeeeeeeee01', 'ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 1),
('eeeeeeee2-eeee-4eee-8eee-eeeeeeeeee02', 'ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 2),
('eeeeeeee3-eeee-4eee-8eee-eeeeeeeeee03', 'ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 3),
-- Space Explorers (2 seasons)
('eeeeeeee4-eeee-4eee-8eee-eeeeeeeeee04', 'ccccccc5-cccc-4ccc-8ccc-ccccccccccc5', 1),
('eeeeeeee5-eeee-4eee-8eee-eeeeeeeeee05', 'ccccccc5-cccc-4ccc-8ccc-ccccccccccc5', 2);

-- ============================================
-- EPISODES
-- ============================================
-- Schema: id, season_id, episode_number, title, description, minio_key, thumbnail_key, duration_sec, status, created_at

INSERT INTO episodes (id, season_id, episode_number, title, description, minio_key, thumbnail_key, duration_sec, status, created_at) VALUES
-- The Royal Kingdom - Season 1 (4 episodes)
('ffffffff1-ffff-4fff-8fff-ffffffffff01', 'eeeeeeee1-eeee-4eee-8eee-eeeeeeeeee01', 1, 'The Beginning', 'Prince Alexander discovers a secret that will change the kingdom forever.', 'videos/series/royal_kingdom/s01e01.mp4', 'thumbnails/series/royal_kingdom/s01e01.jpg', 2700, 'READY', NOW() - INTERVAL '55 days'),
('ffffffff2-ffff-4fff-8fff-ffffffffff02', 'eeeeeeee1-eeee-4eee-8eee-eeeeeeeeee01', 2, 'The Betrayal', 'Alliances shift as the queen reveals her true plans.', 'videos/series/royal_kingdom/s01e02.mp4', 'thumbnails/series/royal_kingdom/s01e02.jpg', 2580, 'READY', NOW() - INTERVAL '50 days'),
('ffffffff3-ffff-4fff-8fff-ffffffffff03', 'eeeeeeee1-eeee-4eee-8eee-eeeeeeeeee01', 3, 'The Hidden Path', 'A mysterious guide leads Alexander to an ancient library.', 'videos/series/royal_kingdom/s01e03.mp4', 'thumbnails/series/royal_kingdom/s01e03.jpg', 2640, 'READY', NOW() - INTERVAL '45 days'),
('ffffffff4-ffff-4fff-8fff-ffffffffff04', 'eeeeeeee1-eeee-4eee-8eee-eeeeeeeeee01', 4, 'The Coronation', 'The kingdom gathers for a historic event.', 'videos/series/royal_kingdom/s01e04.mp4', 'thumbnails/series/royal_kingdom/s01e04.jpg', 2760, 'READY', NOW() - INTERVAL '40 days'),
-- The Royal Kingdom - Season 2 (4 episodes)
('ffffffff5-ffff-4fff-8fff-ffffffffff05', 'eeeeeeee2-eeee-4eee-8eee-eeeeeeeeee02', 1, 'New Dawn', 'Six months after the coronation, Alexander faces his first major crisis.', 'videos/series/royal_kingdom/s02e01.mp4', 'thumbnails/series/royal_kingdom/s02e01.jpg', 2700, 'READY', NOW() - INTERVAL '35 days'),
('ffffffff6-ffff-4fff-8fff-ffffffffff06', 'eeeeeeee2-eeee-4eee-8eee-eeeeeeeeee02', 2, 'The Alliance', 'A unexpected alliance forms between rival kingdoms.', 'videos/series/royal_kingdom/s02e02.mp4', 'thumbnails/series/royal_kingdom/s02e02.jpg', 2520, 'READY', NOW() - INTERVAL '30 days'),
('ffffffff7-ffff-4fff-8fff-ffffffffff07', 'eeeeeeee2-eeee-4eee-8eee-eeeeeeeeee02', 3, 'The Tournament', 'A royal tournament brings together the best warriors.', 'videos/series/royal_kingdom/s02e03.mp4', 'thumbnails/series/royal_kingdom/s02e03.jpg', 2580, 'READY', NOW() - INTERVAL '25 days'),
('ffffffff8-ffff-4fff-8fff-ffffffffff08', 'eeeeeeee2-eeee-4eee-8eee-eeeeeeeeee02', 4, 'The Siege', 'The kingdom faces its greatest threat yet.', 'videos/series/royal_kingdom/s02e04.mp4', 'thumbnails/series/royal_kingdom/s02e04.jpg', 2700, 'READY', NOW() - INTERVAL '20 days'),
-- The Royal Kingdom - Season 3 (3 episodes)
('ffffffff9-ffff-4fff-8fff-fffffffffff9', 'eeeeeeee3-eeee-4eee-8eee-eeeeeeeeee03', 1, 'The Return', 'Prince Alexander returns from exile.', 'videos/series/royal_kingdom/s03e01.mp4', 'thumbnails/series/royal_kingdom/s03e01.jpg', 2700, 'READY', NOW() - INTERVAL '15 days'),
('fffffff10-ffff-4fff-8fff-fffffffff10', 'eeeeeeee3-eeee-4eee-8eee-eeeeeeeeee03', 2, 'The War', 'All-out war engulfs the kingdom.', 'videos/series/royal_kingdom/s03e02.mp4', 'thumbnails/series/royal_kingdom/s03e02.jpg', 2760, 'READY', NOW() - INTERVAL '10 days'),
('fffffff11-ffff-4fff-8fff-fffffffff11', 'eeeeeeee3-eeee-4eee-8eee-eeeeeeeeee03', 3, 'The Peace', 'The kingdom finally finds peace.', 'videos/series/royal_kingdom/s03e03.mp4', 'thumbnails/series/royal_kingdom/s03e03.jpg', 2700, 'READY', NOW() - INTERVAL '5 days'),
-- Space Explorers - Season 1 (3 episodes)
('fffffff12-ffff-4fff-8fff-fffffffff12', 'eeeeeeee4-eeee-4eee-8eee-eeeeeeeeee04', 1, 'Departure', 'Captain Chen leads the crew on their first mission to distant space.', 'videos/series/space_explorers/s01e01.mp4', 'thumbnails/series/space_explorers/s01e01.jpg', 2400, 'READY', NOW() - INTERVAL '40 days'),
('fffffff13-ffff-4fff-8fff-fffffffff13', 'eeeeeeee4-eeee-4eee-8eee-eeeeeeeeee04', 2, 'First Contact', 'The Aurora encounters an unknown civilization.', 'videos/series/space_explorers/s01e02.mp4', 'thumbnails/series/space_explorers/s01e02.jpg', 2460, 'READY', NOW() - INTERVAL '35 days'),
('fffffff14-ffff-4fff-8fff-fffffffff14', 'eeeeeeee4-eeee-4eee-8eee-eeeeeeeeee04', 3, 'The Anomaly', 'A strange spatial anomaly threatens the ship.', 'videos/series/space_explorers/s01e03.mp4', 'thumbnails/series/space_explorers/s01e03.jpg', 2520, 'READY', NOW() - INTERVAL '30 days'),
-- Space Explorers - Season 2 (3 episodes)
('fffffff15-ffff-4fff-8fff-fffffffff15', 'eeeeeeee5-eeee-4eee-8eee-eeeeeeeeee05', 1, 'Return Journey', 'The crew begins their long journey home.', 'videos/series/space_explorers/s02e01.mp4', 'thumbnails/series/space_explorers/s02e01.jpg', 2400, 'READY', NOW() - INTERVAL '20 days'),
('fffffff16-ffff-4fff-8fff-fffffffff16', 'eeeeeeee5-eeee-4eee-8eee-eeeeeeeeee05', 2, 'The Trade', 'A dangerous trade agreement must be negotiated.', 'videos/series/space_explorers/s02e02.mp4', 'thumbnails/series/space_explorers/s02e02.jpg', 2460, 'READY', NOW() - INTERVAL '15 days'),
('fffffff17-ffff-4fff-8fff-fffffffff17', 'eeeeeeee5-eeee-4eee-8eee-eeeeeeeeee05', 3, 'Homecoming', 'The Aurora finally returns to Earth.', 'videos/series/space_explorers/s02e03.mp4', 'thumbnails/series/space_explorers/s02e03.jpg', 2640, 'READY', NOW() - INTERVAL '10 days');

-- ============================================
-- CONTENT_GENRES
-- ============================================

INSERT INTO content_genres (content_id, genre_id) VALUES
-- Movies
('ccccccc1-cccc-4ccc-8ccc-ccccccccccc1', 'ddddddd1-dddd-4ddd-8ddd-ddddddddddd1'), -- Great Adventure - Action
('ccccccc1-cccc-4ccc-8ccc-ccccccccccc1', 'ddddddd6-dddd-4ddd-8ddd-ddddddddddd6'), -- Great Adventure - Adventure
('ccccccc2-cccc-4ccc-8ccc-ccccccccccc2', 'ddddddd4-dddd-4ddd-8ddd-ddddddddddd4'), -- Midnight Mystery - Mystery
('ccccccc2-cccc-4ccc-8ccc-ccccccccccc2', 'ddddddd2-dddd-4ddd-8ddd-ddddddddddd2'), -- Midnight Mystery - Drama
('ccccccc3-cccc-4ccc-8ccc-ccccccccccc3', 'ddddddd1-dddd-4ddd-8ddd-ddddddddddd1'), -- Cosmic Warriors - Action
('ccccccc3-cccc-4ccc-8ccc-ccccccccccc3', 'ddddddd3-dddd-4ddd-8ddd-ddddddddddd3'), -- Cosmic Warriors - Sci-Fi
-- Series
('ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 'ddddddd2-dddd-4ddd-8ddd-ddddddddddd2'), -- Royal Kingdom - Drama
('ccccccc4-cccc-4ccc-8ccc-ccccccccccc4', 'ddddddd6-dddd-4ddd-8ddd-ddddddddddd6'), -- Royal Kingdom - Adventure
('ccccccc5-cccc-4ccc-8ccc-ccccccccccc5', 'ddddddd3-dddd-4ddd-8ddd-ddddddddddd3'), -- Space Explorers - Sci-Fi
('ccccccc5-cccc-4ccc-8ccc-ccccccccccc5', 'ddddddd1-dddd-4ddd-8ddd-ddddddddddd1'); -- Space Explorers - Action

-- ============================================
-- WATCH_HISTORY
-- ============================================
-- Schema: id, profile_id, episode_id, progress_sec, watched_at, completed

INSERT INTO watch_history (id, profile_id, episode_id, progress_sec, watched_at, completed) VALUES
('11111111-aaaa-4aaa-aaaa-aaaaaaaaaa01', 'bbbbbbb1-bbbb-4bbb-bbbb-bbbbbbbbbb01', 'ffffffff1-ffff-4fff-8fff-ffffffffff01', 1200, NOW() - INTERVAL '2 days', false),
('11111111-aaaa-4aaa-aaaa-aaaaaaaaaa02', 'bbbbbbb1-bbbb-4bbb-bbbb-bbbbbbbbbb01', 'ffffffff2-ffff-4fff-8fff-ffffffffff02', 600, NOW() - INTERVAL '1 day', false),
('11111111-aaaa-4aaa-aaaa-aaaaaaaaaa03', 'bbbbbbb1-bbbb-4bbb-bbbb-bbbbbbbbbb01', 'ffffffff12-ffff-4fff-8fff-fffffffff12', 1800, NOW() - INTERVAL '3 days', false),
('11111111-aaaa-4aaa-aaaa-aaaaaaaaaa04', 'bbbbbbb3-bbbb-4bbb-bbbb-bbbbbbbbbb03', 'ffffffff1-ffff-4fff-8fff-ffffffffff01', 2700, NOW() - INTERVAL '5 days', true),
('11111111-aaaa-4aaa-aaaa-aaaaaaaaaa05', 'bbbbbbb3-bbbb-4bbb-bbbb-bbbbbbbbbb03', 'ffffffff5-ffff-4fff-8fff-ffffffffff05', 1500, NOW() - INTERVAL '1 day', false);

-- ============================================
-- REFRESH TOKENS (opcional para testing)
-- ============================================

-- INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, revoked, created_at) VALUES
-- ('22222222-aaaa-4aaa-aaaa-aaaaaaaaaa01', '22222222-2222-4222-8222-222222222222', 'hashed_refresh_token_here', NOW() + INTERVAL '7 days', false, NOW());

-- ============================================
-- VERIFICACIÓN
-- ============================================

SELECT 'Users:' AS table_name, COUNT(*) AS count FROM users
UNION ALL SELECT 'Subscriptions:', COUNT(*) FROM subscriptions
UNION ALL SELECT 'Profiles:', COUNT(*) FROM profiles
UNION ALL SELECT 'Content:', COUNT(*) FROM content
UNION ALL SELECT 'Seasons:', COUNT(*) FROM seasons
UNION ALL SELECT 'Episodes:', COUNT(*) FROM episodes
UNION ALL SELECT 'Genres:', COUNT(*) FROM genres
UNION ALL SELECT 'Content_Genres:', COUNT(*) FROM content_genres
UNION ALL SELECT 'Watch_History:', COUNT(*) FROM watch_history;
