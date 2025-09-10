package moment.moment.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.application.CommentImageService;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
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
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentsResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.notification.application.NotificationQueryService;
import moment.notification.domain.Notification;
import moment.notification.domain.TargetType;
import moment.reply.application.EchoQueryService;
import moment.reply.domain.Echo;
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

    private final MomentRepository momentRepository;
    private final CommentQueryService commentQueryService;
    private final EchoQueryService echoQueryService;
    private final MomentTagQueryService momentTagQueryService;
    private final MomentTagService momentTagService;
    private final UserQueryService userQueryService;
    private final RewardService rewardService;
    private final MomentImageService momentImageService;
    private final CommentImageService commentImageService;
    private final TagService tagService;
    private final NotificationQueryService notificationQueryService;


    private final BasicMomentCreatePolicy basicMomentCreatePolicy;
    private final ExtraMomentCreatePolicy extraMomentCreatePolicy;


    @Transactional
    public MomentCreateResponse addBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        if (!basicMomentCreatePolicy.canCreate(momenter)) {
            throw new MomentException(ErrorCode.MOMENT_ALREADY_EXIST);
        }

        Moment momentWithoutId = new Moment(request.content(), momenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(momentWithoutId);

        Optional<MomentImage> savedMomentImage = momentImageService.create(request, savedMoment);

        for (String tagName : request.tagNames()) {
            Tag registeredTag = tagService.getOrRegister(tagName);
            momentTagService.save(savedMoment, registeredTag);
        }
        List<MomentTag> savedMomentTags = momentTagQueryService.getAllByMoment(savedMoment);

        rewardService.rewardForMoment(momenter, Reason.MOMENT_CREATION, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
    }

    @Transactional
    public MomentCreateResponse addExtraMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        if (!extraMomentCreatePolicy.canCreate(momenter)) {
            throw new MomentException(ErrorCode.USER_NOT_ENOUGH_STAR);
        }

        Moment momentWithoutId = new Moment(request.content(), momenter, WriteType.EXTRA);
        Moment savedMoment = momentRepository.save(momentWithoutId);

        Optional<MomentImage> savedMomentImage = momentImageService.create(request, savedMoment);

        for (String tagName : request.tagNames()) {
            Tag registeredTag = tagService.getOrRegister(tagName);
            momentTagService.save(savedMoment, registeredTag);
        }
        List<MomentTag> savedMomentTags = momentTagQueryService.getAllByMoment(savedMoment);

        rewardService.useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
    }

    public MyMomentPageResponse getMyMoments(String nextCursor, int size, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Moment> momentsWithinCursor = getRawMoments(cursor, momenter, pageSize.getPageRequest());

        return getMyMomentPageResponse(momentsWithinCursor, pageSize, cursor);
    }

    private MyMomentPageResponse getMyMomentPageResponse(List<Moment> momentsWithinCursor,
                                                         PageSize pageSize,
                                                         Cursor cursor) {

        List<Comment> comments = commentQueryService.getAllByMomentIn(momentsWithinCursor);

        boolean hasNextPage = pageSize.hasNextPage(momentsWithinCursor.size());
        List<Moment> momentsWithoutCursor = removeCursor(momentsWithinCursor, pageSize);

        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTagService.getMomentTagsByMoment(momentsWithoutCursor);
        Map<Moment, MomentImage> momentImagesByMoment = momentImageService.getMomentImageByMoment(momentsWithinCursor);

        if (comments.isEmpty()) {
            return MyMomentPageResponse.of(
                    MyMomentsResponse.of(
                            momentsWithoutCursor,
                            momentTagsByMoment,
                            momentImagesByMoment
                    ),
                    cursor.extract(new ArrayList<>(momentsWithinCursor), hasNextPage),
                    hasNextPage,
                    momentsWithoutCursor.size());
        }

        Map<Moment, List<Comment>> commentsByMoment = comments.stream()
                .collect(Collectors.groupingBy(Comment::getMoment));

        Map<Comment, List<Echo>> echosByComment = echoQueryService.getEchosOfComments(comments);

        Map<Comment, CommentImage> commentImagesByMoment = commentImageService.getCommentImageByMoment(comments);

        return MyMomentPageResponse.of(
                MyMomentsResponse.of(
                        momentsWithoutCursor,
                        commentsByMoment,
                        echosByComment,
                        momentTagsByMoment,
                        momentImagesByMoment,
                        commentImagesByMoment
                ),
                cursor.extract(new ArrayList<>(momentsWithinCursor), hasNextPage),
                hasNextPage,
                momentsWithoutCursor.size());
    }

    private List<Moment> getRawMoments(Cursor cursor, User momenter, PageRequest pageable) {
        if (cursor.isFirstPage()) {
            return momentRepository.findMyMomentFirstPage(momenter, pageable);
        }
        return momentRepository.findMyMomentsNextPage(
                momenter,
                cursor.dateTime(),
                cursor.id(),
                pageable);
    }

    private List<Moment> removeCursor(List<Moment> momentsWithinCursor, PageSize pageSize) {
        if (pageSize.hasNextPage(momentsWithinCursor.size())) {
            return momentsWithinCursor.subList(0, pageSize.size());
        }
        return momentsWithinCursor;
    }

    public MomentCreationStatusResponse canCreateMoment(Long id) {
        User user = userQueryService.getUserById(id);

        if (basicMomentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public MomentCreationStatusResponse canCreateExtraMoment(Long id) {
        User user = userQueryService.getUserById(id);

        if (extraMomentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public CommentableMomentResponse getCommentableMoment(Long id, List<String> tagNames) {
        User user = userQueryService.getUserById(id);

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        List<Moment> commentableMoments = Collections.emptyList();

        if (tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMoments(user, threeDaysAgo);
        }
        if (!tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMomentsByTagNames(user, threeDaysAgo, tagNames);
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

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Moment> unreadRawMoments = getUnreadRawMoments(cursor, user, pageSize);

        return getMyMomentPageResponse(unreadRawMoments, pageSize, cursor);
    }

    private List<Moment> getUnreadRawMoments(Cursor cursor, User user, PageSize pageSize) {
        Set<Long> unreadMomentIds = notificationQueryService.getUnreadContentsNotifications(user, TargetType.MOMENT)
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
}
