package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.comment.dto.tobe.CommentComposition;

@Schema(description = "내 그룹 모멘트의 댓글 응답")
public record MyGroupMomentCommentResponse(
        @Schema(description = "댓글 ID", example = "1")
        Long id,

        @Schema(description = "작성자 멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "댓글 내용", example = "좋은 모멘트네요!")
        String content,

        @Schema(description = "작성자 닉네임", example = "닉네임")
        String memberNickname,

        @Schema(description = "이미지 URL", example = "https://example.com/images/sample.jpg")
        String imageUrl,

        @Schema(description = "생성 일시")
        LocalDateTime createdAt,

        @Schema(description = "좋아요 수", example = "5")
        long likeCount,

        @Schema(description = "현재 사용자의 좋아요 여부", example = "false")
        boolean hasLiked
) {
    public static MyGroupMomentCommentResponse of(
            CommentComposition composition,
            long likeCount,
            boolean hasLiked
    ) {
        return new MyGroupMomentCommentResponse(
                composition.id(),
                composition.memberId(),
                composition.content(),
                composition.nickname(),
                composition.imageUrl(),
                composition.commentCreatedAt(),
                likeCount,
                hasLiked
        );
    }
}
