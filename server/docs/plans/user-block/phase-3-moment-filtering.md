# Phase 3: 모멘트 피드 필터링

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1, Phase 2

---

## 목표

차단된 사용자의 모멘트를 그룹 피드에서 DB 레벨로 필터링한다.

---

## 현재 상태 분석

### MomentRepository 수정 대상 쿼리 (4개)

**파일**: `src/main/java/moment/moment/infrastructure/MomentRepository.java`

| # | 메서드명 | 현재 line | 설명 |
|---|---------|-----------|------|
| 1 | `findByGroupIdOrderByIdDesc` | 88-96 | 그룹 모멘트 첫 페이지 |
| 2 | `findByGroupIdAndIdLessThanOrderByIdDesc` | 98-109 | 그룹 모멘트 다음 페이지 |
| 3 | `findMomentIdsInGroup` | 138-148 | 댓글 가능 모멘트 ID (그룹 내) |
| 4 | `findMomentIdsInGroupExcludingReported` | 150-162 | 댓글 가능 모멘트 ID (신고 제외) |

**수정 불필요 메서드**:
- `findByGroupIdAndMemberIdOrderByIdDesc` - 내 모멘트만 조회 (차단 불필요)
- `findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc` - 동일
- `findByGroupIdAndMemberIdAndIdIn` - 동일
- `findByGroupIdAndMemberIdAndIdInAndIdLessThan` - 동일
- Admin용 메서드들 - 차단과 무관
- 비그룹 쿼리 (`findMomentIds`, `findMomentIdsExcludingReported`) - dead code

### MomentService 수정 대상 메서드 (2개)

**파일**: `src/main/java/moment/moment/service/moment/MomentService.java`

| # | 메서드명 | 현재 line | 설명 |
|---|---------|-----------|------|
| 1 | `getByGroup` | 119-125 | Repository 호출부 |
| 2 | `getCommentableMomentIdsInGroup` | 137-145 | Repository 호출부 |

### MomentApplicationService 수정 대상 메서드 (2개)

**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

| # | 메서드명 | 현재 line | 설명 |
|---|---------|-----------|------|
| 1 | `getGroupMoments` | 179-198 | `UserBlockApplicationService` 의존성 추가, `blockedUserIds` 조회 |
| 2 | `getCommentableMomentIdsInGroup` | 140-144 | `blockedUserIds` 조회 후 전달 |

---

## TDD 테스트 목록

### 3-1. MomentService 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `그룹_모멘트_조회_시_차단된_사용자의_모멘트를_제외한다` | blockedUserIds 파라미터 전달 확인 |
| T2 | `차단_목록이_비어있을_때_EMPTY_BLOCK_LIST를_사용한다` | 더미값 치환 확인 |
| T3 | `댓글_가능_모멘트_조회_시_차단된_사용자의_모멘트를_제외한다` | blockedUserIds 전달 확인 |

### 3-2. MomentApplicationService 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `그룹_모멘트_조회_시_차단된_사용자_ID를_한_번만_조회한다` | 단일 호출 확인 |
| T2 | `댓글_가능_모멘트_조회_시_차단된_사용자_ID를_전달한다` | 전달 확인 |

---

## 구현 단계

### Step 1: MomentRepository 쿼리 수정 (4개)

**파일 수정**: `src/main/java/moment/moment/infrastructure/MomentRepository.java`

**1. `findByGroupIdOrderByIdDesc` (line 88-96)**

AS-IS:
```java
@Query("""
      SELECT m FROM moments m
      LEFT JOIN FETCH m.momenter
      LEFT JOIN FETCH m.member
      WHERE m.group.id = :groupId
      ORDER BY m.id DESC
       """)
List<Moment> findByGroupIdOrderByIdDesc(@Param("groupId") Long groupId, Pageable pageable);
```

TO-BE:
```java
@Query("""
      SELECT m FROM moments m
      LEFT JOIN FETCH m.momenter
      LEFT JOIN FETCH m.member
      WHERE m.group.id = :groupId
        AND m.momenter.id NOT IN :blockedUserIds
      ORDER BY m.id DESC
       """)
List<Moment> findByGroupIdOrderByIdDesc(
    @Param("groupId") Long groupId,
    @Param("blockedUserIds") List<Long> blockedUserIds,
    Pageable pageable);
```

**2. `findByGroupIdAndIdLessThanOrderByIdDesc` (line 98-109)**

TO-BE: `AND m.momenter.id NOT IN :blockedUserIds` 조건 추가 + `blockedUserIds` 파라미터 추가

**3. `findMomentIdsInGroup` (line 138-148)**

