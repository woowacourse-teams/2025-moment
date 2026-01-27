# Phase 4: 콘텐츠 관리 기능 구현 문서

## 개요
그룹 내 모멘트와 코멘트 조회/삭제 기능을 TDD 방식으로 구현합니다.

## 선행 작업
- Phase 1 완료 (기본 조회 API)
- Phase 2 완료 (`AdminGroupLog` 엔티티)

## 대상 API

| # | 엔드포인트 | 설명 |
|---|-----------|------|
| 1 | `GET /api/admin/groups/{groupId}/moments` | 모멘트 목록 조회 |
| 2 | `DELETE /api/admin/groups/{groupId}/moments/{momentId}` | 모멘트 삭제 |
| 3 | `GET /api/admin/groups/{groupId}/moments/{momentId}/comments` | 코멘트 목록 조회 |
| 4 | `DELETE /api/admin/groups/{groupId}/comments/{commentId}` | 코멘트 삭제 |

---

## TDD 테스트 목록

### 1. 모멘트 목록 조회 API (4개)

#### E2E 테스트 (`AdminMomentListApiTest`)
```
[ ] 모멘트_목록_조회_성공_그룹의_모멘트_반환
[ ] 모멘트_목록_조회_성공_페이지네이션_적용
[ ] 모멘트_목록_조회_성공_정렬_createdAt_DESC
[ ] 모멘트_목록_조회_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminContentServiceTest`)
```
[ ] getMoments_그룹의_모멘트_목록_반환
[ ] getMoments_페이지네이션_적용
[ ] getMoments_정렬_최신순
[ ] getMoments_삭제되지않은_모멘트만
[ ] getMoments_작성자_정보_포함
[ ] getMoments_그룹없으면_예외
```

---

### 2. 모멘트 삭제 API (5개)

#### E2E 테스트 (`AdminMomentDeleteApiTest`)
```
[ ] 모멘트_삭제_성공_SoftDelete
[ ] 모멘트_삭제_성공_해당_모멘트_코멘트_전체_SoftDelete
[ ] 모멘트_삭제_이미_삭제된_모멘트_삭제시_400
[ ] 모멘트_삭제_모멘트없으면_404_AC001
[ ] 모멘트_삭제_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminContentServiceTest`)
```
[ ] deleteMoment_모멘트_SoftDelete_성공
[ ] deleteMoment_해당_코멘트_전체_삭제
[ ] deleteMoment_이미_삭제된_모멘트_예외
[ ] deleteMoment_모멘트없으면_예외
[ ] deleteMoment_그룹없으면_예외
[ ] deleteMoment_AdminGroupLog_기록_확인
```

---

### 3. 코멘트 목록 조회 API (4개)

#### E2E 테스트 (`AdminCommentListApiTest`)
```
[ ] 코멘트_목록_조회_성공_모멘트의_코멘트_반환
[ ] 코멘트_목록_조회_성공_페이지네이션_적용
[ ] 코멘트_목록_조회_성공_정렬_createdAt_ASC
[ ] 코멘트_목록_조회_모멘트없으면_404
```

#### 서비스 단위 테스트 (`AdminContentServiceTest`)
```
[ ] getComments_모멘트의_코멘트_목록_반환
[ ] getComments_페이지네이션_적용
[ ] getComments_정렬_시간순
[ ] getComments_삭제되지않은_코멘트만
[ ] getComments_작성자_정보_포함
[ ] getComments_모멘트없으면_예외
```

---

### 4. 코멘트 삭제 API (3개)

#### E2E 테스트 (`AdminCommentDeleteApiTest`)
```
[ ] 코멘트_삭제_성공_SoftDelete
[ ] 코멘트_삭제_코멘트없으면_404_AC002
[ ] 코멘트_삭제_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminContentServiceTest`)
```
[ ] deleteComment_코멘트_SoftDelete_성공
[ ] deleteComment_이미_삭제된_코멘트_예외
[ ] deleteComment_코멘트없으면_예외
[ ] deleteComment_그룹없으면_예외
[ ] deleteComment_AdminGroupLog_기록_확인
```

---

## 생성/수정 파일 목록

### 신규 생성

