package moment.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.user.domain.User;
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
    public boolean existsByMomentAndCommenter(Moment moment, User user) {
        return commentRepository.existsByMomentAndCommenter(moment, user);
    }

    @Override
    public List<Comment> getAllByMomentIn(List<Moment> moments) {
        return commentRepository.findAllByMomentIn(moments);
    }

    @Override
    public Comment getCommentWithCommenterById(Long id) {
        return commentRepository.findWithCommenterById(id)
                .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
