# feat/#1018 브랜치 변경사항 보고서

## 개요

이 브랜치는 **Admin 모듈의 SSR(Thymeleaf) → CSR(REST API) 전환** 작업입니다.

### 커밋 요약 (Admin 관련)
| 커밋 | 설명 |
|------|------|
| `8139fb7a` | Admin REST API 인프라 구축 |
| `f0bc6eec` | Admin 인증 REST API 추가 |
| `0f236bc0` | Admin 사용자 관리 REST API 추가 |
| `1f1ce108` | Admin 계정 관리 REST API 추가 (SUPER_ADMIN) |
| `ba59bba3` | Admin 세션 관리 REST API 추가 (SUPER_ADMIN) |
| `cda29d05` | MomentException → AdminException 마이그레이션 |
| `9f66a672` | SSR 컨트롤러 및 Advice 클래스 삭제 |
| `c6892fae` | Thymeleaf 템플릿 파일 삭제 (12개) |
| `95aa0150` | Thymeleaf 의존성 제거 |

---

## 반드시 확인해야 할 사항

### 1. 보안 관련
- [ ] **세션 고정 공격 방지**: 로그인 시 `httpRequest.changeSessionId()` 호출 확인
- [ ] **마지막 SUPER_ADMIN 보호**: 자기 자신 차단 및 마지막 SUPER_ADMIN 차단 방지 로직
- [ ] **차단된 관리자 이중 검증**: 로그인 시 + 요청 시 모두 검증
- [ ] **SUPER_ADMIN 경로 보호**: `/api/admin/accounts`, `/api/admin/sessions`

### 2. 데이터 무결성
- [ ] **Soft Delete 패턴**: Admin, AdminSession 엔티티의 `@SQLDelete`, `@SQLRestriction` 확인
- [ ] **Native Query 우회**: 차단된 관리자 조회 시 `@SQLRestriction` 우회 쿼리 정상 동작

### 3. 세션 관리
- [ ] **Spring Session JDBC 설정**: `SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES` 테이블 생성 확인
- [ ] **이중 세션 관리**: HTTP 세션(Spring Session) + DB 세션(admin_sessions) 동기화
- [ ] **강제 로그아웃**: 관리자 차단 시 모든 활성 세션 무효화

### 4. API 응답 일관성
- [ ] **AdminSuccessResponse<T>** 래퍼 패턴 적용 확인
- [ ] **HTTP 상태 코드**: 200(OK), 201(CREATED), 204(NO_CONTENT) 적절히 사용

### 5. 의존성 제거
- [ ] **Thymeleaf 의존성 완전 제거**: `spring-boot-starter-thymeleaf`, `thymeleaf-layout-dialect`
- [ ] **템플릿 파일 삭제**: `resources/templates/admin/` 디렉토리 전체

---

## API 엔드포인트 목록

### 1. 인증 API (`/api/admin/auth`)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| POST | `/login` | 관리자 로그인 | 없음 |
| POST | `/logout` | 관리자 로그아웃 | 인증 필요 |
| GET | `/me` | 현재 관리자 정보 | 인증 필요 |

### 2. 사용자 관리 API (`/api/admin/users`)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/` | 사용자 목록 (페이징) | ADMIN 이상 |
| GET | `/{id}` | 사용자 상세 | ADMIN 이상 |
| PUT | `/{id}` | 사용자 정보 수정 | ADMIN 이상 |
| DELETE | `/{id}` | 사용자 삭제 (Soft) | ADMIN 이상 |

### 3. 계정 관리 API (`/api/admin/accounts`)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/` | 관리자 목록 (차단 포함) | SUPER_ADMIN |
| POST | `/` | 관리자 생성 | SUPER_ADMIN |
| POST | `/{id}/block` | 관리자 차단 | SUPER_ADMIN |
| POST | `/{id}/unblock` | 관리자 차단 해제 | SUPER_ADMIN |

### 4. 세션 관리 API (`/api/admin/sessions`)
| Method | Endpoint | 설명 | 권한 |
|--------|----------|------|------|
| GET | `/` | 활성 세션 목록 | SUPER_ADMIN |
| GET | `/{id}` | 세션 상세 | SUPER_ADMIN |
| DELETE | `/{sessionId}` | 세션 강제 종료 | SUPER_ADMIN |
| DELETE | `/admin/{adminId}` | 관리자 전체 세션 종료 | SUPER_ADMIN |
| GET | `/history` | 세션 히스토리 | SUPER_ADMIN |

---

## 요청 처리 다이어그램

