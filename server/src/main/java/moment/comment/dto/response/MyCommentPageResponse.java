package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "나의 Comment 페이지 조회 응답")
public record MyCommentPageResponse(
        @Schema(description = "조회된 나의 Comment 목록 응답")
        List<MyCommentResponse> items,

        @Schema(description = "다음 페이지 시작 커서", example = "2025-07-21T10:57:08.926954_1")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage,

        @Schema(description = "페이지 사이즈 (기본 10)", example = "10")
        int pageSize
) {
    public static MyCommentPageResponse of(
            List<MyCommentResponse> responses,
            String nextCursor,
            boolean hasNextPage,
            int pageSize
    ) {
        return new MyCommentPageResponse(responses, nextCursor, hasNextPage, pageSize);
    }
}
