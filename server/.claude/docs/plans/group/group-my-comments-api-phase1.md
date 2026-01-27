# GET /api/v2/groups/{groupId}/my-comments API 구현 - Phase 1: 새 API 구현

> 원본 계획: `group-my-comments-api-enhancement.md`

---

## Phase 1: 새 API 구현 (기능 추가)

### Step 1.1: MyGroupCommentMomentResponse.java 생성

**파일 경로**: `server/src/main/java/moment/comment/dto/response/MyGroupCommentMomentResponse.java`

**참고 파일**:
- `comment/dto/response/MomentDetailResponse.java` (기존 패턴)
- `moment/dto/response/tobe/MomentComposition.java` (변환 소스)

**구현 내용**:
```java
package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.moment.dto.response.tobe.MomentComposition;

@Schema(description = "그룹 내 나의 Comment가 등록된 Moment 상세 내용")
public record MyGroupCommentMomentResponse(
        @Schema(description = "Moment 아이디", example = "1")
        Long id,

        @Schema(description = "Moment 내용", example = "테스트를 겨우 통과했어요!")
        String content,

        @Schema(description = "Moment 작성자 닉네임", example = "따뜻한 감성의 시리우스")
        String nickName,

        @Schema(description = "Moment 이미지 url", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "Moment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt
) {
    public static MyGroupCommentMomentResponse from(MomentComposition momentComposition) {
        return new MyGroupCommentMomentResponse(
                momentComposition.id(),
                momentComposition.content(),
                momentComposition.nickname(),
                momentComposition.imageUrl(),
                momentComposition.momentCreatedAt()
        );
    }
}
```

---

### Step 1.2: MyGroupCommentResponse.java 생성

**파일 경로**: `server/src/main/java/moment/comment/dto/response/MyGroupCommentResponse.java`

**참고 파일**:
- `comment/dto/response/MyCommentResponse.java` (기존 패턴)
- `comment/dto/tobe/CommentComposition.java` (변환 소스)

**CommentComposition 현재 필드** (확인됨):
```java
// comment/dto/tobe/CommentComposition.java
public record CommentComposition(
    Long id,              // 댓글 ID
    String content,       // 댓글 내용
    String nickname,      // 작성자 닉네임
    String imageUrl,      // 이미지 URL
    LocalDateTime commentCreatedAt,  // 생성 시간
    Long momentId         // 모멘트 ID
)
```

**구현 내용**:
```java
package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import moment.comment.dto.tobe.CommentComposition;
import moment.moment.dto.response.tobe.MomentComposition;

@Schema(description = "그룹 내 나의 Comment 단일 응답")
public record MyGroupCommentResponse(
        @Schema(description = "등록된 Comment id", example = "1")
        Long id,

        @Schema(description = "등록된 Comment 내용", example = "정말 멋진 하루군요!")
        String content,

        @Schema(description = "Comment 이미지 url", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "Comment 등록 시간", example = "2025-07-21T10:57:08.926954")
        LocalDateTime createdAt,

        @Schema(description = "Comment가 등록된 Moment")
        MyGroupCommentMomentResponse moment,

        @Schema(description = "내 코멘트 알림 정보")
        CommentNotificationResponse commentNotification
) {
    public static MyGroupCommentResponse of(
            CommentComposition commentComposition,
            MomentComposition momentComposition,
            List<Long> unreadNotificationIds
    ) {
        MyGroupCommentMomentResponse momentDetail = momentComposition == null
                ? null
                : MyGroupCommentMomentResponse.from(momentComposition);

        return new MyGroupCommentResponse(
                commentComposition.id(),
                commentComposition.content(),
                commentComposition.imageUrl(),
                commentComposition.commentCreatedAt(),
                momentDetail,
                CommentNotificationResponse.from(unreadNotificationIds)
        );
    }
}
```

---

### Step 1.3: MyGroupCommentFeedResponse.java 생성

**파일 경로**: `server/src/main/java/moment/comment/dto/response/MyGroupCommentFeedResponse.java`

**참고 파일**:
- `moment/dto/response/MyGroupFeedResponse.java` (Moment 패턴)
- `comment/dto/response/MyCommentPageResponse.java` (기존 Comment 패턴)

