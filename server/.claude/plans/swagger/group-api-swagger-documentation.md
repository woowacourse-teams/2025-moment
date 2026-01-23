# Swagger 문서화 구현 계획서

## 개요

Phase 4에서 추가된 그룹 기반 커뮤니티 기능의 **5개 컨트롤러, 26개 엔드포인트**에 대한 Swagger 문서화 계획입니다.

---

## 수정 대상 파일 요약

### 컨트롤러 (5개)
| 파일 | 엔드포인트 수 |
|------|-------------|
| `group/presentation/GroupController.java` | 5개 |
| `group/presentation/GroupInviteController.java` | 3개 |
| `group/presentation/GroupMemberController.java` | 8개 |
| `group/presentation/GroupMomentController.java` | 6개 |
| `group/presentation/GroupCommentController.java` | 4개 |

### Request DTO (4개)
- `group/dto/request/GroupCreateRequest.java`
- `group/dto/request/GroupUpdateRequest.java`
- `group/dto/request/GroupJoinRequest.java`
- `group/dto/request/ProfileUpdateRequest.java`

### Response DTO (6개)
- `group/dto/response/GroupCreateResponse.java`
- `group/dto/response/GroupDetailResponse.java`
- `group/dto/response/MyGroupResponse.java`
- `group/dto/response/MemberResponse.java`
- `group/dto/response/GroupJoinResponse.java`
- `group/dto/response/InviteInfoResponse.java`

### 추가 수정 (2개)
- `notification/dto/response/NotificationSseResponse.java` - groupId 필드 추가
- `notification/presentation/PushNotificationController.java` - DELETE 문서화

---

## Phase 1: Request DTO @Schema 추가

### 1.1 GroupCreateRequest.java
```java
@Schema(description = "그룹 생성 요청")
public record GroupCreateRequest(
    @Schema(description = "그룹 이름", example = "개발자 모임")
    @NotBlank(message = "그룹 이름은 필수입니다.")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다.")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다.")
    String description,

    @Schema(description = "소유자 닉네임", example = "홍길동")
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    String ownerNickname
) {}
```

### 1.2 GroupUpdateRequest.java
```java
@Schema(description = "그룹 수정 요청")
public record GroupUpdateRequest(
    @Schema(description = "그룹 이름", example = "수정된 그룹명")
    String name,

    @Schema(description = "그룹 설명", example = "수정된 설명입니다.")
    String description
) {}
```

### 1.3 GroupJoinRequest.java
```java
@Schema(description = "그룹 가입 요청")
public record GroupJoinRequest(
    @Schema(description = "초대 코드", example = "abc123xyz")
    String inviteCode,

    @Schema(description = "가입 닉네임", example = "새멤버")
    String nickname
) {}
```

### 1.4 ProfileUpdateRequest.java
```java
@Schema(description = "프로필 수정 요청")
public record ProfileUpdateRequest(
    @Schema(description = "변경할 닉네임", example = "새닉네임")
    String nickname
) {}
```

---

## Phase 2: Response DTO @Schema 추가

### 2.1 GroupCreateResponse.java
```java
@Schema(description = "그룹 생성 응답")
public record GroupCreateResponse(
    @Schema(description = "생성된 그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "소유자 닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "초대 코드", example = "abc123xyz")
    String inviteCode
) { ... }
```

### 2.2 MyGroupResponse.java
```java
@Schema(description = "내 그룹 응답")
public record MyGroupResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "내 멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "내 닉네임", example = "홍길동")
    String myNickname,

    @Schema(description = "소유자 여부", example = "true")
    boolean isOwner,

    @Schema(description = "멤버 수", example = "10")
    long memberCount
) { ... }
```

### 2.3 GroupDetailResponse.java
```java
@Schema(description = "그룹 상세 응답")
public record GroupDetailResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "내 멤버 ID", example = "1")
    Long myMemberId,

    @Schema(description = "내 닉네임", example = "홍길동")
    String myNickname,

    @Schema(description = "소유자 여부", example = "true")
    boolean isOwner,

    @Schema(description = "멤버 수", example = "10")
    long memberCount,

    @Schema(description = "멤버 목록")
    List<MemberResponse> members
) { ... }
```

### 2.4 MemberResponse.java
```java
@Schema(description = "멤버 응답")
public record MemberResponse(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "사용자 ID", example = "100")
    Long userId,

    @Schema(description = "닉네임", example = "홍길동")
    String nickname,

    @Schema(description = "역할", example = "OWNER")
    MemberRole role,

    @Schema(description = "상태", example = "APPROVED")
    MemberStatus status
) { ... }
```

