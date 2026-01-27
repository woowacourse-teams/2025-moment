package moment.comment.dto.response;

import java.time.LocalDateTime;
import moment.comment.domain.Comment;

public record GroupCommentResponse(
    Long commentId,
    String content,
    String memberNickname,
    Long memberId,
    long likeCount,
    boolean hasLiked,
    LocalDateTime createdAt
) {
    public static GroupCommentResponse from(Comment comment, long likeCount, boolean hasLiked) {
        return new GroupCommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getMember() != null ? comment.getMember().getNickname() : null,
            comment.getMember() != null ? comment.getMember().getId() : null,
            likeCount,
            hasLiked,
            comment.getCreatedAt()
        );
    }
}
