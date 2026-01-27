# Phase 2: 그룹 인프라 구축 (Day 3-4)

## 개요
- **목표**: 그룹, 멤버, 초대 링크, 좋아요 엔티티 및 인프라 구축
- **원칙**: TDD 기반 (Entity → Repository → 단위 테스트)
- **검증**: 각 Step 후 `./gradlew compileJava` 및 `./gradlew fastTest`

---

## Step 1: Enum 생성

### 1.1 대상 파일

#### `src/main/java/moment/group/domain/MemberRole.java`
```java
package moment.group.domain;

public enum MemberRole {
    OWNER,   // 그룹 소유자
    MEMBER   // 일반 멤버
}
```

#### `src/main/java/moment/group/domain/MemberStatus.java`
```java
package moment.group.domain;

public enum MemberStatus {
    PENDING,   // 가입 신청 대기
    APPROVED,  // 승인됨
    KICKED     // 강퇴됨
}
```

### 1.2 TDD 테스트 케이스
```java
// 테스트: MemberRole enum 값 확인
@Test
void MemberRole_OWNER_MEMBER_존재() {
    assertThat(MemberRole.values()).containsExactly(MemberRole.OWNER, MemberRole.MEMBER);
}

// 테스트: MemberStatus enum 값 확인
@Test
void MemberStatus_PENDING_APPROVED_KICKED_존재() {
    assertThat(MemberStatus.values()).containsExactly(
        MemberStatus.PENDING, MemberStatus.APPROVED, MemberStatus.KICKED
    );
}
```

### 1.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 2: Group 엔티티 생성

### 2.1 대상 파일

#### `src/main/java/moment/group/domain/Group.java`
```java
package moment.group.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE groups SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Group(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
    }

    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean isOwner(User user) {
        return this.owner.getId().equals(user.getId());
    }
}
```

### 2.2 TDD 테스트 케이스
```java
// 테스트: Group 생성
@Test
void Group_생성_성공() {
    // Given
    User owner = createUser();

    // When
    Group group = new Group("테스트 그룹", "설명", owner);

    // Then
    assertThat(group.getName()).isEqualTo("테스트 그룹");
    assertThat(group.getDescription()).isEqualTo("설명");
    assertThat(group.getOwner()).isEqualTo(owner);
}

// 테스트: Group 정보 수정
@Test
void Group_정보_수정_성공() {
    // Given
    Group group = createGroup();

    // When
    group.updateInfo("새 이름", "새 설명");

    // Then
    assertThat(group.getName()).isEqualTo("새 이름");
    assertThat(group.getDescription()).isEqualTo("새 설명");
}

// 테스트: 소유자 확인
@Test
void Group_소유자_확인() {
    // Given
    User owner = createUser();
    User other = createOtherUser();
    Group group = new Group("그룹", "설명", owner);

    // When/Then
    assertThat(group.isOwner(owner)).isTrue();
    assertThat(group.isOwner(other)).isFalse();
}
```

### 2.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 3: GroupMember 엔티티 생성

### 3.1 대상 파일

#### `src/main/java/moment/group/domain/GroupMember.java`
```java
package moment.group.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE group_members SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public GroupMember(Group group, User user, String nickname, MemberRole role, MemberStatus status) {
        this.group = group;
        this.user = user;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    // Owner로 생성 (그룹 생성 시)
    public static GroupMember createOwner(Group group, User user, String nickname) {
        return new GroupMember(group, user, nickname, MemberRole.OWNER, MemberStatus.APPROVED);
    }

    // 일반 멤버로 가입 신청
    public static GroupMember createPendingMember(Group group, User user, String nickname) {
        return new GroupMember(group, user, nickname, MemberRole.MEMBER, MemberStatus.PENDING);
    }

    public void approve() {
        this.status = MemberStatus.APPROVED;
    }

    public void kick() {
        this.status = MemberStatus.KICKED;
    }

    public void restore(String nickname) {
        this.deletedAt = null;
        this.nickname = nickname;
        this.status = MemberStatus.PENDING;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void transferOwnership() {
        this.role = MemberRole.OWNER;
    }

    public void demoteToMember() {
        this.role = MemberRole.MEMBER;
    }

    public boolean isOwner() {
        return this.role == MemberRole.OWNER;
    }

    public boolean isApproved() {
        return this.status == MemberStatus.APPROVED;
    }

    public boolean isPending() {
        return this.status == MemberStatus.PENDING;
    }
}
```

