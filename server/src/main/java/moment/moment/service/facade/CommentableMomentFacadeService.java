package moment.moment.service.facade;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.service.application.CommentApplicationService;
import moment.group.service.group.GroupMemberService;
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
    private final GroupMemberService groupMemberService;

    public CommentableMomentResponse getCommentableMomentInGroup(Long groupId, Long commenterId) {
        groupMemberService.getByGroupAndUser(groupId, commenterId);

        List<Long> momentIds = momentApplicationService.getCommentableMomentIdsInGroup(groupId, commenterId);
        if (momentIds.isEmpty()) {
            return CommentableMomentResponse.empty();
        }
        List<Long> momentIdsNotCommentedByMe = commentApplicationService.getMomentIdsNotCommentedByMe(momentIds,
                commenterId);
        return momentApplicationService.pickRandomMomentComposition(momentIdsNotCommentedByMe);
    }
}
