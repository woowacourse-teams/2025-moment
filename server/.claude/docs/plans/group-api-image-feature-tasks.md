# 그룹 API 이미지 기능 - 세부 구현 계획

> 기반 문서: `group-api-image-feature.md`
> TDD 사이클: Red → Green → Refactor

---

## 작업 개요

| 작업 번호 | 작업명 | 예상 파일 수 | 의존성 |
|----------|-------|------------|--------|
| Task 0 | Feed → Moment 네이밍 통일 | 12개 | 없음 |
| Task 1 | 모멘트 이미지 생성 기능 | 4개 | Task 0 |
| Task 2 | 코멘트 이미지 생성 기능 | 4개 | Task 0 |
| Task 3 | 그룹 피드 조회 이미지 추가 | 2개 | Task 1 |
| Task 4 | 내 모멘트 조회 이미지 추가 | 3개 | Task 1 |
| Task 5 | 그룹 코멘트 조회 이미지 추가 | 2개 | Task 2 |
| Task 6 | 모멘트 삭제 시 이미지 삭제 | 1개 | Task 1 |
| Task 7 | 코멘트 삭제 시 이미지 삭제 | 1개 | Task 2 |
| Task 8 | E2E 테스트 작성 | 2개 | Task 1-7 |

---

## Task 0: Feed → Moment 네이밍 통일 (구조적 변경)

### 목적
코드베이스에서 "Feed"라는 용어를 "Moment/Comment"로 통일

### 변경 파일 (12개)

#### Step 0-1: DTO 파일명 및 Record명 변경
```
src/main/java/moment/moment/dto/response/GroupFeedResponse.java
  → GroupMomentListResponse.java (파일명 + Record명)

src/main/java/moment/moment/dto/response/MyGroupFeedResponse.java
  → MyGroupMomentListResponse.java (파일명 + Record명)

src/main/java/moment/comment/dto/response/MyGroupCommentFeedResponse.java
  → MyGroupCommentListResponse.java (파일명 + Record명)
```

#### Step 0-2: Service 파일 업데이트
```
src/main/java/moment/moment/service/application/MomentApplicationService.java
  - import 변경
  - getGroupFeed() → getGroupMoments()
  - 반환타입 변경

src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java
  - import 변경
  - buildFeedResponse() → buildMomentListResponse()
  - 반환타입 변경

src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java
  - import 변경
  - buildFeedResponse() → buildCommentListResponse()
  - 반환타입 변경
```

#### Step 0-3: Controller 파일 업데이트
```
src/main/java/moment/group/presentation/GroupMomentController.java
  - import 변경
  - getGroupFeed() → getGroupMoments() 호출
  - 반환타입 변경

src/main/java/moment/group/presentation/GroupCommentController.java
  - import 변경
  - 반환타입 변경
```

#### Step 0-4: Test 파일 업데이트
```
src/test/java/moment/group/presentation/GroupMomentControllerTest.java
src/test/java/moment/moment/service/facade/MyGroupMomentPageFacadeServiceTest.java
src/test/java/moment/comment/service/facade/MyGroupCommentPageFacadeServiceTest.java
src/test/java/moment/group/presentation/GroupCommentControllerTest.java
```

### 검증
```bash
./gradlew fastTest
```

### 커밋
```
refactor: rename Feed to MomentList/CommentList for naming consistency
```

---

## Task 1: 모멘트 이미지 생성 기능

### 목적
`POST /api/v2/groups/{groupId}/moments` API에서 이미지 저장 기능 추가

### TDD 테스트 목록
- [ ] `MomentApplicationServiceTest.createMomentInGroup_이미지_포함_성공()`
- [ ] `MomentApplicationServiceTest.createMomentInGroup_이미지_없이_성공()`

### 변경 파일 (4개)

#### Step 1-1: Request DTO 수정
```
src/main/java/moment/moment/dto/request/GroupMomentCreateRequest.java
```
```java
public record GroupMomentCreateRequest(
    @NotBlank String content,
    String imageUrl,    // 추가
    String imageName    // 추가
) {}
```

#### Step 1-2: Response DTO 수정
```
src/main/java/moment/moment/dto/response/GroupMomentResponse.java
```
- `imageUrl` 필드 추가
- 새 팩토리 메서드: `from(Moment, likeCount, hasLiked, commentCount, imageUrl)`