TO-BE: `AND m.momenter.id NOT IN :blockedUserIds` 조건 추가 + `blockedUserIds` 파라미터 추가

**4. `findMomentIdsInGroupExcludingReported` (line 150-162)**

TO-BE: `AND m.momenter.id NOT IN :blockedUserIds` 조건 추가 + `blockedUserIds` 파라미터 추가

### Step 2: MomentService 수정

**파일 수정**: `src/main/java/moment/moment/service/moment/MomentService.java`

```java
// 클래스 상단에 상수 추가
private static final List<Long> EMPTY_BLOCK_LIST = List.of(-1L);

// getByGroup 수정 (line 119-125)
public List<Moment> getByGroup(Long groupId, Long cursor, int limit, List<Long> blockedUserIds) {
    PageRequest pageable = PageRequest.of(0, limit);
    List<Long> safeBlockedUserIds = blockedUserIds.isEmpty() ? EMPTY_BLOCK_LIST : blockedUserIds;
    if (cursor == null) {
        return momentRepository.findByGroupIdOrderByIdDesc(groupId, safeBlockedUserIds, pageable);
    }
    return momentRepository.findByGroupIdAndIdLessThanOrderByIdDesc(
        groupId, cursor, safeBlockedUserIds, pageable);
}

// getCommentableMomentIdsInGroup 수정 (line 137-145)
public List<Long> getCommentableMomentIdsInGroup(
        Long groupId, User user, List<Long> reportedMomentIds, List<Long> blockedUserIds) {
    LocalDateTime cutoffDateTime = LocalDateTime.now().minusDays(COMMENTABLE_PERIOD_IN_DAYS);
    List<Long> safeBlockedUserIds = blockedUserIds.isEmpty() ? EMPTY_BLOCK_LIST : blockedUserIds;

    if (reportedMomentIds == null || reportedMomentIds.isEmpty()) {
        return momentRepository.findMomentIdsInGroup(groupId, user.getId(), cutoffDateTime, safeBlockedUserIds);
    }
    return momentRepository.findMomentIdsInGroupExcludingReported(
            groupId, user.getId(), cutoffDateTime, reportedMomentIds, safeBlockedUserIds);
}
```

**EMPTY_BLOCK_LIST 설계 근거**:
- AUTO_INCREMENT PK는 항상 양수이므로 `-1L`은 존재 불가한 ID
- `NOT IN` 빈 리스트의 JPQL 동작이 벤더마다 다를 수 있으므로 더미값으로 안전하게 처리
- `EMPTY_BLOCK_LIST` 상수 추출로 매직 넘버 제거

### Step 3: MomentApplicationService 수정

**파일 수정**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

의존성 추가:
```java
private final UserBlockApplicationService userBlockApplicationService;
```

import 추가:
```java
import moment.block.service.application.UserBlockApplicationService;
```

**`getGroupMoments()` 수정 (line 179-198)**:
```java
public GroupMomentListResponse getGroupMoments(Long groupId, Long userId, Long cursor) {
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);
    List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
    List<Moment> moments = momentService.getByGroup(groupId, cursor, DEFAULT_PAGE_SIZE, blockedUserIds);

    // ... 나머지 로직 동일 (댓글 count 보정은 Phase 4에서 처리)
}
```

**`getCommentableMomentIdsInGroup()` 수정 (line 140-144)**:
```java
public List<Long> getCommentableMomentIdsInGroup(Long groupId, Long userId) {
    User user = userService.getUserBy(userId);
    List<Long> reportedMomentIds = reportService.getReportedMomentIdsBy(user.getId());
    List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
    return momentService.getCommentableMomentIdsInGroup(groupId, user, reportedMomentIds, blockedUserIds);
}
```

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 |
|------|----------|
| 수정 | `src/main/java/moment/moment/infrastructure/MomentRepository.java` (쿼리 4개) |
| 수정 | `src/main/java/moment/moment/service/moment/MomentService.java` (메서드 2개 + 상수) |
| 수정 | `src/main/java/moment/moment/service/application/MomentApplicationService.java` (의존성 + 메서드 2개) |

## 의존성

- Phase 2 완료 필수 (`UserBlockApplicationService.getBlockedUserIds()`)
- 시그니처 변경 시 **컴파일러가 미수정 호출부를 감지**하므로 빌드로 누락 방지

## 주의사항

- `getByGroup()` 시그니처 변경으로 모든 호출부 수정 필요. 단, 내 모멘트만 조회하는 메서드는 빈 blockedUserIds 전달
- `getCommentableMoments()` (비그룹 버전)는 프로덕션 호출부 없는 dead code이므로 수정 불필요
- 빌드 확인: `./gradlew build` 실행하여 모든 호출부 수정 완료 확인
