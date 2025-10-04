package moment.comment.service.facade;

import lombok.RequiredArgsConstructor;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.service.tobe.application.CommentApplicationService;
import moment.moment.domain.Moment;
import moment.moment.service.tobe.application.MomentApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCreateFacadeService {

    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;

    public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
        commentApplicationService.validateCreateComment(request, userId);
        Moment moment = momentApplicationService.getMomentBy(request.momentId());
        // TODO
    }
}
