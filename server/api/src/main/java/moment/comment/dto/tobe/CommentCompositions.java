package moment.comment.dto.tobe;

import java.util.List;

public record CommentCompositions(
        List<CommentComposition> commentCompositions,
        String nextCursor,
        boolean hasNextPage,
        int pageSize
) {


    public static CommentCompositions of(List<CommentComposition> commentCompositions,
                                         String extract,
                                         boolean hasNextPage,
                                         int size) {
        return new CommentCompositions(commentCompositions, extract, hasNextPage, size);
    }
}
