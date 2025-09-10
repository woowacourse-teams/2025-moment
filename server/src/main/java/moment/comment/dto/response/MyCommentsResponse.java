package moment.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.reply.domain.Echo;

public record MyCommentsResponse(List<MyCommentResponse> myCommentsResponse) {

    public static MyCommentsResponse of(List<Comment> comments,
                                        Map<Moment, List<MomentTag>> momentTagsOfMoment) {

        return new MyCommentsResponse(comments.stream()
                .map(comment -> MyCommentResponse.from(comment,
                        momentTagsOfMoment.getOrDefault(comment.getMoment(), Collections.emptyList())))
                .toList());
    }

    public static MyCommentsResponse of(List<Comment> comments,
                                        Map<Comment, List<Echo>> commentAndEchos,
                                        Map<Moment, List<MomentTag>> momentTagsOfMoment) {

        return new MyCommentsResponse(comments.stream()
                .map(comment -> {
                    List<Echo> echoes = commentAndEchos.getOrDefault(comment, Collections.emptyList());
                    List<MomentTag> momentTags = momentTagsOfMoment.getOrDefault(comment.getMoment(),
                            Collections.emptyList());
                    return MyCommentResponse.from(comment, echoes, momentTags);
                }).toList());
    }

    @JsonValue
    public List<MyCommentResponse> getMyCommentsResponse() {
        return myCommentsResponse;
    }
}
