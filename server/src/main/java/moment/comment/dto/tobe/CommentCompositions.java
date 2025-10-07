package moment.comment.dto.tobe;

import java.util.List;

public record CommentCompositions(
        List<CommentComposition> commentCompositions,
        String nextCursor,
        boolean hasNextPage,
        int pageSize
) {
}
