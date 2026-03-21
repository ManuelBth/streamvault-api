# SDD — Catalog, Streaming & Watch History Service

## StreamVault Backend

---

> **Versión:** 1.3.0  
> **Fecha:** 2026-03-20  
> **Cambio:** Implementación de Catalog Service, Streaming Service, Watch History y Admin User Service

---

## 1. Objetivo del Cambio

Implementar los módulos faltantes del backend para permitir:
1. Gestión de catálogo de contenido (películas, series, temporadas, episodios)
2. Streaming de video con URLs pre-firmadas de MinIO
3. Historial de reproducción por perfil
4. Panel de administración de usuarios

**Nota importante:** Los videos se suben manualmente a MinIO (no hay conversión automática). El backend solo maneja metadata y genera presigned URLs.

---

## 2. Arquitectura Simplificada

```
┌─────────────────────────────────────────────────────────┐
│ MINIO (Upload Manual)                                     │
│                                                          │
│ Admin: Convierte MP4 → HLS con HandBrake local          │
│ Admin: Sube carpeta HLS a MinIO Dashboard               │
│                                                          │
│ streamvault-content/                                     │
│ ├── movies/{id}/hls/master.m3u8 + segments             │
│ └── series/{id}/seasons/{n}/episodes/{id}/hls/       │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│ SPRING BOOT BACKEND                                      │
│                                                          │
│ CatalogService ──► CRUD metadata (no videos)             │
│ StreamService ───► Genera presigned URLs                 │
│ MinioService ───► Solo para thumbnails                   │
│ WatchHistory ────► Progreso de reproducción              │
│ AdminUserService ► Listar usuarios                      │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Modelo de Datos

### 3.1 Content

```sql
CREATE TABLE content (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(10) NOT NULL CHECK (type IN ('MOVIE', 'SERIES')),
    release_year    INTEGER,
    rating          VARCHAR(10),
    thumbnail_key   VARCHAR(255),
    minio_key      VARCHAR(255),                    -- Solo para MOVIE
    status          VARCHAR(20) DEFAULT 'DRAFT',     -- DRAFT, PUBLISHED, UNPUBLISHED
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);
```

### 3.2 Season (solo para SERIES)

```sql
CREATE TABLE seasons (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id     UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    season_number  INTEGER NOT NULL,
    UNIQUE(content_id, season_number)
);
```

### 3.3 Episode (solo para SERIES)

```sql
CREATE TABLE episodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id       UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    episode_number  INTEGER NOT NULL,
    title           VARCHAR(255),
    description     TEXT,
    minio_key       VARCHAR(255) NOT NULL,           -- HLS path
    thumbnail_key   VARCHAR(255),
    duration_sec    INTEGER,
    status          VARCHAR(20) DEFAULT 'DRAFT',       -- DRAFT, READY, ERROR
    created_at      TIMESTAMP DEFAULT NOW(),
    UNIQUE(season_id, episode_number)
);
```

### 3.4 Genre

```sql
CREATE TABLE genres (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE content_genres (
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    genre_id   UUID REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (content_id, genre_id)
);
```

### 3.5 WatchHistory

```sql
CREATE TABLE watch_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id   UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    episode_id   UUID REFERENCES episodes(id),        -- NULL para movies
    content_id   UUID REFERENCES content(id),          -- Para movies
    progress_sec INTEGER NOT NULL DEFAULT 0,
    completed    BOOLEAN DEFAULT FALSE,
    watched_at   TIMESTAMP DEFAULT NOW(),
    UNIQUE(profile_id, episode_id),
    UNIQUE(profile_id, content_id)
);
```

---

## 4. Endpoints REST

### 4.1 CATÁLOGO

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | `/api/v1/catalog` | JWT | Listar contenido paginado |
| GET | `/api/v1/catalog/{id}` | JWT | Detalle de contenido |
| GET | `/api/v1/catalog/search?q=` | JWT | Buscar por título/género |
| GET | `/api/v1/catalog/{id}/seasons` | JWT | Temporadas de una serie |
| GET | `/api/v1/catalog/seasons/{id}/episodes` | JWT | Episodios de una temporada |
| GET | `/api/v1/catalog/genres` | JWT | Listar géneros |
| POST | `/api/v1/catalog` | JWT + ADMIN | Crear contenido |
| PUT | `/api/v1/catalog/{id}` | JWT + ADMIN | Editar contenido |
| DELETE | `/api/v1/catalog/{id}` | JWT + ADMIN | Eliminar contenido |

### 4.2 STREAMING

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | `/api/v1/stream/{contentId}` | JWT | Obtener presigned URL (movie) |
| GET | `/api/v1/stream/{contentId}/episode/{episodeId}` | JWT | Obtener presigned URL (episode) |

**Response:**
```json
{
  "url": "https://minio.../master.m3u8?presigned...",
  "expiresAt": "2026-03-20T15:00:00Z"
}
```

### 4.3 HISTORIAL

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | `/api/v1/history` | JWT | Historial del perfil activo |
| POST | `/api/v1/history` | JWT | Registrar inicio de reproducción |
| PUT | `/api/v1/history/{id}/progress` | JWT | Actualizar progreso |

### 4.4 ADMIN — USUARIOS

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| GET | `/api/v1/admin/users` | JWT + ADMIN | Listar usuarios |
| GET | `/api/v1/admin/users/{id}` | JWT + ADMIN | Detalle de usuario |

### 4.5 ADMIN — UPLOAD

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | `/api/v1/admin/upload/thumbnail` | JWT + ADMIN | Subir thumbnail |

---

## 5. DTOs

### ContentRequest

```java
public record ContentRequest(
    @NotBlank String title,
    String description,
    @NotNull ContentType type,  // MOVIE, SERIES
    Integer releaseYear,
    String rating,
    String thumbnailKey,
    String minioKey,           // Solo para movies
    List<UUID> genreIds,
    ContentStatus status       // DRAFT, PUBLISHED, UNPUBLISHED
) {}
```

### StreamResponse

```java
public record StreamResponse(
    String url,
    Instant expiresAt
) {}
```

### WatchHistoryRequest

```java
public record WatchHistoryRequest(
    UUID contentId,      // Para movies
    UUID episodeId,      // Para series
    Integer progressSec,
    Boolean completed
) {}
```

---

## 6. Flujo de Streaming

```
1. Usuario autenticado llama: GET /api/v1/stream/{contentId}
         │
         ▼
