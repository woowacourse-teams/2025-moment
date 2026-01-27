# Admin Group API Swagger 문서화 계획

## 개요

AdminGroupApiController에 Swagger 어노테이션을 추가하여 API 문서를 완성합니다.

- **대상**: `AdminGroupApiController` (18개 엔드포인트)
- **패턴**: 상세 문서화 (`@Tag` + `@Operation` + `@ApiResponses` + DTO `@Schema`)
- **참조**: 일반 API 패턴 (GroupController, AuthController 등)

---

## 수정 대상 파일

### 1. 컨트롤러 (1개)
- `src/main/java/moment/admin/presentation/api/AdminGroupApiController.java`

### 2. Request DTO (1개)
- `src/main/java/moment/admin/dto/request/AdminGroupUpdateRequest.java`

### 3. Response DTO (17개)
| 파일명 | 설명 |
|--------|------|
| `AdminGroupStatsResponse.java` | 그룹 통계 |
| `AdminGroupListResponse.java` | 그룹 목록 |
| `AdminGroupSummary.java` | 그룹 요약 |
| `AdminGroupDetailResponse.java` | 그룹 상세 |
| `AdminGroupOwnerInfo.java` | 소유자 정보 |
| `AdminInviteLinkInfo.java` | 초대 링크 정보 |
| `AdminGroupMemberListResponse.java` | 멤버 목록 |
| `AdminGroupMemberResponse.java` | 멤버 정보 |
| `AdminMemberUserInfo.java` | 멤버의 사용자 정보 |
| `AdminGroupInviteLinkResponse.java` | 초대 링크 상세 |
| `AdminGroupLogListResponse.java` | 로그 목록 |
| `AdminGroupLogResponse.java` | 로그 정보 |
| `AdminMomentListResponse.java` | 모멘트 목록 |
| `AdminMomentResponse.java` | 모멘트 정보 |
| `AdminMomentAuthorInfo.java` | 모멘트 작성자 |
| `AdminCommentListResponse.java` | 댓글 목록 |
| `AdminCommentResponse.java` | 댓글 정보 |
| `AdminCommentAuthorInfo.java` | 댓글 작성자 |

---

## 구현 상세

### Phase 1: 컨트롤러 어노테이션

#### 클래스 레벨
```java
@Tag(name = "Admin Group API", description = "관리자용 그룹 관리 API")
```

