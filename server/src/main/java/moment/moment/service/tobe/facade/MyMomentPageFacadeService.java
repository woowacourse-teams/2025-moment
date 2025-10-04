package moment.moment.service.tobe.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.tobe.application.CommentApplicationService;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.dto.response.tobe.MyMomentPageResponse;
import moment.moment.service.tobe.application.MomentApplicationService;
import moment.moment.service.tobe.application.MomentComposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyMomentPageFacadeService {

    private final MomentApplicationService momentApplicationService;
    private final CommentApplicationService commentApplicationService;

    public MyMomentPageResponse getMyMomentsPage(String nextCursor, int limit, Long momenterId) {
        return createMyMomentPage(
                () -> momentApplicationService.getMyMomentCompositions(
                        new Cursor(nextCursor),
                        new PageSize(limit),
                        momenterId)
        );
    }

    public MyMomentPageResponse getUnreadMyMomentsPage(String nextCursor, int limit, Long momenterId) {
        return createMyMomentPage(
                () -> momentApplicationService.getUnreadMyMomentCompositions(
                        new Cursor(nextCursor),
                        new PageSize(limit),
                        momenterId)
        );
    }

    private MyMomentPageResponse createMyMomentPage(MomentComposition momentComposition) {

        MomentCompositions momentCompositions = momentComposition.generate();

        List<Long> myMomentIds = momentCompositions.momentCompositionInfo().stream()
                .map(moment.moment.dto.response.tobe.MomentComposition::id)
                .toList();

        List<CommentComposition> commentCompositions = commentApplicationService.getMyCommentCompositions(
                myMomentIds);

        return MyMomentPageResponse.of(momentCompositions, commentCompositions);
    }
}