**구현 내용**:
```java
package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 내 나의 Comment 피드 응답")
public record MyGroupCommentFeedResponse(
        @Schema(description = "조회된 나의 Comment 목록")
        List<MyGroupCommentResponse> comments,

        @Schema(description = "다음 페이지 시작 커서", example = "123")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage
) {
    public static MyGroupCommentFeedResponse of(List<MyGroupCommentResponse> comments, Long nextCursor) {
        return new MyGroupCommentFeedResponse(comments, nextCursor, nextCursor != null);
    }

    public static MyGroupCommentFeedResponse empty() {
        return new MyGroupCommentFeedResponse(List.of(), null, false);
    }
}
```

---

### Step 2: CommentRepository에 그룹 기반 쿼리 추가

**파일 경로**: `server/src/main/java/moment/comment/infrastructure/CommentRepository.java`

**현재 상태**:
- commenter(User) 기반 쿼리만 존재
- member(GroupMember) 기반 쿼리 없음

**추가할 쿼리 (4개)**:

```java
// 그룹 내 나의 Comment 첫 페이지 조회 (member_id 기준)
@Query("""
        SELECT c
        FROM comments c
        LEFT JOIN FETCH c.member
        WHERE c.member.id = :memberId
        ORDER BY c.id DESC
        """)
List<Comment> findByMemberIdOrderByIdDesc(
        @Param("memberId") Long memberId,
        Pageable pageable);

// 그룹 내 나의 Comment 다음 페이지 조회 (커서 기반)
@Query("""
        SELECT c
        FROM comments c
        LEFT JOIN FETCH c.member
        WHERE c.member.id = :memberId AND c.id < :cursor
        ORDER BY c.id DESC
        """)
List<Comment> findByMemberIdAndIdLessThanOrderByIdDesc(
        @Param("memberId") Long memberId,
        @Param("cursor") Long cursor,
        Pageable pageable);

// 그룹 내 읽지 않은 나의 Comment 첫 페이지 조회
@Query("""
        SELECT c
        FROM comments c
        LEFT JOIN FETCH c.member
        WHERE c.member.id = :memberId AND c.id IN :commentIds
        ORDER BY c.id DESC
        """)
List<Comment> findByMemberIdAndIdInOrderByIdDesc(
        @Param("memberId") Long memberId,
        @Param("commentIds") List<Long> commentIds,
        Pageable pageable);

// 그룹 내 읽지 않은 나의 Comment 다음 페이지 조회 (커서 기반)
@Query("""
        SELECT c
        FROM comments c
        LEFT JOIN FETCH c.member
        WHERE c.member.id = :memberId AND c.id IN :commentIds AND c.id < :cursor
        ORDER BY c.id DESC
        """)
List<Comment> findByMemberIdAndIdInAndIdLessThanOrderByIdDesc(
        @Param("memberId") Long memberId,
        @Param("commentIds") List<Long> commentIds,
        @Param("cursor") Long cursor,
        Pageable pageable);
```

---

### Step 3: CommentService에 그룹 기반 메서드 추가

**파일 경로**: `server/src/main/java/moment/comment/service/comment/CommentService.java`

**추가할 메서드 (2개)**:

```java
public List<Comment> getMyCommentsInGroup(Long memberId, Long cursor, int limit) {
    PageRequest pageable = PageRequest.of(0, limit);
    if (cursor == null) {
        return commentRepository.findByMemberIdOrderByIdDesc(memberId, pageable);
    }
    return commentRepository.findByMemberIdAndIdLessThanOrderByIdDesc(memberId, cursor, pageable);
}

public List<Comment> getUnreadMyCommentsInGroup(
        Long memberId,
        List<Long> commentIds,
        Long cursor,
        int limit
) {
    PageRequest pageable = PageRequest.of(0, limit);
    if (cursor == null) {
        return commentRepository.findByMemberIdAndIdInOrderByIdDesc(memberId, commentIds, pageable);
    }
    return commentRepository.findByMemberIdAndIdInAndIdLessThanOrderByIdDesc(
            memberId, commentIds, cursor, pageable);
}
```

