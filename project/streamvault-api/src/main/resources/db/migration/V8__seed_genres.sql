-- V8: Seed genre data
INSERT INTO genres (id, name) VALUES
    (gen_random_uuid(), 'Acción'),
    (gen_random_uuid(), 'Aventura'),
    (gen_random_uuid(), 'Animación'),
    (gen_random_uuid(), 'Comedia'),
    (gen_random_uuid(), 'Crimen'),
    (gen_random_uuid(), 'Documental'),
    (gen_random_uuid(), 'Drama'),
    (gen_random_uuid(), 'Fantasía'),
    (gen_random_uuid(), 'Historia'),
    (gen_random_uuid(), 'Terror'),
    (gen_random_uuid(), 'Música'),
    (gen_random_uuid(), 'Misterio'),
    (gen_random_uuid(), 'Romance'),
    (gen_random_uuid(), 'Ciencia Ficción'),
    (gen_random_uuid(), 'Suspenso'),
    (gen_random_uuid(), 'Guerra'),
    (gen_random_uuid(), 'Western')
ON CONFLICT (name) DO NOTHING;
