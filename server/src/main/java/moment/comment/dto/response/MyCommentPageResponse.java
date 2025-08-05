package moment.comment.dto.response;

import java.util.List;

public record MyCommentPageResponse(
        List<MyCommentsResponse> items,
        String nextCursor,
        boolean hasNextPage,
        int pageSize
) {
    public static MyCommentPageResponse of(
            List<MyCommentsResponse> responses,
            String nextCursor,
            boolean hasNextPage,
            int pageSize
    ) {
        return new MyCommentPageResponse(responses, nextCursor, hasNextPage, pageSize);
    }
}
