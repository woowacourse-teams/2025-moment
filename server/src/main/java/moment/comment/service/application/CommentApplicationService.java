package moment.comment.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.GroupCommentResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.dto.tobe.CommentCompositions;
import moment.comment.service.comment.CommentImageService;
import moment.comment.service.comment.CommentService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
import moment.like.service.CommentLikeService;
import moment.moment.domain.Moment;
import moment.moment.service.moment.MomentService;
import moment.storage.application.PhotoUrlResolver;
import moment.user.domain.User;
import moment.user.service.user.UserService;
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
    private final PhotoUrlResolver photoUrlResolver;
    private final GroupMemberService groupMemberService;
    private final MomentService momentService;
    private final CommentLikeService commentLikeService;

    public List<CommentComposition> getMyCommentCompositionsBy(List<Long> momentIds) {
        List<Comment> comments = commentService.getAllByMomentIds(momentIds);

        List<Long> commenterIds = extractCommenterIdsByComments(comments);

        List<User> commenters = userService.getAllBy(commenterIds);

        Map<Comment, User> commentersByComments = mapCommentersByComments(commenters, comments);

        Map<Comment, CommentImage> commentImageByComment = commentImageService.getCommentImageByComment(comments);

        return comments.stream()
                .map(comment -> {
                    CommentImage image = commentImageByComment.get(comment);
                    String resolvedImageUrl = (image != null) ? photoUrlResolver.resolve(image.getImageUrl()) : null;

                    return CommentComposition.of(
                            comment,
                            commentersByComments.get(comment),
                            resolvedImageUrl
                    );
                })
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
            commentImageService.deleteBy(commentId);
            commentService.deleteBy(commentId);
        }
    }

    public void validateCreateComment(CommentCreateRequest request, Long commenterId) {
        commentService.validateUniqueBy(request.momentId(), commenterId);
    }

    public CommentCreateResponse createComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userService.getUserBy(commenterId);
        Comment commentWithoutId = request.toComment(commenter, request.momentId());
        Comment savedComment = commentService.create(commentWithoutId);

        Optional<CommentImage> commentImage = commentImageService.create(
                savedComment,
                request.imageUrl(),
                request.imageName());

        return commentImage.map(image -> CommentCreateResponse.of(savedComment, image))
                .orElseGet(() -> CommentCreateResponse.from(savedComment));
    }

    public CommentCompositions getMyCommentCompositions(Cursor cursor, PageSize pageSize, Long commenterId) {
        User commenter = userService.getUserBy(commenterId);

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
        Map<Comment, CommentImage> commentImagesByComment = commentImageService.getCommentImageByComment(
                commentsWithoutCursor);

        return commentsWithoutCursor.stream()
                .map(comment -> {
                    CommentImage image = commentImagesByComment.get(comment);
                    String resolvedImageUrl = (image != null) ? photoUrlResolver.resolve(image.getImageUrl()) : null;

                    return CommentComposition.of(comment, commenter, resolvedImageUrl);
                })
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

        User commenter = userService.getUserBy(commenterId);

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

    @Transactional
    public GroupCommentResponse createCommentInGroup(Long groupId, Long momentId, Long userId, String content) {
        User commenter = userService.getUserBy(userId);
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
        Moment moment = momentService.getMomentBy(momentId);

        Comment comment = commentService.createWithMember(moment, commenter, member, content);
        return GroupCommentResponse.from(comment, 0L, false);
    }

    public List<GroupCommentResponse> getCommentsInGroup(Long groupId, Long momentId, Long userId) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
        List<Comment> comments = commentService.getAllByMomentIds(List.of(momentId));

        return comments.stream()
            .map(comment -> {
                long likeCount = commentLikeService.getCount(comment.getId());
                boolean hasLiked = commentLikeService.hasLiked(comment.getId(), member.getId());
                return GroupCommentResponse.from(comment, likeCount, hasLiked);
            })
            .toList();
    }

    @Transactional
    public void deleteCommentInGroup(Long groupId, Long commentId, Long userId) {
        Comment comment = commentService.getCommentBy(commentId);
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        if (!comment.getMember().getId().equals(member.getId())) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }

        commentService.deleteBy(commentId);
    }

    @Transactional
    public boolean toggleCommentLike(Long groupId, Long commentId, Long userId) {
        Comment comment = commentService.getCommentBy(commentId);
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);
        return commentLikeService.toggle(comment, member);
    }
}
