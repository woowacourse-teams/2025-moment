package moment.comment.service.comment;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
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
        return commentRepository.findMomentIdsNotCommentedOnByMe(momentIds, commenterId);
    }

    @Transactional
    public void deleteBy(Long commentId) {
        commentRepository.deleteById(commentId);
        commentRepository.flush();
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
            return commentRepository.findCommentsByIds(firstPageCommentIds);
        }
        List<Long> nextPageCommentIds = commentRepository.findNextPageCommentIdsByCommenter(commenter,
                cursor.dateTime(), cursor.id(), pageable);
        return commentRepository.findCommentsByIds(nextPageCommentIds);
    }

    public List<Comment> getCommentsBy(List<Long> commentIds, Cursor cursor, PageSize pageSize) {
        PageRequest pageable = pageSize.getPageRequest();

        if (cursor.isFirstPage()) {
            return commentRepository.findUnreadCommentsFirstPage(commentIds, pageable);
        }
        return commentRepository.findUnreadCommentsNextPage(commentIds, cursor.dateTime(), cursor.id(), pageable);
    }

    public Long getMomentIdBy(Long commentId) {
        return commentRepository.findMomentIdById(commentId)
                .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    public Comment getCommentBy(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new MomentException(ErrorCode.COMMENT_NOT_FOUND));
    }

    @Transactional
    public Comment createWithMember(Moment moment, User commenter, GroupMember member, String content) {
        Comment comment = new Comment(moment, commenter, member, content);
        return commentRepository.save(comment);
    }

    public long countByMomentId(Long momentId) {
        return commentRepository.countByMomentId(momentId);
    }
}
