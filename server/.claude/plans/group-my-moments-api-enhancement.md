# GET /api/v2/groups/{groupId}/my-moments 응답 확장 및 기존 API 정리 계획

## 목표
1. `/api/v2/groups/{groupId}/my-moments` API에 comments 목록과 momentNotification 정보 추가
2. `/api/v2/groups/{groupId}/my-moments/unread` API 신규 추가 (읽지 않은 모멘트 조회)
3. 기존 `/api/v2/moments/me` 및 `/api/v2/moments/me/unread` API 삭제 (deprecated)

## 현재 상태 vs 목표 상태

### 현재 응답 (GroupMomentResponse)
```java
momentId, content, memberNickname, memberId,
likeCount, hasLiked, commentCount, createdAt
```

### 목표 응답 (확장)
```java
momentId, content, memberNickname, memberId,
likeCount, hasLiked, commentCount, createdAt,
comments: List<MyGroupMomentCommentResponse>,  // 추가
momentNotification: MomentNotificationResponse  // 추가
```

---

## 구현 계획

### Step 1: 새 Response DTO 생성

#### 1.1 `MyGroupMomentCommentResponse.java` (신규)
```
위치: server/src/main/java/moment/moment/dto/response/MyGroupMomentCommentResponse.java
```
- 필드: id, content, memberNickname, memberId, createdAt
- Comment 엔티티에서 변환하는 정적 팩토리 메서드 포함

#### 1.2 `MyGroupMomentResponse.java` (신규)
```
위치: server/src/main/java/moment/moment/dto/response/MyGroupMomentResponse.java
```
- 기존 GroupMomentResponse 필드 + comments + momentNotification 포함
- `MomentNotificationResponse` 재사용

#### 1.3 `MyGroupFeedResponse.java` (신규)
```
위치: server/src/main/java/moment/moment/dto/response/MyGroupFeedResponse.java
```
- 필드: moments (List<MyGroupMomentResponse>), nextCursor, hasNextPage

### Step 2: Facade Service 생성

#### 2.1 `MyGroupMomentPageFacadeService.java` (신규)
```
위치: server/src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java
```

**핵심 로직:**
```java
public MyGroupFeedResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);

    // 2. 모멘트 조회
    List<Moment> moments = momentService.getMyMomentsInGroup(groupId, member.getId(), cursor, pageSize);
    List<Long> momentIds = moments.stream().map(Moment::getId).toList();

    // 3. 배치 조회
    // 3-1. 댓글 조회 (momentId별 그룹화)
    Map<Long, List<Comment>> commentsMap = commentService.getAllByMomentIds(momentIds)
            .stream().collect(Collectors.groupingBy(Comment::getMomentId));

    // 3-2. 알림 조회 (재사용)
    Map<Long, List<Long>> notificationsMap =
            notificationApplicationService.getNotificationsByTargetIdsAndTargetType(momentIds, TargetType.MOMENT);

    // 3-3. 좋아요 정보 (기존 로직 유지)

    // 4. 응답 조합
    return MyGroupFeedResponse.of(moments, commentsMap, notificationsMap, ...);
}
```

**의존성:**
- MomentService
- GroupMemberService
- CommentService (또는 CommentApplicationService)
- NotificationApplicationService
- MomentLikeService

#### 2.2 Unread 모멘트 조회 메서드 추가

**핵심 로직:**
```java
public MyGroupFeedResponse getUnreadMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);

    // 2. 읽지 않은 알림의 모멘트 ID 목록 조회
    List<Long> unreadMomentIds = notificationApplicationService.getUnreadNotifications(
        userId, TargetType.MOMENT);

    // 3. 해당 그룹의 unread 모멘트만 필터링하여 조회
    if (unreadMomentIds == null || unreadMomentIds.isEmpty()) {
        return MyGroupFeedResponse.empty();  // 빈 응답 반환
    }

    // 4. 그룹 내 본인의 unread 모멘트만 조회
    List<Moment> moments = momentService.getUnreadMyMomentsInGroup(
        groupId, member.getId(), unreadMomentIds, cursor, pageSize);

    // 5. 이후 로직은 getMyMomentsInGroup()과 동일
    // (댓글, 알림, 좋아요 배치 조회 및 응답 조합)
}
```

