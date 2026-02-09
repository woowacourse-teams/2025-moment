# 유저 차단 기능 구현 계획

## Context

사용자가 다른 회원을 차단하면 서로의 모멘트와 댓글이 조회되지 않고, 알림도 차단되어야 한다.
현재 그룹 기반 피드에서 모멘트/댓글을 조회하는 쿼리들에 차단 필터링을 추가해야 한다.

### 요구사항 결정 사항
- **차단 방향**: 양방향 (A가 B를 차단하면 A↔B 서로 콘텐츠 비노출)
- **숨김 범위**: 모멘트 + 댓글 모두
- **알림 차단**: 차단된 사용자의 알림도 수신하지 않음
- **생성 차단**: 차단된 사용자 간 댓글/좋아요 생성도 차단 (조회 필터링뿐 아니라 쓰기도 차단)
- **모듈 위치**: `src/main/java/moment/block/` 독립 최상위 모듈 (기존 `report/`, `like/`와 동일 레벨)

### 정책 결정 사항

> **2차 리뷰 반영**: 5명 리뷰어(아키텍처/DB/보안/도메인/반론)의 리뷰를 통해 아래 정책을 명시.

| 항목 | 정책 | 근거 |
|------|------|------|
| **차단 해제 권한** | 차단자만 해제 가능. 피차단자는 별도로 (B→A) 차단/해제 가능하나 원본 (A→B) 차단에는 영향 없음 | 단일 row 양방향 설계의 일관성 |
| **그룹 내 차단** | 멤버 목록은 차단과 무관하게 전체 표시. 피드/댓글만 필터링. 그룹 오너의 관리 기능(승인/강퇴)은 차단과 무관하게 동작 | 그룹 운영 기능과 개인 차단은 별개 관심사 |
| **기존 알림** | 소급 필터링 없음. 차단 이후 새 알림만 차단. 차단 기간 중 억제된 알림은 해제 후에도 복원되지 않음 | 과도한 복잡성 방지 |
| **댓글 count** | 차단 필터 적용 (모멘트 목록의 "댓글 N개" 표시와 실제 노출 댓글 수 일치) | UX 일관성 |
| **좋아요 count** | 차단 필터 미적용 (차단 사용자의 좋아요도 count에 포함) | 좋아요는 익명 집계이므로 개별 사용자를 노출하지 않음. 차단 해제 시에도 count 변동 없어 자연스러움 |
| **댓글 Java 필터링** | 댓글은 단일 모멘트의 전체 댓글을 로드 (페이지네이션 없음)하므로 Java 필터링 채택. DB 필터링 변경 시 `CommentService.getAllByMomentIds()` 모든 호출부에 영향 | 변경 범위 최소화 |

---

## Phase 1: Block 도메인 생성

### 1-1. Flyway 마이그레이션

**MySQL**: `src/main/resources/db/migration/mysql/V38__create_user_blocks.sql`

```sql
CREATE TABLE user_blocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    blocker_id BIGINT NOT NULL,
    blocked_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT uq_user_blocks_blocker_blocked UNIQUE (blocker_id, blocked_user_id),
    CONSTRAINT fk_user_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id),
    CONSTRAINT fk_user_blocks_blocked FOREIGN KEY (blocked_user_id) REFERENCES users(id)
);

CREATE INDEX idx_user_blocks_blocked_user ON user_blocks (blocked_user_id);
```

> **리뷰 반영 사항:**
> - `created_at`에 `NOT NULL` 추가 (BaseEntity 매핑 일치, 프로젝트 컨벤션)
> - `deleted_at`에 `DEFAULT NULL` 명시 (V32 컨벤션 일치)
> - FK에 명시적 이름 부여 (`fk_user_blocks_blocker`, `fk_user_blocks_blocked`)
> - UNIQUE KEY 접두사 `uq_` 사용 (`uq_user_blocks_blocker_blocked`) — 기존 V1, V12, V28, V32 컨벤션 일치
> - `idx_blocker_id` 제거 (UNIQUE KEY의 leftmost prefix와 완전 중복)
> - 인덱스 생성을 CREATE TABLE 외부로 분리 (V32 컨벤션 일치)

**H2 (테스트)**: `src/test/resources/db/migration/h2/V38__create_user_blocks__h2.sql`

MySQL 버전과 동일 내용으로 작성 (H2 MODE=MySQL에서 호환).

### 1-2. UserBlock 엔티티

**파일**: `src/main/java/moment/block/domain/UserBlock.java`

```
UserBlock extends BaseEntity
├── id (Long, PK, auto-generated)
├── blocker (User, ManyToOne LAZY)
├── blockedUser (User, ManyToOne LAZY)
├── deletedAt (LocalDateTime)
├── @UniqueConstraint(blocker_id, blocked_user_id)
├── @SQLDelete / @SQLRestriction (Soft Delete)
│
├── restore()      // deletedAt = null (MomentLike 패턴 참고)
└── isDeleted()    // deletedAt != null
```

