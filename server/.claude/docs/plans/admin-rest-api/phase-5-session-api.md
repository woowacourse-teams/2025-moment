# Phase 5: 세션 관리 API (AdminSessionApiController)

## 목표
관리자 세션 관리 REST API 구현 (활성 세션 조회, 세션 상세, 강제 로그아웃, 히스토리)

---

## 엔드포인트

| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/sessions | 활성 세션 목록 조회 |
| GET | /api/admin/sessions/{sessionId} | 세션 상세 조회 |
| DELETE | /api/admin/sessions/{sessionId} | 특정 세션 강제 종료 |
| DELETE | /api/admin/sessions/admin/{adminId} | 관리자의 모든 세션 강제 종료 |
| GET | /api/admin/sessions/history | 세션 히스토리 조회 |

**권한**: SUPER_ADMIN만 접근 가능

---

## 작업 목록

### 5.1 AdminSessionApiController 생성

**파일**: `server/src/main/java/moment/admin/presentation/api/AdminSessionApiController.java`

**구현 내용**:
```java
package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.response.AdminSessionDetailResponse;
import moment.admin.dto.response.AdminSessionHistoryResponse;
import moment.admin.dto.response.AdminSessionListResponse;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.service.session.AdminSessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin Session API", description = "관리자 세션 관리 API (SUPER_ADMIN 전용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
public class AdminSessionApiController {

    private final AdminSessionService adminSessionService;

    @Operation(summary = "활성 세션 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<List<AdminSessionListResponse>>> getActiveSessions(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String adminName) {

        List<AdminSessionListResponse> response = adminSessionService.getActiveSessionsForApi(adminId, adminName);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "세션 상세 조회")
    @GetMapping("/{sessionId}")
    public ResponseEntity<AdminSuccessResponse<AdminSessionDetailResponse>> getSessionDetail(
            @PathVariable String sessionId) {

        AdminSessionDetailResponse response = adminSessionService.getSessionDetailForApi(sessionId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "특정 세션 강제 종료")
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateSession(
            @PathVariable String sessionId) {

        adminSessionService.invalidateSession(sessionId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "관리자의 모든 세션 강제 종료")
    @DeleteMapping("/admin/{adminId}")
    public ResponseEntity<AdminSuccessResponse<Void>> invalidateAllSessionsByAdmin(
            @PathVariable Long adminId) {

        adminSessionService.invalidateAllSessionsByAdminId(adminId);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }

    @Operation(summary = "세션 히스토리 조회")
    @GetMapping("/history")
    public ResponseEntity<AdminSuccessResponse<Page<AdminSessionHistoryResponse>>> getSessionHistory(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String adminName,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminSessionHistoryResponse> response = adminSessionService.getSessionHistoryForApi(adminId, adminName, pageable);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }
}
```

---

### 5.2 응답 DTO 생성/수정

#### AdminSessionListResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminSessionListResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.AdminSession;

public record AdminSessionListResponse(
    String sessionId,
    Long adminId,
    String adminName,
    String adminEmail,
    String ipAddress,
    String userAgent,
    LocalDateTime createdAt,
    LocalDateTime lastAccessedAt
) {
    public static AdminSessionListResponse from(AdminSession session) {
        return new AdminSessionListResponse(
            session.getSessionId(),
            session.getAdminId(),
            session.getAdminName(),
            session.getAdminEmail(),
            session.getIpAddress(),
            session.getUserAgent(),
            session.getCreatedAt(),
            session.getLastAccessedAt()
        );
    }
}
```

#### AdminSessionDetailResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminSessionDetailResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;

public record AdminSessionDetailResponse(
    String sessionId,
    Long adminId,
    String adminName,
    String adminEmail,
    AdminRole adminRole,
    String ipAddress,
    String userAgent,
    String browser,
    String os,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime lastAccessedAt,
    LocalDateTime expiredAt
) {
    public static AdminSessionDetailResponse from(AdminSession session, String browser, String os) {
        return new AdminSessionDetailResponse(
            session.getSessionId(),
            session.getAdminId(),
            session.getAdminName(),
            session.getAdminEmail(),
            session.getAdminRole(),
            session.getIpAddress(),
            session.getUserAgent(),
            browser,
            os,
            session.isActive(),
            session.getCreatedAt(),
            session.getLastAccessedAt(),
            session.getExpiredAt()
        );
    }
}
```

#### AdminSessionHistoryResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminSessionHistoryResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.AdminSession;

