package moment.comment.service.tobe.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.domain.Echo;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.tobe.comment.CommentImageService;
import moment.comment.service.tobe.comment.CommentService;
import moment.comment.service.tobe.comment.EchoService;
import moment.user.application.tobe.user.UserService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentApplicationService {

    private static final int COMMENT_DELETE_THRESHOLD = 1;

    private final UserService userService;
    private final CommentService commentService;
    private final CommentImageService commentImageService;
    private final EchoService echoService;

    public List<CommentComposition> getMyCommentCompositions(List<Long> momentIds) {
        List<Comment> comments = commentService.getAllByMomentIds(momentIds);

        List<Long> commenterIds = extractCommenterIdsByComments(comments);

        List<User> commenters = userService.getAllByIds(commenterIds);

        Map<Comment, User> commentersByComments = mapCommentersByComments(commenters, comments);

        Map<Comment, CommentImage> commentImageByComment = commentImageService.getCommentImageByComment(comments);

        Map<Comment, List<Echo>> echosByComments = echoService.getEchosOfComments(comments);

        return comments.stream()
                .map(comment -> CommentComposition.of(
                        comment,
                        commentersByComments.get(comment),
                        commentImageByComment.get(comment),
                        echosByComments.get(comment)))
                .toList();
    }

    public List<Long> getMomentIdsNotCommentedByMe(List<Long> momentIds, Long commenterId) {
        return commentService.getMomentIdsNotCommentedByMe(momentIds, commenterId);
    }

    private List<Long> extractCommenterIdsByComments(List<Comment> comments) {
        return comments.stream()
                .map(comment -> comment.getCommenter().getId())
                .toList();
    }

    private Map<Comment, User> mapCommentersByComments(List<User> commenters, List<Comment> comments) {
        Map<Long, User> userById = commenters.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return comments.stream()
                .collect(Collectors.toMap(
                        comment -> comment,
                        comment -> userById.get(comment.getCommenter().getId())
                ));
    }

    @Transactional
    public void deleteByReport(Long commentId, Long reportCount) {
        if (reportCount >= COMMENT_DELETE_THRESHOLD) {
            echoService.deleteBy(commentId);
            commentImageService.deleteBy(commentId);
            commentService.deleteBy(commentId);
        }
    }
}
