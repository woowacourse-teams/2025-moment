# Admin SSR → CSR 전환 계획 (REST API)

## 개요

현재 SSR(Thymeleaf) 기반 Admin 컨트롤러를 REST API로 전환하여 CSR 프론트엔드와 연동 가능하게 합니다.

### 현재 상태
- `@Controller` + Thymeleaf 템플릿 렌더링
- 세션 기반 인증 (`HttpSession` + `AdminSessionManager`)
- 리다이렉트 방식 폼 처리
- ErrorCode가 global 모듈에 통합 관리

### 목표 상태
- `@RestController` + JSON 응답
- 기존 세션 인증 유지 (쿠키 기반)
- **`AdminSuccessResponse<T>` 래핑 응답** (Admin 전용)
- **Admin 전용 ErrorCode/Exception/Response 분리** (모듈 분리 대비)
- **엔드포인트 프리픽스: `/api/admin/`**
- **global 모듈 의존성 최소화**

---

## Phase별 상세 계획

각 Phase의 상세 구현 계획은 개별 파일 참조:

| Phase | 파일 | 설명 |
|-------|------|------|
| 1 | [phase-1-infrastructure.md](./admin-rest-api/phase-1-infrastructure.md) | 인프라 설정 (ErrorCode, Exception, Response, Interceptor) |
| 2 | [phase-2-auth-api.md](./admin-rest-api/phase-2-auth-api.md) | 인증 API (login, logout, me) |
| 3 | [phase-3-user-api.md](./admin-rest-api/phase-3-user-api.md) | 사용자 관리 API (CRUD) |
| 4 | [phase-4-account-api.md](./admin-rest-api/phase-4-account-api.md) | 관리자 계정 API (생성, 차단) |
| 5 | [phase-5-session-api.md](./admin-rest-api/phase-5-session-api.md) | 세션 관리 API (조회, 강제 종료) |
| 6 | [phase-6-service-migration.md](./admin-rest-api/phase-6-service-migration.md) | 서비스 레이어 마이그레이션 |

---

## 엔드포인트 요약

### 인증 API (`/api/admin/auth`)
| Method | 경로 | 설명 |
|--------|------|------|
| POST | /api/admin/auth/login | 로그인 |
| POST | /api/admin/auth/logout | 로그아웃 |
| GET | /api/admin/auth/me | 현재 관리자 정보 |

### 사용자 관리 API (`/api/admin/users`)
| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/users | 목록 조회 |
| GET | /api/admin/users/{id} | 상세 조회 |
| PUT | /api/admin/users/{id} | 수정 |
| DELETE | /api/admin/users/{id} | 삭제 |

### 관리자 계정 API (`/api/admin/accounts`) - SUPER_ADMIN 전용
| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/accounts | 목록 조회 |
| POST | /api/admin/accounts | 생성 |
| POST | /api/admin/accounts/{id}/block | 차단 |
| POST | /api/admin/accounts/{id}/unblock | 차단 해제 |

### 세션 관리 API (`/api/admin/sessions`) - SUPER_ADMIN 전용
| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/sessions | 활성 세션 목록 |
| GET | /api/admin/sessions/{sessionId} | 세션 상세 |
| DELETE | /api/admin/sessions/{sessionId} | 세션 강제 종료 |
| DELETE | /api/admin/sessions/admin/{adminId} | 관리자 전체 세션 종료 |
| GET | /api/admin/sessions/history | 세션 히스토리 |

---

## 응답 형식

### 성공 응답 (AdminSuccessResponse)
```json
{
  "status": 200,
  "data": { ... }
}
```

### 에러 응답 (AdminErrorResponse)
```json
{
  "code": "A-001",
  "message": "관리자 로그인에 실패했습니다.",
  "status": 401
}
```

---

## 신규 생성 파일

- `admin/global/exception/AdminErrorCode.java`
- `admin/global/exception/AdminException.java`
- `admin/dto/response/AdminErrorResponse.java`
- `admin/dto/response/AdminSuccessResponse.java`
- `admin/presentation/api/AdminAuthApiController.java`
- `admin/presentation/api/AdminUserApiController.java`
- `admin/presentation/api/AdminAccountApiController.java`
- `admin/presentation/api/AdminSessionApiController.java`
- `admin/presentation/api/AdminApiExceptionHandler.java`
- `admin/dto/response/AdminLoginResponse.java`
- `admin/dto/response/AdminMeResponse.java`
- `admin/dto/response/AdminUserListResponse.java`
- `admin/dto/response/AdminUserDetailResponse.java`
- `admin/dto/response/AdminAccountListResponse.java`
- `admin/dto/response/AdminAccountResponse.java`
- `admin/dto/response/AdminSessionListResponse.java`
- `admin/dto/response/AdminSessionDetailResponse.java`
- `admin/dto/response/AdminSessionHistoryResponse.java`

---

## 모듈 분리 대비

Admin 모듈은 나중에 별도 모듈로 분리 예정입니다.

### Admin 모듈 내 독립 구성 요소
- `AdminErrorCode` - Admin 전용 에러 코드
- `AdminException` - Admin 전용 예외 클래스
- `AdminErrorResponse` - Admin 전용 에러 응답 DTO
- `AdminSuccessResponse` - Admin 전용 성공 응답 DTO
- `AdminApiExceptionHandler` - Admin 전용 예외 처리 핸들러

### global 모듈 의존성 제거 대상
- ~~`SuccessResponse`~~ → `AdminSuccessResponse`
- ~~`ErrorResponse`~~ → `AdminErrorResponse`
- ~~`ErrorCode` (A-001 ~ A-009)~~ → `AdminErrorCode`
- ~~`MomentException`~~ → `AdminException`

### 외부 의존성 (불가피한 경우)
- **User 도메인**: 사용자 목록 조회, 수정, 삭제 (AdminUserApiController)
