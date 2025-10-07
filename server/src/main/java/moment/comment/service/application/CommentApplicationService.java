package moment.comment.service.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.domain.Echo;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.dto.tobe.CommentCompositions;
import moment.comment.service.comment.CommentImageService;
import moment.comment.service.comment.CommentService;
import moment.comment.service.comment.EchoService;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
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

    public List<CommentComposition> getMyCommentCompositionsBy(List<Long> momentIds) {
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

    public void validateCreateComment(CommentCreateRequest request, Long commenterId) {
        commentService.validateUniqueBy(request.momentId(), commenterId);
    }

    public CommentCreateResponse createComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userService.getUserById(commenterId);
        Comment commentWithoutId = request.toComment(commenter, request.momentId());
        Comment savedComment = commentService.create(commentWithoutId);

        Optional<CommentImage> commentImage = commentImageService.create(request, savedComment);
        
        return commentImage.map(image -> CommentCreateResponse.of(savedComment, image))
                .orElseGet(() -> CommentCreateResponse.from(savedComment));
    }

    public CommentCompositions getMyCommentCompositions(Cursor cursor, PageSize pageSize, Long commenterId) {
        User commenter = userService.getUserById(commenterId);
        
        List<Comment> commentsWithinCursor = commentService.getCommentsBy(commenter, cursor, pageSize);

        List<Comment> commentsWithoutCursor = removeCursor(commentsWithinCursor, pageSize);

        boolean hasNextPage = pageSize.hasNextPage(commentsWithinCursor.size());

        return CommentCompositions.of(
                mapCommentCompositionInfo(commentsWithoutCursor, commenter),
                cursor.extract(new ArrayList<>(commentsWithinCursor), hasNextPage),
                hasNextPage,
                commentsWithoutCursor.size()
        );
    }

    private List<CommentComposition> mapCommentCompositionInfo(List<Comment> commentsWithoutCursor, User commenter) {
        Map<Comment, CommentImage> commentImagesByComment = commentImageService.getCommentImageByComment(commentsWithoutCursor);

        Map<Comment, List<Echo>> echoesByComments = echoService.getEchosOfComments(commentsWithoutCursor);

        return commentsWithoutCursor.stream()
                .map(comment ->
                        CommentComposition.of(
                                comment,
                                commenter,
                                commentImagesByComment.get(comment),
                                echoesByComments.getOrDefault(comment, Collections.emptyList()))
                )
                .toList();
    }

    private List<Comment> removeCursor(List<Comment> commentsWithinCursor, PageSize pageSize) {
        if (pageSize.hasNextPage(commentsWithinCursor.size())) {
            return commentsWithinCursor.subList(0, pageSize.size());
        }
        return commentsWithinCursor;
    }

    public CommentCompositions getUnreadMyCommentCompositions(Cursor cursor,
                                               PageSize pageSize,
                                               Long commenterId,
                                               List<Long> unreadCommentIds) {

        User commenter = userService.getUserById(commenterId);

        List<Comment> commentsWithinCursor = commentService.getCommentsBy(unreadCommentIds, cursor, pageSize);

        List<Comment> commentsWithoutCursor = removeCursor(commentsWithinCursor, pageSize);

        boolean hasNextPage = pageSize.hasNextPage(commentsWithinCursor.size());

        return CommentCompositions.of(
                mapCommentCompositionInfo(commentsWithoutCursor, commenter),
                cursor.extract(new ArrayList<>(commentsWithinCursor), hasNextPage),
                hasNextPage,
                commentsWithoutCursor.size()
        );
    }
}