### 1. 로그인 API (`POST /api/admin/auth/login`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              로그인 요청 흐름                                  │
└─────────────────────────────────────────────────────────────────────────────┘

Client
  │
  │ POST /api/admin/auth/login
  │ Body: { email, password }
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthApiController.login()                                              │
│ • @Valid AdminLoginRequest 검증                                              │
│ • HttpServletRequest에서 IP, User-Agent 추출                                  │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminService.authenticateAdmin(email, password)                             │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 1. AdminRepository.findByEmail(email)                                   │ │
│ │    └─ 없으면 → AdminException(LOGIN_FAILED)                             │ │
│ │                                                                         │ │
│ │ 2. BCrypt.matches(password, admin.getPassword())                        │ │
│ │    └─ 불일치 → AdminException(LOGIN_FAILED)                             │ │
│ │                                                                         │ │
│ │ 3. admin.isBlocked() 확인 (방어적 프로그래밍)                              │ │
│ │    └─ 차단됨 → AdminException(LOGIN_FAILED)                             │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  │ Admin 객체 반환
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthApiController (계속)                                               │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 4. httpRequest.changeSessionId()                                        │ │
│ │    └─ 세션 고정 공격 방지                                                 │ │
│ │                                                                         │ │
│ │ 5. ClientIpExtractor.extractClientIp(request)                           │ │
│ │    └─ X-Forwarded-For → Proxy-Client-IP → ... → RemoteAddr              │ │
│ │                                                                         │ │
│ │ 6. request.getHeader("User-Agent")                                      │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminSessionManager.registerSession(session, adminId, role, ip, userAgent)  │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 7. HTTP 세션에 속성 저장                                                  │ │
│ │    • session.setAttribute("ADMIN_ID", adminId)                          │ │
│ │    • session.setAttribute("ADMIN_ROLE", role)                           │ │
│ │                                                                         │ │
│ │ 8. DB에 AdminSession 엔티티 저장                                         │ │
│ │    • adminId, sessionId, loginTime, ipAddress, userAgent                │ │
│ │    • AdminSessionRepository.save(adminSession)                          │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Response                                                                    │
│ HTTP 200 OK                                                                 │
│ {                                                                           │
│   "status": 200,                                                            │
│   "data": { "id", "email", "name", "role" }                                 │
│ }                                                                           │
│ + Set-Cookie: SESSION=xxx (Spring Session JDBC)                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 2. 인증된 API 요청 흐름 (공통)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           인증된 API 요청 흐름                                │
└─────────────────────────────────────────────────────────────────────────────┘