#### Step 1-3: Service 수정
```
src/main/java/moment/moment/service/application/MomentApplicationService.java
```
- `createMomentInGroup()` 메서드에 이미지 저장 로직 추가
- 의존성 추가: `MomentImageService`, `PhotoUrlResolver`

#### Step 1-4: Controller 수정
```
src/main/java/moment/group/presentation/GroupMomentController.java
```
- `createMoment()` 메서드에서 `request.imageUrl()`, `request.imageName()` 전달

### 검증
```bash
./gradlew test --tests "MomentApplicationServiceTest.createMomentInGroup*"
```

### 커밋
```
feat: add image support for group moment creation

- Add imageUrl, imageName fields to GroupMomentCreateRequest
- Add imageUrl field to GroupMomentResponse
- Save MomentImage when creating moment in group
```

---

## Task 2: 코멘트 이미지 생성 기능

### 목적
`POST /api/v2/groups/{groupId}/moments/{momentId}/comments` API에서 이미지 저장 기능 추가

### TDD 테스트 목록
- [ ] `CommentApplicationServiceTest.createCommentInGroup_이미지_포함_성공()`
- [ ] `CommentApplicationServiceTest.createCommentInGroup_이미지_없이_성공()`

### 변경 파일 (4개)

#### Step 2-1: Request DTO 수정
```
src/main/java/moment/comment/dto/request/GroupCommentCreateRequest.java
```
```java
public record GroupCommentCreateRequest(
    @NotBlank String content,
    String imageUrl,    // 추가
    String imageName    // 추가
) {}
```

#### Step 2-2: Response DTO 수정
```
src/main/java/moment/comment/dto/response/GroupCommentResponse.java
```
- `imageUrl` 필드 추가
- 새 팩토리 메서드: `from(Comment, likeCount, hasLiked, imageUrl)`

#### Step 2-3: Service 수정
```
src/main/java/moment/comment/service/application/CommentApplicationService.java
```
- `createCommentInGroup()` 메서드에 이미지 저장 로직 추가
- 의존성 추가: `CommentImageService`, `PhotoUrlResolver`

#### Step 2-4: Controller 수정
```
src/main/java/moment/group/presentation/GroupCommentController.java
```
- `createComment()` 메서드에서 `request.imageUrl()`, `request.imageName()` 전달

### 검증
```bash
./gradlew test --tests "CommentApplicationServiceTest.createCommentInGroup*"
```

### 커밋
```
feat: add image support for group comment creation

- Add imageUrl, imageName fields to GroupCommentCreateRequest
- Add imageUrl field to GroupCommentResponse
- Save CommentImage when creating comment in group
```

---

## Task 3: 그룹 피드 조회 이미지 추가

### 목적
`GET /api/v2/groups/{groupId}/moments` API 응답에 imageUrl 포함

### TDD 테스트 목록
- [ ] `MomentApplicationServiceTest.getGroupMoments_이미지_URL_포함()`

### 변경 파일 (2개)

#### Step 3-1: Service 수정
```
src/main/java/moment/moment/service/application/MomentApplicationService.java
```
- `getGroupMoments()` (구 `getGroupFeed()`) 메서드 수정
- 배치 이미지 조회: `momentImageService.getMomentImageByMoment(moments)`
- `photoUrlResolver.resolve()` 적용

```java
public GroupMomentListResponse getGroupMoments(Long groupId, Long userId, Long cursor) {
    // ...
    List<Moment> moments = momentService.getByGroup(groupId, cursor, DEFAULT_PAGE_SIZE);

    // 배치 이미지 조회 (N+1 방지)
    Map<Moment, MomentImage> momentImageMap = momentImageService.getMomentImageByMoment(moments);

    List<GroupMomentResponse> responses = moments.stream()
        .map(moment -> {
            MomentImage image = momentImageMap.get(moment);
            String resolvedImageUrl = (image != null)
                ? photoUrlResolver.resolve(image.getImageUrl())
                : null;
            // ... 기존 로직에 resolvedImageUrl 추가
            return GroupMomentResponse.from(moment, likeCount, hasLiked, commentCount, resolvedImageUrl);
        })
        .toList();
    // ...
}
```

### 검증
```bash
./gradlew test --tests "MomentApplicationServiceTest.getGroupMoments*"
```

### 커밋
```
feat: include imageUrl in group moments list response

- Add batch image query for N+1 prevention
- Apply PhotoUrlResolver for optimized URLs
```

---

## Task 4: 내 모멘트 조회 이미지 추가

