package moment.moment.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
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
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.dto.response.tobe.MomentCompositions;
import moment.moment.service.moment.MomentImageService;
import moment.moment.service.moment.MomentService;
import moment.moment.service.moment.MomentTagService;
import moment.moment.service.moment.TagService;
import moment.report.application.report.ReportService;
import moment.reward.domain.Reason;
import moment.reward.service.reward.RewardService;
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

    private final UserService userService;
    private final BasicMomentCreatePolicy basicMomentCreatePolicy;
    private final ExtraMomentCreatePolicy extraMomentCreatePolicy;
    private final MomentService momentService;
    private final MomentImageService momentImageService;
    private final MomentTagService momentTagService;
    private final TagService tagService;
    private final RewardService rewardService;
    private final ReportService reportService;

    @Transactional
    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);

        basicMomentCreatePolicy.validate(momenter);

        Moment savedMoment = momentService.create(request.content(), momenter, WriteType.BASIC);

        Optional<MomentImage> savedMomentImage = momentImageService.create(savedMoment, request.imageUrl(),
                request.imageName());

        List<Tag> tags = tagService.getOrCreate(request.tagNames());

        List<MomentTag> savedMomentTags = momentTagService.createAll(savedMoment, tags);

        rewardService.rewardForMoment(momenter, Reason.MOMENT_CREATION, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
    }

    @Transactional
    public MomentCreateResponse createExtraMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);

        extraMomentCreatePolicy.validate(momenter);

        Moment savedMoment = momentService.create(request.content(), momenter, WriteType.BASIC);

        Optional<MomentImage> savedMomentImage = momentImageService.create(savedMoment, request.imageUrl(),
                request.imageName());

        List<Tag> tags = tagService.getOrCreate(request.tagNames());

        List<MomentTag> savedMomentTags = momentTagService.createAll(savedMoment, tags);

        rewardService.useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
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
        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTagService.getMomentTagsByMoment(moments);
        Map<Moment, MomentImage> momentImageByMoment = momentImageService.getMomentImageByMoment(moments);

        return moments.stream()
                .map(moment ->
                        MomentComposition.of(
                                moment, momentTagsByMoment.get(moment),
                                momentImageByMoment.get(moment)
                        )
                )
                .toList();
    }

    private List<Moment> removeCursor(List<Moment> momentsWithinCursor, PageSize pageSize) {
        if (pageSize.hasNextPage(momentsWithinCursor.size())) {
            return momentsWithinCursor.subList(0, pageSize.size());
        }
        return momentsWithinCursor;
    }

    public MomentCreationStatusResponse canCreateMoment(Long id) {
        User user = userService.getUserBy(id);

        if (basicMomentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public MomentCreationStatusResponse canCreateExtraMoment(Long id) {
        User user = userService.getUserBy(id);

        if (extraMomentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }

    public List<Long> getCommentableMoment(Long id) {
        User user = userService.getUserBy(id);

        List<Long> reportedMomentIds = reportService.getReportedMomentIdsBy(user.getId());

        List<Moment> commentableMoments = momentService.getCommentableMoments(user, reportedMomentIds);

        return commentableMoments.stream()
                .map(Moment::getId)
                .toList();
    }

    public CommentableMomentResponse pickRandomMomentComposition(List<Long> momentIds, List<String> tagNames) {
        if (!tagNames.isEmpty()) {
            momentIds = momentTagService.getMomentIdsByTags(momentIds, tagNames);
        }

        List<Moment> commentableMoments = momentService.getMomentsBy(momentIds);
        Moment moment = commentableMoments.get(RANDOM.nextInt(commentableMoments.size()));
        Optional<MomentImage> momentImage = momentImageService.findMomentImage(moment);

        return CommentableMomentResponse.of(moment, momentImage.orElse(null));
    }

    @Transactional
    public void deleteByReport(Long momentId, Long reportCount) {
        if (reportCount >= MOMENT_DELETE_THRESHOLD) {
            momentImageService.deleteBy(momentId);
            momentTagService.deleteBy(momentId);
            momentService.deleteBy(momentId);
        }
    }

    public void validateExistMoment(Long momentId) {
        boolean exsitsMoment = momentService.existsMoment(momentId);

        if (!exsitsMoment) {
            throw new MomentException(ErrorCode.MOMENT_NOT_FOUND);
        }
    }

    public List<MomentComposition> getMyMomentCompositionsBy(List<Long> momentIds) {
        List<Moment> moments = momentService.getMomentsBy(momentIds);
        return mapMomentCompositionInfoBy(moments);
    }

    public void validateMomenter(Long momentId, Long momenterId) {
        User momenter = userService.getUserBy(momenterId);
        momentService.validateMomenter(momentId, momenter);
    }
}