---

### Step 4: MyGroupCommentPageFacadeService.java 생성

**파일 경로**: `server/src/main/java/moment/comment/service/facade/MyGroupCommentPageFacadeService.java`

**참고 파일**:
- `moment/service/facade/MyGroupMomentPageFacadeService.java` (Moment 패턴)
- `comment/service/facade/MyCommentPageFacadeService.java` (기존 Comment Facade)

**의존성**:
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyGroupCommentPageFacadeService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CommentService commentService;
    private final CommentApplicationService commentApplicationService;
    private final GroupMemberService groupMemberService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;

    // ...
}
```

#### 4.1 getMyCommentsInGroup() 구현

```java
public MyGroupCommentFeedResponse getMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

    // 2. 그룹 내 나의 Comment 조회
    List<Comment> comments = commentService.getMyCommentsInGroup(
            member.getId(), cursor, DEFAULT_PAGE_SIZE);

    if (comments.isEmpty()) {
        return MyGroupCommentFeedResponse.empty();
    }

    return buildFeedResponse(comments, userId);
}
```

#### 4.2 getUnreadMyCommentsInGroup() 구현

```java
public MyGroupCommentFeedResponse getUnreadMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
    // 1. 그룹 멤버 검증
    GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

    // 2. 읽지 않은 Comment ID 목록 조회
    List<Long> unreadCommentIds = notificationApplicationService.getUnreadNotifications(
            userId, TargetType.COMMENT);

    if (unreadCommentIds == null || unreadCommentIds.isEmpty()) {
        return MyGroupCommentFeedResponse.empty();
    }

    // 3. 그룹 내 나의 unread Comment 조회
    List<Comment> comments = commentService.getUnreadMyCommentsInGroup(
            member.getId(), unreadCommentIds, cursor, DEFAULT_PAGE_SIZE);

    if (comments.isEmpty()) {
        return MyGroupCommentFeedResponse.empty();
    }

    return buildFeedResponse(comments, userId);
}
```

#### 4.3 buildFeedResponse() private 메서드

```java
private MyGroupCommentFeedResponse buildFeedResponse(List<Comment> comments, Long userId) {
    List<Long> commentIds = comments.stream().map(Comment::getId).toList();
    List<Long> momentIds = comments.stream().map(Comment::getMomentId).toList();

    // 1. Moment 정보 조회
    List<MomentComposition> momentCompositions =
            momentApplicationService.getMyMomentCompositionsBy(momentIds);
    Map<Long, MomentComposition> momentCompositionMap = momentCompositions.stream()
            .collect(Collectors.toMap(MomentComposition::id, m -> m));

    // 2. CommentComposition 조회 (momentIds 기반)
    List<CommentComposition> allCommentCompositions =
            commentApplicationService.getMyCommentCompositionsBy(momentIds);
    Map<Long, CommentComposition> commentCompositionMap = allCommentCompositions.stream()
            .filter(c -> commentIds.contains(c.id()))
            .collect(Collectors.toMap(CommentComposition::id, c -> c));

    // 3. 알림 정보 조회
    Map<Long, List<Long>> notificationsMap =
            notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
                    commentIds, TargetType.COMMENT);

    // 4. 응답 조합
    List<MyGroupCommentResponse> responses = comments.stream()
            .map(comment -> createMyGroupCommentResponse(
                    comment,
                    commentCompositionMap,
                    momentCompositionMap,
                    notificationsMap))
            .toList();

    // 5. 다음 커서 계산
    Long nextCursor = comments.size() < DEFAULT_PAGE_SIZE
            ? null
            : comments.get(comments.size() - 1).getId();

    return MyGroupCommentFeedResponse.of(responses, nextCursor);
}

