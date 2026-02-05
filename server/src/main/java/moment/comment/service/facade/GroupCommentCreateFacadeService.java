package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.event.GroupCommentCreateEvent;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.moment.domain.Moment;
import moment.moment.service.application.MomentApplicationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupCommentCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public GroupCommentResponse createGroupComment(
            Long groupId, Long momentId, Long userId,
            String content, String imageUrl, String imageName) {
        Moment moment = momentApplicationService.getMomentBy(momentId);
        GroupCommentResponse response = commentApplicationService.createCommentInGroup(
                groupId, momentId, userId, content, imageUrl, imageName);

        if (!moment.getMomenterId().equals(userId)) {
            publisher.publishEvent(new GroupCommentCreateEvent(
                    groupId, momentId, moment.getMomenterId(),
                    response.commentId(), userId, response.memberNickname()));
        }
        return response;
    }
}