2. Backend verifica suscripción activa del usuario
         │
         ├── SUSCRIPCIÓN INACTIVA → 403 Forbidden
         │
         └── SUSCRIPCIÓN ACTIVA
                   │
                   ▼
         3. Obtener minio_key de content
                   │
                   ▼
         4. Generar presigned URL (2 horas)
                   │
                   ▼
         5. Response: { url, expiresAt }
                   │
                   ▼
         6. Angular pasa URL a HLS.js
                   │
                   ▼
         7. HLS.js carga manifest y segmentos DIRECTAMENTE desde MinIO
```

---

## 7. Flujo de Upload de Videos (Manual)

```
ADMIN (Flujo Manual)
         │
         ▼
1. Convierte MP4 → HLS con HandBrake (local)
         │
         ▼
2. Sube carpeta HLS a MinIO Dashboard:
   - movies/{id}/hls/master.m3u8
   - series/{id}/seasons/{n}/episodes/{id}/hls/master.m3u8
         │
         ▼
3. Crea contenido en backend:
   POST /api/v1/catalog
   {
     "title": "Mi Película",
     "type": "MOVIE",
     "minioKey": "movies/{id}/hls/master.m3u8"
   }
         │
         ▼
4. Backend guarda metadata en PostgreSQL
```

---

## 8. Estructura de Paquetes

```
com.betha.streamvault/
│
├── catalog/
│   ├── model/
│   │   ├── Content.java
│   │   ├── Season.java
│   │   ├── Episode.java
│   │   └── Genre.java
│   ├── repository/
│   │   ├── ContentRepository.java
│   │   ├── SeasonRepository.java
│   │   ├── EpisodeRepository.java
│   │   └── GenreRepository.java
│   ├── service/
│   │   └── CatalogService.java
│   ├── controller/
│   │   └── CatalogController.java
│   └── dto/
│       ├── ContentRequest.java
│       ├── ContentResponse.java
│       ├── SeasonResponse.java
│       └── EpisodeResponse.java
│
├── streaming/
│   ├── service/
│   │   ├── StreamService.java
│   │   └── MinioService.java
│   ├── controller/
│   │   └── StreamController.java
│   └── dto/
│       └── StreamResponse.java
│
├── upload/
│   ├── service/
│   │   └── UploadService.java
│   ├── controller/
│   │   └── UploadController.java
│   └── dto/
│       └── UploadResponse.java
│
├── history/
│   ├── model/
│   │   └── WatchHistory.java
│   ├── repository/
│   │   └── WatchHistoryRepository.java
│   ├── service/
│   │   └── HistoryService.java
│   ├── controller/
│   │   └── HistoryController.java
│   └── dto/
│       ├── WatchHistoryRequest.java
│       └── WatchHistoryResponse.java
│
├── admin/
│   ├── service/
│   │   └── AdminUserService.java
│   ├── controller/
│   │   └── AdminUserController.java
│   └── dto/
│       └── AdminUserResponse.java
```

---

## 9. Migraciones Flyway

| Migration | Descripción |
|-----------|-------------|
| V6__create_catalog_tables.sql | Tablas: content, seasons, episodes, genres, content_genres |
| V7__create_watch_history_table.sql | Tabla: watch_history |
| V8__seed_genres.sql | Datos iniciales de géneros |

---

## 10. Dependencias Adicionales

```xml
<!-- MinIO SDK ya está en el proyecto -->
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.7</version>
</dependency>
```

---

## 11. MinIO - Configuración

```yaml
# application-dev.yml
minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-content: streamvault-content
  bucket-thumbnails: streamvault-thumbnails
  presigned-expiry: 7200  # 2 horas en segundos
