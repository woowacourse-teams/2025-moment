package moment.moment.dto.response;

import java.time.LocalDateTime;
import moment.comment.dto.tobe.CommentComposition;

public record MyGroupMomentCommentResponse(
        Long id,
        String content,
        String memberNickname,
        LocalDateTime createdAt,
        long likeCount,
        boolean hasLiked
) {
    public static MyGroupMomentCommentResponse of(
            CommentComposition composition,
            long likeCount,
            boolean hasLiked
    ) {
        return new MyGroupMomentCommentResponse(
                composition.id(),
                composition.content(),
                composition.nickname(),
                composition.commentCreatedAt(),
                likeCount,
                hasLiked
        );
    }
}