### 2.5 InviteInfoResponse.java
```java
@Schema(description = "초대 정보 응답")
public record InviteInfoResponse(
    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "그룹 이름", example = "개발자 모임")
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    String description,

    @Schema(description = "멤버 수", example = "10")
    long memberCount
) { ... }
```

### 2.6 GroupJoinResponse.java
```java
@Schema(description = "그룹 가입 응답")
public record GroupJoinResponse(
    @Schema(description = "멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "그룹 ID", example = "1")
    Long groupId,

    @Schema(description = "닉네임", example = "새멤버")
    String nickname,

    @Schema(description = "상태", example = "PENDING")
    MemberStatus status
) { ... }
```

---

## Phase 3: 컨트롤러 Swagger 어노테이션 추가

### 3.1 GroupController.java

**@Tag 추가:**
```java
@Tag(name = "Group API", description = "그룹 관리 관련 API 명세")
```

**엔드포인트별 문서화:**

| 메서드 | 경로 | summary | 성공 | 에러 코드 |
|--------|------|---------|------|----------|
| POST | `/api/v2/groups` | 그룹 생성 | 201 | T-005, U-009 |
| GET | `/api/v2/groups` | 내 그룹 목록 조회 | 200 | T-005, U-009 |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 조회 | 200 | T-005, GR-001, GM-002 |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 정보 수정 | 204 | T-005, GR-001, GR-002 |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 | 204 | T-005, GR-001, GR-002, GR-003 |

### 3.2 GroupInviteController.java

**@Tag 추가:**
```java
@Tag(name = "Group Invite API", description = "그룹 초대 관련 API 명세")
```

**엔드포인트별 문서화:**

| 메서드 | 경로 | summary | 성공 | 에러 코드 |
|--------|------|---------|------|----------|
| POST | `/{groupId}/invite` | 초대 링크 생성 | 200 | T-005, GR-001, GR-002 |
| GET | `/invite/{code}` | 초대 정보 조회 | 200 | IL-001, IL-002 |
| POST | `/groups/join` | 그룹 가입 신청 | 201 | T-005, IL-001, IL-002, GM-003 |

### 3.3 GroupMemberController.java

**@Tag 추가:**
```java
@Tag(name = "Group Member API", description = "그룹 멤버 관리 관련 API 명세")
```

**엔드포인트별 문서화:**

| 메서드 | 경로 | summary | 성공 | 에러 코드 |
|--------|------|---------|------|----------|
| GET | `/members` | 멤버 목록 조회 | 200 | T-005, GR-001, GM-002 |
| GET | `/pending` | 대기자 목록 조회 | 200 | T-005, GR-001, GR-002 |
| PATCH | `/profile` | 내 프로필 수정 | 204 | T-005, GR-001, GM-002, GM-008 |
| DELETE | `/leave` | 그룹 탈퇴 | 204 | T-005, GR-001, GM-002, GM-007 |
| DELETE | `/members/{memberId}` | 멤버 강퇴 | 204 | T-005, GR-001, GR-002, GM-001, GM-006 |
| POST | `/members/{memberId}/approve` | 멤버 승인 | 204 | T-005, GR-001, GR-002, GM-001, GM-004 |
| POST | `/members/{memberId}/reject` | 멤버 거절 | 204 | T-005, GR-001, GR-002, GM-001, GM-004 |
| POST | `/transfer/{memberId}` | 소유권 이전 | 204 | T-005, GR-001, GR-002, GM-001, GM-005 |

### 3.4 GroupMomentController.java

**@Tag 추가:**
```java
@Tag(name = "Group Moment API", description = "그룹 모멘트 관련 API 명세")
```

**엔드포인트별 문서화:**

| 메서드 | 경로 | summary | 성공 | 에러 코드 |
|--------|------|---------|------|----------|
| POST | `/moments` | 그룹 모멘트 작성 | 201 | T-005, GR-001, GM-002 |
| GET | `/moments` | 그룹 피드 조회 | 200 | T-005, GR-001, GM-002 |
| GET | `/moments/my` | 나의 모멘트 조회 | 200 | T-005, GR-001, GM-002 |
| GET | `/moments/{momentId}` | 모멘트 상세 조회 | 200 | T-005, GR-001, GM-002, M-002 |
| DELETE | `/moments/{momentId}` | 모멘트 삭제 | 204 | T-005, GR-001, GM-002, M-002 |
| POST | `/moments/{momentId}/like` | 모멘트 좋아요 토글 | 200 | T-005, GR-001, GM-002, M-002 |

### 3.5 GroupCommentController.java

