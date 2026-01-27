package moment.moment.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import moment.comment.service.comment.CommentService;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.global.page.Cursor;
import moment.global.page.PageSize;
import moment.group.domain.GroupMember;
import moment.group.service.group.GroupMemberService;
import moment.like.service.MomentLikeService;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentImage;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.GroupFeedResponse;
import moment.moment.dto.response.GroupMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.service.moment.MomentImageService;
import moment.moment.service.moment.MomentService;
import moment.report.application.report.ReportService;
import moment.storage.application.PhotoUrlResolver;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentApplicationService {

    private final static Random RANDOM = new Random();
    private static final int MOMENT_DELETE_THRESHOLD = 3;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final UserService userService;
    private final MomentService momentService;
    private final MomentImageService momentImageService;
    private final ReportService reportService;
    private final PhotoUrlResolver photoUrlResolver;
    private final GroupMemberService memberService;
    private final MomentLikeService momentLikeService;
    private final CommentService commentService;

    @Transactional
    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);

        Moment savedMoment = momentService.create(request.content(), momenter);

        Optional<MomentImage> savedMomentImage = momentImageService.create(savedMoment, request.imageUrl(),
                request.imageName());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment));
    }

    @Transactional
    public MomentCreateResponse createExtraMoment(MomentCreateRequest request, Long momenterId) {
        return createBasicMoment(request, momenterId);
    }

    public MomentCompositions getMyMomentCompositions(Cursor cursor, PageSize pageSize, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);

        List<Moment> momentsWithinCursor = momentService.getMomentsBy(momenter, cursor, pageSize);

        List<Moment> momentsWithoutCursor = removeCursor(momentsWithinCursor, pageSize);

        boolean hasNextPage = pageSize.hasNextPage(momentsWithinCursor.size());

        return MomentCompositions.of(
                mapMomentCompositionInfoBy(momentsWithoutCursor),
                cursor.extract(new ArrayList<>(momentsWithinCursor), hasNextPage),
                hasNextPage,
                momentsWithoutCursor.size()
        );
    }

    public MomentCompositions getUnreadMyMomentCompositions(Cursor cursor, PageSize pageSize, Long momenterId,
                                                            List<Long> unreadMomentIds) {
        User momenter = userService.getUserBy(momenterId);

        List<Moment> momentsWithinCursor = momentService.getUnreadMomentsBy(unreadMomentIds, cursor, pageSize);

        List<Moment> momentsWithoutCursor = removeCursor(momentsWithinCursor, pageSize);

        boolean hasNextPage = pageSize.hasNextPage(momentsWithinCursor.size());

        return MomentCompositions.of(
                mapMomentCompositionInfoBy(momentsWithoutCursor),
                cursor.extract(new ArrayList<>(momentsWithinCursor), hasNextPage),
                hasNextPage,
                momentsWithoutCursor.size()
        );
    }

    private List<MomentComposition> mapMomentCompositionInfoBy(
            List<Moment> moments
    ) {
        Map<Moment, MomentImage> momentImageByMoment = momentImageService.getMomentImageByMoment(moments);

        return moments.stream()
                .map(moment -> {
                    MomentImage image = momentImageByMoment.get(moment);
                    String resolvedImageUrl = (image != null) ? photoUrlResolver.resolve(image.getImageUrl()) : null;

                    return MomentComposition.of(moment, resolvedImageUrl);
                })
                .toList();
    }

    private List<Moment> removeCursor(List<Moment> momentsWithinCursor, PageSize pageSize) {
        if (pageSize.hasNextPage(momentsWithinCursor.size())) {
            return momentsWithinCursor.subList(0, pageSize.size());
        }
        return momentsWithinCursor;
    }

    public MomentCreationStatusResponse canCreateMoment(Long id) {
        userService.getUserBy(id);
        return MomentCreationStatusResponse.createAllowedStatus();
    }

    public MomentCreationStatusResponse canCreateExtraMoment(Long id) {
        userService.getUserBy(id);
        return MomentCreationStatusResponse.createAllowedStatus();
    }

    public List<Long> getCommentableMomentInGroup(Long groupId, Long userId) {
        User user = userService.getUserBy(userId);
        List<Long> reportedMomentIds = reportService.getReportedMomentIdsBy(user.getId());
        List<Moment> commentableMoments = momentService.getCommentableMomentsInGroup(groupId, user, reportedMomentIds);
        return commentableMoments.stream().map(Moment::getId).toList();
    }

    public CommentableMomentResponse pickRandomMomentComposition(List<Long> momentIds) {
        List<Moment> commentableMoments = momentService.getMomentsBy(momentIds);
        if (commentableMoments.isEmpty()) {
            return CommentableMomentResponse.empty();
        }
        Moment moment = commentableMoments.get(RANDOM.nextInt(commentableMoments.size()));
        Optional<MomentImage> momentImage = momentImageService.findMomentImage(moment);

        return CommentableMomentResponse.of(moment, momentImage.orElse(null));
    }

    @Transactional
    public void deleteByReport(Long momentId, Long reportCount) {
        if (reportCount >= MOMENT_DELETE_THRESHOLD) {
            momentImageService.deleteBy(momentId);
            momentService.deleteBy(momentId);
        }
    }

    public Moment getMomentBy(Long momentId) {
        return momentService.getMomentBy(momentId);
    }

    public List<MomentComposition> getMyMomentCompositionsBy(List<Long> momentIds) {
        List<Moment> moments = momentService.getMomentsBy(momentIds);
        return mapMomentCompositionInfoBy(moments);
    }

    public void validateMomenter(Long momentId, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);
        momentService.validateMomenter(momentId, momenter);
    }

    public GroupFeedResponse getGroupFeed(Long groupId, Long userId, Long cursor) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        List<Moment> moments = momentService.getByGroup(groupId, cursor, DEFAULT_PAGE_SIZE);

        List<GroupMomentResponse> responses = moments.stream()
                .map(moment -> {
                    long likeCount = momentLikeService.getCount(moment.getId());
                    boolean hasLiked = momentLikeService.hasLiked(moment.getId(), member.getId());
                    long commentCount = commentService.countByMomentId(moment.getId());
                    return GroupMomentResponse.from(moment, likeCount, hasLiked, commentCount);
                })
                .toList();

        Long nextCursor = moments.isEmpty() ? null : moments.get(moments.size() - 1).getId();
        return GroupFeedResponse.of(responses, nextCursor);
    }

    public GroupFeedResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        List<Moment> moments = momentService.getMyMomentsInGroup(groupId, member.getId(), cursor, DEFAULT_PAGE_SIZE);

        List<GroupMomentResponse> responses = moments.stream()
                .map(moment -> {
                    long likeCount = momentLikeService.getCount(moment.getId());
                    boolean hasLiked = momentLikeService.hasLiked(moment.getId(), member.getId());
                    long commentCount = commentService.countByMomentId(moment.getId());
                    return GroupMomentResponse.from(moment, likeCount, hasLiked, commentCount);
                })
                .toList();

        Long nextCursor = moments.isEmpty() ? null : moments.get(moments.size() - 1).getId();
        return GroupFeedResponse.of(responses, nextCursor);
    }

    @Transactional
    public GroupMomentResponse createMomentInGroup(Long groupId, Long userId, String content) {
        User momenter = userService.getUserBy(userId);
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        moment.group.domain.Group group = member.getGroup();

        Moment moment = momentService.createInGroup(momenter, group, member, content);
        return GroupMomentResponse.from(moment, 0L, false, 0L);
    }

    @Transactional
    public void deleteMomentInGroup(Long groupId, Long momentId, Long userId) {
        Moment momentToDelete = momentService.getMomentBy(momentId);
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);

        if (!momentToDelete.getMember().getId().equals(member.getId())) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }

        momentService.deleteBy(momentId);
    }

    @Transactional
    public boolean toggleMomentLike(Long groupId, Long momentId, Long userId) {
        Moment moment = momentService.getMomentBy(momentId);
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        return momentLikeService.toggle(moment, member);
    }
}
