package moment.moment.service.tobe.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.tobe.application.CommentApplicationService;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.service.tobe.application.MomentApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentFacadeService {

    private final MomentApplicationService momentApplicationService;
     private final CommentApplicationService commentApplicationService;
    // private final NotificationService notificationService;

    public MyMomentPageResponse getMyMomentsPage(String nextCursor, int limit, Long momenterId) {
        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(limit);

        List<MomentComposition> myMomentCompositions = momentApplicationService.getMyMomentCompositions(
                cursor, 
                pageSize, 
                momenterId);

        List<Long> myMomentIds = myMomentCompositions.stream()
                .map(MomentComposition::id)
                .toList();

        List<CommentComposition> myCommentCompositions = commentApplicationService.getMyCommentCompositions(
                myMomentIds);

        return null;
    }
}