Client
  │
  │ Cookie: SESSION=xxx
  │ GET/POST/PUT/DELETE /api/admin/*
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthInterceptor.preHandle()                                            │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 1. HTTP 세션 존재 확인                                                    │ │
│ │    └─ 없음 → handleUnauthorized() → 401 응답                             │ │
│ │                                                                         │ │
│ │ 2. sessionManager.validateAuthorized(session)                           │ │
│ │    └─ ADMIN_ID, ADMIN_ROLE 속성 확인                                     │ │
│ │    └─ 없음 → handleUnauthorized() → 401 응답                             │ │
│ │                                                                         │ │
│ │ 3. sessionManager.isSessionActiveInDb(sessionId)                        │ │
│ │    └─ DB에서 logout_time IS NULL 확인                                    │ │
│ │    └─ 비활성 → handleUnauthorized() → 401 응답                           │ │
│ │       (차단되었거나 강제 로그아웃된 경우)                                    │ │
│ │                                                                         │ │
│ │ 4. SUPER_ADMIN 전용 경로 확인                                             │ │
│ │    • /api/admin/accounts, /api/admin/sessions                           │ │
│ │    └─ SUPER_ADMIN 아님 → handleForbidden() → 403 응답                    │ │
│ │                                                                         │ │
│ │ 5. sessionManager.updateLastAccessTime(sessionId)                       │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  │ return true (통과)
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Controller → Service → Repository → Response                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 3. 관리자 차단 API (`POST /api/admin/accounts/{id}/block`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              관리자 차단 흐름                                  │
└─────────────────────────────────────────────────────────────────────────────┘

Client (SUPER_ADMIN)
  │
  │ POST /api/admin/accounts/{targetAdminId}/block
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthInterceptor.preHandle()                                            │
│ • 세션 검증 통과                                                              │
│ • SUPER_ADMIN 권한 확인 통과                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAccountApiController.blockAdmin(targetAdminId, session)                │
│ • currentAdminId = sessionManager.getId(session)                            │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminManagementApplicationService.blockAdminAndInvalidateSessions()         │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ @Transactional                                                          │ │
│ │                                                                         │ │
│ │ 1. AdminService.validateNotSelfBlock(currentId, targetId)               │ │
│ │    └─ 같으면 → AdminException(CANNOT_BLOCK_SELF)                        │ │
│ │                                                                         │ │
│ │ 2. AdminService.blockAdmin(targetAdminId)                               │ │
│ │    ┌─────────────────────────────────────────────────────────────────┐  │ │
│ │    │ 2-1. validateNotLastSuperAdmin(adminId)                         │  │ │
│ │    │      • AdminRepository.countByRole(SUPER_ADMIN)                 │  │ │
│ │    │      └─ count <= 1 && target.isSuperAdmin()                     │  │ │
│ │    │         → AdminException(CANNOT_BLOCK_LAST_SUPER_ADMIN)         │  │ │
│ │    │                                                                 │  │ │
│ │    │ 2-2. @SQLDelete 실행 (Soft Delete)                              │  │ │
│ │    │      UPDATE admins SET deleted_at = NOW() WHERE id = ?          │  │ │
│ │    └─────────────────────────────────────────────────────────────────┘  │ │
│ │                                                                         │ │
│ │ 3. SessionManager.invalidateAllSessionsForAdmin(targetAdminId)          │ │
│ │    ┌─────────────────────────────────────────────────────────────────┐  │ │
│ │    │ • 해당 관리자의 모든 활성 세션 조회                                  │  │ │
│ │    │   WHERE admin_id = ? AND logout_time IS NULL                    │  │ │
│ │    │                                                                 │  │ │
│ │    │ • 각 세션에 logout_time = NOW() 설정                              │  │ │
│ │    │   (다음 요청 시 인터셉터에서 401 반환됨)                             │  │ │
│ │    └─────────────────────────────────────────────────────────────────┘  │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Response                                                                    │
│ HTTP 200 OK                                                                 │
│ { "status": 200, "data": null }                                             │
└─────────────────────────────────────────────────────────────────────────────┘

                                    ┌─────────────────────────────────────────┐
차단된 관리자의 다음 요청 ─────────────▶ │ AdminAuthInterceptor                    │
                                    │ isSessionActiveInDb() → false          │
                                    │ → 401 Unauthorized                     │
                                    └─────────────────────────────────────────┘
```

---

### 4. 사용자 관리 API (`GET /api/admin/users`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           사용자 목록 조회 흐름                                │
└─────────────────────────────────────────────────────────────────────────────┘

Client (ADMIN 이상)
  │
  │ GET /api/admin/users?page=0&size=15
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthInterceptor.preHandle()                                            │
│ • 세션 검증 통과 (ADMIN 이상이면 접근 가능)                                     │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminUserApiController.getUsers(@RequestParam page, size)                   │
│ • 기본값: page=0, size=15                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminUserService.getAllUsers(page, size)                                    │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ Pageable pageable = PageRequest.of(                                     │ │
│ │   page, size,                                                           │ │
│ │   Sort.by(Direction.DESC, "createdAt")                                  │ │
│ │ )                                                                       │ │
│ │                                                                         │ │
│ │ UserRepository.findAll(pageable)                                        │ │
│ │ └─ @SQLRestriction("deleted_at IS NULL") 자동 적용                       │ │
│ │                                                                         │ │
│ │ Page<User> → Page<AdminUserListResponse> 변환                            │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Response                                                                    │
│ HTTP 200 OK                                                                 │
│ {                                                                           │
│   "status": 200,                                                            │
│   "data": {                                                                 │
│     "content": [                                                            │
│       { "id", "email", "nickname", "providerType", "createdAt" }, ...       │
│     ],                                                                      │
│     "totalElements": 100,                                                   │
│     "totalPages": 7,                                                        │
│     "number": 0,                                                            │
│     "size": 15                                                              │
│   }                                                                         │
│ }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 5. 세션 히스토리 조회 API (`GET /api/admin/sessions/history`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         세션 히스토리 조회 흐름                                │
└─────────────────────────────────────────────────────────────────────────────┘

Client (SUPER_ADMIN)
  │
  │ GET /api/admin/sessions/history?adminId=1&startDate=2025-01-01&endDate=2025-01-31
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAuthInterceptor.preHandle()                                            │
│ • 세션 검증 통과                                                              │
│ • SUPER_ADMIN 권한 확인 통과                                                  │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminSessionApiController.getSessionHistory(adminId?, startDate?, endDate?) │
│ • 모든 파라미터 선택사항 (AND 조건으로 조합)                                    │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminSessionService.getSessionHistory(adminId, startDate, endDate, pageable)│
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 조건별 분기:                                                             │ │
│ │ • 모두 null → findAllSessionHistory(pageable)                           │ │
│ │ • adminId만 → findSessionHistoryByAdminId(adminId, pageable)            │ │
│ │ • 기간만 → findSessionHistoryByDateRange(start, end, pageable)          │ │
│ │ • 둘 다 → findSessionHistoryByAdminIdAndDateRange(...)                  │ │
│ │                                                                         │ │
│ │ N+1 최적화:                                                              │ │
│ │ ┌─────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 1. 세션 목록에서 adminId 추출                                         │ │ │
│ │ │    Set<Long> adminIds = sessions.map(s -> s.getAdminId())           │ │ │
│ │ │                                                                     │ │ │
│ │ │ 2. 배치로 Admin 조회 (단일 쿼리)                                       │ │ │
│ │ │    List<Admin> admins = AdminRepository.findAllByIdIn(adminIds)     │ │ │
│ │ │                                                                     │ │ │
│ │ │ 3. Map으로 변환하여 메모리에서 조회                                     │ │ │
│ │ │    Map<Long, Admin> adminMap = admins.toMap(a -> a.getId(), a)      │ │ │
│ │ │                                                                     │ │ │
│ │ │ 4. 세션마다 Admin 정보 조합                                           │ │ │
│ │ │    • adminMap.get(session.getAdminId())                             │ │ │
│ │ │    • 없으면 (삭제된 관리자) → "삭제된 관리자" 표시                       │ │ │
│ │ └─────────────────────────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Response                                                                    │
│ HTTP 200 OK                                                                 │
│ {                                                                           │
│   "status": 200,                                                            │
│   "data": {                                                                 │
│     "content": [                                                            │
│       {                                                                     │
│         "id", "adminId", "adminName", "adminEmail",                         │
│         "sessionId", "loginTime", "logoutTime",                             │
│         "ipAddress", "userAgent"                                            │
│       }, ...                                                                │
│     ],                                                                      │
│     "totalElements": 500,                                                   │
│     "totalPages": 34                                                        │
│   }                                                                         │
│ }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 6. 관리자 생성 API (`POST /api/admin/accounts`)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              관리자 생성 흐름                                  │
└─────────────────────────────────────────────────────────────────────────────┘

Client (SUPER_ADMIN)
  │
  │ POST /api/admin/accounts
  │ Body: { "email": "new@admin.com", "name": "홍길동", "password": "Pass1234!" }
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ @Valid AdminCreateRequest 검증                                              │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ email: @NotBlank @Pattern(이메일 정규식)                                  │ │
│ │ name: @NotBlank @Size(min=2, max=15) @Pattern(한글/영문 정규식)            │ │
│ │ password: @NotBlank @Size(min=8, max=16)                                │ │
│ │           @Pattern(소문자 + 숫자 + 특수문자 포함)                           │ │
│ │                                                                         │ │
│ │ 검증 실패 → 400 Bad Request + MethodArgumentNotValidException           │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminAccountApiController.createAdmin(request)                              │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ AdminService.createAdmin(email, name, password)                             │
│ ┌─────────────────────────────────────────────────────────────────────────┐ │
│ │ 1. 이메일 중복 확인                                                       │ │
│ │    AdminRepository.existsByEmail(email)                                 │ │
│ │    └─ 존재 → AdminException(DUPLICATE_EMAIL)                            │ │
│ │                                                                         │ │
│ │ 2. 비밀번호 암호화                                                        │ │
│ │    BCrypt.hashpw(password, BCrypt.gensalt())                            │ │
│ │                                                                         │ │
│ │ 3. Admin 엔티티 생성 (기본 역할: ADMIN)                                    │ │
│ │    Admin.create(email, encodedPassword, name)                           │ │
│ │    └─ 내부에서 도메인 검증 (Admin.validate())                             │ │
│ │                                                                         │ │
│ │ 4. 저장                                                                  │ │
│ │    AdminRepository.save(admin)                                          │ │
│ └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
  │
  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ Response                                                                    │
│ HTTP 201 CREATED                                                            │
│ {                                                                           │
│   "status": 201,                                                            │
│   "data": { "id", "email", "name", "role": "ADMIN", "createdAt" }           │
│ }                                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Admin 모듈 아키텍처                                  │
└─────────────────────────────────────────────────────────────────────────────┘

                              ┌───────────────────┐
                              │     Client        │
                              │  (React/Next.js)  │
                              └─────────┬─────────┘
                                        │
                                        │ REST API (JSON)
                                        ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                                Spring Boot                                     │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                      AdminAuthInterceptor                                │  │
│  │  • 세션 검증 (HTTP + DB)                                                 │  │
│  │  • RBAC (ADMIN / SUPER_ADMIN)                                           │  │
│  │  • lastAccessTime 갱신                                                   │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│                                     │                                          │
│                                     ▼                                          │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                         Presentation Layer                               │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │  │
│  │  │ AuthApi     │ │ UserApi     │ │ AccountApi  │ │ SessionApi  │       │  │
│  │  │ Controller  │ │ Controller  │ │ Controller  │ │ Controller  │       │  │
│  │  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘       │  │
│  └─────────┼───────────────┼───────────────┼───────────────┼───────────────┘  │
│            │               │               │               │                   │
│            ▼               ▼               ▼               ▼                   │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │                          Service Layer                                   │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│  │
│  │  │ Admin       │ │ AdminUser   │ │ AdminSession│ │ AdminManagement     ││  │
│  │  │ Service     │ │ Service     │ │ Service     │ │ ApplicationService  ││  │
│  │  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────────┬──────────┘│  │
│  └─────────┼───────────────┼───────────────┼───────────────────┼───────────┘  │
│            │               │               │                   │               │
│            ▼               ▼               ▼                   │               │
│  ┌─────────────────────────────────────────────────────────────┼───────────┐  │
│  │                       Repository Layer                       │           │  │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐            │           │  │
│  │  │ Admin       │ │ User        │ │ AdminSession│◀───────────┘           │  │
│  │  │ Repository  │ │ Repository  │ │ Repository  │                        │  │
│  │  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘                        │  │
│  └─────────┼───────────────┼───────────────┼───────────────────────────────┘  │
│            │               │               │                                   │
└────────────┼───────────────┼───────────────┼───────────────────────────────────┘
             │               │               │
             ▼               ▼               ▼
     ┌───────────────────────────────────────────────────┐
     │                      MySQL                         │
     │  ┌───────────┐ ┌───────────┐ ┌──────────────────┐ │
     │  │  admins   │ │   users   │ │  admin_sessions  │ │
     │  │ (Soft Del)│ │ (Soft Del)│ │  (감사 추적)      │ │
     │  └───────────┘ └───────────┘ └──────────────────┘ │
     │  ┌────────────────────────────────────────────┐   │
     │  │  SPRING_SESSION / SPRING_SESSION_ATTRIBUTES│   │
     │  │  (HTTP 세션 영속화)                          │   │
     │  └────────────────────────────────────────────┘   │
     └───────────────────────────────────────────────────┘
```

---

## 삭제된 파일 목록

### Thymeleaf 템플릿 (12개)
```
resources/templates/admin/
├── login.html
├── layout.html
├── accounts/new.html, list.html
├── users/list.html, edit.html
├── sessions/list.html, history.html, detail.html
└── error/error.html, 500.html, forbidden.html
```

### SSR 컨트롤러 (5개)
```
moment.admin.presentation/
├── AdminAuthController.java
├── AdminManagementController.java
├── AdminUserController.java
├── AdminSessionController.java
└── AdminErrorController.java
```

### SSR 테스트 (5개)
```
moment.admin.presentation/ (Test)
├── AdminAuthControllerTest.java
├── AdminManagementControllerTest.java
├── AdminUserControllerTest.java
├── AdminSessionControllerTest.java
└── AdminErrorControllerTest.java
```

---

## 의존성 변경

### 제거
- `spring-boot-starter-thymeleaf`
- `thymeleaf-layout-dialect:3.3.0`

### 추가
- `com.github.ben-manes.caffeine:caffeine` (캐싱)

---

## 주요 파일 경로

| 구분 | 경로 |
|------|------|
| REST API 컨트롤러 | `server/src/main/java/moment/admin/presentation/api/` |
| 서비스 | `server/src/main/java/moment/admin/service/` |
| 도메인 | `server/src/main/java/moment/admin/domain/` |
| 인터셉터 | `server/src/main/java/moment/admin/global/interceptor/` |
| 세션 매니저 | `server/src/main/java/moment/admin/global/util/AdminSessionManager.java` |
| DB 마이그레이션 | `server/src/main/resources/db/migration/mysql/V23, V24` |