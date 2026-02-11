package moment.comment.domain;

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
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity(name = "comments")
@SQLDelete(sql = "UPDATE comments SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Comment extends BaseEntity implements Cursorable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "commenter_id")
    private User commenter;

    private Long momentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private GroupMember member;

    private LocalDateTime deletedAt;

    public Comment(String content, User commenter, Long momentId) {
        validate(content, commenter, momentId);
        this.content = content;
        this.commenter = commenter;
        this.momentId = momentId;
    }

    public Comment(Moment moment, User commenter, GroupMember member, String content) {
        validate(content, commenter, moment.getId());
        this.content = content;
        this.commenter = commenter;
        this.momentId = moment.getId();
        this.member = member;
    }

    private void validate(String content, User commenter, Long momentId) {
        validateContent(content);
        validateCommenter(commenter);
        validateMoment(momentId);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content가 null이거나 빈 값이어서는 안 됩니다.");
        }

        if (content.length() > 200) {
            throw new IllegalArgumentException("content가 200자를 초과해서는 안 됩니다.");
        }
    }

    private void validateCommenter(User commenter) {
        if (commenter == null) {
            throw new IllegalArgumentException("commenter가 null이어서는 안 됩니다.");
        }
    }

    private void validateMoment(Long momentId) {
        if (momentId == null) {
            throw new IllegalArgumentException("momentId가 null이어서는 안 됩니다.");
        }
    }
}