> **리뷰 반영**: `restore()`, `isDeleted()` 도메인 메서드 명시 추가 (기존 `MomentLike` 선례)

### 1-3. UserBlockRepository

**파일**: `src/main/java/moment/block/infrastructure/UserBlockRepository.java`

주요 메서드:
- `Optional<UserBlock> findByBlockerAndBlockedUser(User blocker, User blockedUser)` - 차단 존재 확인
- `boolean existsByBlockerAndBlockedUser(User blocker, User blockedUser)` - 존재 여부
- `List<UserBlock> findAllByBlocker(User blocker)` - 내가 차단한 목록
- **양방향 차단 사용자 ID 조회** (native query):
  ```sql
  SELECT blocked_user_id FROM user_blocks WHERE blocker_id = :userId AND deleted_at IS NULL
  UNION ALL
  SELECT blocker_id FROM user_blocks WHERE blocked_user_id = :userId AND deleted_at IS NULL
  ```
- `findByBlockerAndBlockedUserIncludeDeleted` (native query) - soft delete 된 것 포함 (재차단 시 restore용)

> **리뷰 반영**: `UNION` → `UNION ALL` (A→B 차단 시 B→A를 별도 생성하지 않으므로 중복 불가, 중복 제거 비용 제거)

### 1-4. ErrorCode 추가

`ErrorCode.java`에 추가:
- `BLOCK_SELF("BL-001", "자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST)`
- `BLOCK_ALREADY_EXISTS("BL-002", "이미 차단된 사용자입니다.", HttpStatus.CONFLICT)`
- `BLOCK_NOT_FOUND("BL-003", "차단 관계가 존재하지 않습니다.", HttpStatus.NOT_FOUND)`
- `BLOCKED_USER_INTERACTION("BL-004", "차단된 사용자와 상호작용할 수 없습니다.", HttpStatus.FORBIDDEN)`

> **2차 리뷰 반영**: BL-004 추가 — Phase 5(생성 차단)에서 사용

---

## Phase 2: Block 서비스 레이어

### 2-1. UserBlockService (도메인 서비스)

**파일**: `src/main/java/moment/block/service/block/UserBlockService.java`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockService {

    @Transactional
    public UserBlock block(User blocker, User blockedUser)
    // 1. 자기 자신 차단 방지 (blocker.getId().equals(blockedUser.getId()))
    // 2. 활성 차단 존재 확인 (existsByBlockerAndBlockedUser - @SQLRestriction 적용)
    // 3. soft delete된 레코드 확인 (findByBlockerAndBlockedUserIncludeDeleted - native query)
    //    - 있으면 restore() 호출
    //    - 없으면 새로 생성

    @Transactional
    public void unblock(User blocker, User blockedUser)
    // - 차단 관계 조회, 없으면 예외
    // - soft delete

    public List<Long> getBlockedUserIds(Long userId)
    // - 양방향 차단 사용자 ID 목록 반환 (UNION ALL 쿼리)
    // - 빈 목록이면 Collections.emptyList() 반환

    public boolean isBlocked(Long userId1, Long userId2)
    // - 양방향 차단 여부 확인

    public List<UserBlock> getBlockedUsers(User blocker)
    // - 내가 차단한 사용자 목록 (차단 목록 조회 API용)
}
```

> **향후 확장 참고**: 차단 이벤트 발행(`UserBlockEvent`)이 필요해지면 `UserBlockFacadeService` 추가

### 2-2. UserBlockApplicationService (애플리케이션 서비스)

**파일**: `src/main/java/moment/block/service/application/UserBlockApplicationService.java`

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockApplicationService {
    private final UserService userService;
    private final UserBlockService userBlockService;

    @Transactional
    public UserBlockResponse blockUser(Long blockerId, Long blockedUserId)
    // - UserService로 두 사용자 조회
    // - UserBlockService.block() 호출
    // - UserBlockResponse 반환

    @Transactional
    public void unblockUser(Long blockerId, Long blockedUserId)
    // - UserService로 두 사용자 조회
    // - UserBlockService.unblock() 호출

    public List<Long> getBlockedUserIds(Long userId)
    // - UserBlockService.getBlockedUserIds() 위임

    public boolean isBlocked(Long userId1, Long userId2)
    // - UserBlockService.isBlocked() 위임

    public List<UserBlockListResponse> getBlockedUsers(Long userId)
    // - 차단 목록 반환
}
```

### 2-3. DTO

**Request**: 별도 DTO 불필요 (path variable로 userId 전달)

