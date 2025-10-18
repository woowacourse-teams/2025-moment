package moment.moment.service.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.service.application.CommentApplicationService;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.service.application.MomentApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentableMomentFacadeService {

    private final MomentApplicationService momentApplicationService;
    private final CommentApplicationService commentApplicationService;

    public CommentableMomentResponse getCommentableMoment(Long commenterId, List<String> tagNames) {
        List<Long> momentIds = momentApplicationService.getCommentableMoment(commenterId);

        List<Long> momentIdsNotCommentedByMe = commentApplicationService.getMomentIdsNotCommentedByMe(momentIds,
                commenterId);

        return momentApplicationService.pickRandomMomentComposition(momentIdsNotCommentedByMe, tagNames);
    }
}
