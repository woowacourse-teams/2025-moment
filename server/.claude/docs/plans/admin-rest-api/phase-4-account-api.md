# Phase 4: 관리자 계정 API (AdminAccountApiController)

## 목표
관리자 계정 관리 REST API 구현 (목록 조회, 생성, 차단/해제)

---

## 엔드포인트

| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/accounts | 관리자 목록 조회 (페이징) |
| POST | /api/admin/accounts | 관리자 생성 |
| POST | /api/admin/accounts/{id}/block | 관리자 차단 |
| POST | /api/admin/accounts/{id}/unblock | 관리자 차단 해제 |

**권한**: SUPER_ADMIN만 접근 가능

---

## 작업 목록

### 4.1 AdminAccountApiController 생성

**파일**: `server/src/main/java/moment/admin/presentation/api/AdminAccountApiController.java`

**구현 내용**:
```java
package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.response.AdminAccountListResponse;
import moment.admin.dto.response.AdminAccountResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.service.admin.AdminService;
import moment.admin.service.application.AdminManagementApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Account API", description = "관리자 계정 관리 API (SUPER_ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/accounts")
public class AdminAccountApiController {

    private final AdminService adminService;
    private final AdminManagementApplicationService adminManagementApplicationService;
    private final AdminSessionManager sessionManager;

    @Operation(summary = "관리자 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<Page<AdminAccountListResponse>>> getAccounts(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminAccountListResponse> response = adminService.getAllAdminsForApi(pageable);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 생성")
    @PostMapping
    public ResponseEntity<AdminSuccessResponse<AdminAccountResponse>> createAccount(
            @Valid @RequestBody AdminCreateRequest request) {

        AdminAccountResponse response = adminService.createAdminForApi(request);

        HttpStatus status = HttpStatus.CREATED;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 차단")
    @PostMapping("/{id}/block")
    public ResponseEntity<AdminSuccessResponse<Void>> blockAccount(
            @PathVariable Long id,
            HttpSession session) {

        Long currentAdminId = sessionManager.getId(session);
        adminManagementApplicationService.blockAdminAndInvalidateSessions(currentAdminId, id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "관리자 차단 해제")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<AdminSuccessResponse<Void>> unblockAccount(
            @PathVariable Long id) {

        adminService.unblockAdmin(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }
}
```

---

### 4.2 응답 DTO 생성

#### AdminAccountListResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminAccountListResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminAccountListResponse(
    Long id,
    String email,
    String name,
    AdminRole role,
    boolean isBlocked,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminAccountListResponse from(Admin admin) {
        return new AdminAccountListResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.isBlocked(),
            admin.getCreatedAt(),
            admin.getDeletedAt()
        );
    }
}
```

#### AdminAccountResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminAccountResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminAccountResponse(
    Long id,
    String email,
    String name,
    AdminRole role,
    LocalDateTime createdAt
) {
    public static AdminAccountResponse from(Admin admin) {
        return new AdminAccountResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.getCreatedAt()
        );
    }
}
```

---

### 4.3 AdminService 메서드 추가

**파일**: `server/src/main/java/moment/admin/service/admin/AdminService.java`

**추가 메서드**:
```java
public Page<AdminAccountListResponse> getAllAdminsForApi(Pageable pageable) {
    Page<Admin> admins = adminRepository.findAllIncludingBlocked(pageable);
    return admins.map(AdminAccountListResponse::from);
}

@Transactional
public AdminAccountResponse createAdminForApi(AdminCreateRequest request) {
    // 이메일 중복 확인
    if (adminRepository.existsByEmail(request.email())) {
        throw new AdminException(AdminErrorCode.DUPLICATE_EMAIL);
    }

    Admin admin = Admin.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .name(request.name())
            .role(AdminRole.ADMIN)
            .build();

    Admin savedAdmin = adminRepository.save(admin);
    return AdminAccountResponse.from(savedAdmin);
}

@Transactional
public void unblockAdmin(Long adminId) {
    Admin admin = findAdminById(adminId);
    admin.unblock();
}
```

---

### 4.4 인터셉터 SUPER_ADMIN 권한 체크

**파일**: `AdminAuthInterceptor.java`

`/api/admin/accounts/**` 경로가 SUPER_ADMIN_ONLY_PATHS에 포함되어 있는지 확인:

```java
private static final List<String> SUPER_ADMIN_ONLY_PATHS = List.of(
        "/admin/accounts/new",
        "/admin/accounts",
        "/admin/sessions",
        "/admin/sessions/history",
        "/api/admin/accounts",      // 추가
        "/api/admin/sessions"       // 추가
);
```

---

## 요청/응답 예시

### GET /api/admin/accounts

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "super@example.com",
        "name": "슈퍼관리자",
        "role": "SUPER_ADMIN",
        "isBlocked": false,
        "createdAt": "2024-01-01T00:00:00",
        "deletedAt": null
      },
      {
        "id": 2,
        "email": "admin@example.com",
        "name": "일반관리자",
        "role": "ADMIN",
        "isBlocked": true,
        "createdAt": "2024-01-15T10:30:00",
        "deletedAt": null
      }
    ],
    "totalElements": 2,
    "totalPages": 1
  }
}
```

### POST /api/admin/accounts

**Request**:
```json
{
  "email": "newadmin@example.com",
  "password": "password123",
  "name": "새관리자"
}
```

**Response (201)**:
```json
{
  "status": 201,
  "data": {
    "id": 3,
    "email": "newadmin@example.com",
    "name": "새관리자",
    "role": "ADMIN",
    "createdAt": "2024-01-20T15:00:00"
  }
}
```

### POST /api/admin/accounts/2/block

**Response (200)**:
```json
{
  "status": 200,
  "data": null
}
```

**Response (400 - 자기 자신 차단 시도)**:
```json
{
  "code": "A-006",
  "message": "자기 자신을 차단할 수 없습니다.",
  "status": 400
}
```

### POST /api/admin/accounts/2/unblock

**Response (200)**:
```json
{
  "status": 200,
  "data": null
}
```

---

## 완료 조건

- [ ] AdminAccountApiController.java 생성
- [ ] AdminAccountListResponse.java 생성
- [ ] AdminAccountResponse.java 생성
- [ ] AdminService 메서드 추가
- [ ] 인터셉터 SUPER_ADMIN 권한 체크 경로 추가
- [ ] 목록 조회 API 동작 확인
- [ ] 생성 API 동작 확인
- [ ] 차단 API 동작 확인
- [ ] 차단 해제 API 동작 확인
- [ ] SUPER_ADMIN 외 접근 시 403 응답 확인
