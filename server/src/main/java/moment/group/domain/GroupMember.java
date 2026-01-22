package moment.group.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    public static GroupMember createOwner(Group group, User user, String nickname) {
        return new GroupMember(group, user, nickname, MemberRole.OWNER, MemberStatus.APPROVED);
    }

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