### 3.2 TDD 테스트 케이스
```java
// 테스트: Owner 멤버 생성
@Test
void GroupMember_Owner_생성_성공() {
    // Given
    Group group = createGroup();
    User user = createUser();

    // When
    GroupMember member = GroupMember.createOwner(group, user, "닉네임");

    // Then
    assertThat(member.getRole()).isEqualTo(MemberRole.OWNER);
    assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
    assertThat(member.isOwner()).isTrue();
    assertThat(member.isApproved()).isTrue();
}

// 테스트: Pending 멤버 생성
@Test
void GroupMember_Pending_생성_성공() {
    // Given
    Group group = createGroup();
    User user = createUser();

    // When
    GroupMember member = GroupMember.createPendingMember(group, user, "닉네임");

    // Then
    assertThat(member.getRole()).isEqualTo(MemberRole.MEMBER);
    assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
    assertThat(member.isPending()).isTrue();
}

// 테스트: 멤버 승인
@Test
void GroupMember_승인_성공() {
    // Given
    GroupMember member = createPendingMember();

    // When
    member.approve();

    // Then
    assertThat(member.getStatus()).isEqualTo(MemberStatus.APPROVED);
    assertThat(member.isApproved()).isTrue();
}

// 테스트: 멤버 강퇴
@Test
void GroupMember_강퇴_성공() {
    // Given
    GroupMember member = createApprovedMember();

    // When
    member.kick();

    // Then
    assertThat(member.getStatus()).isEqualTo(MemberStatus.KICKED);
}

// 테스트: 멤버 복구 (재가입)
@Test
void GroupMember_복구_성공() {
    // Given
    GroupMember member = createDeletedMember();

    // When
    member.restore("새닉네임");

    // Then
    assertThat(member.getDeletedAt()).isNull();
    assertThat(member.getNickname()).isEqualTo("새닉네임");
    assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING);
}

// 테스트: 소유권 이전
@Test
void GroupMember_소유권_이전_성공() {
    // Given
    GroupMember owner = createOwnerMember();
    GroupMember member = createApprovedMember();

    // When
    owner.demoteToMember();
    member.transferOwnership();

    // Then
    assertThat(owner.getRole()).isEqualTo(MemberRole.MEMBER);
    assertThat(member.getRole()).isEqualTo(MemberRole.OWNER);
}
```

### 3.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 4: GroupInviteLink 엔티티 생성

### 4.1 대상 파일

#### `src/main/java/moment/group/domain/GroupInviteLink.java`
```java
package moment.group.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_invite_links")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE group_invite_links SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class GroupInviteLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public GroupInviteLink(Group group, int validDays) {
        this.group = group;
        this.code = UUID.randomUUID().toString();
        this.expiredAt = LocalDateTime.now().plusDays(validDays);
        this.isActive = true;
    }

    public boolean isValid() {
        return isActive && LocalDateTime.now().isBefore(expiredAt);
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void extendExpiration(int days) {
        this.expiredAt = LocalDateTime.now().plusDays(days);
    }
}
```

