package moment.comment.service.facade;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.response.MyGroupCommentFeedResponse;
import moment.comment.dto.response.MyGroupCommentResponse;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.application.CommentApplicationService;
import moment.comment.service.comment.CommentService;
import moment.global.domain.TargetType;
import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
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

    public MyGroupCommentFeedResponse getMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Comment> comments = commentService.getMyCommentsInGroup(
                member.getId(), cursor, DEFAULT_PAGE_SIZE);

        if (comments.isEmpty()) {
            return MyGroupCommentFeedResponse.empty();
        }

        return buildFeedResponse(comments, userId);
    }

    public MyGroupCommentFeedResponse getUnreadMyCommentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Long> unreadCommentIds = notificationApplicationService.getUnreadNotifications(
                userId, TargetType.COMMENT);

        if (unreadCommentIds == null || unreadCommentIds.isEmpty()) {
            return MyGroupCommentFeedResponse.empty();
        }

        List<Comment> comments = commentService.getUnreadMyCommentsInGroup(
                member.getId(), unreadCommentIds, cursor, DEFAULT_PAGE_SIZE);

        if (comments.isEmpty()) {
            return MyGroupCommentFeedResponse.empty();
        }

        return buildFeedResponse(comments, userId);
    }

    private MyGroupCommentFeedResponse buildFeedResponse(List<Comment> comments, Long userId) {
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
                notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
                        commentIds, TargetType.COMMENT);

        List<MyGroupCommentResponse> responses = comments.stream()
                .map(comment -> createMyGroupCommentResponse(
                        comment,
                        commentCompositionMap,
                        momentCompositionMap,
                        notificationsMap))
                .toList();

        Long nextCursor = comments.size() < DEFAULT_PAGE_SIZE
                ? null
                : comments.get(comments.size() - 1).getId();

        return MyGroupCommentFeedResponse.of(responses, nextCursor);
    }

    private MyGroupCommentResponse createMyGroupCommentResponse(
            Comment comment,
            Map<Long, CommentComposition> commentCompositionMap,
            Map<Long, MomentComposition> momentCompositionMap,
            Map<Long, List<Long>> notificationsMap
    ) {
        Long commentId = comment.getId();
        CommentComposition composition = commentCompositionMap.get(commentId);
        MomentComposition momentComposition = momentCompositionMap.get(comment.getMomentId());
        List<Long> notificationIds = notificationsMap.getOrDefault(commentId, Collections.emptyList());

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

        return MyGroupCommentResponse.of(composition, momentComposition, notificationIds);
    }
}