### 목적
- `GET /api/v2/groups/{groupId}/my-moments`
- `GET /api/v2/groups/{groupId}/my-moments/unread`

API 응답에 imageUrl 포함

### TDD 테스트 목록
- [ ] `MyGroupMomentPageFacadeServiceTest.getMyMomentsInGroup_이미지_URL_포함()`
- [ ] `MyGroupMomentPageFacadeServiceTest.getUnreadMyMomentsInGroup_이미지_URL_포함()`

### 변경 파일 (3개)

#### Step 4-1: Response DTO 수정
```
src/main/java/moment/moment/dto/response/MyGroupMomentResponse.java
```
- `imageUrl` 필드 추가
- 팩토리 메서드 시그니처 수정

```
src/main/java/moment/moment/dto/response/MyGroupMomentCommentResponse.java
```
- `imageUrl` 필드 추가
- `composition.imageUrl()` 사용

#### Step 4-2: Facade Service 수정
```
src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java
```
- 의존성 추가: `MomentImageService`, `PhotoUrlResolver`
- `buildMomentListResponse()` 메서드에 이미지 배치 조회 로직 추가

```java
private MyGroupMomentListResponse buildMomentListResponse(List<Moment> moments, Long memberId) {
    // ...

    // 배치 이미지 조회 추가
    Map<Moment, MomentImage> momentImageMap = momentImageService.getMomentImageByMoment(moments);

    List<MyGroupMomentResponse> responses = moments.stream()
        .map(moment -> {
            MomentImage image = momentImageMap.get(moment);
            String resolvedImageUrl = (image != null)
                ? photoUrlResolver.resolve(image.getImageUrl())
                : null;

            return MyGroupMomentResponse.of(
                moment, likeCount, hasLiked, commentCount,
                comments, momentNotification,
                resolvedImageUrl  // 추가
            );
        })
        .toList();
    // ...
}
```

### 검증
```bash
./gradlew test --tests "MyGroupMomentPageFacadeServiceTest.*"
```

### 커밋
```
feat: include imageUrl in my-moments response

- Add imageUrl field to MyGroupMomentResponse
- Add imageUrl field to MyGroupMomentCommentResponse
- Add batch image query in MyGroupMomentPageFacadeService
```

---

## Task 5: 그룹 코멘트 조회 이미지 추가

### 목적
`GET /api/v2/groups/{groupId}/moments/{momentId}/comments` API 응답에 imageUrl 포함

### TDD 테스트 목록
- [ ] `CommentApplicationServiceTest.getCommentsInGroup_이미지_URL_포함()`

### 변경 파일 (2개)

#### Step 5-1: Service 수정
```
src/main/java/moment/comment/service/application/CommentApplicationService.java
```
- `getCommentsInGroup()` 메서드 수정
- 배치 이미지 조회: `commentImageService.getCommentImageByComment(comments)`
- `photoUrlResolver.resolve()` 적용

```java
public List<GroupCommentResponse> getCommentsInGroup(Long groupId, Long momentId, Long userId) {
    // ...
    List<Comment> comments = commentService.getAllByMomentIds(List.of(momentId));

    // 배치 이미지 조회 (N+1 방지)
    Map<Comment, CommentImage> commentImageMap = commentImageService.getCommentImageByComment(comments);

    return comments.stream()
        .map(comment -> {
            CommentImage image = commentImageMap.get(comment);
            String resolvedImageUrl = (image != null)
                ? photoUrlResolver.resolve(image.getImageUrl())
                : null;
            // ...
            return GroupCommentResponse.from(comment, likeCount, hasLiked, resolvedImageUrl);
        })
        .toList();
}
```

### 검증
```bash
./gradlew test --tests "CommentApplicationServiceTest.getCommentsInGroup*"
```

### 커밋
```
feat: include imageUrl in group comments list response

- Add batch image query for N+1 prevention
- Apply PhotoUrlResolver for optimized URLs
```

---

## Task 6: 모멘트 삭제 시 이미지 삭제

### 목적
`DELETE /api/v2/groups/{groupId}/moments/{momentId}` API에서 이미지 Soft Delete 적용

### TDD 테스트 목록
- [ ] `MomentApplicationServiceTest.deleteMomentInGroup_이미지_삭제_검증()`

### 변경 파일 (1개)

#### Step 6-1: Service 수정
```
src/main/java/moment/moment/service/application/MomentApplicationService.java
```
- `deleteMomentInGroup()` 메서드에 이미지 삭제 로직 추가

