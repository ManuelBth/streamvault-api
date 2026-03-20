# PRD — Backend Service

## StreamVault by Betha

### Spring Boot 3.5.11 · Java 21 · Docker

---

> **Versión:** 1.2.0  
> **VM:** VM-2 — `192.168.1.20:8080`  
> **Equipo:** Betha  
> **Fecha:** 2025

---

## Tabla de Contenidos

1. [Visión General](#1-visión-general)
2. [Posición en la Arquitectura](#2-posición-en-la-arquitectura)
3. [Módulos y Servicios Internos](#3-módulos-y-servicios-internos)
4. [Modelo de Datos — Entidades](#4-modelo-de-datos--entidades)
5. [Contrato de API — Endpoints REST](#5-contrato-de-api--endpoints-rest)
6. [Protocolos y Conexiones](#6-protocolos-y-conexiones)
7. [Integración SMTP — Detalle Completo](#7-integración-smtp--detalle-completo)
8. [Subida de Archivos a MinIO](#8-subida-de-archivos-a-minio)
9. [WebSocket STOMP](#9-websocket-stomp)
10. [Seguridad](#10-seguridad)
11. [Dependencias — pom.xml](#11-dependencias--pomxml)
12. [Estructura de Paquetes](#12-estructura-de-paquetes)
13. [Perfiles de Configuración](#13-perfiles-de-configuración)
14. [Configuración Docker](#14-configuración-docker)
15. [Variables de Entorno](#15-variables-de-entorno)
16. [Extensiones e Plugins Recomendados](#16-extensiones-e-plugins-recomendados)

---

## 1. Visión General

El backend es el núcleo de la plataforma **StreamVault**. Se implementa como un **monolito modular** en Spring Boot 3 sobre Java 21, exponiendo una API REST reactiva (WebFlux) consumida por el frontend Angular en VM-1.

### Responsabilidades principales

- **Autenticación y autorización** mediante JWT RS256 con dos roles: `ROLE_USER` y `ROLE_ADMIN`
- **Catálogo de contenido**: CRUD completo de películas, series, temporadas y episodios (administrado por ADMIN)
- **Streaming de video**: generación de URLs pre-firmadas hacia MinIO para que el player HLS descargue los segmentos directamente
- **Subida de archivos**: recepción de videos y miniaturas desde el frontend y almacenamiento en MinIO
- **Gestión de usuarios**: perfiles múltiples por cuenta, historial de reproducción
- **Panel Admin**: endpoints exclusivos para listar y consultar usuarios registrados
- **Correo electrónico**: emails transaccionales del sistema (bienvenida, confirmación, etc.) Y reenvío de mensajes entre usuarios vía SMTP
- **Tiempo real**: notificaciones push via WebSocket STOMP

---

## 2. Posición en la Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                      AS INTERNA — GNS3                      │
│                                                             │
│  [VM-1 Frontend]          [VM-2 Backend]   [VM-3 Servicios] │
│  Angular + NGINX    ◄───► Spring Boot 3 ◄──► PostgreSQL     │
│  192.168.1.10             192.168.1.20      Postfix SMTP     │
│       ▲                        ▲            BIND9 DNS        │
│       │   HTTPS/WSS            │            MinIO            │
│       └────────────────────────┘            192.168.1.30     │
└─────────────────────────────────────────────────────────────┘
```

### Tabla de conexiones

| VM / Servicio                    | IP : Puerto         | Protocolo    | Relación con Backend           |
| -------------------------------- | ------------------- | ------------ | ------------------------------ |
| VM-1 Frontend (Angular + NGINX)  | `192.168.1.10:443`  | HTTPS / WSS  | Consume REST API y WebSocket   |
| **VM-2 Backend (Spring Boot 3)** | `192.168.1.20:8080` | —            | **Este servicio**              |
| VM-3 BIND9 DNS                   | `192.168.1.30:53`   | UDP/TCP DNS  | Resolución de nombres internos |
| VM-3 Postfix SMTP                | `192.168.1.30:25`   | TCP SMTP     | Envío de todos los emails      |
| VM-3 PostgreSQL                  | `192.168.1.30:5432` | TCP / R2DBC  | Persistencia relacional        |
| VM-3 MinIO                       | `192.168.1.30:9000` | TCP / S3 API | Videos, miniaturas, HLS        |

### Diagrama de conexiones salientes

```
[Spring Boot :8080]
    │

    ├── SMTP :25   ──►  Postfix          (JavaMailSender)
    ├── HTTP :9000 ──►  MinIO            (MinIO SDK S3)
    └── UDP :53    ──►  BIND9 DNS        (resolución interna)
```

---

## 3. Módulos y Servicios Internos

El proyecto sigue arquitectura de **monolito modular por dominio**. Cada módulo tiene su propio `Controller`, `Service`, `Repository` y carpeta `model/`. Pueden extraerse como microservicios independientes en una fase futura.

---

### 3.1 Auth Service

| Aspecto            | Detalle                                                                      |
| ------------------ | ---------------------------------------------------------------------------- |
| Responsabilidad    | Registro, login, refresco y revocación de tokens                             |
| Protocolo expuesto | `HTTPS REST → POST /api/v1/auth/**`                                          |
| Seguridad          | JWT access token (15 min, RS256) + Refresh Token en HttpOnly Cookie (7 días) |
| Roles soportados   | `ROLE_USER`, `ROLE_ADMIN`                                                    |
| Email              | Dispara email de bienvenida y confirmación al registrar usuario              |
| Endpoints          | `POST /register` `POST /login` `POST /refresh` `POST /logout` `GET /confirm` |

---

public class RegisterRequest {

    @NotBlank
    @Email
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@streamvault\\.local$",
        message = "Solo se permiten emails del dominio @streamvault.local"
    )
    private String email;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;

    @NotBlank
    private String name;

}

```

> Esta validación rechaza en el backend cualquier email que no sea `@streamvault.local`, garantizando que Postfix siempre pueda entregar localmente. El frontend Angular debe reflejar este mismo mensaje de error al usuario en el formulario de registro.

---

## Dónde viven estos dos archivos en la estructura del proyecto
```

streamvault-backend/
├── src/
│ └── main/
│ ├── java/com/betha/streamvault/
│ │ └── auth/
│ │ └── dto/
│ │ └── RegisterRequest.java ← Ajuste 2
│ └── resources/
│ ├── application.yml (configuración base)
│ └── application-prod.yml ← Ajuste 1

### 3.2 User & Profile Service

| Aspecto            | Detalle                                                                                           |
| ------------------ | ------------------------------------------------------------------------------------------------- |
| Responsabilidad    | Gestión de cuentas de usuario y perfiles múltiples (máx. 4 por cuenta)                            |
| Protocolo expuesto | `HTTPS REST → /api/v1/users/**` `/api/v1/profiles/**`                                             |
| BD                 | Tablas: `users`, `profiles`, `subscriptions` en PostgreSQL                                        |
| Endpoints          | `GET /me` `PUT /me` `GET /profiles` `POST /profiles` `PUT /profiles/{id}` `DELETE /profiles/{id}` |

---

### 3.3 Admin User Service _(nuevo en v1.1)_

| Aspecto            | Detalle                                                                                            |
| ------------------ | -------------------------------------------------------------------------------------------------- |
| Responsabilidad    | Acceso exclusivo del ADMIN para consultar y listar todos los usuarios registrados en la plataforma |
| Protocolo expuesto | `HTTPS REST → /api/v1/admin/users/**`                                                              |
| Autorización       | Solo accesible con `ROLE_ADMIN` — protegido en Spring Security                                     |
| BD                 | Consultas sobre la tabla `users` con paginación y filtros                                          |
| Endpoints          | `GET /admin/users` `GET /admin/users/{id}`                                                         |

---

### 3.4 Catalog Service

| Aspecto            | Detalle                                                                            |
| ------------------ | ---------------------------------------------------------------------------------- |
| Responsabilidad    | CRUD completo de películas, series, temporadas y episodios                         |
| Protocolo expuesto | `HTTPS REST → /api/v1/catalog/**`                                                  |
| Autorización       | Lectura: `ROLE_USER` y `ROLE_ADMIN`. Escritura/edición/borrado: solo `ROLE_ADMIN`  |
| BD                 | Tablas: `content`, `seasons`, `episodes`, `genres`, `content_genres` en PostgreSQL |

---

### 3.5 Streaming Service

| Aspecto            | Detalle                                                                                                               |
| ------------------ | --------------------------------------------------------------------------------------------------------------------- |
| Responsabilidad    | Generar URL pre-firmada de MinIO para que el player HLS descargue los segmentos directamente sin pasar por el backend |
| Protocolo expuesto | `HTTPS REST → GET /api/v1/stream/{contentId}`                                                                         |
| Protocolo interno  | `HTTP TCP :9000 → MinIO SDK (S3-compatible)`                                                                          |
| Seguridad          | Valida suscripción activa del usuario antes de generar la URL. URL firmada con expiración de 2 horas                  |
| Formato de video   | HLS — manifest `.m3u8` + segmentos `.ts` almacenados en MinIO                                                         |

---

### 3.6 Upload Service _(nuevo en v1.1)_

| Aspecto            | Detalle                                                                                   |
| ------------------ | ----------------------------------------------------------------------------------------- |
| Responsabilidad    | Recibir archivos de video y miniaturas desde el frontend (panel Admin) y subirlos a MinIO |
| Protocolo expuesto | `HTTPS Multipart → POST /api/v1/admin/upload/**`                                          |
| Protocolo interno  | `HTTP TCP :9000 → MinIO SDK`                                                              |
| Autorización       | Solo `ROLE_ADMIN`                                                                         |
| Tipos soportados   | Video: `.mp4` → se procesa y guarda como HLS. Miniatura: `.jpg`, `.png`, `.webp`          |
| Respuesta          | Devuelve la `minio_key` generada para asociarla al contenido                              |

> **Nota:** La conversión de MP4 a HLS (segmentación en `.m3u8` + `.ts`) se realiza en el servidor con `ffmpeg` ejecutado desde el servicio de upload antes de almacenar en MinIO.

---

### 3.7 Mail Service — Transaccional y Entre Usuarios

| Aspecto         | Detalle                                                                                                                                            |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| Responsabilidad | Envío de **dos tipos** de correo: (1) emails transaccionales del sistema y (2) reenvío de mensajes de un usuario hacia otro usuario por email real |
| Protocolo       | `SMTP TCP :25 → Postfix (mail.netflix.local)` resuelto por BIND9                                                                                   |
| Librería        | `spring-boot-starter-mail` + `JavaMailSender` + `Thymeleaf`                                                                                        |

#### Tipo 1 — Emails Transaccionales del Sistema

| Evento                    | Trigger en código                | Asunto                     | Template            |
| ------------------------- | -------------------------------- | -------------------------- | ------------------- |
| Registro de usuario       | `AuthService.register()`         | Bienvenido a StreamVault   | `welcome.html`      |
| Confirmación de email     | `AuthService.sendVerification()` | Confirma tu cuenta         | `verify-email.html` |
| Cambio de contraseña      | `UserService.changePassword()`   | Tu contraseña fue cambiada | `pwd-changed.html`  |
| Nuevo contenido publicado | `ContentService.publish()`       | Nuevo contenido disponible | `new-content.html`  |

#### Tipo 2 — Mensajes Entre Usuarios (Formulario de Contacto)

El frontend Angular expone un formulario de contacto donde el usuario autenticado puede escribir un mensaje con asunto, cuerpo y el email del destinatario. Este formulario hace `POST /api/v1/mail/send` al backend, que valida los datos y reenvía el mensaje vía SMTP usando Postfix.

```
[Usuario X — Angular]
    └── POST /api/v1/mail/send
          { to: "usuarioy@example.com", subject: "...", body: "..." }
              └── MailService.sendUserMessage()
                    └── JavaMailSender → SMTP :25
                          └── Postfix (mail.netflix.local)
                                └── Entrega al email real del destinatario
                                    (o relay externo si la AS está conectada)
```

**Restricciones de seguridad del endpoint:**

- Solo usuarios con JWT válido pueden enviarlo (autenticados)
- El campo `from` se extrae del JWT — el usuario no puede falsificar el remitente
- Rate limiting: máximo 5 emails por usuario por hora (Bucket4j)
- El campo `to` se valida como email válido con Bean Validation
- El cuerpo del mensaje se sanea para prevenir inyección de headers SMTP

---

### 3.8 WebSocket Service

| Aspecto         | Detalle                                                                                          |
| --------------- | ------------------------------------------------------------------------------------------------ |
| Responsabilidad | Canal bidireccional de eventos en tiempo real hacia el frontend Angular                          |
| Protocolo       | `WSS :8080/ws` — WebSocket sobre TLS con subprotocolo STOMP                                      |
| Librería        | `spring-boot-starter-websocket` + `spring-messaging` + SockJS fallback                           |
| Tópicos         | `/topic/notifications` `/topic/content-updates` `/user/queue/alerts`                             |
| Casos de uso    | Nuevo episodio disponible · Confirmación de email enviado · Notificación de nuevo email recibido |

---

### 3.9 Watch History Service

| Aspecto            | Detalle                                                     |
| ------------------ | ----------------------------------------------------------- |
| Responsabilidad    | Registro y consulta del progreso de reproducción por perfil |
| Protocolo expuesto | `HTTPS REST → /api/v1/history/**`                           |
| BD                 | Tabla `watch_history` en PostgreSQL                         |
| Endpoints          | `POST /history` `GET /history` `PUT /history/{id}/progress` |

---

## 4. Modelo de Datos — Entidades

### 4.1 User

```sql
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,           -- BCrypt strength 12
    role            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    is_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    subscription_id UUID REFERENCES subscriptions(id)
);
```

| Campo             | Tipo         | Descripción                |
| ----------------- | ------------ | -------------------------- |
| `id`              | UUID         | PK auto-generado           |
| `email`           | VARCHAR(255) | Único, clave de acceso     |
| `password_hash`   | VARCHAR(255) | BCrypt strength 12         |
| `role`            | VARCHAR(20)  | `ROLE_USER` / `ROLE_ADMIN` |
| `is_verified`     | BOOLEAN      | Confirmación de email      |
| `created_at`      | TIMESTAMP    | Fecha de registro          |
| `subscription_id` | UUID (FK)    | → `subscriptions.id`       |

---

### 4.2 Profile

```sql
CREATE TABLE profiles (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(50)  NOT NULL,
    avatar_url VARCHAR(255),
    is_kids    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT max_profiles_per_user CHECK (
        (SELECT COUNT(*) FROM profiles p WHERE p.user_id = user_id) <= 4
    )
);
```

---

### 4.3 Subscription

```sql
CREATE TABLE subscriptions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID UNIQUE NOT NULL REFERENCES users(id),
    plan       VARCHAR(20) NOT NULL,   -- BASIC / STANDARD / PREMIUM
    started_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP   NOT NULL,
    active     BOOLEAN     NOT NULL DEFAULT TRUE
);
```

---

### 4.4 Content

```sql
CREATE TABLE content (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(10)  NOT NULL,   -- MOVIE / SERIES
    release_year    INTEGER,
    rating          VARCHAR(10),             -- PG / R / TV-MA / etc.
    thumbnail_key   VARCHAR(255),            -- clave MinIO de la miniatura
    minio_base_key  VARCHAR(255),            -- ruta base HLS en MinIO (solo MOVIE)
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by      UUID REFERENCES users(id)  -- ADMIN que lo creó
);
```

---

### 4.5 Season y Episode

```sql
CREATE TABLE seasons (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content_id     UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    season_number  INTEGER NOT NULL,
    UNIQUE(content_id, season_number)
);

CREATE TABLE episodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    season_id       UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    episode_number  INTEGER NOT NULL,
    title           VARCHAR(255),
    description     TEXT,
    minio_key       VARCHAR(255) NOT NULL,   -- ruta HLS del episodio en MinIO
    duration_sec    INTEGER,
    UNIQUE(season_id, episode_number)
);
```

---

### 4.6 Genre y relación N:N

```sql
CREATE TABLE genres (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE content_genres (
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    genre_id   UUID REFERENCES genres(id)  ON DELETE CASCADE,
    PRIMARY KEY (content_id, genre_id)
);
```

---

### 4.7 WatchHistory

```sql
CREATE TABLE watch_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id   UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    episode_id   UUID NOT NULL REFERENCES episodes(id),
    progress_sec INTEGER NOT NULL DEFAULT 0,
    watched_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(profile_id, episode_id)
);
```

---

### 4.8 RefreshToken

```sql
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,   -- SHA-256 del token
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

---

### 4.9 Mail Message _(nuevo en v1.1)_

Registra los mensajes enviados entre usuarios para trazabilidad y auditoría.

```sql
CREATE TABLE mail_messages (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id   UUID NOT NULL REFERENCES users(id),
    to_email    VARCHAR(255) NOT NULL,
    subject     VARCHAR(255) NOT NULL,
    body        TEXT         NOT NULL,
    sent_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    status      VARCHAR(20)  NOT NULL DEFAULT 'SENT'  -- SENT / FAILED
);
```

---

### Diagrama de relaciones

```
users ──────────── subscriptions  (1:1)
  │
  ├──────────────── profiles  (1:N, máx 4)
  │                    │
  │                    └──── watch_history ──── episodes
  │
  ├──────────────── refresh_tokens  (1:N)
  └──────────────── mail_messages  (1:N — como sender)

content ─────────── seasons  (1:N)
  │                    └──── episodes  (1:N)
  │
  └──────────────── content_genres ──── genres  (N:N)
```

---

## 5. Contrato de API — Endpoints REST

### AUTH

| Método | Endpoint                      | Auth    | Descripción                             |
| ------ | ----------------------------- | ------- | --------------------------------------- |
| `POST` | `/api/v1/auth/register`       | Público | Registrar usuario + email de bienvenida |
| `POST` | `/api/v1/auth/login`          | Público | Login → JWT + Refresh Cookie            |
| `POST` | `/api/v1/auth/refresh`        | Cookie  | Renovar access token                    |
| `POST` | `/api/v1/auth/logout`         | JWT     | Revocar refresh token                   |
| `GET`  | `/api/v1/auth/confirm?token=` | Público | Confirmar email del usuario             |

---

### USUARIOS & PERFILES

| Método   | Endpoint                | Auth | Descripción                   |
| -------- | ----------------------- | ---- | ----------------------------- |
| `GET`    | `/api/v1/users/me`      | JWT  | Datos del usuario autenticado |
| `PUT`    | `/api/v1/users/me`      | JWT  | Actualizar datos del usuario  |
| `GET`    | `/api/v1/profiles`      | JWT  | Listar perfiles de la cuenta  |
| `POST`   | `/api/v1/profiles`      | JWT  | Crear nuevo perfil (máx. 4)   |
| `PUT`    | `/api/v1/profiles/{id}` | JWT  | Actualizar nombre / avatar    |
| `DELETE` | `/api/v1/profiles/{id}` | JWT  | Eliminar perfil               |

---

### ADMIN — USUARIOS _(nuevo en v1.1)_

| Método | Endpoint                   | Auth        | Descripción                                                          |
| ------ | -------------------------- | ----------- | -------------------------------------------------------------------- |
| `GET`  | `/api/v1/admin/users`      | JWT + ADMIN | Listar todos los usuarios (paginado, filtrable por email/rol/estado) |
| `GET`  | `/api/v1/admin/users/{id}` | JWT + ADMIN | Detalle completo de un usuario específico                            |

**Query params de `/api/v1/admin/users`:**

| Param      | Tipo    | Descripción                            |
| ---------- | ------- | -------------------------------------- |
| `page`     | int     | Número de página (default: 0)          |
| `size`     | int     | Elementos por página (default: 20)     |
| `role`     | string  | Filtrar por `ROLE_USER` o `ROLE_ADMIN` |
| `verified` | boolean | Filtrar por estado de verificación     |
| `search`   | string  | Búsqueda por email (LIKE)              |

---

### CATÁLOGO

| Método   | Endpoint                                | Auth        | Descripción                                   |
| -------- | --------------------------------------- | ----------- | --------------------------------------------- |
| `GET`    | `/api/v1/catalog`                       | JWT         | Listar contenido paginado (page, size, genre) |
| `GET`    | `/api/v1/catalog/{id}`                  | JWT         | Detalle de película o serie                   |
| `GET`    | `/api/v1/catalog/search?q=`             | JWT         | Búsqueda por título o género                  |
| `GET`    | `/api/v1/catalog/{id}/seasons`          | JWT         | Temporadas de una serie                       |
| `GET`    | `/api/v1/catalog/seasons/{id}/episodes` | JWT         | Episodios de una temporada                    |
| `POST`   | `/api/v1/catalog`                       | JWT + ADMIN | Crear contenido (metadatos)                   |
| `PUT`    | `/api/v1/catalog/{id}`                  | JWT + ADMIN | Editar metadatos del contenido                |
| `DELETE` | `/api/v1/catalog/{id}`                  | JWT + ADMIN | Eliminar contenido                            |

---

### ADMIN — UPLOAD DE ARCHIVOS _(nuevo en v1.1)_

| Método | Endpoint                         | Auth        | Descripción                                      |
| ------ | -------------------------------- | ----------- | ------------------------------------------------ |
| `POST` | `/api/v1/admin/upload/video`     | JWT + ADMIN | Subir archivo de video (multipart) → MinIO → HLS |
| `POST` | `/api/v1/admin/upload/thumbnail` | JWT + ADMIN | Subir miniatura (multipart) → MinIO              |

**Request body de `/api/v1/admin/upload/video`:**

```
Content-Type: multipart/form-data

file        : archivo de video (.mp4)
contentId   : UUID del contenido al que pertenece (opcional si se crea junto al contenido)
episodeId   : UUID del episodio (si aplica para series)
```

**Response:**

```json
{
  "minioKey": "content/{contentId}/hls/master.m3u8",
  "thumbnailKey": null,
  "status": "UPLOADED"
}
```

---

### STREAMING & HISTORIAL

| Método | Endpoint                        | Auth | Descripción                        |
| ------ | ------------------------------- | ---- | ---------------------------------- |
| `GET`  | `/api/v1/stream/{contentId}`    | JWT  | URL pre-firmada MinIO para HLS     |
| `POST` | `/api/v1/history`               | JWT  | Registrar inicio de reproducción   |
| `GET`  | `/api/v1/history`               | JWT  | Historial del perfil activo        |
| `PUT`  | `/api/v1/history/{id}/progress` | JWT  | Actualizar segundo de reproducción |

---

### CORREO ENTRE USUARIOS _(nuevo en v1.1)_

| Método | Endpoint            | Auth | Descripción                                               |
| ------ | ------------------- | ---- | --------------------------------------------------------- |
| `POST` | `/api/v1/mail/send` | JWT  | Enviar email a otro usuario vía SMTP                      |
| `GET`  | `/api/v1/mail/sent` | JWT  | Historial de mensajes enviados por el usuario autenticado |

**Request body de `POST /api/v1/mail/send`:**

```json
{
  "to": "usuario@ejemplo.com",
  "subject": "Asunto del mensaje",
  "body": "Cuerpo del mensaje de texto plano o HTML básico"
}
```

**Validaciones aplicadas:**

- `to` → formato email válido (Bean Validation `@Email`)
- `subject` → no vacío, máx. 255 caracteres
- `body` → no vacío, máx. 5000 caracteres
- `from` → extraído del JWT, no editable por el usuario
- Rate limiting → máx. 5 envíos por hora por usuario (Bucket4j)

---

## 6. Protocolos y Conexiones

### 6.1 Conexiones salientes (Backend → otros servicios)

| Destino           | Protocolo            | Puerto  | Propósito                                                                                 |
| ----------------- | -------------------- | ------- | ----------------------------------------------------------------------------------------- |
| VM-3 PostgreSQL   | TCP / R2DBC reactivo | `:5432` | Persistencia de todas las entidades                                                       |
| VM-3 Postfix SMTP | TCP / SMTP           | `:25`   | Emails transaccionales + mensajes entre usuarios                                          |
| VM-3 MinIO        | TCP / HTTP S3 API    | `:9000` | URLs pre-firmadas HLS + subida de videos y miniaturas                                     |
| VM-3 BIND9 DNS    | UDP / DNS            | `:53`   | Resolución de `mail.streamvault.local`, `db.streamvault.local`, `minio.streamvault.local` |

### 6.2 Conexiones entrantes (clientes → Backend)

| Origen               | Protocolo     | Puerto / Path | Propósito                     |
| -------------------- | ------------- | ------------- | ----------------------------- |
| VM-1 NGINX / Angular | HTTPS / HTTP2 | `:8080`       | Todas las peticiones REST API |
| VM-1 Angular         | WSS / STOMP   | `:8080/ws`    | Canal WebSocket tiempo real   |

### 6.3 Protocolos por capa OSI

| Capa OSI            | Protocolo                         | Uso en el backend                                |
| ------------------- | --------------------------------- | ------------------------------------------------ |
| **L7 Aplicación**   | HTTP/2, HTTPS, SMTP, DNS, S3 API  | API REST, email, resolución de nombres, MinIO    |
| **L6 Presentación** | TLS 1.3, JWT (RS256)              | Cifrado en tránsito, tokens de autenticación     |
| **L5 Sesión**       | WebSocket (STOMP), OAuth2 Session | Canal tiempo real, gestión de sesiones           |
| **L4 Transporte**   | TCP, UDP                          | HTTP/HTTPS/SMTP usan TCP; DNS usa UDP            |
| **L3 Red**          | IPv4                              | Comunicación entre VMs en la AS `192.168.1.0/24` |

---

## 7. Integración SMTP — Detalle Completo

### 7.1 Configuración `application.yml`

```yaml
spring:
  mail:
    host: mail.streamvault.local
    port: 25
    username: noreply@streamvault.local # ← buzón del sistema en Dovecot
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false
      mail.smtp.connectiontimeout: 5000
      mail.smtp.timeout: 5000
```

### 7.2 Flujo completo de email transaccional

```
Usuario registra cuenta
  └─► AuthService.register()
        ├─► UserRepository.save(user)
        └─► NotificationService.sendWelcome(user)
              └─► JavaMailSender.send(MimeMessage)
                    └─► SMTP TCP:25 → Postfix (mail.netflix.local)
                          └─► Entrega en buzón local (Dovecot / IMAP :143)
```

### 7.3 Flujo completo de email entre usuarios

```
Usuario X rellena formulario en Angular
  └─► POST /api/v1/mail/send  { to, subject, body }
        └─► MailController → MailService.sendUserMessage()
              ├─► Validación Bean Validation (@Email, @NotBlank)
              ├─► Rate limit check (Bucket4j — 5/hora)
              ├─► MailMessageRepository.save() → PostgreSQL (auditoría)
              ├─► MimeMessage builder:
              │     from:    noreply@streamvault.local
              │     replyTo: email del Usuario X (extraído del JWT)
              │     to:      email del destinatario
              │     subject: [StreamVault] {subject}
              │     body:    template Thymeleaf user-message.html
              └─► JavaMailSender.send()
                    └─► SMTP TCP:25 → Postfix
                          └─► Relay externo (si AS conectada a otra AS)
                                └─► Entrega al email real del Usuario Y
```

> **Nota sobre `replyTo`:** El campo `from` siempre es `noreply@streamvault.local` para que Postfix lo acepte. El email real del remitente se pone en `Reply-To`, así el destinatario puede responder directamente al Usuario X desde su cliente de correo.

---

## 8. Subida de Archivos a MinIO

### 8.1 Flujo de subida de video (ADMIN)

```
ADMIN sube video desde el panel Angular
  └─► POST /api/v1/admin/upload/video (multipart/form-data)
        └─► UploadController → UploadService
              ├─► Validar tipo de archivo (solo .mp4)
              ├─► Guardar temporalmente en /tmp/{uuid}.mp4
              ├─► Ejecutar ffmpeg para segmentar en HLS:
              │     ffmpeg -i input.mp4 \
              │       -codec: copy \
              │       -start_number 0 \
              │       -hls_time 10 \
              │       -hls_list_size 0 \
              │       -f hls /tmp/{uuid}/master.m3u8
              ├─► Subir todos los archivos .m3u8 y .ts a MinIO:
              │     Bucket: streamvault-videos
              │     Key:    content/{contentId}/hls/
              ├─► Limpiar archivos temporales de /tmp
              └─► Retornar minioKey al controlador
```

### 8.2 Estructura en MinIO

```
Bucket: streamvault-videos
  └── content/
        └── {contentId}/
              ├── hls/
              │     ├── master.m3u8
              │     ├── segment000.ts
              │     ├── segment001.ts
              │     └── ...
              └── thumbnail.jpg

Bucket: streamvault-thumbnails
  └── content/
        └── {contentId}/
              └── thumbnail.{ext}
```

### 8.3 Generación de URL pre-firmada para el player

```java
// StreamService.java
public String generatePresignedUrl(UUID contentId) {
    // 1. Verificar suscripción activa del usuario
    // 2. Obtener minio_base_key del contenido
    // 3. Generar URL pre-firmada con expiración de 2 horas
    GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket("streamvault-videos")
        .object(minioKey + "/hls/master.m3u8")
        .expiry(2, TimeUnit.HOURS)
        .build();
    return minioClient.getPresignedObjectUrl(args);
}
```

---

## 9. WebSocket STOMP

### 9.1 Configuración

```java
// WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://192.168.1.10")
                .withSockJS();  // fallback para navegadores sin WS nativo
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

### 9.2 Tópicos y casos de uso

| Tópico                   | Tipo      | Descripción                                                     | Disparado por                |
| ------------------------ | --------- | --------------------------------------------------------------- | ---------------------------- |
| `/topic/notifications`   | Broadcast | Notificación global de nuevo contenido publicado                | `ContentService.publish()`   |
| `/topic/content-updates` | Broadcast | Actualización o edición de contenido existente                  | `ContentService.update()`    |
| `/user/queue/alerts`     | Personal  | Alerta de sesión próxima a expirar, email enviado correctamente | `AuthService`, `MailService` |

### 9.3 Flujo de notificación de nuevo contenido

```
ADMIN publica contenido nuevo
  └─► ContentService.save()
        └─► NotificationPublisher.notifyNewContent(content)
              └─► SimpMessagingTemplate.convertAndSend(
                    "/topic/notifications",
                    new ContentNotificationDTO(content)
                  )
                    └─► WebSocket → todos los clientes conectados
                          └─► Angular NotificationToastComponent
```

---

## 10. Seguridad

### 10.1 Capas de seguridad

| Capa          | Mecanismo       | Detalle                                                                    |
| ------------- | --------------- | -------------------------------------------------------------------------- |
| Transporte    | TLS 1.3         | Todo tráfico externo HTTPS / WSS                                           |
| Autenticación | JWT RS256       | Access token 15 min firmado con clave privada RSA-2048                     |
| Sesión        | HttpOnly Cookie | Refresh token — inaccesible desde JavaScript                               |
| API           | CORS Filter     | Permite solo `Origin: https://192.168.1.10`                                |
| API           | Rate Limiting   | Bucket4j — 100 req/min por IP en `/auth/**`; 5 emails/hora en `/mail/send` |
| Contraseñas   | BCrypt (12)     | Hash con coste computacional elevado                                       |
| MinIO         | URL pre-firmada | Expira en 2 h; valida suscripción activa antes de generar                  |
| Admin         | `@PreAuthorize` | Endpoints `/admin/**` requieren `ROLE_ADMIN`                               |
| Email         | Reply-To        | Campo `from` fijo en `noreply@` — el remitente va en `Reply-To`            |

### 10.2 Configuración CORS

```java
// CorsConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://192.168.1.10"));
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With"));
    config.setAllowCredentials(true);  // necesario para cookies HttpOnly
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

### 10.3 Protección de rutas en Spring Security

```java
// SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers(HttpMethod.GET, "/api/v1/catalog/**").authenticated()
    .requestMatchers(HttpMethod.POST, "/api/v1/catalog/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.PUT,  "/api/v1/catalog/**").hasRole("ADMIN")
    .requestMatchers(HttpMethod.DELETE,"/api/v1/catalog/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/mail/send").authenticated()
    .anyRequest().authenticated()
);
```

---

## 11. Dependencias — pom.xml

### Parent

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.11</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>
```

### Core Framework

| Dependencia                                  | Versión  | Propósito                                   |
| -------------------------------------------- | -------- | ------------------------------------------- |
| `spring-boot-starter-webflux`                | Boot 3.x | API REST reactiva (Project Reactor + Netty) |
| `spring-boot-starter-security`               | Boot 3.x | Filtros, CORS, BCrypt, @PreAuthorize        |
| `spring-boot-starter-oauth2-resource-server` | Boot 3.x | Validación JWT RS256                        |
| `spring-boot-starter-mail`                   | Boot 3.x | JavaMailSender — integración SMTP           |
| `spring-boot-starter-thymeleaf`              | Boot 3.x | Templates HTML para todos los emails        |
| `thymeleaf-extras-springsecurity6`           | 3.x      | Integración Thymeleaf + Spring Security     |
| `spring-boot-starter-websocket`              | Boot 3.x | WebSocket + STOMP + SockJS                  |
| `spring-boot-starter-validation`             | Boot 3.x | Bean Validation — DTOs y formularios        |
| `spring-boot-starter-actuator`               | Boot 3.x | Health checks y métricas                    |

### Persistencia

| Dependencia                      | Versión  | Propósito                         |
| -------------------------------- | -------- | --------------------------------- |
| `spring-boot-starter-data-r2dbc` | Boot 3.x | Repositorios reactivos PostgreSQL |
| `r2dbc-postgresql` (io.r2dbc)    | 1.0.x    | Driver R2DBC para PostgreSQL      |
| `postgresql` (org.postgresql)    | 42.x     | Driver JDBC requerido por Flyway  |
| `flyway-core`                    | 10.x     | Migraciones de esquema de BD      |
| `flyway-database-postgresql`     | 10.x     | Soporte Flyway para PostgreSQL    |

### Seguridad & Tokens

| Dependencia                    | Versión | Propósito                             |
| ------------------------------ | ------- | ------------------------------------- |
| `jjwt-api` (io.jsonwebtoken)   | 0.12.x  | API para creación y validación de JWT |
| `jjwt-impl`                    | 0.12.x  | Implementación JJWT (runtime)         |
| `jjwt-jackson`                 | 0.12.x  | Serialización JSON para JWT           |
| `bucket4j-core` (com.bucket4j) | 8.x     | Rate limiting por token bucket        |

### MinIO & Almacenamiento

| Dependencia                     | Versión | Propósito                           |
| ------------------------------- | ------- | ----------------------------------- |
| `minio` (io.minio)              | 8.5.x   | SDK oficial MinIO / S3-compatible   |
| `okhttp` (com.squareup.okhttp3) | 4.x     | HTTP client requerido por MinIO SDK |

### Utilidades

| Dependencia                            | Versión | Propósito                                          |
| -------------------------------------- | ------- | -------------------------------------------------- |
| `lombok`                               | 1.18.x  | `@Data`, `@Builder`, `@Slf4j` — reduce boilerplate |
| `mapstruct`                            | 1.5.x   | Mapeo Entity ↔ DTO en tiempo de compilación        |
| `springdoc-openapi-starter-webflux-ui` | 2.x     | Swagger UI / OpenAPI 3 en `/swagger-ui.html`       |
| `micrometer-registry-prometheus`       | 1.x     | Métricas exportadas a Prometheus / Grafana         |

### Testing

| Dependencia                           | Scope | Propósito                                                    |
| ------------------------------------- | ----- | ------------------------------------------------------------ |
| `spring-boot-starter-test`            | test  | JUnit 5, Mockito, AssertJ                                    |
| `reactor-test` (io.projectreactor)    | test  | `StepVerifier` para flujos reactivos                         |
| `spring-security-test`                | test  | `@WithMockUser`, `SecurityMockMvc`                           |
| `testcontainers` (org.testcontainers) | test  | PostgreSQL y MinIO en contenedores para tests de integración |

---

```
com.betha.streamvault/
│
├── StreamVaultApplication.java
│
├── auth/
│   ├── model/
│   │   └── RefreshToken.java
│   ├── repository/
│   │   └── RefreshTokenRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   └── RefreshTokenService.java
│   ├── controller/
│   │   └── AuthController.java
│   └── dto/
│       ├── LoginRequest.java
│       ├── RegisterRequest.java
│       └── TokenResponse.java
│
├── user/
│   ├── model/
│   │   ├── User.java
│   │   ├── Profile.java
│   │   └── Subscription.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProfileRepository.java
│   │   └── SubscriptionRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── ProfileService.java
│   ├── controller/
│   │   ├── UserController.java
│   │   └── ProfileController.java
│   └── dto/
│       ├── UserResponse.java
│       ├── UpdateUserRequest.java
│       ├── ProfileResponse.java
│       └── CreateProfileRequest.java
│
├── admin/
│   ├── service/
│   │   └── AdminUserService.java
│   ├── controller/
│   │   └── AdminUserController.java
│   └── dto/
│       ├── AdminUserResponse.java
│       └── AdminUserFilterRequest.java
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
│       ├── ContentResponse.java
│       ├── ContentRequest.java
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
│       └── StreamUrlResponse.java
│
├── upload/
│   ├── service/
│   │   ├── UploadService.java
│   │   └── FfmpegService.java
│   ├── controller/
│   │   └── UploadController.java
│   └── dto/
│       ├── UploadRequest.java
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
│       ├── WatchHistoryResponse.java
│       └── ProgressUpdateRequest.java
│
├── mail/
│   ├── model/
│   │   └── MailMessage.java
│   ├── repository/
│   │   └── MailMessageRepository.java
│   ├── service/
│   │   └── MailService.java
│   ├── controller/
│   │   └── MailController.java
│   └── dto/
│       ├── SendMailRequest.java
│       └── MailMessageResponse.java
│
├── notification/
│   ├── service/
│   │   └── NotificationService.java
│   ├── websocket/
│   │   ├── WebSocketConfig.java
│   │   └── NotificationPublisher.java
│   └── dto/
│       └── ContentNotificationDTO.java
│
└── shared/
    ├── config/
    │   ├── SecurityConfig.java
    │   ├── CorsConfig.java
    │   ├── R2dbcConfig.java
    │   ├── MinioConfig.java
    │   └── RateLimitConfig.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   ├── ResourceNotFoundException.java
    │   ├── UnauthorizedException.java
    │   └── RateLimitExceededException.java
    └── util/
        └── SecurityUtils.java
```

---

## 12. Perfiles de Configuración

El proyecto utiliza **Spring Profiles** para gestionar diferentes configuraciones entre desarrollo y producción.

### 12.1 Perfiles disponibles

| Perfil | Uso               | Servicios                          |
| ------ | ----------------- | ---------------------------------- |
| `dev`  | Desarrollo local  | Docker Compose (localhost)         |
| `prod` | Producción en VMs | PostgreSQL, MinIO, Postfix remotos |

### 12.2 Estructura de archivos de configuración

```
src/main/resources/
├── application.yml          # Configuración común (base)
├── application-dev.yml      # Desarrollo (Docker Compose local)
└── application-prod.yml     # Producción (VMs remotas)
```

### 12.3 application.yml (Base)

```yaml
spring:
  application:
    name: streamvault-api
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  r2dbc:
    username: ${DB_USER:streamvault}
    password: ${DB_PASSWORD}
  mail:
    username: ${MAIL_FROM:noreply@streamvault.local}
    password: ${MAIL_PASSWORD}
  flyway:
    enabled: true
    baseline-on-migrate: true
  servlet:
    multipart:
      max-file-size: 10GB
      max-request-size: 10GB

app:
  jwt:
    access-token-expiration: 900000 # 15 minutos en ms
    refresh-token-expiration: 604800000 # 7 días en ms
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:https://192.168.1.10}
  upload:
    temp-dir: /tmp/streamvault-uploads
    max-file-size: 10737418240 # 10GB
  rate-limit:
    auth-requests-per-minute: 100
    mail-per-hour: 5

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized
```

### 12.4 application-dev.yml (Desarrollo)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/streamvault
  mail:
    host: localhost
    port: 1025
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false

app:
  jwt:
    public-key: classpath:keys/jwt-public.pem
    private-key: classpath:keys/jwt-private.pem

# MinIO: usa la API de MinIO directamente (no pre-firmado en dev)
minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-videos: streamvault-videos
  bucket-thumbnails: streamvault-thumbnails

logging:
  level:
    com.betha.streamvault: DEBUG
    org.springframework.security: DEBUG
```

### 12.5 application-prod.yml (Producción - VMs)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST:db.streamvault.local}:${DB_PORT:5432}/${DB_NAME:streamvault}
  mail:
    host: ${MAIL_HOST:mail.streamvault.local}
    port: ${MAIL_PORT:25}
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false

app:
  jwt:
    public-key: ${JWT_PUBLIC_KEY}
    private-key: ${JWT_PRIVATE_KEY}

# MinIO: producción
minio:
  url: ${MINIO_URL:http://minio.streamvault.local:9000}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket-videos: ${MINIO_BUCKET_VIDEOS:streamvault-videos}
  bucket-thumbnails: ${MINIO_BUCKET_THUMBNAILS:streamvault-thumbnails}

logging:
  level:
    com.betha.streamvault: INFO
    org.springframework.security: WARN

server:
  ssl:
    enabled: true
    key-store: classpath:keystore/streamvault.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

### 12.6 Configuración de Correo por Perfil

#### Desarrollo (MailHog)

Para desarrollo local, se recomienda usar **MailHog** (o cualquier mock SMTP) para capturar emails sin enviarlos realmente:

```yaml
# docker-compose.dev.yml (agregar este servicio)
services:
  mailhog:
    image: mailhog/mailhog
    ports:
      - "1025:1025" # SMTP
      - "8025:8025" # Web UI
```

```yaml
# application-dev.yml
spring:
  mail:
    host: localhost
    port: 1025
```

Acceder a MailHog: <http://localhost:8025>

#### Producción (Postfix real)

```yaml
# application-prod.yml
spring:
  mail:
    host: ${MAIL_HOST:mail.streamvault.local}
    port: ${MAIL_PORT:25}
    username: ${MAIL_FROM:noreply@streamvault.local}
    password: ${MAIL_PASSWORD}
```

### 12.7 Configuración de MinIO por Perfil

#### Desarrollo

```yaml
# application-dev.yml
minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
```

#### Producción

```yaml
# application-prod.yml
minio:
  url: ${MINIO_URL}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
```

### 12.8 Cómo ejecutar con diferentes perfiles

```bash
# Desarrollo (Docker Compose)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Producción
SPRING_PROFILES_ACTIVE=prod java -jar streamvault-api.jar

# Con Docker
docker run -e SPRING_PROFILES_ACTIVE=prod streamvault-api
```

---

## 13. Configuración Docker

### 13.1 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime

# Instalar ffmpeg para conversión MP4 → HLS
RUN apk add --no-cache ffmpeg

WORKDIR /app
COPY target/streamvault-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 13.2 docker-compose.yml (VM-2)

```yaml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db.streamvault.local
      - DB_PORT=5432
      - DB_NAME=streamvault
      - DB_USER=streamvault
      - DB_PASSWORD=${DB_PASSWORD}
      - MAIL_HOST=mail.streamvault.local
      - MAIL_PORT=25
      - MAIL_FROM=noreply@streamvault.local
      - MAIL_PASSWORD=${MAIL_PASSWORD}
      - MINIO_URL=http://minio.streamvault.local:9000
      - MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
      - MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
      - MINIO_BUCKET_VIDEOS=streamvault-videos
      - MINIO_BUCKET_THUMBNAILS=streamvault-thumbnails
      - JWT_PRIVATE_KEY=${JWT_PRIVATE_KEY}
      - JWT_PUBLIC_KEY=${JWT_PUBLIC_KEY}
    dns:
      - 192.168.1.30
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

---

## 14. Variables de Entorno

### Variables Comunes

| Variable                 | Descripción                  | Ejemplo                |
| ------------------------ | ---------------------------- | ---------------------- |
| `SPRING_PROFILES_ACTIVE` | Perfil activo (dev o prod)   | `prod`                 |
| `DB_HOST`                | Host PostgreSQL              | `db.streamvault.local` |
| `DB_PORT`                | Puerto PostgreSQL            | `5432`                 |
| `DB_NAME`                | Nombre de la BD              | `streamvault`          |
| `DB_USER`                | Usuario PostgreSQL           | `streamvault`          |
| `DB_PASSWORD`            | Contraseña PostgreSQL        | Secret — via `.env`    |
| `CORS_ALLOWED_ORIGINS`   | Origins permitidos para CORS | `https://192.168.1.10` |

### Variables de Email (Postfix)

| Variable        | Descripción           | Ejemplo                     |
| --------------- | --------------------- | --------------------------- |
| `MAIL_HOST`     | Hostname SMTP         | `mail.streamvault.local`    |
| `MAIL_PORT`     | Puerto SMTP           | `25`                        |
| `MAIL_FROM`     | Remitente del sistema | `noreply@streamvault.local` |
| `MAIL_PASSWORD` | Contraseña SMTP       | Vacío en red interna        |

### Variables de MinIO

| Variable                  | Descripción          | Ejemplo                               |
| ------------------------- | -------------------- | ------------------------------------- |
| `MINIO_URL`               | URL de MinIO         | `http://minio.streamvault.local:9000` |
| `MINIO_ACCESS_KEY`        | Access key MinIO     | `minioadmin`                          |
| `MINIO_SECRET_KEY`        | Secret key MinIO     | Secret — via `.env`                   |
| `MINIO_BUCKET_VIDEOS`     | Bucket de videos HLS | `streamvault-videos`                  |
| `MINIO_BUCKET_THUMBNAILS` | Bucket de miniaturas | `streamvault-thumbnails`              |

### Variables de JWT

| Variable          | Descripción                            | Ejemplo                        |
| ----------------- | -------------------------------------- | ------------------------------ |
| `JWT_PUBLIC_KEY`  | Clave pública RSA para verificar JWT   | PEM — configurada directamente |
| `JWT_PRIVATE_KEY` | Clave privada RSA-2048 para firmar JWT | PEM — configurada directamente |

### Variables de SSL (Producción)

| Variable                | Descripción                    | Ejemplo             |
| ----------------------- | ------------------------------ | ------------------- |
| `SSL_KEYSTORE_PASSWORD` | Contraseña del keystore PKCS12 | Secret — via `.env` |

> **Nota para desarrollo:** En el perfil `dev`, las variables de SSL no son necesarias ya que se usa HTTP plano.

---

## 15. Extensiones e Plugins Recomendados

### IntelliJ IDEA (recomendado para Spring Boot)

| Plugin                        | Propósito                                                    |
| ----------------------------- | ------------------------------------------------------------ |
| Spring Boot plugin (Ultimate) | Soporte completo: beans, endpoints, run configs, live reload |
| Lombok plugin                 | Procesamiento de anotaciones `@Data`, `@Builder`, etc.       |
| MapStruct Support             | Navegación y validación de mappers                           |
| SonarLint                     | Análisis de calidad de código en tiempo real                 |
| Docker plugin                 | Gestión de contenedores y docker-compose desde el IDE        |
| .env files support            | Resaltado de variables de entorno en archivos `.env`         |
| HTTP Client (incluido)        | Pruebas de endpoints REST directamente en el IDE             |
| OpenAPI Specifications        | Visualización de la spec Swagger/OpenAPI                     |

### VS Code (alternativa)

| Extensión                           | Propósito                           |
| ----------------------------------- | ----------------------------------- |
| Extension Pack for Java (Microsoft) | JDK, Maven, depuración              |
| Spring Boot Extension Pack (VMware) | Dashboard Spring, live beans        |
| Lombok Annotations Support          | Soporte Lombok                      |
| Docker (Microsoft)                  | Gestión de contenedores             |
| REST Client (Huachao Mao)           | Pruebas HTTP desde archivos `.http` |

### Herramientas CLI / Externas

| Herramienta            | Propósito                                          |
| ---------------------- | -------------------------------------------------- |
| Postman / Insomnia     | Pruebas manuales de la API REST y WebSocket        |
| DBeaver / pgAdmin      | Exploración y gestión de PostgreSQL                |
| MinIO Console (Web UI) | Gestión visual de buckets, objetos y policies      |
| Flyway CLI             | Ejecución manual de migraciones de BD              |
| ffmpeg CLI             | Conversión manual de videos MP4 → HLS para pruebas |

---

_PRD v1.1 — StreamVault Backend — Betha TM — 2026_