#### DTO
```
src/main/java/moment/admin/dto/response/
├── AdminMomentListResponse.java
├── AdminMomentResponse.java
├── AdminMomentAuthorInfo.java
├── AdminCommentListResponse.java
├── AdminCommentResponse.java
└── AdminCommentAuthorInfo.java
```

#### Service
```
src/main/java/moment/admin/service/
└── AdminContentService.java
```

#### Test
```
src/test/java/moment/admin/presentation/
├── AdminMomentListApiTest.java
├── AdminMomentDeleteApiTest.java
├── AdminCommentListApiTest.java
└── AdminCommentDeleteApiTest.java

src/test/java/moment/admin/service/
└── AdminContentServiceTest.java
```

### 수정

```
src/main/java/moment/admin/presentation/
└── AdminGroupApiController.java  (콘텐츠 관리 엔드포인트 추가)

src/main/java/moment/moment/infrastructure/
└── MomentRepository.java  (메서드 추가)

src/main/java/moment/comment/infrastructure/
└── CommentRepository.java  (메서드 추가)

src/main/java/moment/global/exception/
└── ErrorCode.java  (AC-001, AC-002 추가)
```

---

## 시그니처 정의

### Response DTO

```java
// AdminMomentListResponse.java
public record AdminMomentListResponse(
    List<AdminMomentResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

// AdminMomentResponse.java
public record AdminMomentResponse(
    Long momentId,
    String content,
    String imageUrl,
    int commentCount,
    int likeCount,
    AdminMomentAuthorInfo author,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminMomentResponse from(Moment moment) {
        // ...
    }
}

// AdminMomentAuthorInfo.java
public record AdminMomentAuthorInfo(
    Long memberId,
    String groupNickname,
    Long userId,
    String userEmail,
    String userNickname
) {
    public static AdminMomentAuthorInfo from(GroupMember member) {
        // ...
    }
}

// AdminCommentListResponse.java
public record AdminCommentListResponse(
    List<AdminCommentResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

// AdminCommentResponse.java
public record AdminCommentResponse(
    Long commentId,
    String content,
    AdminCommentAuthorInfo author,
    LocalDateTime createdAt,
    LocalDateTime deletedAt
) {
    public static AdminCommentResponse from(Comment comment) {
        // ...
    }
}

// AdminCommentAuthorInfo.java
public record AdminCommentAuthorInfo(
    Long memberId,
    String groupNickname,
    Long userId,
    String userEmail,
    String userNickname
) {
    public static AdminCommentAuthorInfo from(GroupMember member) {
        // ...
    }
}
```

### Service

```java
// AdminContentService.java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminContentService {

    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final GroupRepository groupRepository;
    private final AdminGroupLogService adminGroupLogService;

    public AdminMomentListResponse getMoments(Long groupId, int page, int size);

    @Transactional
    public void deleteMoment(Long groupId, Long momentId, Long adminId);

    public AdminCommentListResponse getComments(Long groupId, Long momentId, int page, int size);

    @Transactional
    public void deleteComment(Long groupId, Long commentId, Long adminId);
}
```

### Controller (추가 엔드포인트)

```java
// AdminGroupApiController.java (추가)
@GetMapping("/{groupId}/moments")
public ResponseEntity<SuccessResponse<AdminMomentListResponse>> getMoments(
    @PathVariable Long groupId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
);

@DeleteMapping("/{groupId}/moments/{momentId}")
public ResponseEntity<SuccessResponse<Void>> deleteMoment(
    @PathVariable Long groupId,
    @PathVariable Long momentId,
    @AdminAuth Long adminId
);

@GetMapping("/{groupId}/moments/{momentId}/comments")
public ResponseEntity<SuccessResponse<AdminCommentListResponse>> getComments(
    @PathVariable Long groupId,
    @PathVariable Long momentId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
);

@DeleteMapping("/{groupId}/comments/{commentId}")
public ResponseEntity<SuccessResponse<Void>> deleteComment(
    @PathVariable Long groupId,
    @PathVariable Long commentId,
    @AdminAuth Long adminId
);
```

### ErrorCode 추가

```java
// ErrorCode.java (추가)
ADMIN_MOMENT_NOT_FOUND("AC-001", "모멘트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
ADMIN_COMMENT_NOT_FOUND("AC-002", "코멘트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
```

