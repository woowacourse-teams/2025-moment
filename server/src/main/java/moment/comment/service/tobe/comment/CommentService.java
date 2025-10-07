package moment.comment.service.tobe.comment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.user.domain.User;
import org.springframework.data.domain.PageRequest;
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

    @Transactional
    public void deleteBy(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public void validateUniqueBy(Long momentId, Long commenterId) {
        boolean isExists = commentRepository.existsByMomentIdAndCommenterId(momentId, commenterId);
        if (isExists) {
            throw new MomentException(ErrorCode.COMMENT_CONFLICT);
        }
    }

    @Transactional
    public Comment create(Comment commentWithoutId) {
        return commentRepository.save(commentWithoutId);
    }

    public List<Comment> getCommentsBy(User commenter, Cursor cursor, PageSize pageSize) {
        PageRequest pageable = pageSize.getPageRequest();
        if (cursor.isFirstPage()) {
            List<Long> firstPageCommentIds = commentRepository.findFirstPageCommentIdsByCommenter(commenter,
                    pageable);
            return commentRepository.findCommentsWithDetailsByIds(firstPageCommentIds);
        }
        List<Long> nextPageCommentIds = commentRepository.findNextPageCommentIdsByCommenter(commenter,
                cursor.dateTime(), cursor.id(), pageable);
        return commentRepository.findCommentsWithDetailsByIds(nextPageCommentIds);
    }
}
