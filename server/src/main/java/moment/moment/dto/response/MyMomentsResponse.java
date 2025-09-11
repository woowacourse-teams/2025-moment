package moment.moment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moment.comment.domain.Comment;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.reply.domain.Echo;

public record MyMomentsResponse(List<MyMomentResponse> myMomentsResponse) {

    public static MyMomentsResponse of(List<Moment> moments,
                                       Map<Moment, List<Comment>> commentsByMoment,
                                       Map<Comment, List<Echo>> echosByComment,
                                       Map<Moment, List<MomentTag>> momentTagsByMoment) {

        List<MyMomentResponse> myMomentResponses = moments.stream()
                .map(moment -> {
                    List<Comment> momentComments = commentsByMoment.getOrDefault(moment, List.of());

                    Map<Long, List<Echo>> commentEchos = getEchosFromCommentsOfMoment(echosByComment, momentComments);

                    List<MomentTag> momentTag = momentTagsByMoment.getOrDefault(moment, List.of());

                    return MyMomentResponse.of(moment, momentComments, commentEchos, momentTag);
                })
                .toList();

        return new MyMomentsResponse(myMomentResponses);
    }

    private static Map<Long, List<Echo>> getEchosFromCommentsOfMoment(Map<Comment, List<Echo>> echosByComment,
                                                                      List<Comment> momentComments) {
        return momentComments.stream()
                .collect(Collectors.toMap(Comment::getId,
                        comment -> echosByComment.getOrDefault(comment, List.of())));
    }

    public static MyMomentsResponse of(List<Moment> moments,
                                       Map<Moment, List<MomentTag>> momentTagsByMoment) {

        List<MyMomentResponse> myMomentResponses = moments.stream()
                .map(moment -> MyMomentResponse.of(
                        moment,
                        Collections.emptyList(),
                        Collections.emptyMap(),
                        momentTagsByMoment.getOrDefault(moment, Collections.emptyList())))
                .toList();

        return new MyMomentsResponse(myMomentResponses);
    }

    @JsonValue
    public List<MyMomentResponse> getMyMomentsResponse() {
        return myMomentsResponse;
    }
}
