package moment.comment.service.facade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.response.MyGroupCommentListResponse;
import moment.comment.dto.response.MyGroupCommentResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.application.CommentApplicationService;
import moment.comment.service.comment.CommentService;

import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
import moment.like.service.CommentLikeService;
import moment.like.service.MomentLikeService;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.service.application.MomentApplicationService;
import moment.notification.service.application.NotificationApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyGroupCommentPageFacadeService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CommentService commentService;
    private final CommentApplicationService commentApplicationService;
    private final GroupMemberService groupMemberService;
    private final MomentApplicationService momentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final CommentLikeService commentLikeService;
    private final MomentLikeService momentLikeService;

    public MyGroupCommentListResponse getMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Comment> comments = commentService.getMyCommentsInGroup(
                member.getId(), cursor, DEFAULT_PAGE_SIZE);

        if (comments.isEmpty()) {
            return MyGroupCommentListResponse.empty();
        }

        return buildCommentListResponse(comments, member.getId(), userId);
    }

    public MyGroupCommentListResponse getUnreadMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Long> unreadCommentIds = notificationApplicationService.getUnreadCommentIds(userId);

        if (unreadCommentIds == null || unreadCommentIds.isEmpty()) {
            return MyGroupCommentListResponse.empty();
        }

        List<Comment> comments = commentService.getUnreadMyCommentsInGroup(
                member.getId(), unreadCommentIds, cursor, DEFAULT_PAGE_SIZE);

        if (comments.isEmpty()) {
            return MyGroupCommentListResponse.empty();
        }

        return buildCommentListResponse(comments, member.getId(), userId);
    }

    private MyGroupCommentListResponse buildCommentListResponse(List<Comment> comments, Long memberId, Long userId) {
        List<Long> commentIds = comments.stream().map(Comment::getId).toList();
        List<Long> momentIds = comments.stream().map(Comment::getMomentId).toList();

        List<MomentComposition> momentCompositions =
                momentApplicationService.getMyMomentCompositionsBy(momentIds);
        Map<Long, MomentComposition> momentCompositionMap = momentCompositions.stream()
                .collect(Collectors.toMap(MomentComposition::id, m -> m));

        List<CommentComposition> allCommentCompositions =
                commentApplicationService.getMyCommentCompositionsBy(momentIds);
        Map<Long, CommentComposition> commentCompositionMap = allCommentCompositions.stream()
                .filter(c -> commentIds.contains(c.id()))
                .collect(Collectors.toMap(CommentComposition::id, c -> c));

        Map<Long, List<Long>> notificationsMap =
                notificationApplicationService.getNotificationsByCommentIds(userId, commentIds);

        List<MyGroupCommentResponse> responses = comments.stream()
                .map(comment -> createMyGroupCommentResponse(
                        comment,
                        memberId,
                        commentCompositionMap,
                        momentCompositionMap,
                        notificationsMap))
                .toList();

        Long nextCursor = comments.size() < DEFAULT_PAGE_SIZE
                ? null
                : comments.get(comments.size() - 1).getId();

        return MyGroupCommentListResponse.of(responses, nextCursor);
    }

    private MyGroupCommentResponse createMyGroupCommentResponse(
            Comment comment,
            Long memberId,
            Map<Long, CommentComposition> commentCompositionMap,
            Map<Long, MomentComposition> momentCompositionMap,
            Map<Long, List<Long>> notificationsMap
    ) {
        Long commentId = comment.getId();
        Long momentId = comment.getMomentId();
        CommentComposition composition = commentCompositionMap.get(commentId);
        MomentComposition momentComposition = momentCompositionMap.get(momentId);
        List<Long> notificationIds = notificationsMap.getOrDefault(commentId, Collections.emptyList());

        long commentLikeCount = commentLikeService.getCount(commentId);
        boolean commentHasLiked = commentLikeService.hasLiked(commentId, memberId);

        long momentLikeCount = 0L;
        boolean momentHasLiked = false;
        if (momentComposition != null) {
            momentLikeCount = momentLikeService.getCount(momentId);
            momentHasLiked = momentLikeService.hasLiked(momentId, memberId);
        }

        if (composition == null) {
            composition = new CommentComposition(
                    comment.getId(),
                    comment.getContent(),
                    comment.getMember() != null ? comment.getMember().getNickname() : null,
                    null,
                    comment.getCreatedAt(),
                    comment.getMomentId()
            );
        }

        return MyGroupCommentResponse.of(
                composition,
                momentComposition,
                notificationIds,
                commentLikeCount,
                commentHasLiked,
                momentLikeCount,
                momentHasLiked
        );
    }
}
