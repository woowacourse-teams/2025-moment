package moment.comment.application;

import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultCommentQueryService implements CommentQueryService {

    private final CommentRepository commentRepository;

    @Override
    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Override
    public boolean existsByMoment(Moment moment) {
        return commentRepository.existsByMoment(moment);
    }
}
