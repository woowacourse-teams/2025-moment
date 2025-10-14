package moment.moment.dto.response.tobe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moment.comment.dto.tobe.CommentComposition;

public record MyMomentsResponse(List<MyMomentResponse> myMomentsResponse) {

    public static MyMomentsResponse of(List<MomentComposition> momentCompositionInfo,
                                       List<CommentComposition> commentCompositionInfo,
                                       Map<Long, List<Long>> unreadNotificationsByMomentIds) {

        Map<Long, List<CommentComposition>> commentCompositionsByMomentIds = commentCompositionInfo.stream()
                .collect(Collectors.groupingBy(CommentComposition::momentId));

        List<MyMomentResponse> myMomentsResponse = momentCompositionInfo.stream()
                .map(momentComposition -> MyMomentResponse.of(
                        momentComposition,
                        commentCompositionsByMomentIds.getOrDefault(momentComposition.id(), List.of()),
                        unreadNotificationsByMomentIds.getOrDefault(momentComposition.id(), List.of())
                )).toList();

        return new MyMomentsResponse(myMomentsResponse);
    }
}
