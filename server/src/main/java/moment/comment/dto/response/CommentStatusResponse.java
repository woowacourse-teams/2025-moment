package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Comment 등록 전 상태 체크 응답")
public record CommentStatusResponse(
        @Schema(description = "코멘트를 달 모멘트가 매칭이 된 상태인지 여부", example = "true")
        boolean isAlreadyMatched,

        @Schema(description = "이미 매칭된 모멘트에 대해 코멘트를 달았는지 여부", example = "false")
        boolean isAlreadyCommented
) {
    public static CommentStatusResponse createNotMatchedStatus() {
        return new CommentStatusResponse(false, false);
    }

    public static CommentStatusResponse createAlreadyCommentedStatus() {
        return new CommentStatusResponse(true, true);
    }

    public static CommentStatusResponse createWritingAvailableStatus() {
        return new CommentStatusResponse(true, false);
    }
}
