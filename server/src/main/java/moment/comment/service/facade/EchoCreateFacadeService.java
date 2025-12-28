package moment.comment.service.facade;


import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentForEcho;
import moment.comment.dto.EchoCreateEvent;
import moment.comment.dto.request.EchoCreateRequest;
import moment.comment.service.application.CommentApplicationService;
import moment.moment.service.application.MomentApplicationService;
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
    private final RewardApplicationService rewardApplicationService;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public void createEchos(EchoCreateRequest request, Long momenterId) {
        CommentForEcho commentForEcho = commentApplicationService.getCommentForEchoBy(request.commentId());
        momentApplicationService.validateMomenter(commentForEcho.momentId(), momenterId);

        commentApplicationService.createEcho(request.commentId(), momenterId, request.echoTypes());
        rewardApplicationService.rewardForEcho(commentForEcho.commenterId(), Reason.ECHO_RECEIVED, request.commentId());

        publisher.publishEvent(new EchoCreateEvent(request.commentId(), commentForEcho.commenterId()));
    }
}
