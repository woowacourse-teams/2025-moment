# GET /api/v2/groups/{groupId}/my-comments API 구현 - Phase 2: 기존 API 삭제

> 원본 계획: `group-my-comments-api-enhancement.md`

---

## Phase 2: 기존 API 삭제 (정리)

### Step 2.1: CommentControllerTest.java 테스트 삭제

**파일 경로**: `server/src/test/java/moment/comment/presentation/CommentControllerTest.java`

**삭제 대상 (2개 메서드)**:

| 메서드명 | 예상 라인 범위 |
|----------|---------------|
| `나의_Comment_목록을_조회한다()` | 약 121-171 |
| `나의_Comment_목록을_조회시_삭제된_모멘트는_비어있다()` | 약 173-211 |

**삭제 대상 import**:
```java
import moment.comment.dto.response.MyCommentPageResponse;
```

---

### Step 2.2: CommentController.java 메서드 삭제

**파일 경로**: `server/src/main/java/moment/comment/presentation/CommentController.java`

**현재 상태 (라인 70-119)**:
```java
@Operation(summary = "나의 Comment 목록 조회", ...)
@GetMapping("/me")
public ResponseEntity<SuccessResponse<MyCommentPageResponse>> readMyComments(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(defaultValue = "10") int limit,
        @AuthenticationPrincipal Authentication authentication) {
    Long userId = authentication.id();
    MyCommentPageResponse response = myCommentPageFacadeService.getMyCommentsPage(nextCursor, limit, userId);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}

@Operation(summary = "알림을 확인하지 않은 나의 Comment 목록 조회", ...)
@GetMapping("/me/unread")
public ResponseEntity<SuccessResponse<MyCommentPageResponse>> readMyUnreadComments(
        @RequestParam(required = false) String nextCursor,
        @RequestParam(defaultValue = "10") int limit,
        @AuthenticationPrincipal Authentication authentication) {
    Long userId = authentication.id();
    MyCommentPageResponse response = myCommentPageFacadeService.getUnreadMyCommentsPage(nextCursor, limit, userId);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

**삭제 대상**:

1. **필드 제거** (라인 36):
   ```java
   private final MyCommentPageFacadeService myCommentPageFacadeService;
   ```

2. **import 제거** (라인 14, 16):
   ```java
   import moment.comment.dto.response.MyCommentPageResponse;
   import moment.comment.service.facade.MyCommentPageFacadeService;
   ```

3. **메서드 삭제**:
   - `readMyComments()` (라인 70-93) - Swagger 문서 포함
   - `readMyUnreadComments()` (라인 95-119) - Swagger 문서 포함

**변경 후 CommentController 구조**:
```java
@Tag(name = "Comment API", description = "Comment 관련 API 명세")
@RestController
@RequestMapping("/api/v2/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentCreateFacadeService commentCreateFacadeService;
    // myCommentPageFacadeService 필드 제거

    @Operation(summary = "Comment 등록", description = "새로운 Comment를 등록합니다.")
    @PostMapping
    public ResponseEntity<SuccessResponse<CommentCreateResponse>> createComment(...) {
        // 기존 유지
    }

    // readMyComments(), readMyUnreadComments() 메서드 삭제
}
```

---

### Step 2.3: Facade 서비스 삭제

**삭제 파일**: `server/src/main/java/moment/comment/service/facade/MyCommentPageFacadeService.java`

**파일 내용 (삭제 전 확인용)**:
```java
package moment.comment.service.facade;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCommentPageFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;

    public MyCommentPageResponse getMyCommentsPage(String nextCursor, int limit, Long commenterId) {
        // ...
    }

    public MyCommentPageResponse getUnreadMyCommentsPage(String nextCursor, int limit, Long commenterId) {
        // ...
    }

    private MyCommentPageResponse createMyCommentPage(CommentComposable commentComposable) {
        // ...
    }
}
```

**삭제 확인**: 다른 곳에서 사용되지 않음 (CommentController에서만 참조)

---

### Step 2.4: DTO 파일 삭제 (4개)

**삭제 파일 목록**:

#### 1. MyCommentPageResponse.java
**경로**: `server/src/main/java/moment/comment/dto/response/MyCommentPageResponse.java`

**사용처 확인** (삭제 가능):
- `CommentController.java` (삭제 예정)
- `MyCommentPageFacadeService.java` (삭제 예정)
- `CommentControllerTest.java` (삭제 예정)

#### 2. MyCommentsResponse.java
**경로**: `server/src/main/java/moment/comment/dto/response/MyCommentsResponse.java`

**사용처 확인** (삭제 가능):
- `MyCommentPageResponse.java` (삭제 예정)

#### 3. MyCommentResponse.java
**경로**: `server/src/main/java/moment/comment/dto/response/MyCommentResponse.java`

**사용처 확인** (삭제 가능):
- `MyCommentsResponse.java` (삭제 예정)
- `CommentControllerTest.java` (삭제 예정)

#### 4. MomentDetailResponse.java
**경로**: `server/src/main/java/moment/comment/dto/response/MomentDetailResponse.java`

**사용처 확인** (삭제 가능):
- `MyCommentResponse.java` (삭제 예정)

---

## 삭제 순서 (의존성 고려)

**권장 삭제 순서**:

1. **테스트 먼저 삭제** (의존성 없음)
   - `CommentControllerTest.java`의 테스트 메서드 2개 삭제

2. **Controller 메서드 삭제**
   - `CommentController.java`의 메서드 2개, 필드 1개, import 2개 삭제

3. **Facade 서비스 삭제**
   - `MyCommentPageFacadeService.java` 파일 삭제

4. **DTO 삭제** (역의존성 순서)
   - `MyCommentPageResponse.java` 삭제
   - `MyCommentsResponse.java` 삭제
   - `MyCommentResponse.java` 삭제
   - `MomentDetailResponse.java` 삭제

---

## 검증 계획

### 1. 빌드 & 테스트
```bash
cd server
./gradlew clean build    # 전체 빌드
./gradlew fastTest       # 빠른 테스트 (e2e 제외)
```

### 2. 삭제 확인

**기존 API 호출 시 404 반환 확인**:
```bash
# 삭제된 API - 404 반환 예상
curl -X GET "http://localhost:8080/api/v2/comments/me" \
  -H "Cookie: accessToken={token}"
