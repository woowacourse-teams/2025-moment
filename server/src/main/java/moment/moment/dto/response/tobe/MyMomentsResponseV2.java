package moment.moment.dto.response.tobe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import moment.comment.dto.tobe.CommentComposition;

public record MyMomentsResponseV2(List<MyMomentResponseV2> myMomentsResponse) {

    public static MyMomentsResponseV2 of(List<MomentComposition> momentCompositionInfo,
                                         List<CommentComposition> commentCompositionInfo) {

        Map<Long, List<CommentComposition>> commentCompositionsByMomentIds = commentCompositionInfo.stream()
                .collect(Collectors.groupingBy(CommentComposition::momentId));

        List<MyMomentResponseV2> myMomentsResponse = momentCompositionInfo.stream()
                .map(momentComposition -> MyMomentResponseV2.of(
                        momentComposition, commentCompositionsByMomentIds.get(momentComposition.id())
                )).toList();

        return new MyMomentsResponseV2(myMomentsResponse);
    }
}