**Response** (`@Schema` 어노테이션 포함):
- `UserBlockResponse` - 차단 결과 (`@Schema` + blockedUserId, createdAt)
- `UserBlockListResponse` - 차단 목록 (`@Schema` + blockedUserId, nickname, createdAt)

### 2-4. Controller

**파일**: `src/main/java/moment/block/presentation/UserBlockController.java`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v2/users/{userId}/blocks` | 사용자 차단 |
| DELETE | `/api/v2/users/{userId}/blocks` | 사용자 차단 해제 |
| GET | `/api/v2/users/blocks` | 차단 목록 조회 |

> **리뷰 반영**: `/block` → `/blocks` 복수형으로 통일 (REST 리소스 네이밍 컨벤션)

인증 패턴: `@AuthenticationPrincipal Authentication authentication`으로 blocker ID 획득, `{userId}`는 차단 대상 사용자 ID.

---

## Phase 3: 모멘트 피드 필터링

차단된 사용자의 모멘트를 피드에서 제외한다.

### 3-1. MomentRepository 수정 - 기존 쿼리에 파라미터 통합

**파일**: `src/main/java/moment/moment/infrastructure/MomentRepository.java`

> **리뷰 반영**: 쿼리 6개 추가 대신, 기존 쿼리에 `blockedUserIds` 파라미터를 통합하여 **조합 폭발(combinatorial explosion) 방지**.

**수정 대상 쿼리 (기존 쿼리의 시그니처와 JPQL 변경)**:

| 기존 메서드 | 변경 내용 |
|------------|----------|
| `findByGroupIdOrderByIdDesc` | `blockedUserIds` 파라미터 추가 + `AND m.momenter.id NOT IN :blockedUserIds` |
| `findByGroupIdAndIdLessThanOrderByIdDesc` | 동일 |
| `findMomentIds` | 동일 |
| `findMomentIdsExcludingReported` | 동일 |
| `findMomentIdsInGroup` | 동일 |
| `findMomentIdsInGroupExcludingReported` | 동일 |

패턴 예시:
```java
// AS-IS
@Query("SELECT m FROM moments m WHERE m.group.id = :groupId ORDER BY m.id DESC")
List<Moment> findByGroupIdOrderByIdDesc(@Param("groupId") Long groupId, Pageable pageable);

// TO-BE: blockedUserIds 파라미터 통합
@Query("""
    SELECT m FROM moments m
    WHERE m.group.id = :groupId
      AND m.momenter.id NOT IN :blockedUserIds
    ORDER BY m.id DESC
    """)
List<Moment> findByGroupIdOrderByIdDesc(
    @Param("groupId") Long groupId,
    @Param("blockedUserIds") List<Long> blockedUserIds,
    Pageable pageable);
```

> **호출부 전수 검사 필요**: 시그니처 변경 시 컴파일러가 미수정 호출부를 감지하므로, 빌드 확인으로 누락 방지.

### 3-2. MomentService 수정

**파일**: `src/main/java/moment/moment/service/moment/MomentService.java`

- `getByGroup()` - blockedUserIds 파라미터 추가
- `getCommentableMomentIdsInGroup()` - blockedUserIds 파라미터 추가

빈 리스트 처리 패턴:
```java
private static final List<Long> EMPTY_BLOCK_LIST = List.of(-1L); // AUTO_INCREMENT PK이므로 -1은 존재 불가

