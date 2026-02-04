# 그룹 API 이미지 기능 추가 구현 계획

## 개요
그룹 기반 Moment/Comment API에서 누락된 이미지 저장/조회/삭제 기능을 추가합니다.

## 현재 문제
- 그룹 모멘트/코멘트 생성 시 이미지 저장 로직 누락
- 그룹 피드 조회 시 이미지 URL 미포함
- 삭제 시 이미지 Soft Delete 미적용

## 결정 사항 (사용자 확인 완료)
- **이미지 개수**: 단일 이미지 (1:1) - 기존 @OneToOne 관계 유지
- **이미지 조회**: 배치 조회 + PhotoUrlResolver 최적화 적용
- **이미지 필수 여부**: 선택적 - 텍스트만 있는 모멘트/코멘트 허용
- **수정 기능**: 없음 - 생성/삭제만 지원
- **삭제 처리**: Soft Delete 동기화

---

## API 엔드포인트별 이미지 상태

| # | API 엔드포인트 | Response DTO | 이미지 상태 |
|---|---|---|---|
| 1 | `POST /api/v2/groups/{groupId}/moments` | `GroupMomentResponse` | ❌ 생성 누락 |
| 2 | `GET /api/v2/groups/{groupId}/moments` | `GroupMomentResponse` | ❌ 조회 누락 |
| 3 | `GET /api/v2/groups/{groupId}/my-moments` | `MyGroupMomentResponse` | ❌ 조회 누락 |
| 4 | `GET /api/v2/groups/{groupId}/my-moments/unread` | `MyGroupMomentResponse` | ❌ 조회 누락 |
| 5 | `POST /api/v2/groups/{groupId}/moments/{momentId}/comments` | `GroupCommentResponse` | ❌ 생성 누락 |
| 6 | `GET /api/v2/groups/{groupId}/moments/{momentId}/comments` | `GroupCommentResponse` | ❌ 조회 누락 |
| 7 | `GET /api/v2/groups/{groupId}/my-comments` | `MyGroupCommentResponse` | ✅ 정상 |
| 8 | `GET /api/v2/groups/{groupId}/my-comments/unread` | `MyGroupCommentResponse` | ✅ 정상 |
| 9 | `GET /api/v2/groups/{groupId}/moments/commentable` | `CommentableMomentResponse` | ✅ 정상 |
| 10 | `DELETE /api/v2/groups/{groupId}/moments/{momentId}` | - | ❌ 삭제 누락 |
| 11 | `DELETE /api/v2/groups/{groupId}/comments/{commentId}` | - | ❌ 삭제 누락 |

---

## Phase 1: Request DTO 수정

### 1.1 GroupMomentCreateRequest
**파일**: `src/main/java/moment/moment/dto/request/GroupMomentCreateRequest.java`

```java
public record GroupMomentCreateRequest(
    @NotBlank String content,
    String imageUrl,    // 추가
    String imageName    // 추가
) {}
```

### 1.2 GroupCommentCreateRequest
**파일**: `src/main/java/moment/comment/dto/request/GroupCommentCreateRequest.java`

```java
public record GroupCommentCreateRequest(
    @NotBlank String content,
    String imageUrl,    // 추가
    String imageName    // 추가
) {}
```

---

## Phase 2: Response DTO 수정

### 2.1 GroupMomentResponse
**파일**: `src/main/java/moment/moment/dto/response/GroupMomentResponse.java`

- `imageUrl` 필드 추가
- 새 팩토리 메서드: `from(Moment, likeCount, hasLiked, commentCount, imageUrl)`

### 2.2 GroupCommentResponse
**파일**: `src/main/java/moment/comment/dto/response/GroupCommentResponse.java`

- `imageUrl` 필드 추가
- 새 팩토리 메서드: `from(Comment, likeCount, hasLiked, imageUrl)`

### 2.3 MyGroupMomentResponse
**파일**: `src/main/java/moment/moment/dto/response/MyGroupMomentResponse.java`

- `imageUrl` 필드 추가
- 팩토리 메서드 시그니처 수정

### 2.4 MyGroupMomentCommentResponse
**파일**: `src/main/java/moment/moment/dto/response/MyGroupMomentCommentResponse.java`

- `imageUrl` 필드 추가 필요
- `CommentComposition`에는 이미 `imageUrl`이 있으나 사용 안함
- `of()` 메서드에서 `composition.imageUrl()` 전달 추가

