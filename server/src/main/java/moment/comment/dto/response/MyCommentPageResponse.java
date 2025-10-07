package moment.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.comment.dto.tobe.CommentCompositions;
import moment.moment.dto.response.tobe.MomentComposition;

@Schema(description = "나의 Comment 페이지 조회 응답")
public record MyCommentPageResponse(
        @Schema(description = "조회된 나의 Comment 목록 응답")
        MyCommentsResponse items,

        @Schema(description = "다음 페이지 시작 커서", example = "2025-07-21T10:57:08.926954_1")
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNextPage,

        @Schema(description = "페이지 사이즈 (기본 10)", example = "10")
        int pageSize
) {
    public static MyCommentPageResponse of(
            MyCommentsResponse responses,
            String nextCursor,
            boolean hasNextPage,
            int pageSize
    ) {
        return new MyCommentPageResponse(responses, nextCursor, hasNextPage, pageSize);
    }

    public static MyCommentPageResponse of(CommentCompositions myCommentCompositions,
                                           List<MomentComposition> myMomentCompositions,
                                           Map<Long, List<Long>> unreadNotificationsByCommentIds) {
        return new MyCommentPageResponse(
                MyCommentsResponse.of(myCommentCompositions.commentCompositions(), myMomentCompositions, unreadNotificationsByCommentIds),
                myCommentCompositions.nextCursor(),
                myCommentCompositions.hasNextPage(),
                myCommentCompositions.pageSize()
        )
    }
}