### Step 3: Controller 수정

#### 3.1 `GroupMomentController.java` 수정
```
위치: server/src/main/java/moment/group/presentation/GroupMomentController.java
Line: 113-121
```

**변경 내용:**
1. `MyGroupMomentPageFacadeService` 의존성 추가
2. `getMyMoments()` 메서드 반환 타입 변경: `GroupFeedResponse` → `MyGroupFeedResponse`
3. 새 Facade 서비스 호출

```java
@GetMapping("/my-moments")
public ResponseEntity<SuccessResponse<MyGroupFeedResponse>> getMyMoments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupFeedResponse response = myGroupMomentPageFacadeService.getMyMomentsInGroup(
        groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

#### 3.2 Unread 모멘트 조회 엔드포인트 추가 (신규)

```java
@Operation(summary = "그룹 내 읽지 않은 내 모멘트 조회",
           description = "그룹 내에서 알림을 읽지 않은 자신의 모멘트를 조회합니다.")
@GetMapping("/my-moments/unread")
public ResponseEntity<SuccessResponse<MyGroupFeedResponse>> getUnreadMyMoments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupFeedResponse response = myGroupMomentPageFacadeService.getUnreadMyMomentsInGroup(
        groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

---

## 파일 목록

### 신규 생성 (4개)
| 파일 | 설명 |
|------|------|
| `moment/dto/response/MyGroupMomentCommentResponse.java` | 그룹 모멘트 댓글 응답 DTO |
| `moment/dto/response/MyGroupMomentResponse.java` | 그룹 내 나의 모멘트 응답 DTO |
| `moment/dto/response/MyGroupFeedResponse.java` | 피드 응답 래퍼 |
| `moment/service/facade/MyGroupMomentPageFacadeService.java` | Facade 서비스 |

### 수정 (1개)
| 파일 | 변경 내용 |
|------|-----------|
| `group/presentation/GroupMomentController.java` | 의존성 추가, 반환 타입 변경 |

---

## 재사용 컴포넌트

| 컴포넌트 | 용도 |
|----------|------|
| `MomentNotificationResponse` | 알림 정보 DTO (isRead, notificationIds) |
| `NotificationApplicationService.getNotificationsByTargetIdsAndTargetType()` | 알림 배치 조회 |
| `CommentService.getAllByMomentIds()` | 댓글 배치 조회 |

---

## 검증 계획

### 1. 단위 테스트
- `MyGroupMomentPageFacadeServiceTest` 작성
  - `getMyMomentsInGroup()` 테스트
  - `getUnreadMyMomentsInGroup()` 테스트
- 각 DTO의 정적 팩토리 메서드 테스트

### 2. 통합 테스트 (E2E)
- `/api/v2/groups/{groupId}/my-moments` 호출
  - 응답에 `comments` 배열 포함 확인
  - 응답에 `momentNotification` 객체 포함 확인
- `/api/v2/groups/{groupId}/my-moments/unread` 호출
  - 읽지 않은 모멘트만 반환되는지 확인
  - 빈 목록일 때 정상 응답 확인

### 3. 수동 테스트

**내 모멘트 조회:**
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-moments" \
  -H "Authorization: Bearer {token}"
```

**읽지 않은 내 모멘트 조회:**
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-moments/unread" \
  -H "Authorization: Bearer {token}"
```

예상 응답:
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "moments": [
      {
        "momentId": 1,
        "content": "...",
        "memberNickname": "...",
        "memberId": 1,
        "likeCount": 5,
        "hasLiked": true,
        "commentCount": 3,
        "createdAt": "...",
        "comments": [
          {
            "id": 1,
            "content": "댓글 내용",
            "memberNickname": "작성자",
            "memberId": 2,
            "createdAt": "..."
          }
        ],
        "momentNotification": {
          "isRead": false,
          "notificationIds": [101, 102]
        }
      }
    ],
    "nextCursor": 0,
    "hasNextPage": false
  }
}
```

---

## Part 2: 기존 `/api/v2/moments/me` API 삭제

### 삭제 대상 파일 (5개)

| 파일 | 경로 | 이유 |
|------|------|------|
| `MyMomentPageFacadeService.java` | `moment/service/facade/` | deprecated API 전용 Facade |
| `MyMomentPageResponse.java` | `moment/dto/response/tobe/` | deprecated API 전용 응답 DTO |
| `MyMomentResponse.java` | `moment/dto/response/tobe/` | deprecated API 전용 모멘트 DTO |
| `MyMomentsResponse.java` | `moment/dto/response/tobe/` | deprecated API 전용 래퍼 DTO |
| `MyMomentCommentResponse.java` | `moment/dto/response/` | deprecated API 전용 댓글 DTO |

### 삭제 대상 Controller 메서드 (2개)

**파일**: `moment/presentation/MomentController.java`

| 라인 | 메서드 | 엔드포인트 |
|------|--------|------------|
| 104-115 | `readMyMoment()` | `GET /api/v2/moments/me` |
| 117-140 | `readUnreadMyMoment()` | `GET /api/v2/moments/me/unread` |

### 삭제 대상 테스트 메서드 (5개)

**파일**: `moment/presentation/MomentControllerTest.java`

| 라인 | 테스트 메서드명 |
|------|-----------------|
| 209-255 | `내_모멘트를_등록_시간_순으로_정렬한_페이지를_조회한다()` |
| 258-317 | `내_모멘트_조회_시_읽음_상태를_함께_반환한다()` |
| 319-346 | `내_모멘트_조회_시_모멘트_태그가_없는_경우도_조회된다()` |
| 348-392 | `DB에_저장된_Moment가_limit보다_적을_경우_남은_목록을_반환한다()` |
| 555-595 | `나의_Moment_목록을_조회한다()` |

### 유지해야 할 파일 (삭제 금지)

| 파일 | 이유 |
|------|------|
| `MomentComposition.java` (DTO) | MyComment 관련 API에서 재사용 |
| `MomentCompositions.java` | MomentApplicationService에서 재사용 |
| `MomentNotificationResponse.java` | 새 API에서 재사용 예정 |

---

## 구현 순서

### Phase 1: 새 API 구현 (기능 추가)
1. 새 DTO 생성 (3개)
2. MyGroupMomentPageFacadeService 생성 (2개 메서드 포함)
   - `getMyMomentsInGroup()` - 내 모멘트 조회
   - `getUnreadMyMomentsInGroup()` - 읽지 않은 내 모멘트 조회
3. GroupMomentController 수정 (2개 엔드포인트)
   - `GET /my-moments` 수정
   - `GET /my-moments/unread` 신규 추가
4. 테스트 작성 및 검증

### Phase 2: 기존 API 삭제 (정리)
1. MomentController에서 deprecated 메서드 삭제
2. MomentControllerTest에서 관련 테스트 삭제
3. MyMomentPageFacadeService 삭제
4. 관련 DTO 파일 삭제 (4개)

---

## 최종 파일 변경 요약

### 신규 생성 (4개)
- `MyGroupMomentCommentResponse.java`
- `MyGroupMomentResponse.java`
- `MyGroupFeedResponse.java`
- `MyGroupMomentPageFacadeService.java`

### 수정 (2개)
- `GroupMomentController.java` - 새 Facade 의존성 추가, 2개 엔드포인트 (my-moments 수정 + my-moments/unread 추가)
- `MomentController.java` - deprecated 메서드 2개 삭제 (/me, /me/unread)

### 삭제 (5개)
- `MyMomentPageFacadeService.java`
- `MyMomentPageResponse.java`
- `MyMomentResponse.java`
- `MyMomentsResponse.java`
- `MyMomentCommentResponse.java`

### 테스트 수정 (1개)
- `MomentControllerTest.java` - deprecated 테스트 메서드 5개 삭제

---

## 새 API 엔드포인트 요약

| 엔드포인트 | 설명 | 대체 대상 |
|------------|------|-----------|
| `GET /api/v2/groups/{groupId}/my-moments` | 그룹 내 나의 모멘트 조회 (comments, notification 포함) | `/api/v2/moments/me` |
| `GET /api/v2/groups/{groupId}/my-moments/unread` | 그룹 내 읽지 않은 나의 모멘트 조회 | `/api/v2/moments/me/unread` |