---

## Phase 3: 생성 로직 수정

### 3.1 MomentApplicationService.createMomentInGroup()
**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

```java
@Transactional
public GroupMomentResponse createMomentInGroup(Long groupId, Long userId,
        String content, String imageUrl, String imageName) {
    // ... 기존 로직 ...
    Moment moment = momentService.createInGroup(momenter, group, member, content);

    // 이미지 저장 추가
    Optional<MomentImage> savedImage = momentImageService.create(moment, imageUrl, imageName);
    String resolvedImageUrl = savedImage.map(img -> photoUrlResolver.resolve(img.getImageUrl())).orElse(null);

    return GroupMomentResponse.from(moment, 0L, false, 0L, resolvedImageUrl);
}
```

### 3.2 CommentApplicationService.createCommentInGroup()
**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

- 동일 패턴 적용: `commentImageService.create()` 호출 추가

---

## Phase 4: 조회 로직 수정

### 4.1 MomentApplicationService.getGroupFeed()
**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

```java
public GroupFeedResponse getGroupFeed(Long groupId, Long userId, Long cursor) {
    // ...
    List<Moment> moments = momentService.getByGroup(groupId, cursor, DEFAULT_PAGE_SIZE);

    // 배치 이미지 조회 (N+1 방지)
    Map<Moment, MomentImage> momentImageMap = momentImageService.getMomentImageByMoment(moments);

    // ... 각 moment에 대해 photoUrlResolver.resolve() 적용 ...
}
```

### 4.2 CommentApplicationService.getCommentsInGroup()
**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

- `commentImageService.getCommentImageByComment()` + `photoUrlResolver` 적용

### 4.3 MyGroupMomentPageFacadeService (핵심)
**파일**: `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

이 파일이 다음 API들의 핵심 로직을 담당:
- `GET /api/v2/groups/{groupId}/my-moments`
- `GET /api/v2/groups/{groupId}/my-moments/unread`

수정 내용:
- 의존성 추가: `MomentImageService`, `PhotoUrlResolver`
- `buildFeedResponse()` 메서드에 이미지 배치 조회 로직 추가
- `MyGroupMomentResponse.of()` 호출 시 imageUrl 파라미터 전달

---

## Phase 5: 삭제 로직 수정

### 5.1 MomentApplicationService.deleteMomentInGroup()
```java
@Transactional
public void deleteMomentInGroup(Long groupId, Long momentId, Long userId) {
    // ... 권한 검증 ...
    momentImageService.deleteBy(momentId);  // 추가
    momentService.deleteBy(momentId);
}
```

### 5.2 CommentApplicationService.deleteCommentInGroup()
```java
@Transactional
public void deleteCommentInGroup(Long groupId, Long commentId, Long userId) {
    // ... 권한 검증 ...
    commentImageService.deleteBy(commentId);  // 추가
    commentService.deleteBy(commentId);
}
```

---

## Phase 6: Controller 수정

### 6.1 GroupMomentController
**파일**: `src/main/java/moment/group/presentation/GroupMomentController.java`

```java
momentApplicationService.createMomentInGroup(
    groupId, authentication.id(),
    request.content(), request.imageUrl(), request.imageName());  // 파라미터 추가
