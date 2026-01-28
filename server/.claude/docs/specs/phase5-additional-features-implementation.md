# Phase 5: 부가 기능 구현 문서

## 개요
초대 링크 조회 및 (선택) Admin 로그 조회 기능을 TDD 방식으로 구현합니다.

## 선행 작업
- Phase 1 ~ Phase 4 완료

## 대상 API

| # | 엔드포인트 | 설명 | 우선순위 |
|---|-----------|------|---------|
| 1 | `GET /api/admin/groups/{groupId}/invite-link` | 초대 링크 조회 | 필수 |
| 2 | `GET /api/admin/groups/logs` | Admin 로그 조회 | 선택 |

---

## TDD 테스트 목록

### 1. 초대 링크 조회 API (4개)

#### E2E 테스트 (`AdminInviteLinkApiTest`)
```
[ ] 초대링크_조회_성공_활성_초대링크_반환
[ ] 초대링크_조회_성공_fullUrl_생성_확인
[ ] 초대링크_조회_성공_만료여부_isExpired_정확성
[ ] 초대링크_조회_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] getInviteLink_활성_초대링크_반환
[ ] getInviteLink_fullUrl_도메인_포함
[ ] getInviteLink_만료된_링크_isExpired_true
[ ] getInviteLink_만료되지않은_링크_isExpired_false
[ ] getInviteLink_그룹없으면_예외
[ ] getInviteLink_초대링크없으면_null_반환
```

---

### 2. (선택) Admin 로그 조회 API (4개)

#### E2E 테스트 (`AdminGroupLogApiTest`)
```
[ ] Admin로그_조회_성공_그룹별_로그_반환
[ ] Admin로그_조회_성공_페이지네이션_적용
[ ] Admin로그_조회_성공_정렬_createdAt_DESC
[ ] Admin로그_조회_성공_로그타입_필터
```

#### 서비스 단위 테스트 (`AdminGroupLogServiceTest`)
```
[ ] getGroupLogs_그룹별_로그_반환
[ ] getGroupLogs_페이지네이션_적용
[ ] getGroupLogs_정렬_최신순
[ ] getGroupLogs_로그타입_필터_적용
[ ] getGroupLogs_그룹없어도_빈_리스트_반환
```

---

## 생성/수정 파일 목록

### 신규 생성

#### DTO
```
src/main/java/moment/admin/dto/response/
├── AdminGroupInviteLinkResponse.java
└── AdminGroupLogListResponse.java  (선택)
└── AdminGroupLogResponse.java      (선택)
```

#### Test
```
src/test/java/moment/admin/presentation/
├── AdminInviteLinkApiTest.java
└── AdminGroupLogApiTest.java  (선택)

src/test/java/moment/admin/service/
└── AdminGroupLogServiceTest.java  (선택)
```

### 수정

```
src/main/java/moment/admin/service/
├── AdminGroupService.java  (초대링크 조회 메서드 추가)
└── AdminGroupLogService.java  (로그 조회 메서드 추가, 선택)

src/main/java/moment/admin/presentation/
└── AdminGroupApiController.java  (엔드포인트 추가)

src/main/java/moment/admin/infrastructure/
└── AdminGroupLogRepository.java  (조회 메서드 추가, 선택)
```

---

## 시그니처 정의

### Response DTO

```java
// AdminGroupInviteLinkResponse.java
public record AdminGroupInviteLinkResponse(
    String code,
    String fullUrl,
    LocalDateTime expiresAt,
    boolean isActive,
    boolean isExpired,
    LocalDateTime createdAt
) {
    public static AdminGroupInviteLinkResponse from(InviteLink inviteLink, String baseUrl) {
        return new AdminGroupInviteLinkResponse(
            inviteLink.getCode(),
            baseUrl + "/invite/" + inviteLink.getCode(),
            inviteLink.getExpiresAt(),
            inviteLink.isActive(),
            inviteLink.isExpired(),
            inviteLink.getCreatedAt()
        );
    }
}

// AdminGroupLogListResponse.java (선택)
public record AdminGroupLogListResponse(
    List<AdminGroupLogResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

// AdminGroupLogResponse.java (선택)
public record AdminGroupLogResponse(
    Long id,
    Long adminId,
    String adminEmail,
    String type,
    Long groupId,
    Long targetId,
    String description,
    String beforeValue,
    String afterValue,
    LocalDateTime createdAt
) {
    public static AdminGroupLogResponse from(AdminGroupLog log) {
        return new AdminGroupLogResponse(
            log.getId(),
            log.getAdminId(),
            log.getAdminEmail(),
            log.getType().name(),
            log.getGroupId(),
            log.getTargetId(),
            log.getDescription(),
            log.getBeforeValue(),
            log.getAfterValue(),
            log.getCreatedAt()
        );
    }
}
```

### Service

```java
// AdminGroupService.java (추가)
public AdminGroupInviteLinkResponse getInviteLink(Long groupId);

// AdminGroupLogService.java (추가, 선택)
public AdminGroupLogListResponse getGroupLogs(
    Long groupId,
    AdminGroupLogType type,
    int page,
    int size
);
```

### Controller (추가 엔드포인트)

