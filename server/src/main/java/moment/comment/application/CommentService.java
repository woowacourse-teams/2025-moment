package moment.comment.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.request.CommentReportCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.CommentReportCreateResponse;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.application.MomentImageService;
import moment.moment.application.MomentQueryService;
import moment.moment.application.MomentTagService;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.notification.application.NotificationFacade;
import moment.notification.application.NotificationQueryService;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.reply.application.EchoQueryService;
import moment.reply.application.EchoService;
import moment.reply.domain.Echo;
import moment.report.application.ReportService;
import moment.report.domain.Report;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;
    private final CommentRepository commentRepository;
    private final EchoQueryService echoQueryService;
    private final CommentQueryService commentQueryService;
    private final RewardService rewardService;
    private final NotificationFacade notificationFacade;
    private final CommentImageService commentImageService;
    private final MomentImageService momentImageService;
    private final NotificationQueryService notificationQueryService;
    private final MomentTagService momentTagService;
    private final ReportService reportService;
    private final EchoService echoService;

    @Transactional
    public CommentCreateResponse addComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        if (commentQueryService.existsByMomentAndCommenter(moment, commenter)) {
            throw new MomentException(ErrorCode.COMMENT_CONFLICT);
        }

        Comment commentWithoutId = request.toComment(commenter, moment);
        Comment savedComment = commentRepository.save(commentWithoutId);

        Optional<CommentImage> commentImage = commentImageService.create(request, savedComment);

        notificationFacade.sendSseNotificationAndNotification(
                moment.getMomenter(),
                moment.getId(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT);

        rewardService.rewardForComment(commenter, Reason.COMMENT_CREATION, savedComment.getId());

        return commentImage.map(image -> CommentCreateResponse.of(savedComment, image))
                .orElseGet(() -> CommentCreateResponse.from(savedComment));
    }

    public MyCommentPageResponse getCommentsByUserIdWithCursor(String nextCursor, int size, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Comment> commentsWithinCursor = getRawComments(cursor, commenter, pageSize);
        List<Notification> allNotifications = notificationQueryService.getUnreadContentsNotifications(
                commenter,
                TargetType.COMMENT);

        return getMyCommentPageResponse(pageSize, commentsWithinCursor, cursor, allNotifications);
    }

    private MyCommentPageResponse getMyCommentPageResponse(PageSize pageSize,
                                                           List<Comment> commentsWithinCursor,
                                                           Cursor cursor,
                                                           List<Notification> allNotifications) {

        boolean hasNextPage = pageSize.hasNextPage(commentsWithinCursor.size());

        List<Comment> commentsWithoutCursor = removeCursor(commentsWithinCursor, pageSize);
        Map<Comment, CommentImage> commentImages = commentImageService.getCommentImageByMoment(commentsWithoutCursor);

        List<Moment> momentsOfComment = commentsWithoutCursor.stream()
                .map(Comment::getMoment)
                .filter(Objects::nonNull)
                .toList();

        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTagService.getMomentTagsByMoment(momentsOfComment);
        Map<Moment, MomentImage> momentImages = momentImageService.getMomentImageByMoment(momentsOfComment);

        List<Echo> allEchoes = echoQueryService.getAllByCommentIn(commentsWithoutCursor);
        Map<Comment, List<Notification>> notificationsForComments = mapNotificationsForComment(
                commentsWithoutCursor,
                allNotifications);

        if (allEchoes.isEmpty()) {
            return MyCommentPageResponse.of(
                    MyCommentsResponse.of(
                            commentsWithoutCursor,
                            momentTagsByMoment,
                            momentImages,
                            commentImages,
                            notificationsForComments),
                    cursor.extract(new ArrayList<>(commentsWithinCursor), hasNextPage),
                    hasNextPage,
                    commentsWithoutCursor.size()
            );
        }

        Map<Comment, List<Echo>> commentAndEchos = allEchoes.stream()
                .collect(Collectors.groupingBy(Echo::getComment));

        return MyCommentPageResponse.of(
                MyCommentsResponse.of(
                        commentsWithoutCursor,
                        commentAndEchos,
                        momentTagsByMoment,
                        momentImages,
                        commentImages,
                        notificationsForComments),
                cursor.extract(new ArrayList<>(commentsWithinCursor), hasNextPage),
                hasNextPage,
                commentsWithoutCursor.size()
        );
    }

    private List<Comment> getRawComments(Cursor cursor, User commenter, PageSize pageSize) {
        if (cursor.isFirstPage()) {
            List<Long> commentIds = commentRepository.findFirstPageCommentIdsByCommenter(
                    commenter,
                    pageSize.getPageRequest());

            return commentRepository.findCommentsWithDetailsByIds(commentIds);
        }
        List<Long> nextCommentIds = commentRepository.findNextPageCommentIdsByCommenter(
                commenter,
                cursor.dateTime(),
                cursor.id(),
                pageSize.getPageRequest());

        return commentRepository.findCommentsWithDetailsByIds(nextCommentIds);
    }

    private List<Comment> removeCursor(List<Comment> commentsWithinCursor, PageSize pageSize) {
        if (pageSize.hasNextPage(commentsWithinCursor.size())) {
            return commentsWithinCursor.subList(0, pageSize.size());
        }
        return commentsWithinCursor;
    }

    public MyCommentPageResponse getMyUnreadComments(String nextCursor, int size, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Notification> allNotifications = notificationQueryService.getUnreadContentsNotifications(
                commenter,
                TargetType.COMMENT);

        List<Comment> unreadRawComments = getUnreadRawComments(cursor, pageSize, allNotifications);

        return getMyCommentPageResponse(pageSize, unreadRawComments, cursor, allNotifications);
    }

    private List<Comment> getUnreadRawComments(Cursor cursor,
                                               PageSize pageSize,
                                               List<Notification> allNotifications) {
        Set<Long> unreadCommentIds = allNotifications.stream()
                .map(Notification::getTargetId)
                .collect(Collectors.toSet());

        if (cursor.isFirstPage()) {
            return commentRepository.findUnreadCommentsFirstPage(unreadCommentIds, pageSize.getPageRequest());
        }
        return commentRepository.findUnreadCommentsNextPage(
                unreadCommentIds,
                cursor.dateTime(),
                cursor.id(),
                pageSize.getPageRequest());
    }

    @Transactional
    public CommentReportCreateResponse reportComment(Long commentId, Long id, CommentReportCreateRequest request) {
        User reporter = userQueryService.getUserById(id);
        Comment comment = commentQueryService.getCommentWithCommenterById(commentId);

        Report savedReport = reportService.createReport(
                TargetType.COMMENT,
                reporter,
                comment.getId(),
                request.reason());

        echoService.deleteByComment(comment);
        commentImageService.deleteByComment(comment);
        commentRepository.delete(comment);

        return CommentReportCreateResponse.from(savedReport);
    }

    private Map<Comment, List<Notification>> mapNotificationsForComment(List<Comment> comments,
                                                                        List<Notification> notifications) {
        Map<Comment, List<Notification>> notificationsForComments = new HashMap<>();
        for (Comment comment : comments) {
            List<Notification> notificationsForComment = notifications.stream()
                    .filter(notification -> notification.getTargetId().equals(comment.getId()))
                    .toList();

            notificationsForComments.put(comment, notificationsForComment);
        }

        return notificationsForComments;
    }
}
