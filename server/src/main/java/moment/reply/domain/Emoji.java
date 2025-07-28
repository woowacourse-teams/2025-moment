package moment.reply.domain;

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
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import moment.comment.domain.Comment;
import moment.global.domain.BaseEntity;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;

@Entity(name = "emojis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class Emoji extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private EmojiType emojiType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "comment_id")
    private Comment comment;

    public Emoji(EmojiType emojiType, User user, Comment comment) {
        validate(user, comment);
        this.emojiType = emojiType;
        this.user = user;
        this.comment = comment;
    }

    private void validate(User user, Comment comment) {
        validateUser(user);
        validateComment(comment);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user가 null이 되어서는 안 됩니다.");
        }
    }

    private void validateComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("comment가 null이 되어서는 안 됩니다.");
        }
    }

    public void checkWriter(User writer) {
        if (!user.equals(writer)) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }
    }
}
