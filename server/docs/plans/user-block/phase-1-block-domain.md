# Phase 1: Block 도메인 생성

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)

---

## 목표

UserBlock 엔티티, 리포지토리, Flyway 마이그레이션, ErrorCode를 생성하여 차단 도메인의 기반을 구축한다.

---

## 현재 상태 분석

### 참고 패턴: MomentLike 엔티티

**파일**: `src/main/java/moment/like/domain/MomentLike.java`

```java
@Entity
@Table(name = "moment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"moment_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE moment_likes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MomentLike extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMember member;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void restore() { this.deletedAt = null; }
    public boolean isDeleted() { return this.deletedAt != null; }
}
```

### 참고 패턴: Flyway V32 DDL

**파일**: `src/main/resources/db/migration/mysql/V32__create_likes.sql`

- `created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- `deleted_at TIMESTAMP NULL DEFAULT NULL`
- FK에 명시적 이름: `CONSTRAINT fk_moment_likes_moment FOREIGN KEY ...`
- UNIQUE에 `uq_` 접두사: `CONSTRAINT uq_moment_like UNIQUE (...)`
- 인덱스 생성은 CREATE TABLE 외부: `CREATE INDEX idx_... ON ...`
- H2 테스트 마이그레이션은 MySQL 버전과 동일 내용

### ErrorCode 현재 상태

**파일**: `src/main/java/moment/global/exception/ErrorCode.java`

- 마지막 항목: `APPLE_AUTH_SERVER_ERROR("AP-005", ...)` (line 89)
- 세미콜론은 line 90
- BL 접두사는 아직 사용되지 않음

### 최신 Flyway 버전

- MySQL: V37 (`V37__add_notification_indexes.sql`)
- 다음 버전: **V38**

---

## TDD 테스트 목록

### 1-1. UserBlock 엔티티 단위 테스트

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `UserBlock을_생성한다` | blocker, blockedUser로 UserBlock 생성 확인 |
| T2 | `restore_호출_시_deletedAt이_null이_된다` | restore() 메서드 동작 확인 |
| T3 | `isDeleted_deletedAt이_null이면_false를_반환한다` | isDeleted() false 케이스 |
| T4 | `isDeleted_deletedAt이_존재하면_true를_반환한다` | isDeleted() true 케이스 |

---

## 구현 단계

### Step 1: Flyway 마이그레이션 작성

**파일 생성**: `src/main/resources/db/migration/mysql/V38__create_user_blocks.sql`

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

**설계 근거**:
- `idx_blocker_id` 미생성: UNIQUE KEY `uq_user_blocks_blocker_blocked`의 leftmost prefix와 완전 중복
- `idx_user_blocks_blocked_user` 생성: 양방향 차단 조회 시 `blocked_user_id` 단독 검색 필요 (UNION ALL 쿼리)
- FK 명시적 이름 부여: `fk_user_blocks_blocker`, `fk_user_blocks_blocked`

**파일 생성**: `src/test/resources/db/migration/h2/V38__create_user_blocks__h2.sql`

MySQL 버전과 동일 내용 (H2 MODE=MySQL 호환).

### Step 2: UserBlock 엔티티 생성

**파일 생성**: `src/main/java/moment/block/domain/UserBlock.java`

```java
package moment.block.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "user_blocks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"blocker_id", "blocked_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user_blocks SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class UserBlock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public UserBlock(User blocker, User blockedUser) {
        this.blocker = blocker;
        this.blockedUser = blockedUser;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
```

### Step 3: UserBlockRepository 생성

**파일 생성**: `src/main/java/moment/block/infrastructure/UserBlockRepository.java`

```java
package moment.block.infrastructure;

import java.util.List;
import java.util.Optional;
import moment.block.domain.UserBlock;
import moment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByBlockerAndBlockedUser(User blocker, User blockedUser);

    boolean existsByBlockerAndBlockedUser(User blocker, User blockedUser);

    List<UserBlock> findAllByBlocker(User blocker);

    @Query(value = """
        SELECT blocked_user_id FROM user_blocks WHERE blocker_id = :userId AND deleted_at IS NULL
        UNION ALL
        SELECT blocker_id FROM user_blocks WHERE blocked_user_id = :userId AND deleted_at IS NULL
        """, nativeQuery = true)
    List<Long> findBlockedUserIds(@Param("userId") Long userId);

    @Query(value = """
        SELECT * FROM user_blocks
        WHERE blocker_id = :blockerId AND blocked_user_id = :blockedUserId
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserBlock> findByBlockerAndBlockedUserIncludeDeleted(
            @Param("blockerId") Long blockerId,
            @Param("blockedUserId") Long blockedUserId);

    @Query(value = """
        SELECT COUNT(*) > 0 FROM user_blocks
        WHERE ((blocker_id = :userId1 AND blocked_user_id = :userId2)
            OR (blocker_id = :userId2 AND blocked_user_id = :userId1))
          AND deleted_at IS NULL
        """, nativeQuery = true)
    boolean existsBidirectionalBlock(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);
}
```

**설계 근거**:
- `findBlockedUserIds`: `UNION ALL` 사용 (A->B 차단 시 B->A를 별도 생성하지 않으므로 중복 불가, 중복 제거 비용 제거)
- `findByBlockerAndBlockedUserIncludeDeleted`: native query로 `@SQLRestriction` 우회 (재차단 시 restore용)
- `existsBidirectionalBlock`: 양방향 차단 여부 확인용 native query

### Step 4: ErrorCode 추가

**파일 수정**: `src/main/java/moment/global/exception/ErrorCode.java`

line 89 (`APPLE_AUTH_SERVER_ERROR`) 다음, line 90 세미콜론 앞에 추가:

```java
    // Block (BL)
    BLOCK_SELF("BL-001", "자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),
    BLOCK_ALREADY_EXISTS("BL-002", "이미 차단된 사용자입니다.", HttpStatus.CONFLICT),
    BLOCK_NOT_FOUND("BL-003", "차단 관계가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    BLOCKED_USER_INTERACTION("BL-004", "차단된 사용자와 상호작용할 수 없습니다.", HttpStatus.FORBIDDEN),
```

### Step 5: UserBlockFixture 생성 (테스트용)

**파일 생성**: `src/test/java/moment/fixture/UserBlockFixture.java`

```java
package moment.fixture;

import moment.block.domain.UserBlock;
import moment.user.domain.User;

public class UserBlockFixture {

    public static UserBlock createUserBlock(User blocker, User blockedUser) {
        return new UserBlock(blocker, blockedUser);
    }

    public static UserBlock createUserBlockWithId(Long id, User blocker, User blockedUser) {
        UserBlock userBlock = new UserBlock(blocker, blockedUser);
        setId(userBlock, id);
        return userBlock;
    }

    private static void setId(UserBlock userBlock, Long id) {
        try {
            java.lang.reflect.Field idField = UserBlock.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(userBlock, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }
}
```

### Step 6: UserBlock 엔티티 테스트 작성

**파일 생성**: `src/test/java/moment/block/domain/UserBlockTest.java`

```java
package moment.block.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.fixture.UserFixture;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class UserBlockTest {

    @Test
    void UserBlock을_생성한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);

        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        assertThat(userBlock.getBlocker()).isEqualTo(blocker);
        assertThat(userBlock.getBlockedUser()).isEqualTo(blockedUser);
        assertThat(userBlock.isDeleted()).isFalse();
    }

    @Test
    void restore_호출_시_deletedAt이_null이_된다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        userBlock.restore();

        assertThat(userBlock.isDeleted()).isFalse();
    }

    @Test
    void isDeleted_deletedAt이_null이면_false를_반환한다() {
        User blocker = UserFixture.createUserWithId(1L);
        User blockedUser = UserFixture.createUserWithId(2L);
        UserBlock userBlock = new UserBlock(blocker, blockedUser);

        assertThat(userBlock.isDeleted()).isFalse();
    }
}
```

---

## 생성/수정 파일 목록

| 작업 | 파일 경로 |
|------|----------|
| 생성 | `src/main/resources/db/migration/mysql/V38__create_user_blocks.sql` |
| 생성 | `src/test/resources/db/migration/h2/V38__create_user_blocks__h2.sql` |
| 생성 | `src/main/java/moment/block/domain/UserBlock.java` |
| 생성 | `src/main/java/moment/block/infrastructure/UserBlockRepository.java` |
| 수정 | `src/main/java/moment/global/exception/ErrorCode.java` |
| 생성 | `src/test/java/moment/fixture/UserBlockFixture.java` |
| 생성 | `src/test/java/moment/block/domain/UserBlockTest.java` |

## 의존성

- 이 Phase는 독립적으로 구현 가능 (다른 Phase에 의존 없음)
- 후속 Phase 2~7이 이 Phase의 결과물에 의존

## 주의사항

- `isDeleted()` true 케이스 테스트: `UserBlock`에 `deletedAt`을 직접 설정하는 방법이 필요. Reflection으로 설정하거나 `UserBlockFixture`에 삭제 상태 생성 메서드 추가 고려
- H2 마이그레이션은 MySQL 버전과 동일 내용으로 작성 (H2 MODE=MySQL 호환 확인됨 - V32 선례)
