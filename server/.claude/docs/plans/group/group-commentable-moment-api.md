# 구현 계획: 그룹별 코멘트 가능 모멘트 조회 API

## 목표
기존 `/api/v2/moments/commentable` 엔드포인트를 `/api/v2/groups/{groupId}/moments/commentable`로 변경하여 특정 그룹 내에서만 코멘트 가능한 모멘트를 조회하도록 수정

## 현재 상태 분석

### 현재 엔드포인트
- **URL**: `GET /api/v2/moments/commentable`
- **위치**: `MomentController.java:207-215`
- **기능**: 전체 모멘트 중 코멘트 가능한 모멘트를 랜덤으로 1개 반환

### 기존 비즈니스 로직
1. 7일 이내 생성된 모멘트만 대상
2. 본인 모멘트 제외 (`m.momenter.id <> :userId`)
3. 신고한 모멘트 제외
4. 이미 코멘트한 모멘트 제외
5. 랜덤 선택

---

## 수정 파일 목록

### 1. Repository Layer
**파일**: `server/src/main/java/moment/moment/infrastructure/MomentRepository.java`

**추가할 메서드**:
```java
@Query("""
    SELECT m.id FROM moments m
    WHERE
        m.group.id = :groupId
        AND m.momenter.id <> :userId
        AND m.createdAt >= :someDaysAgo
""")
List<Long> findMomentIdsInGroup(
        @Param("groupId") Long groupId,
        @Param("userId") Long userId,
        @Param("someDaysAgo") LocalDateTime someDaysAgo);

@Query("""
    SELECT m.id FROM moments m
    WHERE
        m.group.id = :groupId
        AND m.momenter.id <> :userId
        AND m.createdAt >= :someDaysAgo
        AND m.id NOT IN :reportedMoments
""")
List<Long> findMomentIdsInGroupExcludingReported(
        @Param("groupId") Long groupId,
        @Param("userId") Long userId,
        @Param("someDaysAgo") LocalDateTime someDaysAgo,
        @Param("reportedMoments") List<Long> reportedMoments);
```

### 2. Domain Service Layer
**파일**: `server/src/main/java/moment/moment/service/moment/MomentService.java`

**추가할 메서드**:
```java
public List<Moment> getCommentableMomentsInGroup(Long groupId, User user, List<Long> reportedMomentIds) {
    LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(COMMENTABLE_PERIOD_IN_DAYS);

    List<Long> momentIds;
    if (reportedMomentIds == null || reportedMomentIds.isEmpty()) {
        momentIds = momentRepository.findMomentIdsInGroup(groupId, user.getId(), cutoffDateTime);
    } else {
        momentIds = momentRepository.findMomentIdsInGroupExcludingReported(
                groupId, user.getId(), cutoffDateTime, reportedMomentIds);
    }

    if (momentIds.isEmpty()) {
        return Collections.emptyList();
    }

    int randomIndex = RANDOM.nextInt(momentIds.size());
    Long randomId = momentIds.get(randomIndex);

    return momentRepository.findById(randomId)
            .map(Collections::singletonList)
            .orElse(Collections.emptyList());
}
```

### 3. Application Service Layer
**파일**: `server/src/main/java/moment/moment/service/application/MomentApplicationService.java`

**추가할 메서드**:
```java
public List<Long> getCommentableMomentInGroup(Long groupId, Long userId) {
    User user = userService.getUserBy(userId);
    List<Long> reportedMomentIds = reportService.getReportedMomentIdsBy(user.getId());
    List<Moment> commentableMoments = momentService.getCommentableMomentsInGroup(groupId, user, reportedMomentIds);
    return commentableMoments.stream().map(Moment::getId).toList();
}
```

### 4. Facade Service Layer
**파일**: `server/src/main/java/moment/moment/service/facade/CommentableMomentFacadeService.java`

**수정사항**:
1. `GroupMemberService` 의존성 추가
2. 새 메서드 추가:

