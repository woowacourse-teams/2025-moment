package moment.moment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.global.page.Cursorable;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "moments")
@SQLDelete(sql = "UPDATE moments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Moment extends BaseEntity implements Cursorable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "momenter_id")
    private User momenter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private GroupMember member;

    private LocalDateTime deletedAt;

    public Moment(String content, User momenter) {
        validate(content, momenter);
        this.content = content;
        this.momenter = momenter;
    }

    public Moment(User momenter, Group group, GroupMember member, String content) {
        validate(content, momenter);
        this.momenter = momenter;
        this.group = group;
        this.member = member;
        this.content = content;
    }

    private void validate(String content, User momenter) {
        validateContent(content);
        validateContentLength(content);
        validateUser(momenter);
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("moment의 content는 null이거나 빈 값이어서는 안 됩니다.");
        }
    }

    private void validateUser(User momenter) {
        if (momenter == null) {
            throw new IllegalArgumentException("momenter가 null이 되어서는 안 됩니다.");
        }
    }

    private void validateContentLength(String content) {
        if (content.length() > 200) {
            throw new IllegalArgumentException("모멘트는 1자 이상, 200자 이하로만 작성 가능합니다.");
        }
    }

    public boolean isNotSame(User user) {
        return !momenter.equals(user);
    }

    public Long getMomenterId() {
        return momenter.getId();
    }
}
