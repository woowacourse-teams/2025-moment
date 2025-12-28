package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import moment.reward.domain.Reason;
import moment.reward.service.application.RewardApplicationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final RewardApplicationService rewardApplicationService;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
        commentApplicationService.validateCreateComment(request, userId);

        Moment moment = momentApplicationService.getMomentBy(request.momentId());
        CommentCreateResponse createdComment = commentApplicationService.createComment(request, userId);

        rewardApplicationService.rewardForComment(userId, Reason.COMMENT_CREATION, createdComment.commentId());

        publisher.publishEvent(CommentCreateEvent.of(moment));

        return createdComment;
    }
}