### 4.2 TDD 테스트 케이스
```java
// 테스트: 초대 링크 생성
@Test
void GroupInviteLink_생성_성공() {
    // Given
    Group group = createGroup();

    // When
    GroupInviteLink link = new GroupInviteLink(group, 7);

    // Then
    assertThat(link.getCode()).isNotNull();
    assertThat(link.getCode()).hasSize(36); // UUID format
    assertThat(link.isActive()).isTrue();
    assertThat(link.getExpiredAt()).isAfter(LocalDateTime.now());
}

// 테스트: 초대 링크 유효성 검사 - 유효
@Test
void GroupInviteLink_유효한_링크() {
    // Given
    GroupInviteLink link = new GroupInviteLink(createGroup(), 7);

    // When/Then
    assertThat(link.isValid()).isTrue();
}

// 테스트: 초대 링크 유효성 검사 - 비활성화
@Test
void GroupInviteLink_비활성화된_링크() {
    // Given
    GroupInviteLink link = new GroupInviteLink(createGroup(), 7);

    // When
    link.deactivate();

    // Then
    assertThat(link.isValid()).isFalse();
}

// 테스트: 초대 링크 만료 연장
@Test
void GroupInviteLink_만료_연장_성공() {
    // Given
    GroupInviteLink link = new GroupInviteLink(createGroup(), 1);
    LocalDateTime originalExpiry = link.getExpiredAt();

    // When
    link.extendExpiration(7);

    // Then
    assertThat(link.getExpiredAt()).isAfter(originalExpiry);
}
```

### 4.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 5: Like 엔티티 생성

### 5.1 대상 파일

#### `src/main/java/moment/like/domain/MomentLike.java`
```java
package moment.like.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.global.domain.BaseEntity;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "moment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"moment_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE moment_likes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MomentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMember member;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public MomentLike(Moment moment, GroupMember member) {
        this.moment = moment;
        this.member = member;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void toggleDeleted() {
        if (isDeleted()) {
            restore();
        } else {
            this.deletedAt = LocalDateTime.now();
        }
    }
}
```

#### `src/main/java/moment/like/domain/CommentLike.java`
```java
package moment.like.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moment.comment.domain.Comment;
import moment.global.domain.BaseEntity;
import moment.group.domain.GroupMember;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE comment_likes SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class CommentLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private GroupMember member;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public CommentLike(Comment comment, GroupMember member) {
        this.comment = comment;
        this.member = member;
    }

    public void restore() {
        this.deletedAt = null;
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void toggleDeleted() {
        if (isDeleted()) {
            restore();
        } else {
            this.deletedAt = LocalDateTime.now();
        }
    }
}
```

### 5.2 TDD 테스트 케이스
```java
// 테스트: MomentLike 생성
@Test
void MomentLike_생성_성공() {
    // Given
    Moment moment = createMoment();
    GroupMember member = createMember();

    // When
    MomentLike like = new MomentLike(moment, member);

    // Then
    assertThat(like.getMoment()).isEqualTo(moment);
    assertThat(like.getMember()).isEqualTo(member);
    assertThat(like.isDeleted()).isFalse();
}

// 테스트: MomentLike 토글 (좋아요 → 취소)
@Test
void MomentLike_토글_취소() {
    // Given
    MomentLike like = createMomentLike();
    assertThat(like.isDeleted()).isFalse();

    // When
    like.toggleDeleted();

    // Then
    assertThat(like.isDeleted()).isTrue();
}

// 테스트: MomentLike 토글 (취소 → 좋아요)
@Test
void MomentLike_토글_복구() {
    // Given
    MomentLike like = createDeletedMomentLike();
    assertThat(like.isDeleted()).isTrue();

    // When
    like.toggleDeleted();

    // Then
    assertThat(like.isDeleted()).isFalse();
}

// CommentLike도 동일한 테스트 패턴
```

### 5.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 6: 기존 엔티티 수정

### 6.1 대상 파일

#### `moment/domain/Moment.java`
```java
// 추가 필드
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "group_id")
private Group group;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private GroupMember member;

// 새 생성자 (그룹 컨텍스트)
public Moment(User momenter, Group group, GroupMember member, String content) {
    this.momenter = momenter;
    this.group = group;
    this.member = member;
    this.content = content;
}
```