private MyGroupCommentResponse createMyGroupCommentResponse(
        Comment comment,
        Map<Long, CommentComposition> commentCompositionMap,
        Map<Long, MomentComposition> momentCompositionMap,
        Map<Long, List<Long>> notificationsMap
) {
    Long commentId = comment.getId();
    CommentComposition composition = commentCompositionMap.get(commentId);
    MomentComposition momentComposition = momentCompositionMap.get(comment.getMomentId());
    List<Long> notificationIds = notificationsMap.getOrDefault(commentId, Collections.emptyList());

    // composition이 null이면 직접 생성
    if (composition == null) {
        composition = new CommentComposition(
                comment.getId(),
                comment.getContent(),
                comment.getMember() != null ? comment.getMember().getNickname() : null,
                null, // imageUrl - 별도 조회 필요시 추가
                comment.getCreatedAt(),
                comment.getMomentId()
        );
    }

    return MyGroupCommentResponse.of(composition, momentComposition, notificationIds);
}
```

---

### Step 5: GroupCommentController.java 수정

**파일 경로**: `server/src/main/java/moment/group/presentation/GroupCommentController.java`

#### 5.1 의존성 추가

```java
// 기존 의존성 유지
private final CommentApplicationService commentApplicationService;
private final CommentLikeService commentLikeService;

