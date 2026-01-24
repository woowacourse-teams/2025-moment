package moment.moment.dto.response;

import java.time.LocalDateTime;
import moment.comment.dto.tobe.CommentComposition;

public record MyGroupMomentCommentResponse(
        Long id,
        String content,
        String memberNickname,
        LocalDateTime createdAt
) {
    public static MyGroupMomentCommentResponse from(CommentComposition composition) {
        return new MyGroupMomentCommentResponse(
                composition.id(),
                composition.content(),
                composition.nickname(),
                composition.commentCreatedAt()
        );
    }
}
