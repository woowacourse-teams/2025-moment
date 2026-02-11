package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 내 나의 Comment 목록 응답")
public record MyGroupCommentListResponse(
        @Schema(description = "조회된 나의 Comment 목록")
        List<MyGroupCommentResponse> comments,

        @Schema(description = "다음 페이지 시작 커서", example = "123")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage
) {
    public static MyGroupCommentListResponse of(List<MyGroupCommentResponse> comments, Long nextCursor) {
        return new MyGroupCommentListResponse(comments, nextCursor, nextCursor != null);
    }

    public static MyGroupCommentListResponse empty() {
        return new MyGroupCommentListResponse(List.of(), null, false);
    }
}