```

### 6.2 GroupCommentController
**파일**: `src/main/java/moment/group/presentation/GroupCommentController.java`

- 동일 패턴 적용

---

## 수정 파일 목록

| # | 파일 경로 | 변경 내용 |
|---|----------|----------|
| 1 | `moment/dto/request/GroupMomentCreateRequest.java` | imageUrl, imageName 필드 추가 |
| 2 | `comment/dto/request/GroupCommentCreateRequest.java` | imageUrl, imageName 필드 추가 |
| 3 | `moment/dto/response/GroupMomentResponse.java` | imageUrl 필드 + 팩토리 오버로드 |
| 4 | `comment/dto/response/GroupCommentResponse.java` | imageUrl 필드 + 팩토리 오버로드 |
| 5 | `moment/dto/response/MyGroupMomentResponse.java` | imageUrl 필드 추가 |
| 6 | `moment/dto/response/MyGroupMomentCommentResponse.java` | imageUrl 필드 추가 + `composition.imageUrl()` 사용 |
| 7 | `moment/service/application/MomentApplicationService.java` | 생성/조회/삭제 이미지 처리 |
| 8 | `comment/service/application/CommentApplicationService.java` | 생성/조회/삭제 이미지 처리 |
| 9 | `group/presentation/GroupMomentController.java` | request 파라미터 전달 |
| 10 | `group/presentation/GroupCommentController.java` | request 파라미터 전달 |
| 11 | `moment/service/facade/MyGroupMomentPageFacadeService.java` | 이미지 배치 조회 + URL 변환 (핵심) |

### 영향받는 API 엔드포인트

**생성 API (2개)**
- `POST /api/v2/groups/{groupId}/moments`
- `POST /api/v2/groups/{groupId}/moments/{momentId}/comments`

**조회 API (4개)**
- `GET /api/v2/groups/{groupId}/moments` (그룹 피드)
- `GET /api/v2/groups/{groupId}/my-moments` (내 모멘트)
- `GET /api/v2/groups/{groupId}/my-moments/unread` (읽지 않은 내 모멘트)
- `GET /api/v2/groups/{groupId}/moments/{momentId}/comments` (코멘트 목록)

**삭제 API (2개)**
- `DELETE /api/v2/groups/{groupId}/moments/{momentId}`
- `DELETE /api/v2/groups/{groupId}/comments/{commentId}`

---

## TDD 테스트 순서

### Unit 테스트 (Moment)
1. `MomentApplicationService.createMomentInGroup()` - 이미지 저장 검증
2. `MomentApplicationService.getGroupFeed()` - 이미지 URL 포함 검증
3. `MomentApplicationService.deleteMomentInGroup()` - 이미지 삭제 검증
4. `MyGroupMomentPageFacadeService.getMyMomentsInGroup()` - 이미지 URL 포함 검증
5. `MyGroupMomentPageFacadeService.getUnreadMyMomentsInGroup()` - 이미지 URL 포함 검증

### Unit 테스트 (Comment)
6. `CommentApplicationService.createCommentInGroup()` - 이미지 저장 검증
7. `CommentApplicationService.getCommentsInGroup()` - 이미지 URL 포함 검증
8. `CommentApplicationService.deleteCommentInGroup()` - 이미지 삭제 검증

### E2E 테스트
9. `POST /groups/{groupId}/moments` - 이미지 포함 모멘트 작성
10. `GET /groups/{groupId}/moments` - 피드에 imageUrl 포함
11. `GET /groups/{groupId}/my-moments` - 내 모멘트에 imageUrl 포함
12. `GET /groups/{groupId}/my-moments/unread` - 읽지 않은 모멘트에 imageUrl 포함
13. `POST /groups/{groupId}/moments/{momentId}/comments` - 이미지 포함 코멘트 작성
14. `GET /groups/{groupId}/moments/{momentId}/comments` - 코멘트에 imageUrl 포함
15. `DELETE` 시 이미지 Soft Delete 검증

---

## 검증 방법

```bash
# 빠른 테스트 (개발 중)
./gradlew fastTest

# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "MomentApplicationServiceTest"
./gradlew test --tests "GroupMomentControllerTest"
```

### E2E 검증 시나리오
1. 이미지 업로드 URL 요청 (`POST /api/v2/storage/upload-url`)
2. S3에 이미지 업로드 (클라이언트)
3. 모멘트 생성 시 imageUrl, imageName 전달
4. 피드 조회 시 최적화된 imageUrl 반환 확인
5. 삭제 후 MomentImage soft delete 확인

---

## 추가 발견 사항 (선택적 개선)

### V2 비그룹 생성 API 응답 일관성 (우선순위: 낮음)

현재 V2 비그룹 생성 API는 `imageId`만 반환하고 `imageUrl`은 반환하지 않음:

| API | Response DTO | 현재 응답 | 비고 |
|-----|--------------|---------|------|
| `POST /api/v2/moments` | `MomentCreateResponse` | `imageId: Long` | imageUrl 없음 |
| `POST /api/v2/moments/extra` | `MomentCreateResponse` | `imageId: Long` | imageUrl 없음 |
| `POST /api/v2/comments` | `CommentCreateResponse` | `commentImageId: Long` | imageUrl 없음 |

**분석**:
- 클라이언트가 생성 시 이미 `imageUrl`을 알고 있으므로 `imageId`만 반환하는 것은 의도된 설계일 수 있음
- 그러나 그룹 API (`GroupMomentResponse`, `GroupCommentResponse`)는 `imageUrl`을 반환하므로 일관성 검토 필요

**권장 사항**:
- 현재 범위에서는 그룹 API만 수정
- 향후 비그룹 API 응답 통일 검토 (별도 작업으로 분리)

### MyGroupMomentCommentResponse 상세 수정 사항

**현재 코드** (`MyGroupMomentCommentResponse.java`):
```java
public record MyGroupMomentCommentResponse(
    Long id,
    String content,
    String memberNickname,
    LocalDateTime createdAt,
    long likeCount,
    boolean hasLiked
    // ❌ imageUrl 필드 없음
)
```

**수정 필요**:
```java
public record MyGroupMomentCommentResponse(
    Long id,
    String content,
    String imageUrl,  // ✅ 추가
    String memberNickname,
    LocalDateTime createdAt,
    long likeCount,
    boolean hasLiked
) {
    public static MyGroupMomentCommentResponse of(
        CommentComposition composition,
        long likeCount,
        boolean hasLiked
    ) {
        return new MyGroupMomentCommentResponse(
            composition.id(),
            composition.content(),
            composition.imageUrl(),  // ✅ 추가
            composition.nickname(),
            composition.commentCreatedAt(),
            likeCount,
            hasLiked
        );
    }
}
```

**참고**: `CommentComposition`에는 이미 `imageUrl` 필드가 존재함 (line 11)

---

## Phase 0: "Feed" → "Moment/Comment" 네이밍 통일 (선행 작업)

> **목적**: 코드베이스에서 "Feed"라는 용어를 "Moment/Comment"로 통일하여 도메인 용어 일관성 확보
> **우선순위**: 높음 - 이미지 기능 추가 전 선행 작업으로 수행

### 변경 대상 요약

| 범주 | 항목 수 | 설명 |
|------|--------|------|
| **Response DTO 파일** | 3개 | Feed가 포함된 record 파일명 변경 |
| **메서드명** | 4개 | `getGroupFeed`, `buildFeedResponse` 등 |
| **변수명** | 1개 | `feed` 변수명 |
| **영향받는 파일** | 9개 | Controller, Service, Test 파일 |

---

### 0.1 Response DTO 파일명 및 Record명 변경 (3개)

| 현재 | 변경 후 | 파일 경로 |
|------|--------|----------|
| `GroupFeedResponse` | `GroupMomentListResponse` | `moment/dto/response/GroupFeedResponse.java` |
| `MyGroupFeedResponse` | `MyGroupMomentListResponse` | `moment/dto/response/MyGroupFeedResponse.java` |
| `MyGroupCommentFeedResponse` | `MyGroupCommentListResponse` | `comment/dto/response/MyGroupCommentFeedResponse.java` |

---

### 0.2 메서드명 변경 (4개)

| 파일 | 현재 메서드명 | 변경 후 |
|------|-------------|--------|
| `MomentApplicationService.java` | `getGroupFeed()` | `getGroupMoments()` |
| `MyGroupMomentPageFacadeService.java` | `buildFeedResponse()` | `buildMomentListResponse()` |
| `MyGroupCommentPageFacadeService.java` | `buildFeedResponse()` | `buildCommentListResponse()` |

---

### 0.3 변경 필요 파일 상세 (9개)

#### A. **DTO 파일 (3개)** - 파일명 + Record명 변경

1. `moment/dto/response/GroupFeedResponse.java`
   - 파일명: `GroupFeedResponse.java` → `GroupMomentListResponse.java`
   - Record명: `GroupFeedResponse` → `GroupMomentListResponse`

2. `moment/dto/response/MyGroupFeedResponse.java`
   - 파일명: `MyGroupFeedResponse.java` → `MyGroupMomentListResponse.java`
   - Record명: `MyGroupFeedResponse` → `MyGroupMomentListResponse`

3. `comment/dto/response/MyGroupCommentFeedResponse.java`
   - 파일명: `MyGroupCommentFeedResponse.java` → `MyGroupCommentListResponse.java`
   - Record명: `MyGroupCommentFeedResponse` → `MyGroupCommentListResponse`

#### B. **Service 파일 (3개)** - import, 반환타입, 메서드명 변경

4. `moment/service/application/MomentApplicationService.java`
   - import 변경
   - `getGroupFeed()` → `getGroupMoments()`
   - 반환타입: `GroupFeedResponse` → `GroupMomentListResponse`
   - `GroupFeedResponse.of()` → `GroupMomentListResponse.of()`

5. `moment/service/facade/MyGroupMomentPageFacadeService.java`
   - import 변경
   - `buildFeedResponse()` → `buildMomentListResponse()`
   - 반환타입: `MyGroupFeedResponse` → `MyGroupMomentListResponse`
   - `MyGroupFeedResponse.empty()/.of()` → `MyGroupMomentListResponse.empty()/.of()`

6. `comment/service/facade/MyGroupCommentPageFacadeService.java`
   - import 변경
   - `buildFeedResponse()` → `buildCommentListResponse()`
   - 반환타입: `MyGroupCommentFeedResponse` → `MyGroupCommentListResponse`
   - `MyGroupCommentFeedResponse.empty()/.of()` → `MyGroupCommentListResponse.empty()/.of()`

#### C. **Controller 파일 (2개)** - import, 반환타입 변경

7. `group/presentation/GroupMomentController.java`
   - import 변경
   - `getGroupFeed()` 호출 → `getGroupMoments()` 호출
   - 반환타입: `GroupFeedResponse` → `GroupMomentListResponse`
   - 반환타입: `MyGroupFeedResponse` → `MyGroupMomentListResponse`

8. `group/presentation/GroupCommentController.java`
   - import 변경
   - 반환타입: `MyGroupCommentFeedResponse` → `MyGroupCommentListResponse`

#### D. **Test 파일 (4개)** - import, 변수타입 변경

9. `test/.../GroupMomentControllerTest.java`
   - import 변경
   - 변수타입: `GroupFeedResponse` → `GroupMomentListResponse`
   - 변수타입: `MyGroupFeedResponse` → `MyGroupMomentListResponse`
   - 변수명: `feed` → `response` (선택적)

10. `test/.../MyGroupMomentPageFacadeServiceTest.java`
    - import 변경
    - 변수타입: `MyGroupFeedResponse` → `MyGroupMomentListResponse`

11. `test/.../MyGroupCommentPageFacadeServiceTest.java`
    - import 변경
    - 변수타입: `MyGroupCommentFeedResponse` → `MyGroupCommentListResponse`

12. `test/.../GroupCommentControllerTest.java`
    - import 변경
    - 변수타입: `MyGroupCommentFeedResponse` → `MyGroupCommentListResponse`

---

### 0.4 변경 순서 (권장)

Tidy First 원칙에 따라 **구조적 변경만** 수행 (동작 변경 없음):

1. **Step 1**: DTO 파일명 및 Record명 변경 (3개)
   - IDE 리팩토링 기능 활용 권장 (Rename Class)

2. **Step 2**: Service 파일 업데이트 (3개)
   - import 경로 자동 수정
   - 메서드명 변경: `buildFeedResponse()` → `buildMomentListResponse()`/`buildCommentListResponse()`
   - 메서드명 변경: `getGroupFeed()` → `getGroupMoments()`

3. **Step 3**: Controller 파일 업데이트 (2개)
   - import 경로 자동 수정
   - 메서드 호출명 변경

4. **Step 4**: Test 파일 업데이트 (4개)
   - import 경로 자동 수정
   - 변수타입 변경

5. **Step 5**: 테스트 실행
   ```bash
   ./gradlew fastTest
   ```

---

### 0.5 변경 후 이미지 기능 계획 업데이트

> Phase 0 완료 후, 기존 계획의 아래 부분들이 새 네이밍으로 자동 반영됨:

| 기존 계획 내용 | 변경 후 |
|--------------|--------|
| `GroupFeedResponse` | `GroupMomentListResponse` |
| `MyGroupFeedResponse` | `MyGroupMomentListResponse` |
| `getGroupFeed()` | `getGroupMoments()` |
| `buildFeedResponse()` | `buildMomentListResponse()` / `buildCommentListResponse()` |

---

### 0.6 Git 커밋 전략

```bash
# 구조적 변경만 포함 (Tidy First)
git commit -m "refactor: rename Feed to MomentList/CommentList for naming consistency

- GroupFeedResponse → GroupMomentListResponse
- MyGroupFeedResponse → MyGroupMomentListResponse
- MyGroupCommentFeedResponse → MyGroupCommentListResponse
- getGroupFeed() → getGroupMoments()
- buildFeedResponse() → buildMomentListResponse()/buildCommentListResponse()

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>"
```