// 빈 리스트일 때 더미값으로 치환하여 단일 쿼리로 처리
List<Long> safeBlockedUserIds = blockedUserIds.isEmpty() ? EMPTY_BLOCK_LIST : blockedUserIds;
return momentRepository.findByGroupIdOrderByIdDesc(groupId, safeBlockedUserIds, pageable);
```

> **2차 리뷰 반영**:
> - `List.of(-1L)` → `EMPTY_BLOCK_LIST` 상수 추출 (매직 넘버 제거)
> - 기존 `reportedMomentIds`의 `isEmpty()` 분기 패턴과 다르지만, 두 필터를 조합하면 4가지 쿼리 조합이 필요하므로 더미값 패턴이 조합 폭발 방지에 적합
> - **TODO**: Hibernate 6 (Spring Boot 3.5.3)에서 빈 리스트 `NOT IN` 동작을 검증하여 더미값 제거 가능 여부 확인
> - `getCommentableMoments()` (비그룹 버전, line 59)는 프로덕션 호출부 없는 dead code이므로 수정 불필요. 별도 정리 이슈로 등록

> **참고**: `getMyMomentsInGroup()`, `getUnreadMyMomentsInGroup()`은 내 모멘트만 조회하므로 차단 필터링 불필요. 단, 내 모멘트에 달린 댓글의 필터링은 Phase 4에서 처리.

### 3-3. MomentApplicationService 수정

**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

- `UserBlockApplicationService` 의존성 추가
- `getGroupMoments()` 내에서:
  1. `userBlockApplicationService.getBlockedUserIds(userId)` **한 번만** 조회
  2. `MomentService.getByGroup()`에 `blockedUserIds` 전달
  3. 댓글 count 계산 시에도 동일한 `blockedUserIds`를 활용 (Phase 4-4 참조)
- `getCommentableMomentIdsInGroup()` - blockedUserIds 조회 후 MomentService에 전달

> **2차 리뷰 반영**: `getBlockedUserIds()`를 상위에서 한 번 조회하고 파라미터로 전달하여 동일 요청 내 중복 DB 호출 방지. request-scope 캐시 대신 명시적 파라미터 전달 방식 채택 (단순성 우선).

### 3-4. CommentableMomentFacadeService 수정 불필요

`MomentApplicationService.getCommentableMomentIdsInGroup()` 내부에서 처리하므로 facade 수정 불필요.

---

## Phase 4: 댓글 필터링

차단된 사용자의 댓글을 조회에서 제외한다.

> **설계 근거 (2차 리뷰 반영)**: 댓글은 단일 모멘트의 전체 댓글을 로드 (페이지네이션 없음)하므로 Java 필터링 채택. DB 쿼리 필터링으로 변경하면 `CommentService.getAllByMomentIds()`의 모든 호출부에 영향이 가므로 변경 범위가 과도함. 모멘트(Phase 3)의 DB 필터링과 접근 방식이 다르지만 의도적 선택.

### 4-1. CommentApplicationService 수정

**파일**: `src/main/java/moment/comment/service/application/CommentApplicationService.java`

- `UserBlockApplicationService` 의존성 추가

**수정 메서드**:

1. `getCommentsInGroup()` (line 196) - 댓글 조회 후 Java에서 필터:
   ```java
   List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
   Set<Long> blockedUserIdSet = new HashSet<>(blockedUserIds);
   comments = comments.stream()
       .filter(c -> !blockedUserIdSet.contains(c.getCommenter().getId()))
       .toList();
   ```

2. `getMyCommentCompositionsBy()` (line 49) - 호출하는 쪽(Facade)에서 필터링 처리

> **리뷰 반영**: `List.contains()` O(n) → `Set<Long>` O(1) 해시 룩업으로 성능 개선

### 4-2. MyGroupMomentPageFacadeService 수정

**파일**: `src/main/java/moment/moment/service/facade/MyGroupMomentPageFacadeService.java`

- `UserBlockApplicationService` 의존성 추가
- `buildMomentListResponse()` 내에서:
  1. `userBlockApplicationService.getBlockedUserIds(userId)` **한 번** 조회
  2. `getMyCommentCompositionsBy()` 결과에서 `Set<Long> blockedUserIdSet`에 해당하는 댓글 제거

> **2차 리뷰 반영**: `getBlockedUserIds()`를 facade 상위에서 한 번 조회하여 하위 서비스 호출에 재사용.

### 4-3. MyGroupCommentPageFacadeService 수정

> **2차 리뷰 반영 (보안/도메인 리뷰어 독립 발견)**: 기존 계획에서 누락되었던 "내가 받은 댓글" 페이지의 차단 필터링 추가.

**파일**: `src/main/java/moment/moment/service/facade/MyGroupCommentPageFacadeService.java`

- `UserBlockApplicationService` 의존성 추가
- `buildCommentListResponse()` 내에서:
  1. `userBlockApplicationService.getBlockedUserIds(userId)` 조회
  2. `commentApplicationService.getMyCommentCompositionsBy(momentIds)` 결과에서 차단된 사용자의 댓글 제거
  3. `momentApplicationService.getMyMomentCompositionsBy(momentIds)` 결과는 내 모멘트이므로 필터링 불필요

### 4-4. 댓글 count 보정

> **2차 리뷰 반영 (도메인 리뷰어 단독 발견)**: 모멘트 목록의 "댓글 N개" 표시가 실제 노출 댓글 수와 불일치하는 문제 해결.

**문제**: `MomentApplicationService.getGroupMoments()` 내 `commentService.countByMomentId(moment.getId())`는 차단 사용자의 댓글도 포함하여 카운트. "댓글 5개"로 표시되나 필터링 후 3개만 보임.

**해결 방안**: `getGroupMoments()`에서 이미 조회한 `blockedUserIds`를 활용하여 댓글 count를 보정.
- 방안 A: `countByMomentId` 쿼리에 `blockedUserIds` 조건 추가 (DB 레벨)
- 방안 B: 댓글 목록을 가져온 후 Java에서 필터링된 결과의 size() 사용

> **선택**: 방안 A (DB 레벨) — count 쿼리에 `AND c.commenter.id NOT IN :blockedUserIds` 추가. `getGroupMoments()`에서 이미 `blockedUserIds`를 한 번 조회하므로 추가 DB 호출 없음.

---

## Phase 5: 댓글/좋아요 생성 차단

> **2차 리뷰 반영 (보안/도메인/반론 리뷰어 3명 독립 발견 — CRITICAL)**: 기존 계획은 조회 필터링과 알림 차단만 다뤘으나, 차단된 사용자가 직접 API 호출로 댓글/좋아요를 생성하는 것도 차단해야 함.

### 5-1. 댓글 생성 차단

**수정 파일**:
- `comment/service/facade/CommentCreateFacadeService.java`
- `comment/service/facade/GroupCommentCreateFacadeService.java`

두 Facade에 `UserBlockApplicationService` 의존성 추가. 댓글 생성 전 차단 관계 확인:

```java
// GroupCommentCreateFacadeService.createGroupComment() 수정
@Transactional
public GroupCommentCreateResponse createGroupComment(GroupCommentCreateRequest request, Long userId) {
    // 기존 검증 로직...
    Moment moment = momentApplicationService.getMomentBy(request.momentId());

    // 차단 관계 확인: 댓글 작성자 ↔ 모멘트 작성자
    if (userBlockApplicationService.isBlocked(userId, moment.getMomenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }

    // 기존 댓글 생성 로직...
}
```

`CommentCreateFacadeService.createComment()`에도 동일 패턴 적용.

### 5-2. 좋아요 생성 차단

**수정 파일**:
- `like/service/MomentLikeService.java`
- `like/service/CommentLikeService.java`

> **설계 결정**: like 모듈은 Application Service가 없으므로 도메인 서비스에서 직접 `UserBlockApplicationService`를 참조. 이는 레이어 관점에서 `MomentLikeService`(도메인 서비스)가 외부 모듈의 Application Service를 참조하는 것이므로, 의존성 방향은 올바름 (서비스 → 외부 Application Service). 단, like 모듈에 Application Service를 도입하는 것도 대안이나 현재 규모에서는 과도한 레이어링.

```java
// MomentLikeService.toggle() 수정
@Transactional
public void toggle(Moment moment, GroupMember member) {
    // 차단 관계 확인: 좋아요 누른 사용자 ↔ 모멘트 작성자
    if (userBlockApplicationService.isBlocked(member.getUser().getId(), moment.getMomenter().getId())) {
        throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
    }
    // 기존 토글 로직...
}
```

`CommentLikeService.toggle()`에도 동일 패턴 적용 (좋아요 누른 사용자 ↔ 댓글 작성자 확인).

---

## Phase 6: 알림 필터링

차단된 사용자로부터의 알림을 차단한다.

> **참고**: Phase 5에서 생성 자체를 차단하므로 알림 이벤트가 발행되지 않는 경우가 대부분. 하지만 race condition(차단 직후 이벤트가 이미 발행된 경우)이나 향후 생성 차단 없이 알림만 필터링하는 이벤트를 위해 핸들러 차단도 유지.

### 6-1. NotificationEventHandler 수정

**파일**: `src/main/java/moment/notification/service/eventHandler/NotificationEventHandler.java`

- `UserBlockApplicationService` 의존성 추가 (Application Service를 통해 접근 - 레이어 규칙 준수)
- 각 이벤트 핸들러에서 **발신자-수신자 간 차단 관계 확인** 후 알림 전송 스킵

> **2차 리뷰 반영**: self-notification skip 체크를 차단 DB 조회보다 먼저 수행하여 불필요한 쿼리 절감.

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentCreateEvent(CommentCreateEvent event) {
    // 1. self-notification skip (DB 호출 없음)
    if (event.commenterId().equals(event.momenterId())) {
        return;
    }
    // 2. 차단 관계 확인 (DB 호출)
    if (userBlockApplicationService.isBlocked(event.commenterId(), event.momenterId())) {
        log.info("Skipping notification due to block: commenter={}, momentOwner={}",
            event.commenterId(), event.momenterId());
        return;
    }
    // 3. 기존 알림 로직
}

@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleGroupCommentCreateEvent(GroupCommentCreateEvent event) {
    if (event.commenterId().equals(event.momentOwnerId())) {
        return;
    }
    if (userBlockApplicationService.isBlocked(event.commenterId(), event.momentOwnerId())) {
        log.info("Skipping notification due to block: commenter={}, momentOwner={}",
            event.commenterId(), event.momentOwnerId());
        return;
    }
    // 기존 알림 로직
}
```

> **리뷰 반영**: `UserBlockService`(도메인 서비스) 직접 참조 → `UserBlockApplicationService`(애플리케이션 서비스)로 변경. 외부 모듈은 반드시 Application Service를 통해 접근해야 함.

**적용 대상 이벤트 (발신자 userId ↔ 수신자 userId 확인)**:
- `CommentCreateEvent` - `commenterId`(userId) ↔ `momenterId`(userId) ✅ 바로 사용 가능
- `GroupCommentCreateEvent` - `commenterId`(userId) ↔ `momentOwnerId`(userId) ✅ 바로 사용 가능
- `MomentLikeEvent` - `likerUserId`(추가 필드) ↔ `momentOwnerId`(userId) ✅ 필드 추가 후 사용
- `CommentLikeEvent` - `likerUserId`(추가 필드) ↔ 댓글 작성자(userId) ✅ 필드 추가 후 사용

> **리뷰 반영**: `CommentCreateEvent`를 적용 대상에 명시적 포함. 두 이벤트 모두 구현 예시 코드 제공.

**적용 불필요**:
- `GroupJoinRequestEvent` - 가입 신청은 차단과 무관
- `GroupJoinApprovedEvent` - 승인 알림은 차단과 무관
- `GroupKickedEvent` - 강퇴 알림은 차단과 무관

### 6-2. Like 이벤트 레코드 수정

**`MomentLikeEvent`** (`like/dto/event/MomentLikeEvent.java`):
- `likerUserId` 필드 추가 (Long)
- `of()` 정적 팩토리 메서드 추가 (CommentCreateEvent 컨벤션 일치):
  ```java
  public record MomentLikeEvent(
      Long momentId, Long momentOwnerId, Long likeMemberId,
      String likerNickname, Long groupId, Long likerUserId
  ) {
      public static MomentLikeEvent of(Moment moment, GroupMember member) {
          return new MomentLikeEvent(
              moment.getId(), moment.getMomenter().getId(),
              member.getId(), member.getNickname(),
              moment.getGroup().getId(), member.getUser().getId()
          );
      }
  }
  ```
- `MomentLikeService.toggle()`에서 `new MomentLikeEvent(...)` → `MomentLikeEvent.of(moment, member)` 변경

**`CommentLikeEvent`** (`like/dto/event/CommentLikeEvent.java`):
- `likerUserId` 필드 추가 (Long)
- `of()` 정적 팩토리 메서드 추가 (동일 패턴)
- `CommentLikeService.toggle()`에서 `of()` 팩토리 사용으로 변경

> **리뷰 반영**: 이벤트 record에 `of()` 팩토리 메서드 추가하여 생성 로직 캡슐화 (Tidy First - 구조적 변경으로 선행 커밋 가능)

---

## Phase 7: 테스트

### 단위 테스트
- `UserBlockServiceTest` - 차단/해제/중복차단/자기차단/restore 등
- `UserBlockApplicationServiceTest` - 오케스트레이션 테스트
- `MomentServiceTest` - blockedUserIds 필터링 동작 확인
- `CommentApplicationServiceTest` - 댓글 필터링 동작 확인
- `NotificationEventHandlerTest` - 차단 시 알림 스킵 확인

> **2차 리뷰 반영 엣지 케이스 테스트**:
> - A가 B를 차단하고, B도 A를 차단한 경우 (두 row 존재)
> - 차단된 사용자가 동시에 신고(report)된 경우 — 두 필터 모두 적용
> - 댓글 가능한 모멘트가 모두 차단 사용자의 것인 경우 — 빈 결과 반환
> - 같은 그룹 내 차단된 사용자의 모멘트/댓글 미노출 확인
> - 차단된 사용자가 댓글/좋아요 생성 시도 시 BL-004 에러 반환
> - 차단 해제 후 콘텐츠 즉시 복원 확인
> - `EMPTY_BLOCK_LIST` (List.of(-1L)) 더미값으로 쿼리 정상 동작 확인
> - 댓글 count가 차단 필터 적용 후 정확한지 확인
> - `DataIntegrityViolationException` 발생 시 적절한 에러 응답 (nice-to-have)

### E2E 테스트
- `UserBlockControllerTest` - 차단/해제/목록 API
- 그룹 피드에서 차단된 사용자 모멘트 미노출 확인
- 그룹 댓글에서 차단된 사용자 댓글 미노출 확인
- 차단된 사용자의 댓글 작성 시 403 에러 확인
- 차단된 사용자의 좋아요 시 403 에러 확인

---

## 수정 대상 파일 요약

### 새로 생성하는 파일
| 파일 | 설명 |
|------|------|
| `block/domain/UserBlock.java` | 엔티티 (restore/isDeleted 포함) |
| `block/infrastructure/UserBlockRepository.java` | 리포지토리 |
| `block/service/block/UserBlockService.java` | 도메인 서비스 |
| `block/service/application/UserBlockApplicationService.java` | 애플리케이션 서비스 |
| `block/presentation/UserBlockController.java` | REST 컨트롤러 |
| `block/dto/response/UserBlockResponse.java` | 응답 DTO (@Schema 포함) |
| `block/dto/response/UserBlockListResponse.java` | 목록 응답 DTO (@Schema 포함) |
| `db/migration/mysql/V38__create_user_blocks.sql` | MySQL 마이그레이션 |
| `db/migration/h2/V38__create_user_blocks__h2.sql` | H2 마이그레이션 (테스트용) |

### 수정하는 파일
| 파일 | 변경 내용 |
|------|----------|
| `global/exception/ErrorCode.java` | BL-001~004 에러 코드 추가 |
| `moment/infrastructure/MomentRepository.java` | 기존 쿼리 6개에 `blockedUserIds` 파라미터 통합 (새 쿼리 추가 아님) |
| `moment/service/moment/MomentService.java` | blockedUserIds 파라미터 추가 + `EMPTY_BLOCK_LIST` 상수 처리 (2개 메서드) |
| `moment/service/application/MomentApplicationService.java` | UserBlockApplicationService 의존성 추가, blockedUserIds 한 번 조회 및 전달 |
| `comment/service/application/CommentApplicationService.java` | UserBlockApplicationService 의존성 추가, `Set<Long>` 기반 댓글 필터링 |
| `comment/infrastructure/CommentRepository.java` | 댓글 count 쿼리에 blockedUserIds 조건 추가 (Phase 4-4) |
| `moment/service/facade/MyGroupMomentPageFacadeService.java` | UserBlockApplicationService 의존성 추가, `Set<Long>` 기반 댓글 필터링 |
| `moment/service/facade/MyGroupCommentPageFacadeService.java` | UserBlockApplicationService 의존성 추가, 댓글 필터링 **(2차 리뷰 반영)** |
| `comment/service/facade/CommentCreateFacadeService.java` | 차단 관계 확인 후 댓글 생성 차단 **(2차 리뷰 반영)** |
| `comment/service/facade/GroupCommentCreateFacadeService.java` | 차단 관계 확인 후 댓글 생성 차단 **(2차 리뷰 반영)** |
| `like/service/MomentLikeService.java` | `MomentLikeEvent.of()` 사용 + 차단 관계 확인 **(2차 리뷰 반영)** |
| `like/service/CommentLikeService.java` | `CommentLikeEvent.of()` 사용 + 차단 관계 확인 **(2차 리뷰 반영)** |
| `notification/service/eventHandler/NotificationEventHandler.java` | UserBlockApplicationService 의존성 추가, 차단 관계 확인 후 알림 스킵 |
| `like/dto/event/MomentLikeEvent.java` | `likerUserId` 필드 + `of()` 팩토리 메서드 추가 |
| `like/dto/event/CommentLikeEvent.java` | `likerUserId` 필드 + `of()` 팩토리 메서드 추가 |

---

## 구현 순서

1. **(구조적 변경 선행)** Like 이벤트에 `of()` 팩토리 메서드 추가 → 기존 생성 코드를 `of()` 사용으로 변경 → 별도 커밋
2. Flyway 마이그레이션 (MySQL + H2) → UserBlock 엔티티 → UserBlockRepository
3. ErrorCode 추가 (BL-001~004)
4. UserBlockService → UserBlockApplicationService
5. DTO → UserBlockController
6. MomentRepository 기존 쿼리에 blockedUserIds 파라미터 통합 → MomentService 수정 → MomentApplicationService 수정
7. CommentApplicationService 수정 → MyGroupMomentPageFacadeService 수정 → **MyGroupCommentPageFacadeService 수정**
8. **댓글 count 보정** (CommentRepository 쿼리 수정)
9. **댓글/좋아요 생성 차단** (CommentCreateFacadeService, GroupCommentCreateFacadeService, MomentLikeService, CommentLikeService)
10. NotificationEventHandler 수정 (UserBlockApplicationService 사용)
11. 테스트 작성

> **Tidy First 원칙**: 1번(구조적 변경)과 2~11번(행동적 변경)을 별도 커밋으로 분리

---

## 검증 방법

1. `./gradlew fastTest` - 전체 빠른 테스트 통과 확인
2. 차단 API 호출 후 그룹 피드 조회 → 차단된 사용자 모멘트 미노출 확인
3. 차단 API 호출 후 댓글 조회 → 차단된 사용자 댓글 미노출 확인
4. 차단된 사용자가 내 모멘트에 댓글 작성 시도 → **403 에러 확인**
5. 차단된 사용자가 내 모멘트에 좋아요 시도 → **403 에러 확인**
6. 차단된 사용자가 내 모멘트에 댓글/좋아요 → 알림 미수신 확인
7. 모멘트 목록의 댓글 count가 필터링 후 실제 댓글 수와 일치 확인
8. 차단 해제 후 → 콘텐츠 정상 노출 확인
9. **내가 받은 댓글 페이지**에서 차단된 사용자 댓글 미노출 확인

---

## 리뷰 반영 변경 이력

### 1차 리뷰 반영

| 항목 | 변경 전 | 변경 후 | 근거 |
|------|--------|--------|------|
| DDL `idx_blocker_id` | 존재 | **삭제** | UNIQUE KEY leftmost prefix와 완전 중복 |
| DDL `created_at` | `TIMESTAMP DEFAULT` | `TIMESTAMP NOT NULL DEFAULT` | BaseEntity 매핑 일치 |
| DDL FK 이름 | 자동 생성 | 명시적 네이밍 | 유지보수 용이, V32 컨벤션 |
| UNION 쿼리 | `UNION` | `UNION ALL` | 양방향 차단 설계상 중복 불가, 비용 절감 |
| API 경로 | `/block` | `/blocks` | REST 복수형 컨벤션 |
| MomentRepository | 쿼리 6개 추가 | 기존 쿼리에 파라미터 통합 | 조합 폭발 방지 |
| 댓글 필터링 | `List.contains()` | `Set<Long>.contains()` | O(n) → O(1) |
| EventHandler 의존성 | `UserBlockService` | `UserBlockApplicationService` | 레이어 위반 수정 |
| Like 이벤트 | 필드만 추가 | 필드 + `of()` 팩토리 | CommentCreateEvent 컨벤션 일치 |
| UserBlock 엔티티 | 필드만 명시 | `restore()`/`isDeleted()` 추가 | MomentLike 선례 |
| H2 마이그레이션 | 누락 | 추가 | 테스트 환경 필수 |
| Phase 5 대상 | `CommentCreateEvent` 암묵적 | 명시적 포함 | 누락 방지 |

### 2차 리뷰 반영 (5인 팀 리뷰)

| 항목 | 변경 전 | 변경 후 | 근거 | 발견자 |
|------|--------|--------|------|--------|
| UNIQUE KEY 접두사 | `uk_` | `uq_` | V1/V12/V28/V32 전체 컨벤션 일치 | DB 리뷰어 |
| 댓글/좋아요 생성 | 조회 필터링만 | **Phase 5 생성 차단 추가** | 직접 API 호출로 생성 가능한 보안 gap | 보안/도메인/반론 (3명) |
| ErrorCode | BL-001~003 | **BL-004 추가** | Phase 5 생성 차단에 사용 | 보안 리뷰어 |
| MyGroupCommentPage | 누락 | **Phase 4-3 추가** | "내가 받은 댓글" 페이지 차단 필터 누락 | 보안/도메인 (2명) |
| 댓글 count | 차단 미고려 | **Phase 4-4 count 보정** | "댓글 5개" 표시 vs 실제 3개 UX 불일치 | 도메인 리뷰어 |
| `List.of(-1L)` | 인라인 매직 넘버 | **`EMPTY_BLOCK_LIST` 상수 추출** | 매직 넘버 제거, 의도 명시 | DB/반론 리뷰어 |
| `getBlockedUserIds()` | 여러 서비스에서 각각 호출 | **상위에서 한 번 조회, 파라미터 전달** | 동일 요청 내 중복 UNION ALL 쿼리 방지 | DB/반론 리뷰어 |
| 정책 결정 | 암묵적 | **Context에 명시적 문서화** | 차단 해제 권한, 그룹 내 차단, 기존 알림, count 정책 | 반론/도메인 리뷰어 |
| 알림 핸들러 | 차단 확인만 | **self-notification skip 선행** | 불필요한 차단 확인 DB 쿼리 절감 | 아키텍처 리뷰어 |
| CommentCreateEvent | 구현 예시 없음 | **핸들러 코드 예시 추가** | Phase 6-1에 두 이벤트 모두 예시 제공 | 도메인 리뷰어 |
| 좋아요 count | 미고려 | **정책 결정 명시 (필터 미적용)** | 익명 집계이므로 차단 영향 없음 | 반론 리뷰어 (전원 누락 발견) |
| 테스트 계획 | 기본 시나리오만 | **엣지 케이스 10+ 추가** | 양방향 차단, 빈 결과, count 검증 등 | 반론 리뷰어 |
| 비그룹 getCommentableMoments | 수정 대상 | **제외 (dead code)** | 프로덕션 호출부 없음, 별도 정리 이슈 | 반론 리뷰어 |

### 향후 TODO (이번 PR 범위 외)
- `MomentApplicationService` → `ReportService` 직접 참조를 `ReportApplicationService`로 변경 (기존 레이어 위반)
- `MomentService.getCommentableMoments()` dead code 정리
- Hibernate 6에서 빈 리스트 `NOT IN` 동작 검증 → `EMPTY_BLOCK_LIST` 더미값 제거 가능 여부
- `DataIntegrityViolationException` → `BLOCK_ALREADY_EXISTS` 에러 변환 (nice-to-have)
