# Phase 2: 인증 API (AdminAuthApiController)

## 목표
관리자 인증 관련 REST API 구현 (로그인, 로그아웃, 현재 관리자 정보 조회)

---

## 엔드포인트

| Method | 경로 | 설명 |
|--------|------|------|
| POST | /api/admin/auth/login | 관리자 로그인 |
| POST | /api/admin/auth/logout | 관리자 로그아웃 |
| GET | /api/admin/auth/me | 현재 로그인한 관리자 정보 |

---

## 작업 목록

### 2.1 AdminAuthApiController 생성

**파일**: `server/src/main/java/moment/admin/presentation/api/AdminAuthApiController.java`

**구현 내용**:
```java
package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminLoginResponse;
import moment.admin.dto.response.AdminMeResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.global.util.AdminSessionManager;
import moment.admin.global.util.ClientIpExtractor;
import moment.admin.service.admin.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth API", description = "관리자 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthApiController {

    private final AdminService adminService;
    private final AdminSessionManager sessionManager;

    @Operation(summary = "관리자 로그인")
    @PostMapping("/login")
    public ResponseEntity<AdminSuccessResponse<AdminLoginResponse>> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        Admin admin = adminService.authenticateAdmin(request.email(), request.password());

        // 세션 고정 공격 방지
        httpRequest.changeSessionId();

        String ipAddress = ClientIpExtractor.extract(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        sessionManager.registerSession(session, admin.getId(), admin.getRole(), ipAddress, userAgent);

        AdminLoginResponse response = AdminLoginResponse.from(admin);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "관리자 로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<AdminSuccessResponse<Void>> logout(HttpSession session) {
        sessionManager.invalidate(session);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "현재 관리자 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<AdminSuccessResponse<AdminMeResponse>> getCurrentAdmin(HttpSession session) {
        Long adminId = sessionManager.getId(session);
        Admin admin = adminService.getAdminById(adminId);

        AdminMeResponse response = AdminMeResponse.from(admin);
        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }
}
```

---

### 2.2 AdminService 메서드 확인/추가

**파일**: `server/src/main/java/moment/admin/service/admin/AdminService.java`

**필요 메서드 확인**:
- `authenticateAdmin(String email, String password)` - 이미 존재하면 그대로 사용
- `getAdminById(Long adminId)` - 없으면 추가

**추가할 메서드** (없는 경우):
```java
public Admin getAdminById(Long adminId) {
    return adminRepository.findById(adminId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND));
}
```

---

## 요청/응답 예시

### POST /api/admin/auth/login

**Request**:
```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "name": "관리자",
    "role": "SUPER_ADMIN"
  }
}
```

**Response (401 - 로그인 실패)**:
```json
{
  "code": "A-001",
  "message": "관리자 로그인에 실패했습니다.",
  "status": 401
}
```

---

### POST /api/admin/auth/logout

**Response (200)**:
```json
{
  "status": 200,
  "data": null
}
```

---

### GET /api/admin/auth/me

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "name": "관리자",
    "role": "SUPER_ADMIN",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

**Response (401 - 미인증)**:
```json
{
  "code": "A-003",
  "message": "관리자 권한이 없습니다.",
  "status": 401
}
```

---

## 검증 방법

1. 로그인 테스트
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}' \
  -c cookies.txt -v
```

2. 현재 관리자 정보 조회
```bash
curl -X GET http://localhost:8080/api/admin/auth/me \
  -b cookies.txt
```

3. 로그아웃
```bash
curl -X POST http://localhost:8080/api/admin/auth/logout \
  -b cookies.txt
```

---

## 완료 조건

- [ ] AdminAuthApiController.java 생성
- [ ] AdminService.getAdminById() 메서드 확인/추가
- [ ] 로그인 API 동작 확인
- [ ] 로그아웃 API 동작 확인
- [ ] /me API 동작 확인
- [ ] 미인증 시 401 응답 확인
