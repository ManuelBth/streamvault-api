# StreamVault API - Insomnia Testing Guide

This document provides all the API endpoints and how to test them using Insomnia.

## Base URL

```
http://localhost:8080/api/v1
```

---

## Authentication Endpoints

### 1. Register User

Register a new user in the system.

**Endpoint:** `POST /auth/register`

**Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
  "email": "testuser@streamvault.local",
  "password": "password123",
  "name": "Test User"
}
```

**Validation Rules:**

- Email must end with `@streamvault.local`
- Password must be at least 8 characters
- Name is required

**Expected Response (201 Created):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

---

### 2. Login

Authenticate an existing user.

**Endpoint:** `POST /auth/login`

**Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
  "email": "testuser@streamvault.local",
  "password": "password123"
}
```

**Expected Response (200 OK):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

---

### 3. Refresh Token

Get new access token using refresh token.

**Endpoint:** `POST /auth/refresh`

**Headers:**

```
Content-Type: application/json
```

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Expected Response (200 OK):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

---

### 4. Logout

Invalidate the current refresh token.

**Endpoint:** `POST /auth/logout`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Expected Response (204 No Content)**

---

### 5. Confirm Email

Confirm user email address (placeholder endpoint).

**Endpoint:** `GET /auth/confirm`

**Query Parameters:**

- `token` (required): Confirmation token

**Example:**

```
GET /auth/confirm?token=abc123
```

**Expected Response:** Redirect or success message

---

## Health Check

### Health Endpoint

Check if the API is running.

**Endpoint:** `GET /actuator/health`

**Expected Response (200 OK):**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

## Insomnia Collection Setup

### Step 1: Create Environment

Create a new environment in Insomnia with these variables:

| Variable       | Initial Value           | Description           |
| -------------- | ----------------------- | --------------------- |
| `baseUrl`      | `http://localhost:8080` | API base URL          |
| `accessToken`  | ``                      | Current access token  |
| `refreshToken` | ``                      | Current refresh token |

### Step 2: Create Authentication Flow

Use Insomnia's authentication feature to automatically update tokens:

1. Create a request **"Login"**
2. Add a **Post-Response Script** to save tokens:

```javascript
const data = JSON(response.body);
inspine.environment.set("accessToken", data.accessToken);
inspine.environment.set("refreshToken", data.refreshToken);
```

1. In other requests, use Authorization header:

```
Authorization: Bearer {{accessToken}}
```

### Step 3: Create Request Collection

Create these requests in Insomnia:

#### Auth Folder

| Request Name  | Method | URL                                   | Body Type |
| ------------- | ------ | ------------------------------------- | --------- |
| Register      | POST   | `{{baseUrl}}/auth/register`           | JSON      |
| Login         | POST   | `{{baseUrl}}/auth/login`              | JSON      |
| Refresh Token | POST   | `{{baseUrl}}/auth/refresh`            | JSON      |
| Logout        | POST   | `{{baseUrl}}/auth/logout`             | JSON      |
| Confirm Email | GET    | `{{baseUrl}}/auth/confirm?token=test` | -         |

#### System Folder

| Request Name | Method | URL                           |
| ------------ | ------ | ----------------------------- |
| Health Check | GET    | `{{baseUrl}}/actuator/health` |

---

## Testing Scenarios

### Scenario 1: Register a New User

1. Send **POST /auth/register** with valid data
2. Verify 201 status code
3. Verify response contains accessToken and refreshToken
4. Save tokens to environment

### Scenario 2: Login with Existing User

1. Send **POST /auth/login** with registered credentials
2. Verify 200 status code
3. Verify tokens are returned

### Scenario 3: Access Protected Endpoint

1. Copy accessToken from login response
2. Send request to protected endpoint with:

   ```
   Authorization: Bearer <accessToken>
   ```

3. Verify 200 OK

### Scenario 4: Token Expiration

1. Wait for access token to expire (15 minutes)
2. Send request with expired token
3. Verify 401 Unauthorized

### Scenario 5: Refresh Token

1. Use expired access token
2. Send **POST /auth/refresh** with refresh token
3. Verify new tokens are returned

### Scenario 6: Logout

1. Send **POST /auth/logout** with valid refresh token
2. Verify 204 No Content
3. Try to use the same refresh token
4. Verify token is now invalid

---

## Error Responses

### 400 Bad Request

```json
{
  "error": "Validation failed",
  "details": ["Email is required", "Password must be at least 8 characters"]
}
```

### 401 Unauthorized

```json
{
  "error": "Invalid credentials"
}
```

### 404 Not Found

```json
{
  "error": "Resource not found"
}
```

### 500 Internal Server Error

```json
{
  "error": "Internal server error"
}
```

---

## Notes

- All timestamps are in milliseconds
- Access token expires in 15 minutes (900000ms)
- Refresh token expires in 7 days (604800000ms)
- CORS is configured to allow `http://localhost:4200` by default
- All protected endpoints require Bearer token in Authorization header

---

## Notification Endpoints

### 1. Get All Notifications

Get all notifications for the authenticated user.

**Endpoint:** `GET /notifications`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Expected Response (200 OK):**