```java
// AdminGroupApiController.java (추가)

// 필수
@GetMapping("/{groupId}/invite-link")
public ResponseEntity<SuccessResponse<AdminGroupInviteLinkResponse>> getInviteLink(
    @PathVariable Long groupId
);

// 선택
@GetMapping("/logs")
public ResponseEntity<SuccessResponse<AdminGroupLogListResponse>> getGroupLogs(
    @RequestParam(required = false) Long groupId,
    @RequestParam(required = false) AdminGroupLogType type,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
);
```

### Repository 메서드 추가 (선택)

```java
// AdminGroupLogRepository.java (추가)
Page<AdminGroupLog> findByGroupId(Long groupId, Pageable pageable);

Page<AdminGroupLog> findByGroupIdAndType(Long groupId, AdminGroupLogType type, Pageable pageable);

Page<AdminGroupLog> findByType(AdminGroupLogType type, Pageable pageable);
```

---

## 구현 순서

### 필수: 초대 링크 조회
1. **DTO 생성** → `AdminGroupInviteLinkResponse`
2. **Service 확장** → `AdminGroupService.getInviteLink()`
3. **Controller 확장** → 초대 링크 엔드포인트
4. **테스트 작성 및 통과**

### 선택: Admin 로그 조회
1. **DTO 생성** → `AdminGroupLogListResponse`, `AdminGroupLogResponse`
2. **Repository 메서드 추가**
3. **Service 확장** → `AdminGroupLogService.getGroupLogs()`
4. **Controller 확장** → 로그 조회 엔드포인트
5. **테스트 작성 및 통과**

---

## 비즈니스 로직 상세

### 초대 링크 조회
```java
public AdminGroupInviteLinkResponse getInviteLink(Long groupId) {
    Group group = findGroupIncludingDeletedOrThrow(groupId);

    InviteLink inviteLink = inviteLinkRepository.findByGroupId(groupId)
        .orElse(null);

    if (inviteLink == null) {
        return null;
    }

    String baseUrl = applicationProperties.getBaseUrl();  // e.g., "https://moment.com"

    return AdminGroupInviteLinkResponse.from(inviteLink, baseUrl);
}
```

### (선택) Admin 로그 조회
```java
public AdminGroupLogListResponse getGroupLogs(
    Long groupId,
    AdminGroupLogType type,
    int page,
    int size
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<AdminGroupLog> logs;

    if (groupId != null && type != null) {
        logs = adminGroupLogRepository.findByGroupIdAndType(groupId, type, pageable);
    } else if (groupId != null) {
        logs = adminGroupLogRepository.findByGroupId(groupId, pageable);
    } else if (type != null) {
        logs = adminGroupLogRepository.findByType(type, pageable);
    } else {
        logs = adminGroupLogRepository.findAll(pageable);
    }

    List<AdminGroupLogResponse> content = logs.getContent().stream()
        .map(AdminGroupLogResponse::from)
        .toList();

    return new AdminGroupLogListResponse(
        content,
        logs.getNumber(),
        logs.getSize(),
        logs.getTotalElements(),
        logs.getTotalPages()
    );
}
```

---

## 초대 링크 응답 예시

```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "code": "abc123-def456",
    "fullUrl": "https://moment.com/invite/abc123-def456",
    "expiresAt": "2024-01-22T10:30:00",
    "isActive": true,
    "isExpired": false,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

## (선택) Admin 로그 응답 예시

```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "id": 1,
        "adminId": 100,
        "adminEmail": "admin@example.com",
        "type": "GROUP_UPDATE",
        "groupId": 1,
        "targetId": null,
        "description": "그룹 정보 수정",
        "beforeValue": "{\"name\":\"기존이름\",\"description\":\"기존설명\"}",
        "afterValue": "{\"name\":\"새이름\",\"description\":\"새설명\"}",
        "createdAt": "2024-01-20T15:30:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

## 설정 파일 참고

초대 링크의 `fullUrl` 생성을 위해 base URL 설정이 필요합니다:

```yaml
# application.yml
app:
  base-url: ${APP_BASE_URL:https://moment.com}
```

```java
// ApplicationProperties.java
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
    String baseUrl
) {}
```

---

## 테스트 총 개수: ~8개

| 카테고리 | E2E | 단위 | 합계 |
|---------|-----|-----|------|
| 초대 링크 조회 | 4 | 6 | 10 |
| (선택) Admin 로그 조회 | 4 | 5 | 9 |
| **총계** | **8** | **11** | **19** |

> 참고: Admin 로그 조회는 선택 기능이며, 실제 구현 시 테스트 케이스는 조정될 수 있습니다.

---

## 전체 Phase 요약

| Phase | 기능 | 테스트 수 |
|-------|------|----------|
| Phase 1 | 핵심 조회 기능 | ~55개 |
| Phase 2 | 그룹 관리 기능 | ~39개 |
| Phase 3 | 멤버 관리 기능 | ~49개 |
| Phase 4 | 콘텐츠 관리 기능 | ~39개 |
| Phase 5 | 부가 기능 | ~19개 |
| **총계** | | **~201개** |

---

## Phase 5 완료 후 체크리스트

- [ ] 모든 API 엔드포인트 구현 완료
- [ ] 모든 테스트 통과 (Green 상태)
- [ ] ErrorCode 전체 등록 완료
- [ ] Flyway 마이그레이션 적용 완료
- [ ] API 문서화 (Swagger/OpenAPI) 확인
- [ ] 코드 리뷰 완료
- [ ] 통합 테스트 수행
