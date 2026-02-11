package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.block.service.application.UserBlockApplicationService;
import moment.comment.dto.CommentCreateEvent;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final UserBlockApplicationService userBlockApplicationService;

    private final ApplicationEventPublisher publisher;

    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
        commentApplicationService.validateCreateComment(request, userId);

        Moment moment = momentApplicationService.getMomentBy(request.momentId());

        if (userBlockApplicationService.isBlocked(userId, moment.getMomenter().getId())) {
            throw new MomentException(ErrorCode.BLOCKED_USER_INTERACTION);
        }

        CommentCreateResponse createdComment = commentApplicationService.createComment(request, userId);

        if (!moment.getMomenterId().equals(userId)) {
            publisher.publishEvent(CommentCreateEvent.of(moment, userId));
        }

        return createdComment;
    }
}
