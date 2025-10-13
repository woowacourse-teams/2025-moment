package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.global.domain.TargetType;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import moment.notification.domain.NotificationType;
import moment.notification.service.application.NotificationApplicationService;
import moment.reward.service.application.RewardApplicationService;
import moment.reward.domain.Reason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final RewardApplicationService rewardApplicationService;

    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
        commentApplicationService.validateCreateComment(request, userId);
        Moment moment = momentApplicationService.getMomentBy(request.momentId());
        CommentCreateResponse createdComment = commentApplicationService.createComment(request, userId);

        rewardApplicationService.rewardForComment(userId, Reason.COMMENT_CREATION, createdComment.commentId());

        notificationApplicationService.createNotificationAndSendSse(
                moment.getMomenterId(),
                createdComment.commentId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT
        );

        return createdComment;
    }
}