```json
[
  {
    "id": "uuid-here",
    "type": "NEW_CONTENT",
    "title": "New series available",
    "message": "A new series has been added",
    "relatedId": null,
    "isRead": false,
    "createdAt": "2026-03-17T15:30:00"
  }
]
```

---

### 2. Get Unread Notifications

Get only unread notifications for the authenticated user.

**Endpoint:** `GET /notifications/unread`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Expected Response (200 OK):**

```json
[
  {
    "id": "uuid-here",
    "type": "NEW_EPISODE",
    "title": "New episode released",
    "message": "Season 2 Episode 5 is now available",
    "relatedId": "uuid-of-episode",
    "isRead": false,
    "createdAt": "2026-03-17T15:30:00"
  }
]
```

---

### 3. Get Unread Count

Get the count of unread notifications.

**Endpoint:** `GET /notifications/unread/count`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Expected Response (200 OK):**

```json
{
  "count": 5
}
```

---

### 4. Mark Notification as Read

Mark a specific notification as read.

**Endpoint:** `PUT /notifications/{id}/read`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (UUID): Notification ID

**Expected Response (200 OK):**

```
(No content - empty body)
```

---

### 5. Mark All Notifications as Read

Mark all notifications as read for the authenticated user.

**Endpoint:** `PUT /notifications/read-all`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Expected Response (200 OK):**

```
(No content - empty body)
```

---

## Email Endpoints

### 1. Send Email

Send an email (requires admin privileges in future).

**Endpoint:** `POST /mail/send`

**Headers:**

```
Content-Type: application/json
Authorization: Bearer <access_token>
```

**Request Body:**

```json
{
  "to": "user@example.com",
  "subject": "Test Email",
  "body": "This is a test email content"
}
```

**Validation Rules:**

- `to`: Required, must be valid email
- `subject`: Required
- `body`: Required

**Expected Response (200 OK):**

```
(No content - empty body)
```

---

## WebSocket Endpoints

### WebSocket Connection for Real-Time Notifications

Connect to WebSocket to receive real-time notifications.

**WebSocket URL:**

```
ws://localhost:8080/ws/notifications
```

**Connection Headers:**

- Optional: `Authorization: Bearer <access_token>` (for user identification)

**Authentication:**
The user ID must be passed in the WebSocket session attributes. In production, this is set via the authentication interceptor.

**Incoming Messages (Notification Payload):**

```json
{
  "id": "uuid-here",
  "type": "NEW_CONTENT",
  "title": "New content available",
  "message": "A new movie has been added to the platform",
  "relatedId": null,
  "isRead": false,
  "createdAt": "2026-03-17T15:30:00"
}
```

**Testing WebSocket with Insomnia:**

1. Insomnia supports WebSocket connections since version 7.0+
2. Create a new request
3. Select WebSocket as the method
4. Enter URL: `ws://localhost:8080/ws/notifications`
5. Connect and observe incoming messages

---

## Updated Insomnia Collection

### Step 1: Create Environment

| Variable       | Initial Value           | Description           |
| -------------- | ----------------------- | --------------------- |
| `baseUrl`      | `http://localhost:8080` | API base URL          |
| `accessToken`  | ``                      | Current access token  |
| `refreshToken` | ``                      | Current refresh token |
| `userId`       | ``                      | Current user ID       |

### Step 2: Add These Requests

#### Notifications Folder

| Request Name             | Method | URL                                             | Body Type |
| ------------------------ | ------ | ----------------------------------------------- | --------- |
| Get All Notifications    | GET    | `{{baseUrl}}/api/v1/notifications`              | -         |
| Get Unread Notifications | GET    | `{{baseUrl}}/api/v1/notifications/unread`       | -         |
| Get Unread Count         | GET    | `{{baseUrl}}/api/v1/notifications/unread/count` | -         |
| Mark as Read             | PUT    | `{{baseUrl}}/api/v1/notifications/{id}/read`    | -         |
| Mark All Read            | PUT    | `{{baseUrl}}/api/v1/notifications/read-all`     | -         |

#### Mail Folder

| Request Name | Method | URL                            | Body Type |
| ------------ | ------ | ------------------------------ | --------- |
| Send Email   | POST   | `{{baseUrl}}/api/v1/mail/send` | JSON      |

---

## Testing Scenarios - Notifications

### Scenario 1: Get Notifications

1. Send **GET /api/v1/notifications** with valid token
2. Verify 200 OK
3. Verify array of notifications is returned

### Scenario 2: Mark Notification as Read

1. Get notifications and note an ID
2. Send **PUT /api/v1/notifications/{id}/read**
3. Verify 200 OK
4. Get notifications again and verify isRead is true

### Scenario 3: Mark All as Read

1. Send **PUT /api/v1/notifications/read-all**
2. Verify 200 OK
3. Get unread count and verify it's 0

### Scenario 4: Send Email

1. Send **POST /api/v1/mail/send** with valid email data
2. Verify 200 OK
3. Check email inbox (if Postfix is configured)

### Scenario 5: WebSocket Real-Time Notifications

1. Connect to WebSocket at `ws://localhost:8080/ws/notifications`
2. Create a notification via backend (or wait for system notification)
3. Verify notification is received in real-time
