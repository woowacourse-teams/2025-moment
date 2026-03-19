# Fix: existsBidirectionalBlock ClassCastException

## Context

`UserBlockService.isBlocked()` 호출 시 `ClassCastException: Long cannot be cast to Boolean` 발생.
`UserBlockRepository.existsBidirectionalBlock()`의 native query `SELECT COUNT(*) > 0`이 MySQL에서 Long(0/1)을 반환하지만, 메서드 반환 타입이 `boolean`이라 Hibernate 타입 캐스팅 실패.

서비스 단위 테스트는 `willReturn(true)`로 mock하여 실제 SQL이 실행되지 않았고, `UserBlockRepositoryTest`가 존재하지 않아 버그가 검출되지 못함.

**영향 범위**: CommentCreateFacadeService, GroupCommentCreateFacadeService, MomentApplicationService, CommentApplicationService, NotificationEventHandler 등 6곳에서 런타임 에러 발생.

### 보안 영향 분석 (fail-open/fail-closed)

모든 호출 지점이 **fail-closed** 패턴으로, ClassCastException 발생 시 작업이 실패(500 에러)하므로 차단 우회 보안 취약점은 없음.

| 호출 지점 | 패턴 | 동작 |
|----------|------|------|
| `CommentCreateFacadeService:34` | `if (isBlocked()) throw` | 예외 전파 → 500 에러 |
| `GroupCommentCreateFacadeService:32` | `if (isBlocked()) throw` | 예외 전파 → 500 에러 |
| `MomentApplicationService:258` | `if (isBlocked()) throw` | 예외 전파 → 500 에러 |
| `CommentApplicationService:240` | `if (isBlocked()) throw` | 예외 전파 → 500 에러 |
| `NotificationEventHandler:41,106,128,150` | `if (isBlocked()) return` | @Async 메서드 중단 → 알림 미발송 |

결론: 차단된 사용자의 상호작용은 차단되지만, 차단되지 않은 사용자의 정상 작업도 500 에러로 실패함.

---

## Phase 1: Repository 통합 테스트 작성 (RED)

**파일 생성**: `src/test/java/moment/block/infrastructure/UserBlockRepositoryTest.java`

기존 `UserRepositoryTest` 패턴 준수:
```
@Tag(TestTags.INTEGRATION)
@ActiveProfiles("test")
@DataJpaTest
@DisplayNameGeneration(ReplaceUnderscores.class)
```

### 테스트 목록

| # | 메서드 | 테스트명 | 기대 결과 |
|---|--------|---------|-----------|
| 1 | `existsBidirectionalBlock` | 양방향_차단이_존재하면_true를_반환한다_정방향 | A→B 차단 후 (A,B) 조회 → `true` |
| 2 | `existsBidirectionalBlock` | 양방향_차단이_존재하면_true를_반환한다_역방향 | A→B 차단 후 (B,A) 조회 → `true` |
| 3 | `existsBidirectionalBlock` | 차단이_존재하지_않으면_false를_반환한다 | 차단 없이 조회 → `false` |
| 4 | `existsBidirectionalBlock` | soft_delete된_차단은_존재하지_않는_것으로_판단한다 | 차단 후 삭제 → `false` |
| 5 | `existsBidirectionalBlock` | 양방향_모두_차단된_경우에도_true를_반환한다 | A→B, B→A 모두 차단 후 (A,B) 조회 → `true` |
| 6 | `findBlockedUserIds` | 양방향_차단된_사용자_ID_목록을_반환한다 | A→B, C→A → [B.id, C.id] |
| 7 | `findBlockedUserIds` | 차단이_없으면_빈_목록을_반환한다 | 빈 리스트 |
| 8 | `findByBlockerAndBlockedUserIncludeDeleted` | 삭제된_차단을_포함하여_조회한다 | soft-deleted 포함 → `Present` |
| 9 | `findByBlockerAndBlockedUser` | 삭제된_차단은_일반_조회에서_제외된다 | soft-deleted 제외 → `Empty` |
| 10 | `findAllByBlockerWithBlockedUser` | 차단_목록을_차단된_사용자와_함께_조회한다 | fetch join 검증 |
| 11 | `existsByBlockerAndBlockedUser` | 차단_존재_여부를_확인한다 | 단방향만 `true` |

### soft-delete 테스트 셋업 방법 (테스트 #4, #8, #9)

`UserBlock` 엔티티에 `@SQLDelete(sql = "UPDATE user_blocks SET deleted_at = NOW() WHERE id = ?")`가 적용되어 있으므로:

```java
// 1. UserBlock 저장
UserBlock block = userBlockRepository.save(new UserBlock(userA, userB));
// 2. repository.delete() 호출 → @SQLDelete가 UPDATE deleted_at = NOW() 실행
userBlockRepository.delete(block);
// 3. flush로 DB 반영
entityManager.flush();
entityManager.clear();
// 4. 이후 조회 시 @SQLRestriction("deleted_at IS NULL")에 의해 필터링됨
```

**검증**: `./gradlew test --tests "moment.block.infrastructure.UserBlockRepositoryTest"` → 테스트 1~5 ClassCastException으로 실패 확인

---

## Phase 2: 쿼리 수정 (GREEN)

**파일 수정**: `src/main/java/moment/block/infrastructure/UserBlockRepository.java:40-48`

**Before** (native query):
```java
@Query(value = """
    SELECT COUNT(*) > 0 FROM user_blocks
    WHERE ((blocker_id = :userId1 AND blocked_user_id = :userId2)
        OR (blocker_id = :userId2 AND blocked_user_id = :userId1))
      AND deleted_at IS NULL
    """, nativeQuery = true)
boolean existsBidirectionalBlock(...)
```

**After** (JPQL):
```java
@Query("""
    SELECT CASE WHEN COUNT(ub) > 0 THEN true ELSE false END
    FROM UserBlock ub
    WHERE (ub.blocker.id = :userId1 AND ub.blockedUser.id = :userId2)
       OR (ub.blocker.id = :userId2 AND ub.blockedUser.id = :userId1)
    """)
boolean existsBidirectionalBlock(...)
```

**변경 근거**:
- JPQL `CASE WHEN ... THEN true ELSE false END`는 Hibernate가 Boolean으로 정확히 반환
- `@SQLRestriction("deleted_at IS NULL")`이 JPQL에서 자동 적용 → `AND deleted_at IS NULL` 불필요
- H2/MySQL 모두 호환 (dialect 의존성 제거)

**인덱스 참고**: user_blocks 테이블의 `UNIQUE KEY (blocker_id, blocked_user_id)`가 OR 조건의 양쪽을 커버하므로 쿼리 성능에 문제 없음.

**검증**: `./gradlew fastTest` → 전체 테스트 통과

---

## 검증

```bash
./gradlew test --tests "moment.block.infrastructure.UserBlockRepositoryTest"  # 신규 테스트 확인
./gradlew fastTest    # 전체 테스트 통과 확인
```
