# Phase 3: 사용자 관리 API (AdminUserApiController)

## 목표
일반 사용자(User) 관리 REST API 구현 (목록 조회, 상세 조회, 수정, 삭제)

---

## 엔드포인트

| Method | 경로 | 설명 |
|--------|------|------|
| GET | /api/admin/users | 사용자 목록 조회 (페이징) |
| GET | /api/admin/users/{id} | 사용자 상세 조회 |
| PUT | /api/admin/users/{id} | 사용자 정보 수정 |
| DELETE | /api/admin/users/{id} | 사용자 삭제 (Soft Delete) |

---

## 작업 목록

### 3.1 AdminUserApiController 생성

**파일**: `server/src/main/java/moment/admin/presentation/api/AdminUserApiController.java`

**구현 내용**:
```java
package moment.admin.presentation.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.admin.dto.response.AdminSuccessResponse;
import moment.admin.dto.response.AdminUserDetailResponse;
import moment.admin.dto.response.AdminUserListResponse;
import moment.admin.service.user.AdminUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin User API", description = "관리자용 사용자 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserApiController {

    private final AdminUserService adminUserService;

    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    public ResponseEntity<AdminSuccessResponse<Page<AdminUserListResponse>>> getUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AdminUserListResponse> response = adminUserService.getUsers(keyword, pageable);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminUserDetailResponse>> getUser(
            @PathVariable Long id) {

        AdminUserDetailResponse response = adminUserService.getUserDetail(id);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 정보 수정")
    @PutMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<AdminUserDetailResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {

        AdminUserDetailResponse response = adminUserService.updateUser(id, request);

        HttpStatus status = HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, response));
    }

    @Operation(summary = "사용자 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<AdminSuccessResponse<Void>> deleteUser(
            @PathVariable Long id) {

        adminUserService.deleteUser(id);

        HttpStatus status = HttpStatus.NO_CONTENT;
        return ResponseEntity.status(status)
                .body(AdminSuccessResponse.of(status, null));
    }
}
```

---

### 3.2 응답 DTO 생성

#### AdminUserListResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminUserListResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.user.domain.User;
import moment.user.domain.ProviderType;
import moment.user.domain.UserLevel;

public record AdminUserListResponse(
    Long id,
    String email,
    String nickname,
    ProviderType providerType,
    UserLevel level,
    int expStar,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminUserListResponse from(User user) {
        return new AdminUserListResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProviderType(),
            user.getLevel(),
            user.getExpStar(),
            user.getCreatedAt(),
            user.getDeletedAt()
        );
    }
}
```

#### AdminUserDetailResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminUserDetailResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.user.domain.User;
import moment.user.domain.ProviderType;
import moment.user.domain.UserLevel;

public record AdminUserDetailResponse(
    Long id,
    String email,
    String nickname,
    String profileImageUrl,
    ProviderType providerType,
    UserLevel level,
    int expStar,
    int point,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminUserDetailResponse from(User user) {
        return new AdminUserDetailResponse(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getProfileImageUrl(),
            user.getProviderType(),
            user.getLevel(),
            user.getExpStar(),
            user.getPoint(),
            user.getCreatedAt(),
            user.getDeletedAt()
        );
    }
}
```

---

### 3.3 AdminUserService 메서드 확인/추가

**파일**: `server/src/main/java/moment/admin/service/user/AdminUserService.java`

기존 메서드를 확인하고, REST API용 반환 타입으로 수정 또는 추가:

```java
package moment.admin.service.user;

import lombok.RequiredArgsConstructor;
import moment.admin.dto.request.AdminUserUpdateRequest;
import moment.admin.dto.response.AdminUserDetailResponse;
import moment.admin.dto.response.AdminUserListResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserListResponse> getUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.findByNicknameContainingOrEmailContaining(keyword, keyword, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(AdminUserListResponse::from);
    }

    public AdminUserDetailResponse getUserDetail(Long id) {
        User user = findUserById(id);
        return AdminUserDetailResponse.from(user);
    }

    @Transactional
    public AdminUserDetailResponse updateUser(Long id, AdminUserUpdateRequest request) {
        User user = findUserById(id);
        user.updateNickname(request.nickname());
        // 필요한 추가 업데이트 로직
        return AdminUserDetailResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        user.delete(); // Soft Delete
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AdminException(AdminErrorCode.NOT_FOUND));
    }
}
```

---

## 요청/응답 예시

### GET /api/admin/users?page=0&size=15

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user1@example.com",
        "nickname": "사용자1",
        "providerType": "EMAIL",
        "level": "ASTEROID_WHITE",
        "expStar": 100,
        "createdAt": "2024-01-15T10:30:00",
        "deletedAt": null
      }
    ],
    "pageable": { ... },
    "totalElements": 100,
    "totalPages": 7,
    "size": 15,
    "number": 0
  }
}
```

### GET /api/admin/users/1

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "user1@example.com",
    "nickname": "사용자1",
    "profileImageUrl": "https://...",
    "providerType": "EMAIL",
    "level": "ASTEROID_WHITE",
    "expStar": 100,
    "point": 500,
    "createdAt": "2024-01-15T10:30:00",
    "deletedAt": null
  }
}
```

### PUT /api/admin/users/1

**Request**:
```json
{
  "nickname": "새닉네임"
}
```

**Response (200)**:
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "user1@example.com",
    "nickname": "새닉네임",
    ...
  }
}
```

### DELETE /api/admin/users/1

**Response (204)**:
```json
{
  "status": 204,
  "data": null
}
```

---

## 완료 조건

- [ ] AdminUserApiController.java 생성
- [ ] AdminUserListResponse.java 생성
- [ ] AdminUserDetailResponse.java 생성
- [ ] AdminUserService 메서드 확인/수정
- [ ] 목록 조회 API 동작 확인
- [ ] 상세 조회 API 동작 확인
- [ ] 수정 API 동작 확인
- [ ] 삭제 API 동작 확인
