package moment.moment.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.global.domain.TargetType;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.request.MomentReportCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MomentReportCreateResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentsResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.application.NotificationQueryService;
import moment.notification.domain.Notification;
import moment.comment.domain.Echo;
import moment.report.application.ReportService;
import moment.report.domain.Report;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {

    private static final int MOMENT_DELETE_THRESHOLD = 3;

    private final MomentRepository momentRepository;
//    private final CommentQueryService commentQueryService;
//    private final EchoQueryService echoQueryService;
    private final MomentTagQueryService momentTagQueryService;
    private final MomentTagService momentTagService;
    private final UserQueryService userQueryService;
    private final RewardService rewardService;
    private final MomentImageService momentImageService;
//    private final CommentImageService commentImageService;
    private final TagService tagService;
    private final NotificationQueryService notificationQueryService;

    private final ReportService reportService;

    private final BasicMomentCreatePolicy basicMomentCreatePolicy;
    private final ExtraMomentCreatePolicy extraMomentCreatePolicy;
    private final MomentQueryService momentQueryService;

    public MomentCreationStatusResponse canCreateMoment(Long id) {
        User user = userQueryService.getUserById(id);

        if (basicMomentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public MomentCreationStatusResponse canCreateExtraMoment(Long id) {
        User user = userQueryService.getUserById(id);

        if (extraMomentCreatePolicy.validate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public CommentableMomentResponse getCommentableMoment(Long id, List<String> tagNames) {
        User user = userQueryService.getUserById(id);

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(MOMENT_DELETE_THRESHOLD);

        List<Moment> commentableMoments = Collections.emptyList();

        List<Report> reports = reportService.findMomentReportByUser(user);
        List<Long> reportedMomentIds = reports.stream()
                .map(Report::getTargetId)
                .toList();

        if (tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMoments(user, threeDaysAgo, reportedMomentIds);
        }
        if (!tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMomentsByTagNames(user, threeDaysAgo, tagNames,
                    reportedMomentIds);
        }

        if (commentableMoments.isEmpty()) {
            return CommentableMomentResponse.empty();
        }

        Moment moment = commentableMoments.get(new Random().nextInt(commentableMoments.size()));

        Optional<MomentImage> momentImage = momentImageService.findMomentImage(moment);

        return CommentableMomentResponse.of(moment, momentImage.orElse(null));
    }

    public MyMomentPageResponse getMyUnreadMoments(String nextCursor, int size, Long momenterId) {
        User user = userQueryService.getUserById(momenterId);
        List<Notification> allNotifications = notificationQueryService.getUnreadContentsNotifications(
                user,
                TargetType.MOMENT);

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Moment> unreadRawMoments = getUnreadRawMoments(cursor, pageSize, allNotifications);
        Map<Moment, List<Notification>> notificationsForMoments = mapNotificationForMoment(unreadRawMoments,
                allNotifications);

        return getMyMomentPageResponse(unreadRawMoments, notificationsForMoments, pageSize, cursor);
    }

    private List<Moment> getUnreadRawMoments(Cursor cursor, PageSize pageSize, List<Notification> notifications) {
        Set<Long> unreadMomentIds = notifications
                .stream()
                .map(Notification::getTargetId)
                .collect(Collectors.toSet());

        if (cursor.isFirstPage()) {
            return momentRepository.findMyUnreadMomentFirstPage(unreadMomentIds, pageSize.getPageRequest());
        }
        return momentRepository.findMyUnreadMomentNextPage(
                unreadMomentIds,
                cursor.dateTime(),
                cursor.id(),
                pageSize.getPageRequest());
    }

    @Transactional
    public MomentReportCreateResponse reportMoment(Long momentId, Long reporterId, MomentReportCreateRequest request) {
        User user = userQueryService.getUserById(reporterId);
        Moment moment = momentQueryService.getMomentWithMomenterById(momentId);

        Report report = reportService.createReport(TargetType.MOMENT, user, moment.getId(), request.reason());

        long reportCount = reportService.countReportsByTarget(TargetType.MOMENT, moment.getId());

        if (reportCount >= MOMENT_DELETE_THRESHOLD) {
            momentImageService.deleteByMoment(moment);
            momentTagService.deleteByMoment(moment);
            momentRepository.delete(moment);
        }

        return MomentReportCreateResponse.from(report);
    }

    private Map<Moment, List<Notification>> mapNotificationForMoment(List<Moment> moments,
                                                                     List<Notification> allNotifications) {
        Map<Moment, List<Notification>> notificationForMoment = new HashMap<>();

        for (Moment moment : moments) {
            List<Notification> notifications = allNotifications.stream()
                    .filter(notification -> notification.getTargetId().equals(moment.getId()))
                    .toList();
            notificationForMoment.put(moment, notifications);
        }

        return notificationForMoment;
    }
}