# 응답: 404 Not Found

curl -X GET "http://localhost:8080/api/v2/comments/me/unread" \
  -H "Cookie: accessToken={token}"
# 응답: 404 Not Found
```

**새 API 정상 동작 확인**:
```bash
# 새 API - 200 반환 예상
curl -X GET "http://localhost:8080/api/v2/groups/1/my-comments" \
  -H "Cookie: accessToken={token}"
# 응답: 200 OK

curl -X GET "http://localhost:8080/api/v2/groups/1/my-comments/unread" \
  -H "Cookie: accessToken={token}"
# 응답: 200 OK
```

---

## Phase 2 파일 변경 요약

### 삭제 (5개)
| 파일 | 이유 |
|------|------|
| `MyCommentPageFacadeService.java` | deprecated API 전용 |
| `MyCommentPageResponse.java` | deprecated API 전용 |
| `MyCommentsResponse.java` | deprecated API 전용 |
| `MyCommentResponse.java` | deprecated API 전용 |
| `MomentDetailResponse.java` | deprecated API 전용 |

### 수정 (2개)
| 파일 | 변경 내용 |
|------|-----------|
| `CommentController.java` | 메서드 2개 삭제, 필드 1개 삭제, import 2개 삭제 |
| `CommentControllerTest.java` | 테스트 메서드 2개 삭제, import 1개 삭제 |

---

## Phase 2 구현 체크리스트

- [x] Step 2.1: CommentControllerTest.java 테스트 삭제 (2개)
  - [x] `나의_Comment_목록을_조회한다()` 삭제
  - [x] `나의_Comment_목록을_조회시_삭제된_모멘트는_비어있다()` 삭제
  - [x] import 삭제
- [x] Step 2.2: CommentController.java 수정
  - [x] `readMyComments()` 메서드 삭제
  - [x] `readMyUnreadComments()` 메서드 삭제
  - [x] `myCommentPageFacadeService` 필드 삭제
  - [x] import 삭제 (2개)
- [x] Step 2.3: MyCommentPageFacadeService.java 삭제
- [x] Step 2.4: DTO 파일 삭제 (4개)
  - [x] `MyCommentPageResponse.java` 삭제
  - [x] `MyCommentsResponse.java` 삭제
  - [x] `MyCommentResponse.java` 삭제
  - [x] `MomentDetailResponse.java` 삭제
- [x] 빌드 및 테스트 통과 확인
- [x] 기존 API 404 반환 확인
- [x] 새 API 정상 동작 확인