package moment.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.global.domain.BaseEntity;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.user.domain.User;

@Entity(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "commenter_id")
    private User commenter;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "moment_id")
    private Moment moment;

    public Comment(String content, User commenter, Moment moment) {
        validate(content, commenter, moment);
        this.content = content;
        this.commenter = commenter;
        this.moment = moment;
    }

    private void validate(String content, User commenter, Moment moment) {
        validateContent(content);
        validateCommenter(commenter);
        validateMoment(moment);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content가 null이거나 빈 값이어서는 안 됩니다.");
        }

        if (content.length() > 100) {
            throw new IllegalArgumentException("content가 100자를 초과해서는 안 됩니다.");
        }
    }

    private void validateCommenter(User commenter) {
        if (commenter == null) {
            throw new IllegalArgumentException("commenter가 null이어서는 안 됩니다.");
        }
    }

    private void validateMoment(Moment moment) {
        if (moment == null) {
            throw new IllegalArgumentException("moment가 null이어서는 안 됩니다.");
        }
    }

    public void checkAuthorization(User user) {
        User momenter = moment.getMomenter();

        if (!this.commenter.equals(user) && !momenter.equals(user)) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }
    }
}