#### `comment/domain/Comment.java`
```java
// 추가 필드
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private GroupMember member;

// 새 생성자 (그룹 컨텍스트)
public Comment(Moment moment, User commenter, GroupMember member, String content) {
    this.moment = moment;
    this.commenter = commenter;
    this.member = member;
    this.content = content;
}
```

#### `notification/domain/Notification.java`
```java
// 추가 필드
@Column(name = "group_id")
private Long groupId;
```

### 6.2 TDD 테스트 케이스
```java
// 테스트: Moment 그룹 컨텍스트 생성
@Test
void Moment_그룹_컨텍스트_생성_성공() {
    // Given
    User momenter = createUser();
    Group group = createGroup();
    GroupMember member = createMember();

    // When
    Moment moment = new Moment(momenter, group, member, "내용");

    // Then
    assertThat(moment.getGroup()).isEqualTo(group);
    assertThat(moment.getMember()).isEqualTo(member);
}

// 테스트: Comment 멤버 컨텍스트 생성
@Test
void Comment_멤버_컨텍스트_생성_성공() {
    // Given
    Moment moment = createMoment();
    User commenter = createUser();
    GroupMember member = createMember();

    // When
    Comment comment = new Comment(moment, commenter, member, "내용");

    // Then
    assertThat(comment.getMember()).isEqualTo(member);
}
```

### 6.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 7: Repository 생성

### 7.1 대상 파일

#### `src/main/java/moment/group/infrastructure/GroupRepository.java`
```java
package moment.group.infrastructure;

import moment.group.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findById(Long id);

    @Query("SELECT g FROM Group g WHERE g.owner.id = :userId")
    List<Group> findByOwnerId(@Param("userId") Long userId);
}
```

#### `src/main/java/moment/group/infrastructure/GroupMemberRepository.java`
```java
package moment.group.infrastructure;

import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupIdAndStatus(Long groupId, MemberStatus status);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    @Query("SELECT m FROM GroupMember m WHERE m.group.id = :groupId AND m.user.id = :userId")
    Optional<GroupMember> findByGroupIdAndUserIdIncludeDeleted(
        @Param("groupId") Long groupId,
        @Param("userId") Long userId
    );

    boolean existsByGroupIdAndNicknameAndDeletedAtIsNull(Long groupId, String nickname);

    @Query("SELECT m FROM GroupMember m WHERE m.user.id = :userId AND m.status = 'APPROVED'")
    List<GroupMember> findApprovedMembershipsByUserId(@Param("userId") Long userId);

    long countByGroupIdAndStatus(Long groupId, MemberStatus status);
}
```

#### `src/main/java/moment/group/infrastructure/GroupInviteLinkRepository.java`
```java
package moment.group.infrastructure;

import moment.group.domain.GroupInviteLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupInviteLinkRepository extends JpaRepository<GroupInviteLink, Long> {

    Optional<GroupInviteLink> findByCode(String code);

    Optional<GroupInviteLink> findByGroupIdAndIsActiveTrue(Long groupId);
}
```

#### `src/main/java/moment/like/infrastructure/MomentLikeRepository.java`
```java
package moment.like.infrastructure;

import moment.like.domain.MomentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MomentLikeRepository extends JpaRepository<MomentLike, Long> {

    Optional<MomentLike> findByMomentIdAndMemberId(Long momentId, Long memberId);

    @Query(value = "SELECT * FROM moment_likes WHERE moment_id = :momentId AND member_id = :memberId",
           nativeQuery = true)
    Optional<MomentLike> findByMomentIdAndMemberIdIncludeDeleted(
        @Param("momentId") Long momentId,
        @Param("memberId") Long memberId
    );

    long countByMomentId(Long momentId);

    boolean existsByMomentIdAndMemberId(Long momentId, Long memberId);
}
```