### Repository 메서드 추가

```java
// MomentRepository.java (추가)
Page<Moment> findByGroupIdAndDeletedAtIsNull(Long groupId, Pageable pageable);

@Query("SELECT m FROM Moment m WHERE m.id = :momentId AND m.group.id = :groupId")
Optional<Moment> findByIdAndGroupId(@Param("momentId") Long momentId, @Param("groupId") Long groupId);

// CommentRepository.java (추가)
Page<Comment> findByMomentIdAndDeletedAtIsNull(Long momentId, Pageable pageable);

@Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.moment.group.id = :groupId")
Optional<Comment> findByIdAndGroupId(@Param("commentId") Long commentId, @Param("groupId") Long groupId);

@Modifying
@Query("UPDATE Comment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.moment.id = :momentId AND c.deletedAt IS NULL")
int softDeleteByMomentId(@Param("momentId") Long momentId);
```

---

## 구현 순서

1. **ErrorCode 추가** → `AC-001`, `AC-002`
2. **DTO 생성** → Response DTO들
3. **Repository 메서드 추가**
4. **Service 구현** → `AdminContentService`
5. **Controller 확장** → 콘텐츠 관리 엔드포인트
6. **테스트 작성 및 통과**

---

## 비즈니스 로직 상세

### 모멘트 목록 조회
```java
public AdminMomentListResponse getMoments(Long groupId, int page, int size) {
    Group group = findGroupOrThrow(groupId);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Moment> moments = momentRepository.findByGroupIdAndDeletedAtIsNull(groupId, pageable);

    List<AdminMomentResponse> content = moments.getContent().stream()
        .map(AdminMomentResponse::from)
        .toList();

    return new AdminMomentListResponse(
        content,
        moments.getNumber(),
        moments.getSize(),
        moments.getTotalElements(),
        moments.getTotalPages()
    );
}
```

### 모멘트 삭제
```java
@Transactional
public void deleteMoment(Long groupId, Long momentId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    Moment moment = findMomentOrThrow(groupId, momentId);

    // 이미 삭제된 경우 예외
    if (moment.isDeleted()) {
        throw new MomentException(ErrorCode.ADMIN_MOMENT_ALREADY_DELETED);
    }

    // 1. 모멘트 soft delete
    moment.delete();

    // 2. 해당 모멘트의 코멘트 전체 soft delete
    commentRepository.softDeleteByMomentId(momentId);

    // 3. 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.MOMENT_DELETE, ...);
}
```

### 코멘트 목록 조회
```java
public AdminCommentListResponse getComments(Long groupId, Long momentId, int page, int size) {
    Group group = findGroupOrThrow(groupId);
    Moment moment = findMomentOrThrow(groupId, momentId);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    Page<Comment> comments = commentRepository.findByMomentIdAndDeletedAtIsNull(momentId, pageable);

    List<AdminCommentResponse> content = comments.getContent().stream()
        .map(AdminCommentResponse::from)
        .toList();

    return new AdminCommentListResponse(
        content,
        comments.getNumber(),
        comments.getSize(),
        comments.getTotalElements(),
        comments.getTotalPages()
    );
}
```

### 코멘트 삭제
```java
@Transactional
public void deleteComment(Long groupId, Long commentId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    Comment comment = findCommentOrThrow(groupId, commentId);

    // 이미 삭제된 경우 예외
    if (comment.isDeleted()) {
        throw new MomentException(ErrorCode.ADMIN_COMMENT_ALREADY_DELETED);
    }

    // 1. 코멘트 soft delete
    comment.delete();

    // 2. 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.COMMENT_DELETE, ...);
}
```

---

## 테스트 총 개수: ~16개

| 카테고리 | E2E | 단위 | 합계 |
|---------|-----|-----|------|
| 모멘트 목록 조회 | 4 | 6 | 10 |
| 모멘트 삭제 | 5 | 6 | 11 |
| 코멘트 목록 조회 | 4 | 6 | 10 |
| 코멘트 삭제 | 3 | 5 | 8 |
| **총계** | **16** | **23** | **39** |

> 참고: 실제 구현 시 테스트 케이스는 조정될 수 있습니다.