#### Import 추가
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import moment.admin.dto.response.AdminErrorResponse;
```

#### 엔드포인트별 어노테이션

| # | 메서드 | 경로 | summary |
|---|--------|------|---------|
| 1 | GET | `/stats` | 그룹 통계 조회 |
| 2 | GET | `/` | 그룹 목록 조회 |
| 3 | GET | `/{groupId}` | 그룹 상세 조회 |
| 4 | GET | `/{groupId}/members` | 승인된 멤버 목록 조회 |
| 5 | GET | `/{groupId}/pending-members` | 대기 중인 멤버 목록 조회 |
| 6 | PUT | `/{groupId}` | 그룹 정보 수정 |
| 7 | DELETE | `/{groupId}` | 그룹 삭제 |
| 8 | POST | `/{groupId}/restore` | 삭제된 그룹 복구 |
| 9 | POST | `/{groupId}/members/{memberId}/approve` | 멤버 승인 |
| 10 | POST | `/{groupId}/members/{memberId}/reject` | 멤버 거절 |
| 11 | DELETE | `/{groupId}/members/{memberId}` | 멤버 강제 퇴장 |
| 12 | POST | `/{groupId}/transfer-ownership/{newOwnerMemberId}` | 방장 권한 이전 |
| 13 | GET | `/{groupId}/invite-link` | 초대 링크 조회 |
| 14 | GET | `/logs` | Admin 로그 조회 |
| 15 | GET | `/{groupId}/moments` | 그룹 내 모멘트 목록 조회 |
| 16 | DELETE | `/{groupId}/moments/{momentId}` | 모멘트 삭제 |
| 17 | GET | `/{groupId}/moments/{momentId}/comments` | 모멘트 내 댓글 목록 조회 |
| 18 | DELETE | `/{groupId}/comments/{commentId}` | 댓글 삭제 |

---

### Phase 2: 에러 코드 매핑

#### 공통 에러 (모든 엔드포인트)
| HTTP | 에러 코드 |
|------|----------|
| 401 | `[A-009] 세션이 만료되었습니다.` |
| 403 | `[A-003] 관리자 권한이 없습니다.` |
| 404 | `[A-008] 세션을 찾을 수 없습니다.` |

#### 그룹 관련 (AG-)
| HTTP | 에러 코드 | 해당 엔드포인트 |
|------|----------|----------------|
| 404 | `[AG-001] 그룹을 찾을 수 없습니다.` | groupId 사용하는 모든 API |
| 400 | `[AG-002] 삭제되지 않은 그룹은 복원할 수 없습니다.` | restore |
| 400 | `[AG-003] 이미 삭제된 그룹입니다.` | update, delete |

#### 멤버 관련 (AM-)
| HTTP | 에러 코드 | 해당 엔드포인트 |
|------|----------|----------------|
| 404 | `[AM-001] 멤버를 찾을 수 없습니다.` | memberId 사용하는 API |
| 400 | `[AM-002] 그룹장은 추방할 수 없습니다.` | kick |
| 400 | `[AM-003] 승인 대기 중인 멤버가 아닙니다.` | approve, reject |
| 400 | `[AM-004] 승인된 멤버만 그룹장이 될 수 있습니다.` | transfer, kick |
| 400 | `[AM-005] 이미 그룹장인 멤버입니다.` | transfer |
| 400 | `[AM-006] 이미 승인된 멤버입니다.` | approve |

#### 콘텐츠 관련 (AC-)
| HTTP | 에러 코드 | 해당 엔드포인트 |
|------|----------|----------------|
| 404 | `[AC-001] 모멘트를 찾을 수 없습니다.` | moments API |
| 404 | `[AC-002] 코멘트를 찾을 수 없습니다.` | comments API |
| 400 | `[AC-003] 이미 삭제된 모멘트입니다.` | delete moment |
| 400 | `[AC-004] 이미 삭제된 코멘트입니다.` | delete comment |

---

### Phase 3: DTO @Schema 패턴

#### Request DTO 예시
```java
@Schema(description = "그룹 정보 수정 요청")
public record AdminGroupUpdateRequest(
    @Schema(description = "그룹명", example = "개발자 모임")
    @NotBlank
    String name,

    @Schema(description = "그룹 설명", example = "함께 성장하는 개발자 커뮤니티입니다.")
    @NotBlank
    String description
) {}
```

#### Response DTO 예시
```java
@Schema(description = "그룹 통계 응답")
public record AdminGroupStatsResponse(
    @Schema(description = "전체 그룹 수", example = "150")
    long totalGroups,

    @Schema(description = "활성 그룹 수", example = "120")
    long activeGroups,
    // ...
) {}
```

---

## 작업 순서

### Step 1: 컨트롤러
1. Import 문 추가
2. 클래스 레벨 `@Tag` 추가
3. 각 메서드에 `@Operation` + `@ApiResponses` 추가 (18개)

### Step 2: Request DTO
1. `AdminGroupUpdateRequest`에 `@Schema` 추가

### Step 3: Response DTO
1. 17개 Response DTO에 클래스/필드 `@Schema` 추가

### Step 4: 검증
1. `./gradlew build` 성공 확인
2. 서버 실행 후 Swagger UI 확인 (http://localhost:8080/swagger-ui.html)

---

## 검증 방법

```bash
# 1. 빌드 테스트
./gradlew build

# 2. 빠른 테스트 (e2e 제외)
./gradlew fastTest

# 3. 서버 실행 후 Swagger UI 확인
./gradlew bootRun
# 브라우저에서 http://localhost:8080/swagger-ui.html 접속
# "Admin Group API" 섹션 확인
```