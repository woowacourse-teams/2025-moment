package moment.comment.service.tobe.comment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    public List<Comment> getAllByMomentIds(List<Long> momentIds) {
        return commentRepository.findAllByMomentIdIn(momentIds);
    }

    public List<Long> getMomentIdsNotCommentedByMe(List<Long> momentIds, Long commenterId) {
        return commentRepository.findMomentIdsCommentedOnByOthers(momentIds, commenterId);
    }
}
