package moment.comment.dto.tobe;

import java.time.LocalDateTime;
import moment.comment.domain.Comment;
import moment.user.domain.User;

public record CommentComposition(
        Long id,
        String content,
        String nickname,
        String originalImageUrl,
        String optimizedImageUrl,
        LocalDateTime commentCreatedAt,
        Long momentId,
        Long commenterUserId,
        Long memberId
) {
    public static CommentComposition of(Comment comment,
                                        User commenter,
                                        String originalImageUrl,
                                        String optimizedImageUrl
    ) {
        return new CommentComposition(
                comment.getId(),
                comment.getContent(),
                commenter != null ? commenter.getNickname() : "탈퇴한 사용자",
                originalImageUrl,
                optimizedImageUrl,
                comment.getCreatedAt(),
                comment.getMomentId(),
                commenter != null ? commenter.getId() : null,
                comment.getMember() != null ? comment.getMember().getId() : null
        );
    }
}
