package moment.comment.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.reply.domain.Echo;

public record MyCommentsResponse(List<MyCommentResponse> myCommentsResponse) {

    public static MyCommentsResponse of(List<Comment> comments) {

        return new MyCommentsResponse(comments.stream()
                .map(MyCommentResponse::from)
                .toList());
    }

    public static MyCommentsResponse of(List<Comment> comments,
                                        Map<Comment, List<Echo>> commentAndEchos) {

        return new MyCommentsResponse(comments.stream()
                .map(comment -> {
                    List<Echo> echoes = commentAndEchos.getOrDefault(comment, Collections.emptyList());
                    return MyCommentResponse.from(comment, echoes);
                }).toList());
    }
}
