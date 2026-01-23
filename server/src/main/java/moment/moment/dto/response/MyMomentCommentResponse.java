package moment.moment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import moment.comment.dto.tobe.CommentComposition;

@Schema(description = "모멘트에 달린 코멘트 응답")
public record MyMomentCommentResponse(
        @Schema(description = "코멘트 id", example = "1")
        Long id,

        @Schema(description = "코멘트 내용", example = "안됐네요.")
        String content,

        @Schema(description = "코멘터 닉네임", example = "미미")
        String nickname,

        @Schema(description = "코멘트 이미지 url", example = "https://techcourse-project-2025.s3.ap-northeast-2.amazonaws.com/moment-dev/images/2f501dfa-9c7d-4579-9c10-daed5a5da3ff%EA%B3%A0%EC%96%91%EC%9D%B4.jpg")
        String imageUrl,

        @Schema(description = "코멘트 작성 시간", example = "2025-07-14T16:30:34Z")
        LocalDateTime createdAt
) {

    public static MyMomentCommentResponse from(CommentComposition commentComposition) {
        return new MyMomentCommentResponse(
                commentComposition.id(),
                commentComposition.content(),
                commentComposition.nickname(),
                commentComposition.imageUrl(),
                commentComposition.commentCreatedAt()
        );
    }
}
