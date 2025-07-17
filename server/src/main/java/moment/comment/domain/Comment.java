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
        validateContent(content);
        validateCommenter(commenter);
        validateMoment(moment);
        this.content = content;
        this.commenter = commenter;
        this.moment = moment;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new MomentException(ErrorCode.COMMENT_INVALID);
        }

        if (content.length() > 100) {
            throw new MomentException(ErrorCode.COMMENT_INVALID);
        }
    }

    private void validateCommenter(User commenter) {
        if (commenter == null) {
            throw new MomentException(ErrorCode.COMMENT_INVALID);
        }
    }

    private void validateMoment(Moment moment) {
        if (moment == null) {
            throw new MomentException(ErrorCode.COMMENT_INVALID);
        }
    }
}
