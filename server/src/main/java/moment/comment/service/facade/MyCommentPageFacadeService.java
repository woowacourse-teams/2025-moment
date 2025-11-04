package moment.comment.service.facade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.dto.tobe.CommentCompositions;
import moment.comment.service.application.CommentApplicationService;
import moment.comment.service.application.CommentComposable;
import moment.global.domain.TargetType;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.service.application.MomentApplicationService;
import moment.notification.service.application.NotificationApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCommentPageFacadeService {
    
    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;

    public MyCommentPageResponse getMyCommentsPage(String nextCursor, int limit, Long commenterId) {
        return createMyCommentPage(() -> commentApplicationService.getMyCommentCompositions(
                new Cursor(nextCursor),
                new PageSize(limit),
                commenterId));
    }

    public MyCommentPageResponse getUnreadMyCommentsPage(String nextCursor, int limit, Long commenterId) {
        List<Long> unreadCommentIds = notificationApplicationService.getUnreadNotifications(
                commenterId, TargetType.COMMENT);

        if (unreadCommentIds == null || unreadCommentIds.isEmpty()) {
            return createMyCommentPage(() -> CommentCompositions.of(
                    Collections.emptyList(),
                    null,
                    false,
                    0)
            );
        }

        return createMyCommentPage(() -> commentApplicationService.getUnreadMyCommentCompositions(
                new Cursor(nextCursor),
                new PageSize(limit),
                commenterId,
                unreadCommentIds));

    }

    private MyCommentPageResponse createMyCommentPage(CommentComposable commentComposable) {

        CommentCompositions commentCompositions = commentComposable.generate();

        List<Long> momentIds = commentCompositions.commentCompositions().stream()
                .map(CommentComposition::momentId)
                .toList();

        List<MomentComposition> myMomentCompositions = momentApplicationService.getMyMomentCompositionsBy(momentIds);

        List<Long> commentIds = commentCompositions.commentCompositions().stream()
                .map(CommentComposition::id)
                .toList();

        Map<Long, List<Long>> unreadNotificationsByCommentIds
                = notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
                commentIds, TargetType.COMMENT);

        return MyCommentPageResponse.of(commentCompositions, myMomentCompositions, unreadNotificationsByCommentIds);
    }
}
