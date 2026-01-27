# Admin API 명세서

> 프론트엔드 개발자를 위한 Admin REST API 문서

---

## 기본 정보

- **Base URL**: `/api/admin`
- **인증 방식**: Session 기반 (로그인 후 쿠키 자동 전송)
- **응답 형식**: `AdminSuccessResponse<T>` 래퍼

```json
{
  "status": 200,
  "data": { ... }
}
```

---

## 1. 인증 API

**기본 경로**: `/api/admin/auth`

### 1.1 로그인

```
POST /api/admin/auth/login
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "admin@example.com",
  "password": "password123!"
}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "name": "관리자명",
    "role": "SUPER_ADMIN"
  }
}
```

**에러**
- `404` - 이메일 미존재 또는 비밀번호 불일치
- `403` - 차단된 관리자

---

### 1.2 로그아웃

```
POST /api/admin/auth/logout
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": null
}
```

---

### 1.3 현재 관리자 정보 조회

```
GET /api/admin/auth/me
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "name": "관리자명",
    "role": "SUPER_ADMIN",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 2. 관리자 계정 관리 API (SUPER_ADMIN 전용)

**기본 경로**: `/api/admin/accounts`

### 2.1 관리자 목록 조회

```
GET /api/admin/accounts?page=0&size=15&sort=createdAt,desc
```

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | number | 0 | 페이지 번호 |
| size | number | 15 | 페이지 크기 |
| sort | string | createdAt,desc | 정렬 기준 |

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "admin1@example.com",
        "name": "관리자1",
        "role": "SUPER_ADMIN",
        "isBlocked": false,
        "createdAt": "2024-01-15T10:30:00",
        "deletedAt": null
      }
    ],
    "totalElements": 10,
    "totalPages": 1,
    "number": 0,
    "size": 15
  }
}
```

---

### 2.2 관리자 생성

```
POST /api/admin/accounts
Content-Type: application/json
```

**Request Body**
```json
{
  "email": "newadmin@example.com",
  "name": "신규관리자",
  "password": "SecureP@ss123"
}
```

**검증 규칙**
- `email`: 필수, 이메일 형식
- `name`: 필수, 2~15자
- `password`: 필수, 8~16자, 소문자+숫자+특수문자 포함

**Response (201 Created)**
```json
{
  "status": 201,
  "data": {
    "id": 2,
    "email": "newadmin@example.com",
    "name": "신규관리자",
    "role": "ADMIN",
    "isBlocked": false,
    "createdAt": "2024-01-16T14:00:00",
    "deletedAt": null
  }
}
```

**에러**
- `409` - 중복된 이메일
- `400` - 검증 실패

---

### 2.3 관리자 차단

```
POST /api/admin/accounts/{id}/block
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": null
}
```

**에러**
- `403` - 자신을 차단할 수 없음
- `403` - 마지막 SUPER_ADMIN 차단 불가
- `404` - 관리자 미존재

---

### 2.4 관리자 차단 해제

```
POST /api/admin/accounts/{id}/unblock
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": null
}
```

---

## 3. 사용자 관리 API

**기본 경로**: `/api/admin/users`

### 3.1 사용자 목록 조회

```
GET /api/admin/users?page=0&size=15
```

**Query Parameters**
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| page | number | 0 | 페이지 번호 |
| size | number | 15 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user@example.com",
        "nickname": "사용자1",
        "providerType": "EMAIL",
        "createdAt": "2024-01-15T10:30:00",
        "deletedAt": null
      }
    ],
    "totalElements": 100,
    "totalPages": 7,
    "number": 0,
    "size": 15
  }
}
```

**providerType 종류**
- `EMAIL` - 이메일 가입
- `GOOGLE` - 구글 소셜 로그인
- `KAKAO` - 카카오 소셜 로그인
- `APPLE` - 애플 소셜 로그인

---

### 3.2 사용자 상세 조회

```
GET /api/admin/users/{id}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "사용자1",
    "providerType": "EMAIL",
    "createdAt": "2024-01-15T10:30:00",
    "deletedAt": null
  }
}
```

**에러**
- `404` - 사용자 미존재

---

### 3.3 사용자 정보 수정

```
PUT /api/admin/users/{id}
Content-Type: application/json
```

**Request Body**
```json
{
  "nickname": "새로운닉네임"
}
```

**검증 규칙**
- `nickname`: 필수, 1~15자

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "nickname": "새로운닉네임",
    "providerType": "EMAIL",
    "createdAt": "2024-01-15T10:30:00",
    "deletedAt": null
  }
}
```

