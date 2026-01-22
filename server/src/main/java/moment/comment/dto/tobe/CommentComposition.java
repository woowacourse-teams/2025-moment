package moment.comment.dto.tobe;

import java.time.LocalDateTime;
import moment.comment.domain.Comment;
import moment.user.domain.User;

public record CommentComposition(
        Long id,
        String content,
        String nickname,
        String imageUrl,
        LocalDateTime commentCreatedAt,
        Long momentId
) {
    public static CommentComposition of(Comment comment,
                                        User commenter,
                                        String imageUrl) {
        return new CommentComposition(
                comment.getId(),
                comment.getContent(),
                commenter.getNickname(),
                imageUrl,
                comment.getCreatedAt(),
                comment.getMomentId()
        );
    }
}