```java
private final GroupMemberService groupMemberService;

public CommentableMomentResponse getCommentableMomentInGroup(Long groupId, Long commenterId) {
    // 그룹 멤버십 검증 (비멤버는 GM-002 에러)
    groupMemberService.getByGroupAndUser(groupId, commenterId);

    List<Long> momentIds = momentApplicationService.getCommentableMomentInGroup(groupId, commenterId);
    List<Long> momentIdsNotCommentedByMe = commentApplicationService.getMomentIdsNotCommentedByMe(momentIds, commenterId);
    return momentApplicationService.pickRandomMomentComposition(momentIdsNotCommentedByMe);
}
```

### 5. Controller Layer
**파일**: `server/src/main/java/moment/group/presentation/GroupMomentController.java`

**수정사항**:
1. `CommentableMomentFacadeService` 의존성 추가
2. `CommentableMomentResponse` import 추가
3. 새 엔드포인트 추가:

```java
@Operation(summary = "코멘트를 달 수 있는 그룹 모멘트 조회", description = "그룹 내에서 코멘트를 달 수 있는 모멘트를 랜덤으로 조회합니다.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "코멘트 가능 모멘트 조회 성공"),
        @ApiResponse(responseCode = "401", description = """
                - [T-005] 토큰을 찾을 수 없습니다.
                """, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = """
                - [GM-002] 그룹 멤버가 아닙니다.
                """, content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = """
                - [GR-001] 존재하지 않는 그룹입니다.
                """, content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
@GetMapping("/moments/commentable")
public ResponseEntity<SuccessResponse<CommentableMomentResponse>> getCommentableMoment(
        @AuthenticationPrincipal Authentication authentication,
        @PathVariable Long groupId) {
    CommentableMomentResponse response = commentableMomentFacadeService.getCommentableMomentInGroup(
        groupId, authentication.id());
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

---

## 기존 엔드포인트 삭제 (확정)

기존 `/api/v2/moments/commentable` 엔드포인트와 관련 테스트를 삭제합니다.

### 삭제 대상 파일/코드

1. **MomentController.java** (line 207-215)
   - `readCommentableMoment()` 메서드 삭제

2. **MomentControllerTest.java**
   - `코멘트를_작성할_수_있는_모멘트를_조회한다()` 테스트 삭제 (line 443-468)
   - `코멘트를_작성할_수_있는_이미지를_포함한_모멘트를_조회한다()` 테스트 삭제 (line 471-503)

3. **CommentableMomentFacadeService.java**
   - 기존 `getCommentableMoment(Long commenterId)` 메서드는 유지 (새 메서드에서 내부적으로 재사용 가능)
   - 또는 새 메서드로 대체 후 삭제 가능

---

## 테스트 계획

### 1. E2E 테스트 추가
**파일**: `server/src/test/java/moment/group/presentation/GroupMomentControllerTest.java`

**추가할 테스트 케이스**:

#### 테스트 1: 그룹 내 코멘트 가능 모멘트 조회 성공
```java
@Test
void 그룹_내_코멘트를_작성할_수_있는_모멘트를_조회한다() {
    // given
    User user1 = UserFixture.createUser();
    User savedUser1 = userRepository.save(user1);
    String token1 = tokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

    User user2 = UserFixture.createUser();
    User savedUser2 = userRepository.save(user2);
    String token2 = tokenManager.createAccessToken(savedUser2.getId(), savedUser2.getEmail());

    // 그룹 생성 (user1이 그룹장)
    GroupCreateResponse group = 그룹_생성(token1, "테스트 그룹", "설명", "그룹장닉네임");

    // user2를 그룹에 초대 (초대 코드 사용)
    그룹_가입(token2, group.groupId(), group.invitationCode(), "멤버닉네임");

    // user1이 모멘트 작성
    모멘트_작성(token1, group.groupId(), "코멘트 가능한 모멘트");

    // when - user2가 코멘트 가능한 모멘트 조회
    CommentableMomentResponse response = RestAssured.given().log().all()
        .cookie("accessToken", token2)
        .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", CommentableMomentResponse.class);

    // then
    assertAll(
        () -> assertThat(response.id()).isNotNull(),
        () -> assertThat(response.content()).isEqualTo("코멘트 가능한 모멘트")
    );
}
```

#### 테스트 2: 그룹 멤버가 아닌 경우 실패
```java
@Test
void 그룹_멤버가_아닌_경우_코멘트_가능_모멘트_조회_실패() {
    // given
    User groupOwner = UserFixture.createUser();
    User savedOwner = userRepository.save(groupOwner);
    String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

    User nonMember = UserFixture.createUser();
    User savedNonMember = userRepository.save(nonMember);
    String nonMemberToken = tokenManager.createAccessToken(savedNonMember.getId(), savedNonMember.getEmail());

    GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

    // when & then
    RestAssured.given().log().all()
        .cookie("accessToken", nonMemberToken)
        .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.FORBIDDEN.value());
}
```

#### 테스트 3: 본인 모멘트 제외 확인
```java
@Test
void 본인_모멘트는_코멘트_가능_모멘트에서_제외된다() {
    // given - 그룹에 혼자만 있고, 본인 모멘트만 있는 경우
    User user = UserFixture.createUser();
    User savedUser = userRepository.save(user);
    String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

    GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");
    모멘트_작성(token, group.groupId(), "내가 작성한 모멘트");

    // when
    CommentableMomentResponse response = RestAssured.given().log().all()
        .cookie("accessToken", token)
        .when().get("/api/v2/groups/{groupId}/moments/commentable", group.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", CommentableMomentResponse.class);

    // then - null 응답 (본인 모멘트 제외)
    assertThat(response).isNull();
}
```

#### 테스트 4: 다른 그룹 모멘트는 조회 안됨
```java
@Test
void 다른_그룹의_모멘트는_조회되지_않는다() {
    // given
    User user1 = UserFixture.createUser();
    User savedUser1 = userRepository.save(user1);
    String token1 = tokenManager.createAccessToken(savedUser1.getId(), savedUser1.getEmail());

    User user2 = UserFixture.createUser();
    User savedUser2 = userRepository.save(user2);
    String token2 = tokenManager.createAccessToken(savedUser2.getId(), savedUser2.getEmail());

    // 그룹 A 생성 (user1)
    GroupCreateResponse groupA = 그룹_생성(token1, "그룹 A", "설명", "닉네임A");

    // 그룹 B 생성 (user2)
    GroupCreateResponse groupB = 그룹_생성(token2, "그룹 B", "설명", "닉네임B");

    // user2가 그룹 B에 모멘트 작성
    모멘트_작성(token2, groupB.groupId(), "그룹 B의 모멘트");

    // when - user1이 그룹 A에서 코멘트 가능 모멘트 조회
    CommentableMomentResponse response = RestAssured.given().log().all()
        .cookie("accessToken", token1)
        .when().get("/api/v2/groups/{groupId}/moments/commentable", groupA.groupId())
        .then().log().all()
        .statusCode(HttpStatus.OK.value())
        .extract()
        .jsonPath()
        .getObject("data", CommentableMomentResponse.class);

    // then - 그룹 A에는 모멘트가 없으므로 null
    assertThat(response).isNull();
}
```

### 2. 테스트 헬퍼 메서드 추가 필요
```java
private void 그룹_가입(String token, Long groupId, String invitationCode, String nickname) {
    // 그룹 가입 API 호출 로직
}
```

---

## 구현 순서

1. **Repository** - 새 쿼리 메서드 2개 추가
2. **MomentService** - `getCommentableMomentsInGroup()` 메서드 추가
3. **MomentApplicationService** - `getCommentableMomentInGroup()` 메서드 추가
4. **CommentableMomentFacadeService** - `GroupMemberService` 의존성 및 새 메서드 추가
5. **GroupMomentController** - 새 엔드포인트 추가
6. **GroupMomentControllerTest** - E2E 테스트 추가
7. **MomentController** - 기존 `readCommentableMoment()` 메서드 삭제
8. **MomentControllerTest** - 기존 commentable 관련 테스트 2개 삭제

---

## 검증 방법

1. 빌드 확인: `./gradlew build`
2. 테스트 실행: `./gradlew test` 또는 `./gradlew e2eTest`
3. 수동 테스트:
   - 그룹 생성 후 멤버 2명 가입
   - 멤버 A가 모멘트 작성
   - 멤버 B가 `/api/v2/groups/{groupId}/moments/commentable` 호출
   - 멤버 A의 모멘트가 반환되는지 확인