#### `src/main/java/moment/like/infrastructure/CommentLikeRepository.java`
```java
package moment.like.infrastructure;

import moment.like.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndMemberId(Long commentId, Long memberId);

    @Query(value = "SELECT * FROM comment_likes WHERE comment_id = :commentId AND member_id = :memberId",
           nativeQuery = true)
    Optional<CommentLike> findByCommentIdAndMemberIdIncludeDeleted(
        @Param("commentId") Long commentId,
        @Param("memberId") Long memberId
    );

    long countByCommentId(Long commentId);

    boolean existsByCommentIdAndMemberId(Long commentId, Long memberId);
}
```

### 7.2 TDD 테스트 케이스 (Repository 통합 테스트)
```java
@DataJpaTest
class GroupRepositoryTest {

    @Autowired
    private GroupRepository groupRepository;

    @Test
    void 그룹_저장_및_조회() {
        // Given
        Group group = new Group("테스트", "설명", owner);

        // When
        Group saved = groupRepository.save(group);

        // Then
        assertThat(saved.getId()).isNotNull();
    }
}

@DataJpaTest
class GroupMemberRepositoryTest {

    @Autowired
    private GroupMemberRepository memberRepository;

    @Test
    void 닉네임_중복_확인() {
        // Given
        GroupMember member = createMember("닉네임");
        memberRepository.save(member);

        // When
        boolean exists = memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(
            groupId, "닉네임"
        );

        // Then
        assertThat(exists).isTrue();
    }
}
```

### 7.3 검증
```bash
./gradlew compileJava
./gradlew fastTest
```

---

## Step 8: DB 마이그레이션

### 8.1 MySQL 마이그레이션

#### `src/main/resources/db/migration/mysql/V27__create_groups.sql`
```sql
CREATE TABLE groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_groups_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_groups_owner ON groups(owner_id);
CREATE INDEX idx_groups_deleted_at ON groups(deleted_at);
```

#### `src/main/resources/db/migration/mysql/V28__create_group_members.sql`
```sql
CREATE TABLE group_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(20) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES groups(id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_member UNIQUE (group_id, user_id)
);

CREATE INDEX idx_members_group ON group_members(group_id);
CREATE INDEX idx_members_user ON group_members(user_id);
CREATE INDEX idx_members_status ON group_members(status);
CREATE INDEX idx_members_group_nickname ON group_members(group_id, nickname);
```

#### `src/main/resources/db/migration/mysql/V29__create_group_invite_links.sql`
```sql
CREATE TABLE group_invite_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    code VARCHAR(36) NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_invite_group FOREIGN KEY (group_id) REFERENCES groups(id)
);

CREATE INDEX idx_invite_code ON group_invite_links(code);
CREATE INDEX idx_invite_group ON group_invite_links(group_id);
```

#### `src/main/resources/db/migration/mysql/V30__alter_moments_for_groups.sql`
```sql
ALTER TABLE moments ADD COLUMN group_id BIGINT DEFAULT NULL;
ALTER TABLE moments ADD COLUMN member_id BIGINT DEFAULT NULL;

ALTER TABLE moments ADD CONSTRAINT fk_moments_group
    FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE moments ADD CONSTRAINT fk_moments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_moments_group ON moments(group_id);
CREATE INDEX idx_moments_member ON moments(member_id);
```

#### `src/main/resources/db/migration/mysql/V31__alter_comments_for_groups.sql`
```sql
ALTER TABLE comments ADD COLUMN member_id BIGINT DEFAULT NULL;

ALTER TABLE comments ADD CONSTRAINT fk_comments_member
    FOREIGN KEY (member_id) REFERENCES group_members(id);

CREATE INDEX idx_comments_member ON comments(member_id);
```

#### `src/main/resources/db/migration/mysql/V32__create_likes.sql`
```sql
CREATE TABLE moment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    moment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_moment_likes_moment FOREIGN KEY (moment_id) REFERENCES moments(id),
    CONSTRAINT fk_moment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_moment_like UNIQUE (moment_id, member_id)
);

CREATE TABLE comment_likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP DEFAULT NULL,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT fk_comment_likes_member FOREIGN KEY (member_id) REFERENCES group_members(id),
    CONSTRAINT uq_comment_like UNIQUE (comment_id, member_id)
);

CREATE INDEX idx_moment_likes_moment ON moment_likes(moment_id);
CREATE INDEX idx_moment_likes_member ON moment_likes(member_id);
CREATE INDEX idx_comment_likes_comment ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_member ON comment_likes(member_id);
```