public record AdminSessionHistoryResponse(
    String sessionId,
    Long adminId,
    String adminName,
    String adminEmail,
    String ipAddress,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime expiredAt
) {
    public static AdminSessionHistoryResponse from(AdminSession session) {
        return new AdminSessionHistoryResponse(
            session.getSessionId(),
            session.getAdminId(),
            session.getAdminName(),
            session.getAdminEmail(),
            session.getIpAddress(),
            session.isActive(),
            session.getCreatedAt(),
            session.getExpiredAt()
        );
    }
}
```

---

### 5.3 AdminSessionService 메서드 추가

**파일**: `server/src/main/java/moment/admin/service/session/AdminSessionService.java`

**추가 메서드**:
```java
public List<AdminSessionListResponse> getActiveSessionsForApi(Long adminId, String adminName) {
    List<AdminSession> sessions;

    if (adminId != null) {
        sessions = adminSessionRepository.findActiveSessionsByAdminId(adminId);
    } else if (adminName != null && !adminName.isBlank()) {
        sessions = adminSessionRepository.findActiveSessionsByAdminNameContaining(adminName);
    } else {
        sessions = adminSessionRepository.findAllActiveSessions();
    }

    return sessions.stream()
            .map(AdminSessionListResponse::from)
            .toList();
}

public AdminSessionDetailResponse getSessionDetailForApi(String sessionId) {
    AdminSession session = adminSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.SESSION_NOT_FOUND));

    String browser = UserAgentParser.parseBrowser(session.getUserAgent());
    String os = UserAgentParser.parseOs(session.getUserAgent());

    return AdminSessionDetailResponse.from(session, browser, os);
}

@Transactional
public void invalidateSession(String sessionId) {
    AdminSession session = adminSessionRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new AdminException(AdminErrorCode.SESSION_NOT_FOUND));

    session.invalidate();
}

@Transactional
public void invalidateAllSessionsByAdminId(Long adminId) {
    List<AdminSession> sessions = adminSessionRepository.findActiveSessionsByAdminId(adminId);
    sessions.forEach(AdminSession::invalidate);
}

public Page<AdminSessionHistoryResponse> getSessionHistoryForApi(Long adminId, String adminName, Pageable pageable) {
    Page<AdminSession> sessions;

    if (adminId != null) {
        sessions = adminSessionRepository.findByAdminId(adminId, pageable);
    } else if (adminName != null && !adminName.isBlank()) {
        sessions = adminSessionRepository.findByAdminNameContaining(adminName, pageable);
    } else {
        sessions = adminSessionRepository.findAll(pageable);
    }

    return sessions.map(AdminSessionHistoryResponse::from);
}
```

---

## 요청/응답 예시

### GET /api/admin/sessions

**Response (200)**:
```json
{
  "status": 200,
  "data": [
    {
      "sessionId": "abc123...",
      "adminId": 1,
      "adminName": "슈퍼관리자",
      "adminEmail": "super@example.com",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2024-01-20T10:00:00",
      "lastAccessedAt": "2024-01-20T15:30:00"
    }
  ]
}
```

### GET /api/admin/sessions/{sessionId}

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "sessionId": "abc123...",
    "adminId": 1,
    "adminName": "슈퍼관리자",
    "adminEmail": "super@example.com",
    "adminRole": "SUPER_ADMIN",
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "browser": "Chrome 120",
    "os": "Windows 11",
    "isActive": true,
    "createdAt": "2024-01-20T10:00:00",
    "lastAccessedAt": "2024-01-20T15:30:00",
    "expiredAt": null
  }
}
```

### DELETE /api/admin/sessions/{sessionId}

**Response (200)**:
```json
{
  "status": 200,
  "data": null
}
```

### DELETE /api/admin/sessions/admin/2

**Response (200)**:
```json
{
  "status": 200,
  "data": null
}
```

### GET /api/admin/sessions/history

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "sessionId": "abc123...",
        "adminId": 1,
        "adminName": "슈퍼관리자",
        "adminEmail": "super@example.com",
        "ipAddress": "192.168.1.1",
        "isActive": false,
        "createdAt": "2024-01-15T10:00:00",
        "expiredAt": "2024-01-15T18:00:00"
      }
    ],
    "totalElements": 50,
    "totalPages": 4
  }
}
```

---

## 완료 조건

- [ ] AdminSessionApiController.java 생성
- [ ] AdminSessionListResponse.java 생성/수정
- [ ] AdminSessionDetailResponse.java 생성/수정
- [ ] AdminSessionHistoryResponse.java 생성/수정
- [ ] AdminSessionService 메서드 추가
- [ ] 활성 세션 목록 API 동작 확인
- [ ] 세션 상세 조회 API 동작 확인
- [ ] 세션 강제 종료 API 동작 확인
- [ ] 관리자별 전체 세션 종료 API 동작 확인
- [ ] 세션 히스토리 API 동작 확인
