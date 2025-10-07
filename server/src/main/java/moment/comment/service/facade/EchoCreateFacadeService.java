package moment.comment.service.facade;


import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentForEcho;
import moment.comment.service.application.CommentApplicationService;
import moment.global.domain.TargetType;
import moment.moment.service.application.MomentApplicationService;
import moment.notification.domain.NotificationType;
import moment.notification.service.application.NotificationApplicationService;
import moment.comment.dto.request.EchoCreateRequest;
import moment.reward.domain.Reason;
import moment.reward.service.application.RewardApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EchoCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final RewardApplicationService rewardApplicationService;

    @Transactional
    public void createEchos(EchoCreateRequest request, Long momenterId) {
        CommentForEcho commentForEcho = commentApplicationService.getCommentForEchoBy(request.commentId());

        momentApplicationService.validateMomenter(commentForEcho.momentId(), momenterId);

        commentApplicationService.createEcho(request.commentId(), momenterId, request.echoTypes());

        notificationApplicationService.createNotificationAndSendSse(
                momenterId,
                request.commentId(),
                NotificationType.NEW_REPLY_ON_COMMENT,
                TargetType.COMMENT
        );

        rewardApplicationService.rewardForEcho(commentForEcho.commenterId(), Reason.ECHO_RECEIVED, request.commentId());
    }
}
