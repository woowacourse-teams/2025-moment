package moment.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moment.comment.dto.tobe.CommentComposition;
import moment.moment.dto.response.tobe.MomentComposition;

public record MyCommentsResponse(List<MyCommentResponse> myCommentsResponse) {

    public static MyCommentsResponse of(List<CommentComposition> commentCompositions,
                                        List<MomentComposition> momentCompositions,
                                        Map<Long, List<Long>> unreadNotificationsByCommentIds) {

        Map<Long, MomentComposition> momentCompositionsByMomentId = momentCompositions.stream()
                .collect(Collectors.toMap(MomentComposition::id,
                        momentComposition -> momentComposition));

        List<MyCommentResponse> myCommentResponses = commentCompositions.stream()
                .map(commentComposition -> MyCommentResponse.of(
                        commentComposition, momentCompositionsByMomentId.get(commentComposition.momentId()),
                        unreadNotificationsByCommentIds.getOrDefault(commentComposition.id(), Collections.emptyList())))
                .toList();
        
        return new MyCommentsResponse(myCommentResponses);
    }

    @JsonValue
    public List<MyCommentResponse> getMyCommentsResponse() {
        return myCommentsResponse;
    }
}
