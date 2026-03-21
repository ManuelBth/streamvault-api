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
   - [GET /api/v1/auth/confirm](#get-apiv1authconfirm)
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
4. [Catálogo](#4-catálogo)
   - [GET /api/v1/catalog](#get-apiv1catalog)
   - [GET /api/v1/catalog/{id}](#get-apiv1catalogid)
   - [GET /api/v1/catalog/search](#get-apiv1catalogsearch)
   - [GET /api/v1/catalog/{id}/seasons](#get-apiv1catalogidseasons)
   - [GET /api/v1/catalog/seasons/{seasonId}/episodes](#get-apiv1catalogseasonsseasonidepisodes)
   - [GET /api/v1/catalog/genres](#get-apiv1cataloggenres)
   - [POST /api/v1/catalog](#post-apiv1catalog)
   - [PUT /api/v1/catalog/{id}](#put-apiv1catalogid)
   - [DELETE /api/v1/catalog/{id}](#delete-apiv1catalogid)
5. [Streaming](#5-streaming)
   - [GET /api/v1/stream/{contentId}](#get-apiv1streamcontentid)
   - [GET /api/v1/stream/{contentId}/episode/{episodeId}](#get-apiv1streamcontentidepisodeepisodeid)
6. [Historial](#6-historial)
   - [GET /api/v1/history](#get-apiv1history)
   - [GET /api/v1/history/{id}](#get-apiv1historyid)
   - [POST /api/v1/history](#post-apiv1history)
   - [PUT /api/v1/history/{id}/progress](#put-apiv1historyidprogress)
   - [PUT /api/v1/history/{id}/completed](#put-apiv1historyidcompleted)
7. [Administración](#7-administración)
   - [GET /api/v1/admin/users](#get-apiv1adminusers)
   - [GET /api/v1/admin/users/{id}](#get-apiv1adminusersid)
   - [POST /api/v1/admin/upload/thumbnail](#post-apiv1adminuploadthumbnail)
8. [Códigos de Error](#códigos-de-error)
9. [Ejemplos con curl](#ejemplos-con-curl)

---

## 1. Autenticación

### POST /api/v1/auth/register

**Descripción:** Registra un nuevo usuario en la plataforma. Solo se permiten emails del dominio `@streamvault.com`.

**Autenticación:** Public

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
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Respuesta de Error (400):**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El email es obligatorio",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/v1/auth/login

**Descripción:** Inicia sesión y devuelve tokens de acceso y refresh.

**Autenticación:** Public

**Headers:**

- `Content-Type: application/json`

**Request Body:**

```json
{
  "email": "usuario@streamvault.com",
  "password": "contraseña123"
}
```

**Respuesta Exitosa (200):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Respuesta de Error (401):**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/v1/auth/refresh

**Descripción:** Refresca el token de acceso usando un refresh token válido.

**Autenticación:** Public

**Headers:**

- `Content-Type: application/json`

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta Exitosa (200):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

**Respuesta de Error (401):**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Refresh token inválido o expirado",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/v1/auth/logout

**Descripción:** Invalida el refresh token (logout).

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Respuesta Exitosa (200):**

```json
{}
```

---

### GET /api/v1/auth/confirm

**Descripción:** Confirma el email del usuario (endpoint placeholder).

**Autenticación:** Public

**Query Parameters:**

- `token` (string): Token de confirmación enviado por email

**Respuesta Exitosa (200):**

```json
{
  "message": "Email confirmed"
}
```

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
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Respuesta de Error (401):**

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "timestamp": "2024-01-15T10:30:00"
}
```

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
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-16T12:00:00"
}
```

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

**Respuesta Exitosa (200):**

```json
{}
```

**Respuesta de Error (400):**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Contraseña incorrecta",
  "timestamp": "2024-01-15T10:30:00"
}
```

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
  "role": "USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Respuesta de Error (404):**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "timestamp": "2024-01-15T10:30:00"
}
```

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
    "avatarUrl": "https://cdn.streamvault.com/avatars/child.png",
    "createdAt": "2024-01-16T14:00:00"
  }
]
```

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
  "avatarUrl": "https://cdn.streamvault.com/avatars/default.png",
  "createdAt": "2024-01-17T09:00:00"
}
```

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

**Respuesta de Error (403):**

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "No autorizado",
  "timestamp": "2024-01-15T10:30:00"
}
```

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

**Respuesta Exitosa (200):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Nombre Actualizado",
  "avatarUrl": "https://cdn.streamvault.com/avatars/default.png",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### DELETE /api/v1/profiles/{id}

**Descripción:** Elimina un perfil (solo si pertenece al usuario).

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `id` (UUID): ID único del perfil

**Respuesta Exitosa (200):**

```json
{}
```

---

## 4. Catálogo

### GET /api/v1/catalog

**Descripción:** Obtiene el catálogo completo de contenido con paginación.

**Autenticación:** Public

**Query Parameters:**

- `page` (int): Número de página (default: 0)
- `size` (int): Tamaño de página (default: 20)

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

---

### GET /api/v1/catalog/{id}

**Descripción:** Obtiene un contenido específico del catálogo por su ID.

**Autenticación:** Public

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
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440102",
      "name": "Aventura"
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**Respuesta de Error (404):**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Contenido no encontrado",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### GET /api/v1/catalog/search

**Descripción:** Busca contenido en el catálogo por título o descripción.

**Autenticación:** Public

**Query Parameters:**

- `q` (string): Término de búsqueda
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

---

### GET /api/v1/catalog/{id}/seasons

**Descripción:** Obtiene todas las temporadas de una serie.

**Autenticación:** Public

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

---

### GET /api/v1/catalog/seasons/{seasonId}/episodes

**Descripción:** Obtiene todos los episodios de una temporada.

**Autenticación:** Public

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

---

### GET /api/v1/catalog/genres

**Descripción:** Obtiene todos los géneros disponibles.

**Autenticación:** Public

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
    "550e8400-e29b-41d4-a716-446655440100",
    "550e8400-e29b-41d4-a716-446655440101"
  ],
  "status": "DRAFT"
}
```

| Campo        | Tipo   | Requerido | Descripción                      |
| ------------ | ------ | --------- | -------------------------------- |
| title        | string | Sí        | Título del contenido             |
| description  | string | No        | Descripción                      |
| type         | enum   | Sí        | MOVIE o SERIES                   |
| releaseYear  | int    | No        | Año de lanzamiento               |
| rating       | string | No        | Clasificación (ej: PG-13, TV-MA) |
| thumbnailKey | string | No        | Clave del thumbnail en MinIO     |
| minioKey     | string | No        | Clave del video en MinIO         |
| genreIds     | UUID[] | No        | Lista de IDs de géneros          |
| status       | enum   | No        | DRAFT, PUBLISHED, ARCHIVED       |

**Respuesta Exitosa (200):**

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
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440101",
      "name": "Acción"
    }
  ],
  "createdAt": "2024-01-17T10:00:00",
  "updatedAt": "2024-01-17T10:00:00"
}
```

---

### PUT /api/v1/catalog/{id}

**Descripción:** Actualiza contenido existente en el catálogo (requiere rol ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**

- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Path Parameters:**

- `id` (UUID): ID del contenido

**Request Body:** (mismo que POST)

**Respuesta Exitosa (200):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "title": "Película Actualizada",
  "description": "Nueva descripción",
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

---

### DELETE /api/v1/catalog/{id}

**Descripción:** Elimina contenido del catálogo (requiere rol ADMIN).

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `id` (UUID): ID del contenido

**Respuesta Exitosa (204):** Sin contenido

---

## 5. Streaming

### GET /api/v1/stream/{contentId}

**Descripción:** Obtiene la URL de streaming para una película o contenido. Requiere suscripción activa.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `contentId` (UUID): ID del contenido

**Respuesta Exitosa (200):**

```json
{
  "url": "https://cdn.streamvault.com/stream/movie.mp4?token=abc123&expires=1705312800",
  "expiresAt": "2024-01-15T12:00:00Z"
}
```

**Respuesta de Error (403):**

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Suscripción no activa",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Respuesta de Error (404):**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Contenido no encontrado",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### GET /api/v1/stream/{contentId}/episode/{episodeId}

**Descripción:** Obtiene la URL de streaming para un episodio específico. Requiere suscripción activa.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `contentId` (UUID): ID del contenido (serie)
- `episodeId` (UUID): ID del episodio

**Respuesta Exitosa (200):**

```json
{
  "url": "https://cdn.streamvault.com/stream/s01e01.mp4?token=abc123&expires=1705312800",
  "expiresAt": "2024-01-15T12:00:00Z"
}
```

**Respuesta de Error (403):**

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Suscripción no activa",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 6. Historial

### GET /api/v1/history

**Descripción:** Obtiene el historial de visualización del usuario autenticado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

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

---

### GET /api/v1/history/{id}

**Descripción:** Obtiene un registro específico del historial.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `id` (UUID): ID del registro de historial

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

---

### POST /api/v1/history

**Descripción:** Inicia el seguimiento de visualización de un episodio.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`
- `Content-Type: application/json`

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

**Respuesta Exitosa (200):**

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

---

### PUT /api/v1/history/{id}/progress

**Descripción:** Actualiza el progreso de visualización de un registro.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Path Parameters:**

- `id` (UUID): ID del registro de historial

**Request Body:**

```json
{
  "progressSec": 1200,
  "completed": false
}
```

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

---

### PUT /api/v1/history/{id}/completed

**Descripción:** Marca un registro de historial como completado.

**Autenticación:** Required (JWT Bearer token)

**Headers:**

- `Authorization: Bearer {token}`

**Path Parameters:**

- `id` (UUID): ID del registro de historial

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

---

## 7. Administración

### GET /api/v1/admin/users

**Descripción:** Lista todos los usuarios del sistema (solo ADMIN).

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
      "role": "USER",
      "isVerified": true,
      "createdAt": "2024-01-15T10:30:00"
    },
    {
      "id": "550e8400-e29b-41d4-a716-446655440001",
      "email": "admin@streamvault.com",
      "name": "Administrador",
      "role": "ADMIN",
      "isVerified": true,
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "total": 150,
  "page": 0,
  "size": 20
}
```

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
  "role": "USER",
  "isVerified": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Respuesta de Error (404):**

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### POST /api/v1/admin/upload/thumbnail

**Descripción:** Sube una imagen de thumbnail al sistema.

**Autenticación:** Required (JWT Bearer token - ADMIN)

**Headers:**

- `Authorization: Bearer {token}`
- `Content-Type: multipart/form-data`

**Form Data:**

- `file` (File): Archivo de imagen (JPEG, PNG, WebP)

**Respuesta Exitosa (200):**

```json
{
  "key": "thumbnails/uuid-1234.jpg",
  "url": "https://cdn.streamvault.com/thumbnails/uuid-1234.jpg",
  "filename": "thumbnail.jpg",
  "contentType": "image/jpeg",
  "size": 245000,
  "uploadedAt": "2024-01-17T10:00:00Z"
}
```

**Respuesta de Error (400):**

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Tipo de archivo no soportado",
  "timestamp": "2024-01-17T10:00:00"
}
```

---

## Códigos de Error

| Código | Estado         | Descripción                                  |
| ------ | -------------- | -------------------------------------------- |
| 200    | OK             | Solicitud exitosa                            |
| 201    | Created        | Recurso creado exitosamente                  |
| 204    | No Content     | Solicitud exitosa sin contenido de respuesta |
| 400    | Bad Request    | Datos inválidos o faltantes                  |
| 401    | Unauthorized   | Token inválido, expirado o no proporcionado  |
| 403    | Forbidden      | Sin permisos para acceder al recurso         |
| 404    | Not Found      | Recurso no encontrado                        |
| 500    | Internal Error | Error interno del servidor                   |

---

## Ejemplos con curl

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

---

_Documentación generada automáticamente para StreamVault API v1.0_