**@Tag 추가:**
```java
@Tag(name = "Group Comment API", description = "그룹 코멘트 관련 API 명세")
```

**엔드포인트별 문서화:**

| 메서드 | 경로 | summary | 성공 | 에러 코드 |
|--------|------|---------|------|----------|
| POST | `/moments/{momentId}/comments` | 코멘트 작성 | 201 | T-005, GR-001, GM-002, M-002 |
| GET | `/moments/{momentId}/comments` | 코멘트 목록 조회 | 200 | T-005, GR-001, GM-002, M-002 |
| DELETE | `/comments/{commentId}` | 코멘트 삭제 | 204 | T-005, GR-001, GM-002, C-002 |
| POST | `/comments/{commentId}/like` | 코멘트 좋아요 토글 | 200 | T-005, GR-001, GM-002, C-002 |

---

## Phase 4: 추가 수정 사항

### 4.1 NotificationSseResponse에 groupId 추가

**파일:** `notification/dto/response/NotificationSseResponse.java`

```java
@Schema(description = "SSE 알림 응답")
public record NotificationSseResponse(
    // 기존 필드들...

    @Schema(description = "그룹 ID", example = "1")
    Long groupId,  // 추가

    // 나머지 필드들...
) {
    public static NotificationSseResponse createSseResponse(
            Long notificationId,
            NotificationType notificationType,
            TargetType targetType,
            Long targetId,
            Long groupId  // 파라미터 추가
    ) { ... }
}
```

**연관 수정:**
- `SseEmitterService.java` - createSseResponse 호출 부분 수정
- `NotificationFacadeService.java` - SSE 전송 시 groupId 전달

### 4.2 PushNotificationController DELETE 문서화

**파일:** `notification/presentation/PushNotificationController.java`

```java
@Operation(summary = "디바이스 정보 삭제", description = "푸시 알림을 위한 디바이스 정보를 삭제합니다.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "디바이스 정보 삭제 성공"),
    @ApiResponse(responseCode = "404", description = """
        - [U-009] 존재하지 않는 사용자입니다.
        """,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@DeleteMapping
public ResponseEntity<SuccessResponse<Void>> deleteDeviceEndpoint(...)
```

---

## 에러 코드 참조표

| 코드 | 메시지 | HTTP 상태 |
|------|--------|----------|
| T-005 | 토큰을 찾을 수 없습니다. | 401 |
| U-009 | 존재하지 않는 사용자입니다. | 404 |
| GR-001 | 존재하지 않는 그룹입니다. | 404 |
| GR-002 | 그룹 소유자가 아닙니다. | 403 |
| GR-003 | 멤버가 있는 그룹은 삭제할 수 없습니다. | 400 |
| GM-001 | 존재하지 않는 멤버입니다. | 404 |
| GM-002 | 그룹 멤버가 아닙니다. | 403 |
| GM-003 | 이미 그룹 멤버입니다. | 409 |
| GM-004 | 대기 중인 멤버가 아닙니다. | 400 |
| GM-005 | 승인된 멤버가 아닙니다. | 400 |
| GM-006 | 그룹 소유자는 강퇴할 수 없습니다. | 400 |
| GM-007 | 그룹 소유자는 탈퇴할 수 없습니다. | 400 |
| GM-008 | 이미 사용 중인 닉네임입니다. | 409 |
| IL-001 | 유효하지 않은 초대 링크입니다. | 404 |
| IL-002 | 만료된 초대 링크입니다. | 400 |
| M-002 | 존재하지 않는 모멘트입니다. | 404 |
| C-002 | 존재하지 않는 코멘트입니다. | 404 |

---

## 구현 순서

1. **Request DTO @Schema 추가** (4개 파일)
2. **Response DTO @Schema 추가** (6개 파일)
3. **GroupController 문서화**
4. **GroupInviteController 문서화**
5. **GroupMemberController 문서화**
6. **GroupMomentController 문서화**
7. **GroupCommentController 문서화**
8. **NotificationSseResponse groupId 추가**
9. **PushNotificationController DELETE 문서화**

---

## 검증 방법

```bash
# 1. 빌드 확인
./gradlew build

# 2. 테스트 실행
./gradlew fastTest

# 3. 애플리케이션 실행 후 Swagger UI 확인
# http://localhost:8080/swagger-ui/index.html
```

### Swagger UI 검증 체크리스트
- [ ] 5개 API 그룹이 올바르게 표시되는가
- [ ] 각 엔드포인트에 Operation 정보가 있는가
- [ ] Request/Response 스키마가 example과 함께 표시되는가
- [ ] 에러 응답이 올바르게 문서화되어 있는가
