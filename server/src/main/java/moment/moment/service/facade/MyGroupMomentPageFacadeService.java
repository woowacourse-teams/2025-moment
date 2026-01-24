package moment.moment.service.facade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.application.CommentApplicationService;
import moment.global.domain.TargetType;
import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
import moment.like.service.MomentLikeService;
import moment.moment.domain.Moment;
import moment.moment.dto.response.MomentNotificationResponse;
import moment.moment.dto.response.MyGroupFeedResponse;
import moment.moment.dto.response.MyGroupMomentCommentResponse;
import moment.moment.dto.response.MyGroupMomentResponse;
import moment.moment.service.moment.MomentService;
import moment.notification.service.application.NotificationApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyGroupMomentPageFacadeService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MomentService momentService;
    private final GroupMemberService groupMemberService;
    private final CommentApplicationService commentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final MomentLikeService momentLikeService;

    public MyGroupFeedResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Moment> moments = momentService.getMyMomentsInGroup(
                groupId, member.getId(), cursor, DEFAULT_PAGE_SIZE);

        if (moments.isEmpty()) {
            return MyGroupFeedResponse.empty();
        }

        return buildFeedResponse(moments, member.getId());
    }

    public MyGroupFeedResponse getUnreadMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Long> unreadMomentIds = notificationApplicationService.getUnreadNotifications(
                userId, TargetType.MOMENT);

        if (unreadMomentIds == null || unreadMomentIds.isEmpty()) {
            return MyGroupFeedResponse.empty();
        }

        List<Moment> moments = momentService.getUnreadMyMomentsInGroup(
                groupId, member.getId(), unreadMomentIds, cursor, DEFAULT_PAGE_SIZE);

        if (moments.isEmpty()) {
            return MyGroupFeedResponse.empty();
        }

        return buildFeedResponse(moments, member.getId());
    }

    private MyGroupFeedResponse buildFeedResponse(List<Moment> moments, Long memberId) {
        List<Long> momentIds = moments.stream().map(Moment::getId).toList();

        List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);
        Map<Long, List<CommentComposition>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(CommentComposition::momentId));

        Map<Long, List<Long>> notificationsMap =
                notificationApplicationService.getNotificationsByTargetIdsAndTargetType(
                        momentIds, TargetType.MOMENT);

        List<MyGroupMomentResponse> responses = moments.stream()
                .map(moment -> createMyGroupMomentResponse(moment, memberId, commentsMap, notificationsMap))
                .toList();

        Long nextCursor = moments.size() < DEFAULT_PAGE_SIZE
                ? null
                : moments.get(moments.size() - 1).getId();

        return MyGroupFeedResponse.of(responses, nextCursor);
    }

    private MyGroupMomentResponse createMyGroupMomentResponse(
            Moment moment,
            Long memberId,
            Map<Long, List<CommentComposition>> commentsMap,
            Map<Long, List<Long>> notificationsMap
    ) {
        Long momentId = moment.getId();

        long likeCount = momentLikeService.getCount(momentId);
        boolean hasLiked = momentLikeService.hasLiked(momentId, memberId);

        List<CommentComposition> compositions = commentsMap.getOrDefault(momentId, List.of());
        List<MyGroupMomentCommentResponse> comments = compositions.stream()
                .map(MyGroupMomentCommentResponse::from)
                .toList();
        long commentCount = comments.size();

        List<Long> notificationIds = notificationsMap.getOrDefault(momentId, List.of());
        MomentNotificationResponse notification = MomentNotificationResponse.from(notificationIds);

        return MyGroupMomentResponse.of(
                moment, likeCount, hasLiked, commentCount, comments, notification);
    }
}
