# StreamVault API - Referencia de Endpoints

**Versión:** 1.0  
**Base URL:** `https://api.streamvault.com/api/v1`  
**Autenticación:** JWT Bearer Token

---

## Tabla de Contenidos

1. [Autenticación](#1-autenticación)
   - [POST /api/v1/auth/register](#post-apiv1authregister)
   - [POST /api/v1/auth/login](#post-apiv1authlogin)
   - [POST /api/v1/auth/refresh](#post-apiv1authrefresh)
   - [POST /api/v1/auth/logout](#post-apiv1authlogout)
2. [Usuario](#2-usuario)
   - [GET /api/v1/users/me](#get-apiv1usersme)
   - [PUT /api/v1/users/me](#put-apiv1usersme)
   - [PUT /api/v1/users/me/password](#put-apiv1usersmepassword)
   - [GET /api/v1/users/{id}](#get-apiv1usersid)
3. [Perfiles](#3-perfiles)
   - [GET /api/v1/profiles](#get-apiv1profiles)
   - [POST /api/v1/profiles](#post-apiv1profiles)
   - [GET /api/v1/profiles/{id}](#get-apiv1profilesid)
   - [PUT /api/v1/profiles/{id}](#put-apiv1profilesid)
   - [DELETE /api/v1/profiles/{id}](#delete-apiv1profilesid)
4. [Suscripciones](#4-suscripciones)
   - [POST /api/v1/subscriptions/purchase](#post-apiv1subscriptionspurchase)
   - [GET /api/v1/subscriptions/me](#get-apiv1subscriptionsme)
5. [Catálogo](#5-catálogo)
   - [GET /api/v1/catalog](#get-apiv1catalog)
   - [GET /api/v1/catalog/{id}](#get-apiv1catalogid)
   - [GET /api/v1/catalog/search](#get-apiv1catalogsearch)
   - [GET /api/v1/catalog/{id}/seasons](#get-apiv1catalogidseasons)
   - [GET /api/v1/catalog/seasons/{seasonId}/episodes](#get-apiv1catalogseasonsseasonidepisodes)
   - [GET /api/v1/catalog/genres](#get-apiv1cataloggenres)
   - [POST /api/v1/catalog](#post-apiv1catalog)
   - [PUT /api/v1/catalog/{id}](#put-apiv1catalogid)
   - [DELETE /api/v1/catalog/{id}](#delete-apiv1catalogid)
6. [Streaming](#6-streaming)
   - [GET /api/v1/stream/{contentId}](#get-apiv1streamcontentid)
   - [GET /api/v1/stream/{contentId}/episode/{episodeId}](#get-apiv1streamcontentidepisodeepisodeid)
7. [Historial](#7-historial)
   - [GET /api/v1/history](#get-apiv1history)
   - [GET /api/v1/history/{id}](#get-apiv1historyid)
   - [POST /api/v1/history](#post-apiv1history)
   - [PUT /api/v1/history/{id}/progress](#put-apiv1historyidprogress)
   - [PUT /api/v1/history/{id}/completed](#put-apiv1historyidcompleted)
 8. [Administración](#8-administración)
    - [GET /api/v1/admin/users](#get-apiv1adminusers)
    - [GET /api/v1/admin/users/{id}](#get-apiv1adminusersid)
    - [POST /api/v1/admin/upload/thumbnail](#post-apiv1adminuploadthumbnail)
    - [POST /api/v1/admin/notifications](#post-apiv1adminnotifications)
    - [POST /api/v1/admin/notifications/broadcast](#post-apiv1adminnotificationsbroadcast)
 9. [Notificaciones](#9-notificaciones)
10. [WebSocket](#10-websocket)
11. [Correo](#11-correo)
12. [Códigos de Error](#12-códigos-de-error)
13. [Ejemplos con curl](#13-ejemplos-con-curl)

---

## 1. Autenticación

### POST /api/v1/auth/register

**Descripción:** Registra un nuevo usuario en la plataforma. Solo se permiten emails del dominio `@streamvault.com`. El usuario se crea con rol `ROLE_USER` y `isVerified=false`.

**Autenticación:** Public (no requiere token)

**Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "email": "usuario@streamvault.com",
  "password": "contraseña123",
  "name": "Nombre del Usuario"
}
```

| Campo    | Tipo   | Requerido | Descripción                        |
| -------- | ------ | --------- | ---------------------------------- |
| email    | string | Sí        | Email con dominio @streamvault.com |
| password | string | Sí        | Mínimo 8 caracteres                |
| name     | string | Sí        | Nombre completo del usuario        |

**Respuesta Exitosa (201):**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

| Campo       | Tipo   | Descripción                                      |
| ----------- | ------ | ---------------------------------------------- |
| accessToken | string | JWT de acceso (15 min, 900000ms)              |
| refreshToken| string | JWT de refresh (7 días) con JTI único         |
| tokenType   | string | Siempre "Bearer"                             |
| expiresIn   | long   | Tiempo de expiración en milisegundos           |

**Respuestas de Error:**
- **400**: Validación fallida (email vacío, no es @streamvault.com, password < 8 chars, name vacío)
- **409**: El email ya está registrado
- **500**: Error interno del servidor

---

### POST /api/v1/auth/login

**Descripción:** Inicia sesión y devuelve tokens de acceso y refresh.

**Autenticación:** Public (no requiere token)

**Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "email": "usuario@streamvault.com",
  "password": "contraseña123"
}
```

| Campo    | Tipo   | Requerido | Descripción           |
| -------- | ------ | --------- | --------------------- |
| email    | string | Sí        | Email válido          |
| password | string | Sí        | Password del usuario  |

**Respuesta Exitosa (200):**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

**Respuestas de Error:**
- **400**: Email o password vacío
- **401**: Credenciales inválidas (email no existe o password no coincide)
- **500**: Error interno del servidor

---

### POST /api/v1/auth/refresh

**Descripción:** Refresca el token de acceso usando un refresh token válido. El token anterior se invalida automáticamente.

**Autenticación:** Public (no requiere token)

**Headers:**
- `Content-Type: application/json`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

| Campo         | Tipo   | Requerido | Descripción                    |
| ------------- | ------ | --------- | ------------------------------ |
| refreshToken  | string | Sí        | Refresh token válido previamente obtenido |

**Respuesta Exitosa (200):**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

**Respuestas de Error:**
- **400**: refreshToken no proporcionado
- **401**: Token inválido, ya revocado, o expirado
- **500**: Error interno del servidor

---

### POST /api/v1/auth/logout

**Descripción:** Invalida el refresh token (logout). El token se marca como revocado en la base de datos.

**Autenticación:** No requerida (token va en el body)

**Headers:**
- `Authorization: Bearer {token}` (recomendado para logging)
- `Content-Type: application/json`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

| Campo         | Tipo   | Requerido | Descripción                    |
| ------------- | ------ | --------- | ------------------------------ |
| refreshToken  | string | No        | Token a revocar (si es null/empty, no hace nada) |

**Respuesta Exitosa (204):** Sin contenido

**Respuestas de Error:**
- **500**: Error interno del servidor

---

## 2. Usuario

### GET /api/v1/users/me

**Descripción:** Obtiene la información del usuario autenticado actualmente.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@streamvault.com",
  "name": "Nombre del Usuario",
  "role": "ROLE_USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

| Campo      | Tipo            | Descripción                 |
| ---------- | --------------- | --------------------------- |
| id         | UUID            | ID único del usuario        |
| email      | string          | Email del usuario           |
| name       | string          | Nombre del usuario         |
| role       | string          | Rol (ROLE_USER, ROLE_ADMIN)|
| createdAt  | LocalDateTime   | Fecha de creación          |
| updatedAt  | LocalDateTime   | Fecha de última actualización|

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado

---

### PUT /api/v1/users/me

**Descripción:** Actualiza la información del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Nuevo Nombre",
  "email": "nuevo@streamvault.com"
}
```

| Campo | Tipo   | Requerido | Descripción               |
| ----- | ------ | --------- | ------------------------- |
| name  | string | No        | Nombre (2-100 caracteres) |
| email | string | No        | Nuevo email válido        |

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "nuevo@streamvault.com",
  "name": "Nuevo Nombre",
  "role": "ROLE_USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T12:00:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado
- **409**: El email ya está en uso por otro usuario

---

### PUT /api/v1/users/me/password

**Descripción:** Cambia la contraseña del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "currentPassword": "contraseñaActual123",
  "newPassword": "nuevaContraseña456"
}
```

| Campo           | Tipo   | Requerido | Descripción               |
| --------------- | ------ | --------- | ------------------------- |
| currentPassword | string | Sí        | Contraseña actual         |
| newPassword     | string | Sí        | Nueva contraseña (mín. 8) |

**Respuesta Exitosa (204):** Sin contenido (respuesta vacía)

**Respuestas de Error:**
- **400**: La contraseña actual no coincide
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado

---

### GET /api/v1/users/{id}

**Descripción:** Obtiene la información de un usuario por su ID.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID único del usuario

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@streamvault.com",
  "name": "Nombre del Usuario",
  "role": "ROLE_USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado

---

## 3. Perfiles

### GET /api/v1/profiles

**Descripción:** Obtiene todos los perfiles del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Perfil Principal",
    "avatarUrl": "https://cdn.streamvault.com/avatars/default.png",
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440002",
    "name": "Perfil Infantil",
    "avatarUrl": null,
    "createdAt": "2024-01-16T14:00:00"
  }
]
```

| Campo     | Tipo        | Descripción                    |
| --------- | ----------- | ------------------------------ |
| id        | UUID        | ID único del perfil            |
| name      | string      | Nombre del perfil             |
| avatarUrl | string?     | URL del avatar (puede ser null)|
| createdAt | LocalDateTime| Fecha de creación            |

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado

---

### POST /api/v1/profiles

**Descripción:** Crea un nuevo perfil para el usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "name": "Nuevo Perfil"
}
```

| Campo | Tipo   | Requerido | Descripción              |
| ----- | ------ | --------- | ------------------------ |
| name  | string | Sí        | Nombre del perfil (1-50) |

**Respuesta Exitosa (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "name": "Nuevo Perfil",
  "avatarUrl": null,
  "createdAt": "2024-01-17T09:00:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida o límite de perfiles alcanzado
- **401**: Token inválido o expirado
- **404**: Usuario no encontrado

---

### GET /api/v1/profiles/{id}

**Descripción:** Obtiene un perfil específico por su ID (solo si pertenece al usuario).

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID único del perfil

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Perfil Principal",
  "avatarUrl": "https://cdn.streamvault.com/avatars/default.png",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: El perfil no pertenece al usuario autenticado
- **404**: Perfil no encontrado o usuario no encontrado

---

### PUT /api/v1/profiles/{id}

**Descripción:** Actualiza un perfil existente (solo si pertenece al usuario).

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Path Parameters:**
- `id` (UUID): ID único del perfil

**Request Body:**
```json
{
  "name": "Nombre Actualizado"
}
```

| Campo | Tipo   | Requerido | Descripción              |
| ----- | ------ | --------- | ------------------------ |
| name  | string | Sí        | Nombre del perfil (1-50) |

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Nombre Actualizado",
  "avatarUrl": "https://cdn.streamvault.com/avatars/default.png",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **403**: El perfil no pertenece al usuario autenticado
- **404**: Perfil no encontrado

---

### DELETE /api/v1/profiles/{id}

**Descripción:** Elimina un perfil (solo si pertenece al usuario).

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID único del perfil

**Respuesta Exitosa (204):** Sin contenido

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: El perfil no pertenece al usuario autenticado
- **404**: Perfil no encontrado

---

## 4. Suscripciones

### GET /api/v1/subscriptions/me

**Descripción:** Obtiene la suscripción actual del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440100",
  "plan": "DEFAULT",
  "startedAt": "2026-03-30T12:00:00Z",
  "expiresAt": "2026-04-29T12:00:00Z",
  "active": true
}
```

| Campo      | Tipo     | Descripción                          |
| ---------- |----------| ------------------------------------ |
| id         | UUID     | ID único de la suscripción           |
| plan       | string   | Plan de suscripción (DEFAULT = $10/mes) |
| startedAt  | Instant  | Fecha de inicio de la suscripción    |
| expiresAt  | Instant  | Fecha de expiración de la suscripción|
| active     | boolean  | Si la suscripción está activa        |

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Suscripción no encontrada

---

### POST /api/v1/subscriptions/purchase

**Descripción:** Comprar una nueva suscripción. Crea una suscripción activa con duración de 30 días.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Request Body:** (vacío - el frontend maneja la "emulación" del pago)

**Respuesta Exitosa (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440100",
  "plan": "DEFAULT",
  "startedAt": "2026-03-30T12:00:00Z",
  "expiresAt": "2026-04-29T12:00:00Z",
  "active": true
}
```

**Notas:**
- El precio es $10 USD por mes
- La duración es de 30 días
- Si el usuario ya tiene una suscripción activa, devuelve la suscripción existente (409 Conflict)
- El frontend es responsable de manejar la "emulación" del pago

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **409**: El usuario ya tiene una suscripción activa

---

## 6. Catálogo

### Enums de Catálogo

**ContentType:**
| Value   | Descripción |
| --------|-------------|
| MOVIE   | Película    |
| SERIES  | Serie de TV |

**ContentStatus:**
| Value      | Descripción    |
| -----------|----------------|
| DRAFT      | Borrador       |
| PUBLISHED  |Publicado      |
| UNPUBLISHED| Despublicado  |

**EpisodeStatus:**
| Value     | Descripción              |
|-----------|-------------------------|
| DRAFT     | Borrador                |
| READY     | Listo para procesar    |
| AVAILABLE | Disponible             |
| PUBLISHED |Publicado              |
| ARCHIVED  | Archivado              |
| ERROR     | Error en procesamiento  |

---

### GET /api/v1/catalog

**Descripción:** Obtiene el catálogo completo de contenido con paginación. Solo retorna contenido con status=PUBLISHED.

**Autenticación:** Public (sin autenticación)

**Query Parameters:**
- `page` (int): Número de página (default: 0)
- `size` (int): Tamaño de página (default: 20, máximo: 100)

**Respuesta Exitosa (200):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "title": "Serie Popular",
      "description": "Descripción de la serie",
      "type": "SERIES",
      "releaseYear": 2024,
      "rating": "TV-MA",
      "thumbnailKey": "thumbnails/series-01.jpg",
      "minioKey": null,
      "status": "PUBLISHED",
      "genres": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440100",
          "name": "Drama"
        }
      ],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

**Respuestas de Error:**
- **400**: Parámetros de paginación inválidos

---

### GET /api/v1/catalog/{id}

**Descripción:** Obtiene un contenido específico del catálogo por su ID.

**Autenticación:** Public (sin autenticación)

**Path Parameters:**
- `id` (UUID): ID único del contenido

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "title": "Película Destacada",
  "description": "Una película increíble",
  "type": "MOVIE",
  "releaseYear": 2024,
  "rating": "PG-13",
  "thumbnailKey": "thumbnails/movie-01.jpg",
  "minioKey": "movies/featured.mp4",
  "status": "PUBLISHED",
  "genres": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440101",
      "name": "Acción"
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Respuestas de Error:**
- **404**: Contenido no encontrado

---

### GET /api/v1/catalog/search

**Descripción:** Busca contenido en el catálogo por título. Solo retorna contenido PUBLISHED.

**Autenticación:** Public (sin autenticación)

**Query Parameters:**
- `q` (string): **Requerido** - Término de búsqueda
- `page` (int): Número de página (default: 0)
- `size` (int): Tamaño de página (default: 20)

**Respuesta Exitosa (200):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "title": "Búsqueda Resultado",
      "description": "Coincide con la búsqueda",
      "type": "SERIES",
      "releaseYear": 2024,
      "rating": "TV-14",
      "thumbnailKey": "thumbnails/result.jpg",
      "minioKey": null,
      "status": "PUBLISHED",
      "genres": [],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Respuestas de Error:**
- **400**: Falta el parámetro `q`

---

### GET /api/v1/catalog/{id}/seasons

**Descripción:** Obtiene todas las temporadas de una serie. Solo aplica para contenido tipo SERIES.

**Autenticación:** Public (sin autenticación)

**Path Parameters:**
- `id` (UUID): ID del contenido (serie)

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "contentId": "550e8400-e29b-41d4-a716-446655440010",
    "seasonNumber": 1
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440021",
    "contentId": "550e8400-e29b-41d4-a716-446655440010",
    "seasonNumber": 2
  }
]
```

**Respuestas de Error:**
- **404**: Contenido no encontrado

---

### GET /api/v1/catalog/seasons/{seasonId}/episodes

**Descripción:** Obtiene todos los episodios de una temporada.

**Autenticación:** Public (sin autenticación)

**Path Parameters:**
- `seasonId` (UUID): ID de la temporada

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440030",
    "seasonId": "550e8400-e29b-41d4-a716-446655440020",
    "episodeNumber": 1,
    "title": "Episodio 1",
    "description": "Descripción del primer episodio",
    "minioKey": "episodes/s01e01.mp4",
    "thumbnailKey": "episodes/s01e01-thumb.jpg",
    "durationSec": 2400,
    "status": "AVAILABLE",
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440031",
    "seasonId": "550e8400-e29b-41d4-a716-446655440020",
    "episodeNumber": 2,
    "title": "Episodio 2",
    "description": "Descripción del segundo episodio",
    "minioKey": "episodes/s01e02.mp4",
    "thumbnailKey": "episodes/s01e02-thumb.jpg",
    "durationSec": 2200,
    "status": "AVAILABLE",
    "createdAt": "2024-01-16T10:30:00"
  }
]
```

| Campo         | Tipo    | Descripción                     |
|--------------|---------|---------------------------------|
| id           | UUID    | ID único del episodio           |
| seasonId     | UUID    | ID de la temporada             |
| episodeNumber| int    | Número del episodio            |
| title        | string  | Título del episodio            |
| description  | string  | Descripción del episodio       |
| minioKey     | string? | Clave del video en MinIO      |
| thumbnailKey | string? | Clave del thumbnail           |
| durationSec  | int     | Duración en segundos           |
| status       | string  | Estado (AVAILABLE, etc.)      |
| createdAt    | LocalDateTime| Fecha de creación         |

**Respuestas de Error:**
- **404**: Temporada no encontrada

---

### GET /api/v1/catalog/genres

**Descripción:** Obtiene todos los géneros disponibles.

**Autenticación:** Public (sin autenticación)

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440100",
    "name": "Drama"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440101",
    "name": "Acción"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440102",
    "name": "Comedia"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440103",
    "name": "Terror"
  }
]
```

---

### POST /api/v1/catalog

**Descripción:** Crea nuevo contenido en el catálogo (requiere rol ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "title": "Nueva Película",
  "description": "Descripción de la película",
  "type": "MOVIE",
  "releaseYear": 2024,
  "rating": "PG-13",
  "thumbnailKey": "thumbnails/new-movie.jpg",
  "minioKey": "movies/new-movie.mp4",
  "genreIds": [
    "550e8400-e29b-41d4-a716-446655440100"
  ],
  "status": "DRAFT"
}
```

> **Nota para HLS:** Para contenido HLS, usar `playlist.m3u8` como minioKey:
> ```json
> "minioKey": "Peliculas/Nombre_Pelicula/playlist.m3u8"
> ```

| Campo        | Tipo     | Requerido | Descripción                      |
| ------------ | -------- | --------- | -------------------------------- |
| title        | string   | Sí        | Título (max 255)                |
| description  | string   | No        | Descripción                      |
| type         | enum     | Sí        | MOVIE o SERIES                   |
| releaseYear  | int      | No        | Año de lanzamiento               |
| rating       | string   | No        | Clasificación (max 10)           |
| thumbnailKey | string   | No        | Clave del thumbnail en MinIO     |
| minioKey     | string   | No        | Clave del video en MinIO         |
| genreIds     | UUID[]   | No        | Lista de IDs de géneros          |
| status       | enum     | No        | DRAFT, PUBLISHED, UNPUBLISHED   |

**Respuesta Exitosa (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "title": "Nueva Película",
  "description": "Descripción de la película",
  "type": "MOVIE",
  "releaseYear": 2024,
  "rating": "PG-13",
  "thumbnailKey": "thumbnails/new-movie.jpg",
  "minioKey": "movies/new-movie.mp4",
  "status": "DRAFT",
  "genres": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440100",
      "name": "Drama"
    }
  ],
  "createdAt": "2024-01-17T10:00:00",
  "updatedAt": null
}
```

> **Nota:** El campo `genres` puede ser `null` si no se proporcionaron `genreIds` o si los géneros no existen en la base de datos.

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **500**: Error interno

---

### PUT /api/v1/catalog/{id}

**Descripción:** Actualiza contenido existente en el catálogo (requiere rol ADMIN). Solo actualiza campos no nulos.

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Path Parameters:**
- `id` (UUID): ID del contenido

**Request Body:** (mismos campos que POST, todos opcionales)
```json
{
  "title": "Título Actualizado",
  "genreIds": ["550e8400-e29b-41d4-a716-446655440100"],
  "status": "PUBLISHED"
}
```

> **Notas:**
> - Si `genreIds` es un array vacío `[]`, se limpian los géneros asociados
> - Al cambiar `status` a `PUBLISHED`, se envía una notificación automática a todos los usuarios
> - Si el contenido ya estaba publicado, no se envía notificación al actualizar otros campos

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "title": "Título Actualizado",
  "description": "Descripción existente",
  "type": "MOVIE",
  "releaseYear": 2024,
  "rating": "PG-13",
  "thumbnailKey": "thumbnails/updated.jpg",
  "minioKey": "movies/updated.mp4",
  "status": "PUBLISHED",
  "genres": [],
  "createdAt": "2024-01-17T10:00:00",
  "updatedAt": "2024-01-18T12:00:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **404**: Contenido no encontrado o géneros no encontrados
- **500**: Error interno

---

### DELETE /api/v1/catalog/{id}

**Descripción:** Elimina contenido del catálogo (requiere rol ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID del contenido

**Respuesta Exitosa (204):** Sin contenido

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **404**: Contenido no encontrado
- **500**: Error interno

---

## 6. Streaming

### GET /api/v1/stream/{contentId}

**Descripción:** Obtiene la URL de streaming para una película (content tipo MOVIE). Requiere suscripción activa.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `contentId` (UUID): ID del contenido (película)

**Respuesta Exitosa (200):**
```json
{
  "url": "https://minio.example.com/streamvault-videos/movies/uuid-video.mp4?X-Amz-Algorithm=...",
  "expiresAt": "2024-01-15T12:00:00Z"
}
```

| Campo      | Tipo     | Descripción                                    |
| ----------|----------|-----------------------------------------------|
| url       | string   | URL presignada de MinIO para el video         |
| expiresAt | Instant | Fecha de expiración de la URL (default 2h)   |

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: Suscripción no activa o expirada
- **404**: Contenido no encontrado, es una serie, o video no disponible

**Nota:** Solo funciona para contenido tipo MOVIE. Para series usar el endpoint de episodio.

---

### GET /api/v1/stream/{contentId}/episode/{episodeId}

**Descripción:** Obtiene la URL de streaming para un episodio específico de una serie. Requiere suscripción activa.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `contentId` (UUID): ID del contenido (serie)
- `episodeId` (UUID): ID del episodio

**Respuesta Exitosa (200):**
```json
{
  "url": "https://minio.example.com/streamvault-videos/series/uuid-s01e01.mp4?X-Amz-Algorithm=...",
  "expiresAt": "2024-01-15T12:00:00Z"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: Suscripción no activa o expirada
- **404**: Episodio no encontrado, no pertenece al contentId, o video no disponible

---

## 7. Historial

### GET /api/v1/history

**Descripción:** Obtiene el historial de visualización del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Query Parameters:**
- `profileId` (UUID): Opcional - ID del perfil específico (para cuentas con múltiples perfiles)

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440050",
    "profileId": "550e8400-e29b-41d4-a716-446655440001",
    "episodeId": "550e8400-e29b-41d4-a716-446655440030",
    "progressSec": 1200,
    "completed": false,
    "watchedAt": "2024-01-17T15:30:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440051",
    "profileId": "550e8400-e29b-41d4-a716-446655440001",
    "episodeId": "550e8400-e29b-41d4-a716-446655440031",
    "progressSec": 2200,
    "completed": true,
    "watchedAt": "2024-01-17T16:00:00"
  }
]
```

**Respuestas de Error:**
- **401**: Token inválido o expirado

---

### GET /api/v1/history/{id}

**Descripción:** Obtiene un registro específico del historial.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID del registro de historial

**Query Parameters:**
- `profileId` (UUID): Opcional - para validación de propiedad

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440050",
  "profileId": "550e8400-e29b-41d4-a716-446655440001",
  "episodeId": "550e8400-e29b-41d4-a716-446655440030",
  "progressSec": 1200,
  "completed": false,
  "watchedAt": "2024-01-17T15:30:00"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Registro de historial no encontrado

---

### POST /api/v1/history

**Descripción:** Inicia el seguimiento de visualización de un episodio.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Query Parameters:**
- `profileId` (UUID): Opcional - ID del perfil

**Request Body:**
```json
{
  "episodeId": "550e8400-e29b-41d4-a716-446655440030",
  "progressSec": 0,
  "completed": false
}
```

| Campo       | Tipo    | Requerido | Descripción                        |
| ----------- | ------- | --------- | ---------------------------------- |
| episodeId   | UUID    | Sí        | ID del episodio                    |
| progressSec | int     | No        | Segundos reproducidos (default: 0) |
| completed   | boolean | No        | Marcar como completado             |

**Respuesta Exitosa (201):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440050",
  "profileId": "550e8400-e29b-41d4-a716-446655440001",
  "episodeId": "550e8400-e29b-41d4-a716-446655440030",
  "progressSec": 0,
  "completed": false,
  "watchedAt": "2024-01-17T15:30:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida (episodeId null, progressSec negativo)
- **401**: Token inválido o expirado

---

### PUT /api/v1/history/{id}/progress

**Descripción:** Actualiza el progreso de visualización de un registro.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Path Parameters:**
- `id` (UUID): ID del registro de historial

**Query Parameters:**
- `profileId` (UUID): Opcional

**Request Body:**
```json
{
  "progressSec": 1200,
  "completed": false
}
```

| Campo       | Tipo    | Requerido | Descripción                        |
| ----------- | ------- | --------- | ---------------------------------- |
| progressSec | int     | Sí        | Segundos de progreso (no puede ser null ni negativo) |
| completed   | boolean | No        | Marcar como completado             |

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440050",
  "profileId": "550e8400-e29b-41d4-a716-446655440001",
  "episodeId": "550e8400-e29b-41d4-a716-446655440030",
  "progressSec": 1200,
  "completed": false,
  "watchedAt": "2024-01-17T15:45:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **404**: Registro no encontrado

---

### PUT /api/v1/history/{id}/completed

**Descripción:** Marca un registro de historial como completado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID del registro de historial

**Query Parameters:**
- `profileId` (UUID): Opcional

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440050",
  "profileId": "550e8400-e29b-41d4-a716-446655440001",
  "episodeId": "550e8400-e29b-41d4-a716-446655440030",
  "progressSec": 2400,
  "completed": true,
  "watchedAt": "2024-01-17T16:00:00"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Registro no encontrado

---

## 8. Administración

### GET /api/v1/admin/users

**Descripción:** Lista todos los usuarios del sistema con paginación (solo ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`

**Query Parameters:**
- `page` (int): Número de página (default: 0)
- `size` (int): Tamaño de página (default: 20)

**Respuesta Exitosa (200):**
```json
{
  "users": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "usuario@streamvault.com",
      "name": "Usuario Ejemplo",
      "role": "ROLE_USER",
      "isVerified": false,
      "createdAt": "2024-01-15T10:30:00"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "email": "admin@streamvault.com",
      "name": "Administrador",
      "role": "ROLE_ADMIN",
      "isVerified": true,
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "total": 150,
  "page": 0,
  "size": 20
}
```

| Campo      | Tipo        | Descripción                 |
| ---------- | ----------- | --------------------------- |
| users      | User[]      | Lista de usuarios           |
| total      | long        | Total de usuarios           |
| page       | int         | Página actual              |
| size       | int         | Tamaño de página           |

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN

---

### GET /api/v1/admin/users/{id}

**Descripción:** Obtiene detalles de un usuario específico (solo ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID del usuario

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "usuario@streamvault.com",
  "name": "Usuario Ejemplo",
  "role": "ROLE_USER",
  "isVerified": false,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **404**: Usuario no encontrado

---

### POST /api/v1/admin/upload/thumbnail

**Descripción:** Sube una imagen de thumbnail al sistema (solo ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**Form Data:**
- `file` (File): Archivo de imagen

**Restricciones:**
- Tipos permitidos: image/jpeg, image/png, image/webp, image/gif
- Tamaño máximo: 5 MB

**Respuesta Exitosa (201):**
```json
{
  "key": "thumbnails/content/uuid-image.jpg",
  "url": "https://minio.example.com/thumbnails/content/uuid-image.jpg?X-Amz-Algorithm=...",
  "filename": "poster.jpg",
  "contentType": "image/jpeg",
  "size": 245678,
  "uploadedAt": "2024-01-17T10:00:00Z"
}
```

| Campo       | Tipo    | Descripción                    |
|------------|---------|--------------------------------|
| key        | string  | Ruta del archivo en MinIO     |
| url        | string  | URL presignada para acceso   |
| filename   | string  | Nombre original del archivo   |
| contentType| string  | Tipo MIME del archivo         |
| size       | long    | Tamaño en bytes              |
| uploadedAt | Instant| Fecha de subida              |

**Respuestas de Error:**
- **400**: Archivo no proporcionado, tipo inválido, o tamaño > 5MB
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **500**: Error al subir a MinIO

---

### POST /api/v1/admin/notifications

**Descripción:** Envía una notificación a un usuario específico (solo ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "type": "USER_NOTIFICATION",
  "title": "Título de la notificación",
  "message": "Mensaje de la notificación",
  "relatedId": "550e8400-e29b-41d4-a716-446655440010"
}
```

| Campo      | Tipo   | Requerido | Descripción                         |
|------------|--------|-----------|-------------------------------------|
| userId     | UUID   | Sí        | ID del usuario destinatario         |
| type       | enum   | Sí        | USER_NOTIFICATION o SYSTEM         |
| title      | string | Sí        | Título de la notificación          |
| message    | string | Sí        | Mensaje de la notificación         |
| relatedId  | UUID   | No        | ID del contenido relacionado        |

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440060",
  "type": "USER_NOTIFICATION",
  "title": "Título de la notificación",
  "message": "Mensaje de la notificación",
  "relatedId": "550e8400-e29b-41d4-a716-446655440010",
  "isRead": false,
  "createdAt": "2024-01-17T15:30:00"
}
```

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN
- **404**: Usuario no encontrado

---

### POST /api/v1/admin/notifications/broadcast

**Descripción:** Envía una notificación broadcast a todos los usuarios conectados (solo ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "type": "SYSTEM",
  "title": "Anuncio importante",
  "message": "Mensaje para todos los usuarios",
  "relatedId": "550e8400-e29b-41d4-a716-446655440010"
}
```

| Campo      | Tipo   | Requerido | Descripción                         |
|------------|--------|-----------|-------------------------------------|
| type       | enum   | Sí        | NEW_CONTENT, NEW_EPISODE o SYSTEM   |
| title      | string | Sí        | Título de la notificación          |
| message    | string | Sí        | Mensaje de la notificación         |
| relatedId  | UUID   | No        | ID del contenido relacionado        |

**Respuesta Exitosa (200):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440061",
  "type": "SYSTEM",
  "title": "Anuncio importante",
  "message": "Mensaje para todos los usuarios",
  "relatedId": "550e8400-e29b-41d4-a716-446655440010",
  "createdAt": "2024-01-17T15:30:00"
}
```

**Notas:**
- La notificación se guarda en la tabla `broadcast_notifications`
- Se emite por WebSocket a todos los usuarios conectados
- El contenido relacionado se puede acceder mediante `relatedId`

**Respuestas de Error:**
- **400**: Validación fallida
- **401**: Token inválido o expirado
- **403**: No tiene rol ADMIN

---

## 9. Notificaciones

### Enums de Notificaciones

**NotificationType (para notificaciones de usuario):**
| Value            | Descripción                   |
| -----------------|------------------------------|
| USER_NOTIFICATION| Notificación de usuario     |
| SYSTEM           | Notificación del sistema     |

**BroadcastNotificationType (para notificaciones broadcast):**
| Value            | Descripción                   |
| -----------------|------------------------------|
| NEW_CONTENT      | Nuevo contenido disponible   |
| NEW_EPISODE      | Nuevo episodio disponible    |
| SYSTEM           | Notificación del sistema     |

---

### GET /api/v1/notifications

**Descripción:** Obtiene todas las notificaciones del usuario autenticado, ordenadas por fecha de creación (más recientes primero).

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440060",
    "type": "NEW_EPISODE",
    "title": "Nuevo episodio disponible",
    "message": "El episodio 5 de Temporada 2 ya está disponible",
    "relatedId": "550e8400-e29b-41d4-a716-446655440030",
    "isRead": false,
    "createdAt": "2024-01-17T15:30:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440061",
    "type": "NEW_CONTENT",
    "title": "Nuevo contenido",
    "message": "Se agregó una nueva película al catálogo",
    "relatedId": "550e8400-e29b-41d4-a716-446655440010",
    "isRead": true,
    "createdAt": "2024-01-16T10:00:00"
  }
]
```

| Campo      | Tipo            | Descripción                          |
| ---------- | --------------- | ------------------------------------ |
| id         | UUID            | ID único de la notificación          |
| type       | enum            | Tipo de notificación                 |
| title      | string          | Título de la notificación            |
| message    | string          | Mensaje de la notificación           |
| relatedId  | UUID?           | ID del contenido relacionado         |
| isRead     | boolean         | Si la notificación ha sido leída     |
| createdAt  | LocalDateTime   | Fecha de creación                    |

**Respuestas de Error:**
- **401**: Token inválido o expirado

---

### GET /api/v1/notifications/unread

**Descripción:** Obtiene solo las notificaciones no leídas del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440060",
    "type": "NEW_EPISODE",
    "title": "Nuevo episodio disponible",
    "message": "El episodio 5 de Temporada 2 ya está disponible",
    "relatedId": "550e8400-e29b-41d4-a716-446655440030",
    "isRead": false,
    "createdAt": "2024-01-17T15:30:00"
  }
]
```

**Respuestas de Error:**
- **401**: Token inválido o expirado

---

### GET /api/v1/notifications/unread/count

**Descripción:** Obtiene la cantidad de notificaciones no leídas del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):**
```json
{
  "count": 5
}
```

| Campo  | Tipo   | Descripción                          |
| -------| -------| ------------------------------------ |
| count  | long   | Número de notificaciones no leídas   |

**Respuestas de Error:**
- **401**: Token inválido o expirado

---

### PUT /api/v1/notifications/{id}/read

**Descripción:** Marca una notificación específica como leída.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `id` (UUID): ID de la notificación

**Respuesta Exitosa (200):** Sin contenido

**Respuestas de Error:**
- **401**: Token inválido o expirado
- **404**: Notificación no encontrada

---

### PUT /api/v1/notifications/read-all

**Descripción:** Marca todas las notificaciones del usuario autenticado como leídas.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`

**Respuesta Exitosa (200):** Sin contenido

**Respuestas de Error:**
- **401**: Token inválido o expirado

---

## 10. WebSocket

### Conexión WebSocket

**Endpoint:** `ws://TU_SERVER/ws/notifications`

**Descripción:** WebSocket para recibir notificaciones en tiempo real. El cliente se conecta y envía su userId para suscribirse a las notificaciones de ese usuario.

**Autenticación:** No requiere JWT (el userId se envía en el mensaje de conexión)

**Ejemplo de conexión en JavaScript:**
```javascript
const socket = new WebSocket('ws://localhost:8080/ws/notifications');

socket.onopen = () => {
  console.log('Conectado a WebSocket');
  // Suscribirse a notificaciones del usuario
  socket.send(JSON.stringify({ userId: 'uuid-del-usuario' }));
};

socket.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  console.log('Nueva notificación:', notification);
};

socket.onclose = () => {
  console.log('Desconectado del WebSocket');
};

socket.onerror = (error) => {
  console.error('Error en WebSocket:', error);
};
```

**Mensajes del servidor (notificaciones entrantes):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440060",
  "type": "NEW_EPISODE",
  "title": "Nuevo episodio disponible",
  "message": "El episodio 5 de Temporada 2 ya está disponible",
  "relatedId": "550e8400-e29b-41d4-a716-446655440030",
  "isRead": false,
  "createdAt": "2024-01-17T15:30:00"
}
```

**Notas:**
- La conexión debe mantenerse abierta para recibir notificaciones en tiempo real
- Si la conexión se cierra, el cliente debe reconectarse
- El servidor guarda las sesiones activas por userId para enviar notificaciones directamente

---

## 11. Correo

### POST /api/v1/mail/send

**Descripción:** Envía un correo electrónico desde la cuenta del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "to": "destinatario@ejemplo.com",
  "subject": "Notificación de StreamVault",
  "body": "<h1>Hola</h1><p>Este es un mensaje de StreamVault.</p>"
}
```

| Campo | Tipo   | Requerido | Descripción |
|-------|--------|-----------|--------------|
| to    | string | Sí        | Dirección de correo del destinatario |
| subject | string | Sí      | Asunto del correo |
| body  | string | Sí        | Cuerpo del correo (puede contener HTML) |
| from  | string | No        | Remitente (si no se provee, usa noreply@streamvault.com) |

**Respuesta Exitosa (200):** Sin contenido

> **Nota:** Cuando el correo se envía exitosamente, el receptor recibe una notificación en la aplicación indicándole que ha recibido un correo del remitente.

**Respuestas de Error:**
- **400**: Datos inválidos (email mal formateado, campos faltantes)
- **401**: Token inválido o expirado
- **500**: Error interno del servidor

---

## 12. Códigos de Error

Todos los errores siguen este formato:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Mensaje de error específico",
  "timestamp": "2024-01-15T10:30:00"
}
```

| Código | Estado         | Descripción                                  |
| ------ | -------------- | -------------------------------------------- |
| 200    | OK             | Solicitud exitosa                            |
| 201    | Created        | Recurso creado exitosamente                  |
| 204    | No Content     | Solicitud exitosa sin contenido de respuesta |
| 400    | Bad Request    | Datos inválidos o faltantes                  |
| 401    | Unauthorized   | Token inválido, expirado o no proporcionado  |
| 403    | Forbidden      | Sin permisos para acceder al recurso         |
| 404    | Not Found      | Recurso no encontrado                        |
| 409    | Conflict       | Conflicto de datos (ej: email ya existe)     |
| 500    | Internal Error | Error interno del servidor                   |

---

## 13. Ejemplos con curl

### Registro de usuario

```bash
curl -X POST https://api.streamvault.com/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@streamvault.com",
    "password": "contraseña123",
    "name": "Usuario Demo"
  }'
```

### Login

```bash
curl -X POST https://api.streamvault.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@streamvault.com",
    "password": "contraseña123"
  }'
```

### Obtener catálogo

```bash
curl -X GET "https://api.streamvault.com/api/v1/catalog?page=0&size=20"
```

### Buscar contenido

```bash
curl -X GET "https://api.streamvault.com/api/v1/catalog/search?q=drama&page=0&size=10"
```

### Obtener usuario actual (autenticado)

```bash
curl -X GET https://api.streamvault.com/api/v1/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Actualizar perfil

```bash
curl -X PUT https://api.streamvault.com/api/v1/profiles/PROFILE_ID \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Mi Nuevo Perfil"}'
```

### Obtener URL de streaming

```bash
curl -X GET https://api.streamvault.com/api/v1/stream/CONTENT_ID \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Registrar progreso de visualización

```bash
curl -X POST https://api.streamvault.com/api/v1/history \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "episodeId": "EPISODE_UUID",
    "progressSec": 1200,
    "completed": false
  }'
```

### Actualizar progreso

```bash
curl -X PUT https://api.streamvault.com/api/v1/history/HISTORY_ID/progress \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"progressSec": 1800}'
```

### Listar usuarios (ADMIN)

```bash
curl -X GET "https://api.streamvault.com/api/v1/admin/users?page=0&size=20" \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN"
```

### Crear película en el catálogo (ADMIN)

```bash
curl -X POST https://api.streamvault.com/api/v1/catalog \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Peaky Blinders: El Hombre Inmortal",
    "description": "Una película épica basada en la famosa serie",
    "type": "MOVIE",
    "releaseYear": 2026,
    "rating": "R",
    "thumbnailKey": "Peliculas/Peaky-Blinders-poster.jpg",
    "minioKey": "Peliculas/Peaky_Blinders/playlist.m3u8",
    "genreIds": ["ddddddd2-dddd-4ddd-8ddd-ddddddddddd2", "c3bedb91-6b1b-40ca-99ef-13c245f59a11"],
    "status": "PUBLISHED"
  }'
```

> **Nota:** Al crear contenido con `status: PUBLISHED`, se envía una notificación automática a todos los usuarios.

### Subir thumbnail (ADMIN)

```bash
curl -X POST https://api.streamvault.com/api/v1/admin/upload/thumbnail \
  -H "Authorization: Bearer ADMIN_ACCESS_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

### Refrescar token

```bash
curl -X POST https://api.streamvault.com/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

### Logout

```bash
curl -X POST https://api.streamvault.com/api/v1/auth/logout \
  -H "Authorization: Bearer ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

### Comprar suscripción

```bash
curl -X POST https://api.streamvault.com/api/v1/subscriptions/purchase \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Ver suscripción actual

```bash
curl -X GET https://api.streamvault.com/api/v1/subscriptions/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

_StreamVault API v1.0 - Documentación actualizada automáticamente_