// 신규 추가
private final MyGroupCommentPageFacadeService myGroupCommentPageFacadeService;
```

#### 5.2 getMyComments() 신규 추가

```java
@Operation(summary = "그룹 내 나의 코멘트 조회",
        description = "그룹 내에서 자신이 작성한 코멘트를 조회합니다. 알림 정보가 포함됩니다.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "나의 코멘트 조회 성공"),
        @ApiResponse(responseCode = "401", description = """
                - [T-005] 토큰을 찾을 수 없습니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = """
                - [GM-002] 그룹 멤버가 아닙니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = """
                - [GR-001] 존재하지 않는 그룹입니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@GetMapping("/my-comments")
public ResponseEntity<SuccessResponse<MyGroupCommentFeedResponse>> getMyComments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupCommentFeedResponse response = myGroupCommentPageFacadeService.getMyCommentsInGroup(
            groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

#### 5.3 getUnreadMyComments() 신규 추가

```java
@Operation(summary = "그룹 내 읽지 않은 나의 코멘트 조회",
        description = "그룹 내에서 알림을 읽지 않은 자신의 코멘트를 조회합니다.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "읽지 않은 나의 코멘트 조회 성공"),
        @ApiResponse(responseCode = "401", description = """
                - [T-005] 토큰을 찾을 수 없습니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = """
                - [GM-002] 그룹 멤버가 아닙니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = """
                - [GR-001] 존재하지 않는 그룹입니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@GetMapping("/my-comments/unread")
public ResponseEntity<SuccessResponse<MyGroupCommentFeedResponse>> getUnreadMyComments(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId,
        @RequestParam(required = false) Long cursor) {
    MyGroupCommentFeedResponse response = myGroupCommentPageFacadeService.getUnreadMyCommentsInGroup(
            groupId, authentication.id(), cursor);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

---

### Step 6: 테스트 작성

> **기존 프로젝트 테스트 패턴 분석 결과 반영**
> - Facade 테스트: `@SpringBootTest` + `@Transactional` (Hybrid 패턴)
> - E2E 테스트: `@Tag(TestTags.E2E)` + RestAssured + DatabaseCleaner
> - 메서드 네이밍: 한글 + 언더스코어 (`@DisplayNameGeneration(ReplaceUnderscores.class)`)

#### 6.1 Facade 서비스 테스트 (Hybrid 패턴)

**파일 경로**: `server/src/test/java/moment/comment/service/facade/MyGroupCommentPageFacadeServiceTest.java`

**참고 패턴**: `NotificationFacadeServiceTest.java`

```java
@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class MyGroupCommentPageFacadeServiceTest {

    @Autowired
    private MyGroupCommentPageFacadeService facadeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User user;
    private Group group;
    private GroupMember member;

    @BeforeEach
    void setUp() {
        user = userRepository.save(UserFixture.createUser());
        group = groupRepository.save(GroupFixture.createGroup(user));
        member = groupMemberRepository.save(GroupMemberFixture.createMember(group, user, "닉네임"));
    }

    @Test
    void 그룹_내_나의_코멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        Comment comment = commentRepository.save(CommentFixture.createCommentWithMember(moment, user, member));

        // when
        MyGroupCommentFeedResponse response = facadeService.getMyCommentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.comments()).hasSize(1),
            () -> assertThat(response.comments().get(0).moment()).isNotNull(),
            () -> assertThat(response.comments().get(0).commentNotification()).isNotNull()
        );
    }

    @Test
    void 그룹_내_코멘트가_없으면_빈_응답을_반환한다() {
        // given
        // 코멘트 없음

        // when
        MyGroupCommentFeedResponse response = facadeService.getMyCommentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.comments()).isEmpty(),
            () -> assertThat(response.hasNextPage()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_있는_코멘트를_조회한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        Comment comment = commentRepository.save(CommentFixture.createCommentWithMember(moment, user, member));
        notificationRepository.save(NotificationFixture.createUnreadNotification(
            user, comment.getId(), TargetType.COMMENT));

        // when
        MyGroupCommentFeedResponse response = facadeService.getUnreadMyCommentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(response.comments()).hasSize(1),
            () -> assertThat(response.comments().get(0).commentNotification().isRead()).isFalse()
        );
    }

    @Test
    void 읽지_않은_알림이_없으면_빈_응답을_반환한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        Comment comment = commentRepository.save(CommentFixture.createCommentWithMember(moment, user, member));
        // 알림 없음 또는 모두 읽음 처리

        // when
        MyGroupCommentFeedResponse response = facadeService.getUnreadMyCommentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertThat(response.comments()).isEmpty();
    }

    @Test
    void 커서_기반_페이지네이션이_동작한다() {
        // given
        Moment moment = momentRepository.save(MomentFixture.createMoment(group, member));
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            comments.add(commentRepository.save(
                CommentFixture.createCommentWithMember(moment, user, member)));
        }

        // when - 첫 페이지
        MyGroupCommentFeedResponse firstPage = facadeService.getMyCommentsInGroup(
            group.getId(), user.getId(), null);

        // then
        assertAll(
            () -> assertThat(firstPage.comments()).hasSize(10),  // DEFAULT_PAGE_SIZE
            () -> assertThat(firstPage.hasNextPage()).isTrue(),
            () -> assertThat(firstPage.nextCursor()).isNotNull()
        );

        // when - 두 번째 페이지
        MyGroupCommentFeedResponse secondPage = facadeService.getMyCommentsInGroup(
            group.getId(), user.getId(), firstPage.nextCursor());

        // then
        assertAll(
            () -> assertThat(secondPage.comments()).hasSize(5),
            () -> assertThat(secondPage.hasNextPage()).isFalse()
        );
    }
}
```

#### 6.2 Controller E2E 테스트

**파일 경로**: 기존 `GroupCommentControllerTest.java`에 추가

```java
@Test
void 그룹_내_나의_코멘트를_조회한다() {
    // given
    String token = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "닉네임");
    GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "모멘트 내용");
    댓글_작성(token, group.groupId(), moment.momentId(), "첫 번째 댓글");
    댓글_작성(token, group.groupId(), moment.momentId(), "두 번째 댓글");

    // when
    MyGroupCommentFeedResponse response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", token)
        .when().get("/api/v2/groups/{groupId}/my-comments", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", MyGroupCommentFeedResponse.class);

    // then
    assertAll(
        () -> assertThat(response.comments()).hasSize(2),
        () -> assertThat(response.comments().get(0).moment()).isNotNull(),
        () -> assertThat(response.comments().get(0).commentNotification()).isNotNull()
    );
}

@Test
void 그룹_내_읽지_않은_나의_코멘트를_조회한다() {
    // given
    String token = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "닉네임");
    GroupMomentResponse moment = 모멘트_작성(token, group.groupId(), "모멘트 내용");
    댓글_작성(token, group.groupId(), moment.momentId(), "댓글 내용");

    // 다른 사용자가 댓글에 반응 (알림 생성)
    String otherToken = 로그인_토큰_생성(otherUser);
    그룹_가입(otherToken, group.groupId(), "다른닉네임");
    // 좋아요 등으로 알림 생성

    // when
    MyGroupCommentFeedResponse response = RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", token)
        .when().get("/api/v2/groups/{groupId}/my-comments/unread", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", MyGroupCommentFeedResponse.class);

    // then
    assertAll(
        () -> assertThat(response.comments()).hasSize(1),
        () -> assertThat(response.comments().get(0).commentNotification().isRead()).isFalse()
    );
}

@Test
void 그룹_멤버가_아니면_나의_코멘트_조회_시_예외가_발생한다() {
    // given
    String ownerToken = 로그인_토큰_생성(user);
    GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "닉네임");

    String nonMemberToken = 로그인_토큰_생성(nonMemberUser);

    // when & then
    RestAssured.given().log().all()
        .contentType(ContentType.JSON)
        .cookie("accessToken", nonMemberToken)
        .when().get("/api/v2/groups/{groupId}/my-comments", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.FORBIDDEN.value());
}
```

---

## 검증 계획

### 1. 빌드 & 테스트
```bash
cd server
./gradlew clean build    # 전체 빌드
./gradlew fastTest       # 빠른 테스트 (e2e 제외)
```

### 2. 수동 테스트

**나의 코멘트 조회**:
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-comments" \
  -H "Cookie: accessToken={token}"
```

**읽지 않은 나의 코멘트 조회**:
```bash
curl -X GET "http://localhost:8080/api/v2/groups/1/my-comments/unread" \
  -H "Cookie: accessToken={token}"
```

**예상 응답**:
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "comments": [
      {
        "id": 1,
        "content": "정말 멋진 하루군요!",
        "imageUrl": "https://example.com/image.jpg",
        "createdAt": "2025-07-21T10:57:08.926954",
        "moment": {
          "id": 1,
          "content": "오늘 하루는 힘든 하루~",
          "nickName": "따뜻한 감성의 시리우스",
          "imageUrl": "https://example.com/moment-image.jpg",
          "createdAt": "2025-07-21T09:00:00"
        },
        "commentNotification": {
          "isRead": false,
          "notificationIds": [101, 102]
        }
      }
    ],
    "nextCursor": null,
    "hasNextPage": false
  }
}
```

---

## Phase 1 파일 변경 요약

### 신규 생성 (4개)
| 파일 | 설명 |
|------|------|
| `MyGroupCommentMomentResponse.java` | Comment가 달린 Moment 정보 DTO |
| `MyGroupCommentResponse.java` | 그룹 내 나의 Comment 단일 응답 DTO |
| `MyGroupCommentFeedResponse.java` | 피드 응답 래퍼 |
| `MyGroupCommentPageFacadeService.java` | Facade 서비스 |

### 수정 (3개)
| 파일 | 변경 내용 |
|------|-----------|
| `CommentRepository.java` | 그룹 기반 쿼리 4개 추가 |
| `CommentService.java` | 그룹 기반 메서드 2개 추가 |
| `GroupCommentController.java` | 의존성 1개 추가, 엔드포인트 2개 추가 |

### 테스트 (신규)
| 파일 | 테스트 케이스 수 |
|------|-----------------|
| `MyGroupCommentPageFacadeServiceTest.java` | 5개 |
| `GroupCommentControllerTest.java` (추가) | 3개 |

---

## Phase 1 구현 체크리스트

- [x] Step 1.1: MyGroupCommentMomentResponse.java 생성
- [x] Step 1.2: MyGroupCommentResponse.java 생성
- [x] Step 1.3: MyGroupCommentFeedResponse.java 생성
- [x] Step 2: CommentRepository에 그룹 기반 쿼리 추가 (4개)
- [x] Step 3: CommentService에 그룹 기반 메서드 추가 (2개)
- [x] Step 4: MyGroupCommentPageFacadeService.java 생성
  - [x] getMyCommentsInGroup() 구현
  - [x] getUnreadMyCommentsInGroup() 구현
  - [x] buildFeedResponse() 구현
- [x] Step 5: GroupCommentController.java 수정
  - [x] 의존성 추가
  - [x] getMyComments() 신규 추가
  - [x] getUnreadMyComments() 신규 추가
- [x] Step 6: 테스트 작성
  - [x] Facade 테스트 (5개)
  - [x] E2E 테스트 (3개)
- [x] 빌드 및 테스트 통과 확인