```

---

## 12. Tareas de Implementación

### Fase 1: Catalog Service
- [ ] Entidades (Content, Season, Episode, Genre)
- [ ] Repositories
- [ ] DTOs
- [ ] CatalogService
- [ ] CatalogController
- [ ] Migraciones Flyway
- [ ] Tests unitarios

### Fase 2: MinioService + Streaming
- [ ] MinioService (presigned URLs)
- [ ] StreamController
- [ ] Verificación de suscripción
- [ ] Tests

### Fase 3: Upload Service
- [ ] UploadService (solo thumbnails)
- [ ] UploadController
- [ ] Tests

### Fase 4: Watch History
- [ ] Entidad WatchHistory
- [ ] Repository
- [ ] DTOs
- [ ] HistoryService
- [ ] HistoryController
- [ ] Tests

### Fase 5: Admin User Service
- [ ] AdminUserService
- [ ] AdminUserController
- [ ] Tests

---

## 13. Criterios de Aceptación

1. ✅ Admin puede crear, editar y eliminar contenido
2. ✅ Usuarios pueden ver el catálogo
3. ✅ Usuarios autenticados con suscripción activa pueden ver videos
4. ✅ Usuarios sin suscripción reciben error 403
5. ✅ El historial de reproducción se guarda por perfil
6. ✅ Admin puede listar usuarios
7. ✅ Thumbnails se suben a MinIO
8. ✅ Videos se suben manualmente (HandBrake → MinIO Dashboard)
9. ✅ Todos los tests pasan

---

## 14. Decisiones Tomadas

| Decisión | Opción elegida |
|----------|---------------|
| Conversión de videos | Manual (HandBrake) |
| FFmpeg Worker | No se implementa |
| RabbitMQ | No se implementa |
| MinIO para thumbnails | Sí |
| MinIO para videos | Sí (upload manual) |
| HLS como formato | Sí |
| Presigned URLs | Sí (2 horas expiry) |
| Verificación de suscripción | Sí |

---

## 15. A Actualizar en PRD

Después de implementar, actualizar `PRD_Backend_StreamVault.md`:
- Sección 3.4 Catalog Service ✅
- Sección 3.5 Streaming Service ✅
- Sección 3.6 Upload Service ✅
- Sección 3.9 Watch History ✅
- Sección 4.4-4.6 Modelo de datos ✅
- Sección 5 Contrato de API ✅