```java
@Transactional
public void deleteMomentInGroup(Long groupId, Long momentId, Long userId) {
    Moment momentToDelete = momentService.getMomentBy(momentId);
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);

    if (!momentToDelete.getMember().getId().equals(member.getId())) {
        throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
    }

    momentImageService.deleteBy(momentId);  // 추가
    momentService.deleteBy(momentId);
}
```

### 검증
```bash
./gradlew test --tests "MomentApplicationServiceTest.deleteMomentInGroup*"
```

### 커밋
```
feat: delete moment image when deleting group moment

- Add momentImageService.deleteBy() call before moment deletion
```

---

## Task 7: 코멘트 삭제 시 이미지 삭제

### 목적
`DELETE /api/v2/groups/{groupId}/comments/{commentId}` API에서 이미지 Soft Delete 적용

### TDD 테스트 목록
- [ ] `CommentApplicationServiceTest.deleteCommentInGroup_이미지_삭제_검증()`

### 변경 파일 (1개)

#### Step 7-1: Service 수정
```
src/main/java/moment/comment/service/application/CommentApplicationService.java
```
- `deleteCommentInGroup()` 메서드에 이미지 삭제 로직 추가

```java
@Transactional
public void deleteCommentInGroup(Long groupId, Long commentId, Long userId) {
    Comment comment = commentService.getCommentBy(commentId);
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

    if (!comment.getMember().getId().equals(member.getId())) {
        throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
    }

    commentImageService.deleteBy(commentId);  // 추가
    commentService.deleteBy(commentId);
}
```

### 검증
```bash
./gradlew test --tests "CommentApplicationServiceTest.deleteCommentInGroup*"
```

### 커밋
```
feat: delete comment image when deleting group comment

- Add commentImageService.deleteBy() call before comment deletion
```

---

## Task 8: E2E 테스트 작성

### 목적
전체 API 플로우 검증

### TDD 테스트 목록
- [ ] `GroupMomentControllerTest.이미지_포함_모멘트_작성()`
- [ ] `GroupMomentControllerTest.그룹_피드_조회_imageUrl_포함()`
- [ ] `GroupMomentControllerTest.내_모멘트_조회_imageUrl_포함()`
- [ ] `GroupMomentControllerTest.모멘트_삭제_시_이미지_삭제()`
- [ ] `GroupCommentControllerTest.이미지_포함_코멘트_작성()`
- [ ] `GroupCommentControllerTest.코멘트_목록_조회_imageUrl_포함()`
- [ ] `GroupCommentControllerTest.코멘트_삭제_시_이미지_삭제()`

### 변경 파일 (2개)
```
src/test/java/moment/group/presentation/GroupMomentControllerTest.java
src/test/java/moment/group/presentation/GroupCommentControllerTest.java
```

### 검증
```bash
./gradlew e2eTest --tests "GroupMomentControllerTest"
./gradlew e2eTest --tests "GroupCommentControllerTest"
```

### 커밋
```
test: add E2E tests for group API image features

- Test image creation in moments and comments
- Test imageUrl in list responses
- Test image deletion on entity deletion
```

---

## 전체 실행 순서

```
Task 0 (네이밍 통일)
    ↓
┌───┴───┐
Task 1   Task 2  (생성 기능 - 병렬 가능)
│         │
Task 3   Task 5  (조회 기능)
│
Task 4
│         │
Task 6   Task 7  (삭제 기능)
└───┬───┘
    ↓
Task 8 (E2E 테스트)
```

---

## 전체 검증

```bash
# 빠른 테스트 (개발 중)
./gradlew fastTest

# 전체 테스트
./gradlew test

# E2E 테스트만
./gradlew e2eTest
```

---

## 참고: 기존 이미지 처리 패턴

### 정상 작동 패턴 (참조용)
```java
// CommentApplicationService.getMyCommentCompositionsBy()
public List<CommentComposition> getMyCommentCompositionsBy(List<Long> momentIds) {
    List<Comment> comments = commentService.getAllByMomentIds(momentIds);

    // 배치 이미지 조회
    Map<Comment, CommentImage> commentImageByComment =
        commentImageService.getCommentImageByComment(comments);

    return comments.stream()
        .map(comment -> {
            CommentImage image = commentImageByComment.get(comment);
            String resolvedImageUrl = (image != null)
                ? photoUrlResolver.resolve(image.getImageUrl())
                : null;
            return CommentComposition.of(comment, commenter, resolvedImageUrl);
        })
        .toList();
}
```
