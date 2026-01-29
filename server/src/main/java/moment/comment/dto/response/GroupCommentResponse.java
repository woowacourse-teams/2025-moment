package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.comment.domain.Comment;

@Schema(description = "그룹 코멘트 응답")
public record GroupCommentResponse(
    @Schema(description = "코멘트 ID", example = "1")
    Long commentId,

    @Schema(description = "코멘트 내용", example = "좋은 모멘트네요!")
    String content,

    @Schema(description = "작성자 닉네임", example = "닉네임")
    String memberNickname,

    @Schema(description = "작성자 멤버 ID", example = "1")
    Long memberId,

    @Schema(description = "좋아요 수", example = "5")
    long likeCount,

    @Schema(description = "현재 사용자의 좋아요 여부", example = "false")
    boolean hasLiked,

    @Schema(description = "이미지 URL", example = "https://example.com/images/sample.jpg")
    String imageUrl,

    @Schema(description = "생성 일시")
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
            null,
            comment.getCreatedAt()
        );
    }

    public static GroupCommentResponse from(Comment comment, long likeCount, boolean hasLiked, String imageUrl) {
        return new GroupCommentResponse(
            comment.getId(),
            comment.getContent(),
            comment.getMember() != null ? comment.getMember().getNickname() : null,
            comment.getMember() != null ? comment.getMember().getId() : null,
            likeCount,
            hasLiked,
            imageUrl,
            comment.getCreatedAt()
        );
    }
}
