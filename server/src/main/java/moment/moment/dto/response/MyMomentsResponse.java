package moment.moment.dto.response;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.notification.domain.Notification;
import moment.comment.domain.Echo;

public record MyMomentsResponse(List<MyMomentResponse> myMomentsResponse) {

    public static MyMomentsResponse of(List<Moment> moments,
                                       Map<Moment, List<Comment>> commentsByMoment,
                                       Map<Comment, List<Echo>> echosByComment,
                                       Map<Moment, List<MomentTag>> momentTagsByMoment,
                                       Map<Moment, MomentImage> momentImages,
                                       Map<Comment, CommentImage> commentImages,
                                       Map<Moment, List<Notification>> notificationsForMoments) {

        List<MyMomentResponse> myMomentResponses = moments.stream()
                .map(moment -> {
                    List<Comment> momentComments = commentsByMoment.getOrDefault(moment, Collections.emptyList());

                    Map<Long, List<Echo>> commentEchos = getEchosFromCommentsOfMoment(echosByComment, momentComments);

                    List<MomentTag> momentTag = momentTagsByMoment.getOrDefault(moment, Collections.emptyList());

                    MomentImage momentImage = momentImages.getOrDefault(moment, null);

                    List<Notification> notificationsForMoment = notificationsForMoments.getOrDefault(
                            moment,
                            Collections.emptyList());

                    return MyMomentResponse.of(
                            moment,
                            momentComments,
                            commentEchos,
                            momentTag,
                            momentImage,
                            commentImages,
                            notificationsForMoment);
                })
                .toList();

        return new MyMomentsResponse(myMomentResponses);
    }

    private static Map<Long, List<Echo>> getEchosFromCommentsOfMoment(Map<Comment, List<Echo>> echosByComment,
                                                                      List<Comment> momentComments) {
        return momentComments.stream()
                .collect(Collectors.toMap(Comment::getId,
                        comment -> echosByComment.getOrDefault(comment, Collections.emptyList())));
    }

    public static MyMomentsResponse of(List<Moment> moments,
                                       Map<Moment, List<MomentTag>> momentTagsByMoment,
                                       Map<Moment, MomentImage> momentImages,
                                       Map<Moment, List<Notification>> notificationsForMoments) {

        List<MyMomentResponse> myMomentResponses = moments.stream()
                .map(moment -> MyMomentResponse.of(
                        moment,
                        Collections.emptyList(),
                        Collections.emptyMap(),
                        momentTagsByMoment.getOrDefault(moment, Collections.emptyList()),
                        momentImages.getOrDefault(moment, null),
                        Collections.emptyMap(),
                        notificationsForMoments.getOrDefault(moment, Collections.emptyList())
                ))
                .toList();

        return new MyMomentsResponse(myMomentResponses);
    }

    @JsonValue
    public List<MyMomentResponse> getMyMomentsResponse() {
        return myMomentsResponse;
    }
}
