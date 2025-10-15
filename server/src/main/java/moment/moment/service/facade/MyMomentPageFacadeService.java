package moment.moment.service.facade;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.application.CommentApplicationService;
import moment.global.domain.TargetType;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.dto.response.tobe.MyMomentPageResponse;
import moment.moment.service.application.MomentApplicationService;
import moment.moment.service.application.MomentComposition;
import moment.notification.service.application.NotificationApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyMomentPageFacadeService {

    private final MomentApplicationService momentApplicationService;
    private final CommentApplicationService commentApplicationService;
    private final NotificationApplicationService notificationApplicationService;

    public MyMomentPageResponse getMyMomentsPage(String nextCursor, int limit, Long momenterId) {
        return createMyMomentPage(
                () -> momentApplicationService.getMyMomentCompositions(
                        new Cursor(nextCursor),
                        new PageSize(limit),
                        momenterId)
        );
    }

    public MyMomentPageResponse getUnreadMyMomentsPage(String nextCursor, int limit, Long momenterId) {
        List<Long> unreadMomentIds = notificationApplicationService.getUnreadNotifications(momenterId,
                TargetType.MOMENT);

        return createMyMomentPage(
                () -> momentApplicationService.getUnreadMyMomentCompositions(
                        new Cursor(nextCursor),
                        new PageSize(limit),
                        momenterId,
                        unreadMomentIds)
        );
    }

    private MyMomentPageResponse createMyMomentPage(MomentComposition momentComposition) {

        MomentCompositions momentCompositions = momentComposition.generate();

        List<Long> myMomentIds = momentCompositions.momentCompositionInfo().stream()
                .map(moment.moment.dto.response.tobe.MomentComposition::id)
                .toList();

        Map<Long, List<Long>> unreadNotificationsByMomentIds
                = notificationApplicationService.getNotificationsByTargetIdsAndTargetType(myMomentIds, TargetType.MOMENT);

        List<CommentComposition> commentCompositions = commentApplicationService.getMyCommentCompositionsBy(
                myMomentIds);

        return MyMomentPageResponse.of(momentCompositions, commentCompositions, unreadNotificationsByMomentIds);
    }
}
