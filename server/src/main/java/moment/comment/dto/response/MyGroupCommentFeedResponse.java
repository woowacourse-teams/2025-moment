package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 내 나의 Comment 피드 응답")
public record MyGroupCommentFeedResponse(
        @Schema(description = "조회된 나의 Comment 목록")
        List<MyGroupCommentResponse> comments,

        @Schema(description = "다음 페이지 시작 커서", example = "123")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage
) {
    public static MyGroupCommentFeedResponse of(List<MyGroupCommentResponse> comments, Long nextCursor) {
        return new MyGroupCommentFeedResponse(comments, nextCursor, nextCursor != null);
    }

    public static MyGroupCommentFeedResponse empty() {
        return new MyGroupCommentFeedResponse(List.of(), null, false);
    }
}