#### `src/main/resources/db/migration/mysql/V33__alter_notifications_for_groups.sql`
```sql
ALTER TABLE notifications ADD COLUMN group_id BIGINT DEFAULT NULL;

CREATE INDEX idx_notifications_group ON notifications(group_id);
```

### 8.2 H2 마이그레이션 (테스트용)

H2 마이그레이션 파일은 `src/test/resources/db/migration/h2/` 경로에 동일한 내용으로 생성합니다.
H2 문법 차이 반영:
- `TIMESTAMP` → `TIMESTAMP NULL` (nullable인 경우)
- `AUTO_INCREMENT` → `AUTO_INCREMENT` (H2도 지원)

### 8.3 검증
```bash
./gradlew test  # Flyway 마이그레이션 포함 테스트
```

---

## 최종 검증

```bash
# 1. 컴파일 확인
./gradlew compileJava

# 2. 단위 테스트
./gradlew fastTest

# 3. 전체 테스트 (마이그레이션 포함)
./gradlew test

# 4. 빌드
./gradlew build
```

---

## 체크리스트

### Enum 생성 완료
- [ ] MemberRole (OWNER, MEMBER)
- [ ] MemberStatus (PENDING, APPROVED, KICKED)

### 엔티티 생성 완료
- [ ] Group (name, description, owner)
- [ ] GroupMember (group, user, nickname, role, status)
- [ ] GroupInviteLink (group, code, expiredAt, isActive)
- [ ] MomentLike (moment, member)
- [ ] CommentLike (comment, member)

### 기존 엔티티 수정 완료
- [ ] Moment → group_id, member_id 추가
- [ ] Comment → member_id 추가
- [ ] Notification → group_id 추가

### Repository 생성 완료
- [ ] GroupRepository
- [ ] GroupMemberRepository
- [ ] GroupInviteLinkRepository
- [ ] MomentLikeRepository
- [ ] CommentLikeRepository

### DB 마이그레이션 완료
- [ ] V27: groups 테이블 (MySQL, H2)
- [ ] V28: group_members 테이블 (MySQL, H2)
- [ ] V29: group_invite_links 테이블 (MySQL, H2)
- [ ] V30: moments 테이블 수정 (MySQL, H2)
- [ ] V31: comments 테이블 수정 (MySQL, H2)
- [ ] V32: likes 테이블 (MySQL, H2)
- [ ] V33: notifications 테이블 수정 (MySQL, H2)

### 테스트 완료
- [ ] Enum 단위 테스트
- [ ] Entity 단위 테스트
- [ ] Repository 통합 테스트
- [ ] 마이그레이션 테스트

### 최종 검증
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew fastTest` 성공
- [ ] `./gradlew test` 성공
- [ ] `./gradlew build` 성공

---

## 디렉토리 구조

```
src/main/java/moment/
├── group/
│   ├── domain/
│   │   ├── Group.java
│   │   ├── GroupMember.java
│   │   ├── GroupInviteLink.java
│   │   ├── MemberRole.java
│   │   └── MemberStatus.java
│   └── infrastructure/
│       ├── GroupRepository.java
│       ├── GroupMemberRepository.java
│       └── GroupInviteLinkRepository.java
├── like/
│   ├── domain/
│   │   ├── MomentLike.java
│   │   └── CommentLike.java
│   └── infrastructure/
│       ├── MomentLikeRepository.java
│       └── CommentLikeRepository.java
├── moment/
│   └── domain/
│       └── Moment.java  (수정됨)
├── comment/
│   └── domain/
│       └── Comment.java  (수정됨)
└── notification/
    └── domain/
        └── Notification.java  (수정됨)
```
