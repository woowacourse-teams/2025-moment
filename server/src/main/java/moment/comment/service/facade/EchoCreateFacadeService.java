package moment.comment.service.facade;


import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentForEcho;
import moment.comment.dto.EchoCreateEvent;
import moment.comment.dto.request.EchoCreateRequest;
import moment.comment.service.application.CommentApplicationService;
import moment.moment.service.application.MomentApplicationService;
import moment.notification.service.application.NotificationApplicationService;
import moment.reward.domain.Reason;
import moment.reward.service.application.RewardApplicationService;
import org.springframework.context.ApplicationEventPublisher;
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

    private final ApplicationEventPublisher publisher;

    @Transactional
    public void createEchos(EchoCreateRequest request, Long momenterId) {
        CommentForEcho commentForEcho = commentApplicationService.getCommentForEchoBy(request.commentId());
        momentApplicationService.validateMomenter(commentForEcho.momentId(), momenterId);

        commentApplicationService.createEcho(request.commentId(), momenterId, request.echoTypes());
        rewardApplicationService.rewardForEcho(commentForEcho.commenterId(), Reason.ECHO_RECEIVED, request.commentId());

        // 에코가 코멘트에 달림 -> 코멘트를 작성한 사람에게 알림 전송 & 저장
        // 해당 코멘트에 대한 알림 -> 코멘트에 알림을 저장해야 함
        publisher.publishEvent(new EchoCreateEvent(request.commentId(), commentForEcho.commenterId()));

//        notificationApplicationService.createNotificationAndSendSse(
//                commentForEcho.commenterId(),
//                request.commentId(),
//                NotificationType.NEW_REPLY_ON_COMMENT,
//                TargetType.COMMENT
//        );

    }
}
