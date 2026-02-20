package moment.moment.service.facade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.block.service.application.UserBlockApplicationService;
import moment.comment.dto.tobe.CommentComposition;
import moment.comment.service.application.CommentApplicationService;

import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
import moment.like.service.like.CommentLikeService;
import moment.like.service.like.MomentLikeService;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.dto.response.MomentNotificationResponse;
import moment.moment.dto.response.MyGroupMomentListResponse;
import moment.moment.dto.response.MyGroupMomentCommentResponse;
import moment.moment.dto.response.MyGroupMomentResponse;
import moment.moment.service.moment.MomentImageService;
import moment.moment.service.moment.MomentService;
import moment.notification.service.application.NotificationApplicationService;
import moment.storage.service.storage.PhotoUrlResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyGroupMomentPageFacadeService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final MomentService momentService;
    private final MomentImageService momentImageService;
    private final GroupMemberService groupMemberService;
    private final CommentApplicationService commentApplicationService;
    private final NotificationApplicationService notificationApplicationService;
    private final MomentLikeService momentLikeService;
    private final CommentLikeService commentLikeService;
    private final PhotoUrlResolver photoUrlResolver;
    private final UserBlockApplicationService userBlockApplicationService;

    public MyGroupMomentListResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Moment> moments = momentService.getMyMomentsInGroup(
                groupId, member.getId(), cursor, DEFAULT_PAGE_SIZE);

        if (moments.isEmpty()) {
            return MyGroupMomentListResponse.empty();
        }

        return buildMomentListResponse(moments, member.getId(), userId);
    }

    public MyGroupMomentListResponse getUnreadMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = groupMemberService.getByGroupAndUser(groupId, userId);

        List<Long> unreadMomentIds = notificationApplicationService.getUnreadMomentIds(userId);

        if (unreadMomentIds == null || unreadMomentIds.isEmpty()) {
            return MyGroupMomentListResponse.empty();
        }

        List<Moment> moments = momentService.getUnreadMyMomentsInGroup(
                groupId, member.getId(), unreadMomentIds, cursor, DEFAULT_PAGE_SIZE);

        if (moments.isEmpty()) {
            return MyGroupMomentListResponse.empty();
        }

        return buildMomentListResponse(moments, member.getId(), userId);
    }

    private MyGroupMomentListResponse buildMomentListResponse(List<Moment> moments, Long memberId, Long userId) {
        List<Long> momentIds = moments.stream().map(Moment::getId).toList();

        Map<Moment, MomentImage> momentImageMap = momentImageService.getMomentImageByMoment(moments);

        List<Long> blockedUserIds = userBlockApplicationService.getBlockedUserIds(userId);
        Set<Long> blockedUserIdSet = new HashSet<>(blockedUserIds);

        List<CommentComposition> allComments = commentApplicationService.getMyCommentCompositionsBy(momentIds);
        allComments = allComments.stream()
                .filter(c -> c.commenterUserId() == null || !blockedUserIdSet.contains(c.commenterUserId()))
                .toList();
        Map<Long, List<CommentComposition>> commentsMap = allComments.stream()
                .collect(Collectors.groupingBy(CommentComposition::momentId));

        Map<Long, List<Long>> notificationsMap =
                notificationApplicationService.getNotificationsByMomentIds(userId, momentIds);

        List<Long> allCommentIds = allComments.stream()
                .map(CommentComposition::id)
                .toList();
        Map<Long, Long> commentLikeCountMap = commentLikeService.getCountsByCommentIds(allCommentIds);
        Set<Long> likedCommentIds = commentLikeService.getLikedCommentIds(allCommentIds, memberId);

        List<MyGroupMomentResponse> responses = moments.stream()
                .map(moment -> createMyGroupMomentResponse(
                        moment, memberId, momentImageMap, commentsMap, notificationsMap,
                        commentLikeCountMap, likedCommentIds))
                .toList();

        Long nextCursor = moments.size() < DEFAULT_PAGE_SIZE
                ? null
                : moments.get(moments.size() - 1).getId();

        return MyGroupMomentListResponse.of(responses, nextCursor);
    }

    private MyGroupMomentResponse createMyGroupMomentResponse(
            Moment moment,
            Long memberId,
            Map<Moment, MomentImage> momentImageMap,
            Map<Long, List<CommentComposition>> commentsMap,
            Map<Long, List<Long>> notificationsMap,
            Map<Long, Long> commentLikeCountMap,
            Set<Long> likedCommentIds
    ) {
        Long momentId = moment.getId();

        long likeCount = momentLikeService.getCount(momentId);
        boolean hasLiked = momentLikeService.hasLiked(momentId, memberId);

        MomentImage momentImage = momentImageMap.get(moment);
        String imageUrl = (momentImage != null) ? photoUrlResolver.resolve(momentImage.getImageUrl()) : null;

        List<CommentComposition> compositions = commentsMap.getOrDefault(momentId, List.of());
        List<MyGroupMomentCommentResponse> comments = compositions.stream()
                .map(composition -> {
                    long commentLikeCount = commentLikeCountMap.getOrDefault(composition.id(), 0L);
                    boolean commentHasLiked = likedCommentIds.contains(composition.id());
                    return MyGroupMomentCommentResponse.of(composition, commentLikeCount, commentHasLiked);
                })
                .toList();
        long commentCount = comments.size();

        List<Long> notificationIds = notificationsMap.getOrDefault(momentId, List.of());
        MomentNotificationResponse notification = MomentNotificationResponse.from(notificationIds);

        return MyGroupMomentResponse.of(
                moment, likeCount, hasLiked, commentCount, imageUrl, comments, notification);
    }
}