---

### 3.4 사용자 삭제 (Soft Delete)

```
DELETE /api/admin/users/{id}
```

**Response (204 No Content)**
```json
{
  "status": 204,
  "data": null
}
```

---

## 4. 세션 관리 API (SUPER_ADMIN 전용)

**기본 경로**: `/api/admin/sessions`

### 4.1 활성 세션 목록 조회

```
GET /api/admin/sessions?adminId=1&ipAddress=192.168.1.1
```

**Query Parameters** (선택)
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| adminId | number | 특정 관리자로 필터링 |
| ipAddress | string | IP 주소로 필터링 |

**Response (200 OK)**
```json
{
  "status": 200,
  "data": [
    {
      "id": 1,
      "adminId": 1,
      "adminName": "관리자1",
      "adminEmail": "admin1@example.com",
      "adminRole": "SUPER_ADMIN",
      "sessionId": "A1B2C3D4E5F6G7H8...",
      "loginTime": "2024-01-16T10:00:00",
      "lastAccessTime": "2024-01-16T11:30:00",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0..."
    }
  ]
}
```

---

### 4.2 세션 상세 조회

```
GET /api/admin/sessions/{id}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "adminId": 1,
    "adminName": "관리자1",
    "adminEmail": "admin1@example.com",
    "adminRole": "SUPER_ADMIN",
    "sessionId": "A1B2C3D4E5F6G7H8...",
    "loginTime": "2024-01-16T10:00:00",
    "lastAccessTime": "2024-01-16T11:30:00",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "browser": "Chrome",
    "os": "Windows",
    "deviceType": "Desktop",
    "logoutTime": null,
    "isActive": true
  }
}
```

---

### 4.3 특정 세션 강제 종료

```
DELETE /api/admin/sessions/{sessionId}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": null
}
```

---

### 4.4 관리자의 모든 세션 강제 종료

```
DELETE /api/admin/sessions/admin/{adminId}
```

**Response (200 OK)**
```json
{
  "status": 200,
  "data": null
}
```

---

### 4.5 세션 히스토리 조회

```
GET /api/admin/sessions/history?adminId=1&startDate=2024-01-01&endDate=2024-01-31&page=0&size=15
```

**Query Parameters** (선택)
| 파라미터 | 타입 | 기본값 | 설명 |
|---------|------|--------|------|
| adminId | number | - | 특정 관리자로 필터링 |
| startDate | string | - | 시작 날짜 (yyyy-MM-dd) |
| endDate | string | - | 종료 날짜 (yyyy-MM-dd) |
| page | number | 0 | 페이지 번호 |
| size | number | 15 | 페이지 크기 |

**Response (200 OK)**
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "adminName": "관리자1",
        "adminEmail": "admin1@example.com",
        "loginTime": "2024-01-16T10:00:00",
        "logoutTime": "2024-01-16T18:00:00",
        "ipAddress": "192.168.1.1",
        "sessionStatus": "LOGGED_OUT"
      }
    ],
    "totalElements": 50,
    "totalPages": 4,
    "number": 0,
    "size": 15
  }
}
```

**sessionStatus 종류**
- `ACTIVE` - 활성 세션
- `LOGGED_OUT` - 정상 로그아웃
- `FORCED_LOGOUT` - 강제 로그아웃
- `EXPIRED` - 세션 만료

---

## 권한 정리

| API | SUPER_ADMIN | ADMIN |
|-----|-------------|-------|
| 인증 (로그인/로그아웃/me) | ✅ | ✅ |
| 관리자 계정 관리 | ✅ | ❌ |
| 사용자 관리 | ✅ | ✅ |
| 세션 관리 | ✅ | ❌ |

---

## 에러 코드

| 코드 | 설명 |
|------|------|
| LOGIN_FAILED | 로그인 실패 (이메일/비밀번호 불일치) |
| NOT_FOUND | 관리자 미존재 |
| DUPLICATE_EMAIL | 중복된 이메일 |
| UNAUTHORIZED | 권한 없음 |
| CANNOT_BLOCK_SELF | 자신을 차단할 수 없음 |
| CANNOT_BLOCK_LAST_SUPER_ADMIN | 마지막 SUPER_ADMIN 차단 불가 |
| USER_NOT_FOUND | 사용자 미존재 |
| SESSION_NOT_FOUND | 세션 미존재 |
| ADMIN_INVALID_INFO | 검증 실패 |