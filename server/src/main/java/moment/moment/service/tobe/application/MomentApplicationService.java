package moment.moment.service.tobe.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.tobe.MomentComposition;
import moment.moment.service.tobe.moment.MomentImageService;
import moment.moment.service.tobe.moment.MomentService;
import moment.moment.service.tobe.moment.MomentTagService;
import moment.moment.service.tobe.moment.TagService;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.tobe.user.UserService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentApplicationService {

    private final UserService userService;
    private final BasicMomentCreatePolicy basicMomentCreatePolicy;
    private final ExtraMomentCreatePolicy extraMomentCreatePolicy;
    private final MomentService momentService;
    private final MomentImageService momentImageService;
    private final MomentTagService momentTagService;
    private final TagService tagService;
    private final RewardService rewardService;

    @Transactional
    public MomentCreateResponse createBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserById(momenterId);

        basicMomentCreatePolicy.validate(momenter);

        Moment savedMoment = momentService.create(request.content(), momenter, WriteType.BASIC);

        Optional<MomentImage> savedMomentImage = momentImageService.create(savedMoment, request.imageUrl(), request.imageName());

        List<Tag> tags = tagService.getOrCreate(request.tagNames());

        List<MomentTag> savedMomentTags = momentTagService.createAll(savedMoment, tags);

        rewardService.rewardForMoment(momenter, Reason.MOMENT_CREATION, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
    }

    @Transactional
    public MomentCreateResponse addExtraMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userService.getUserById(momenterId);

        extraMomentCreatePolicy.validate(momenter);

        Moment savedMoment = momentService.create(request.content(), momenter, WriteType.BASIC);

        Optional<MomentImage> savedMomentImage = momentImageService.create(savedMoment, request.imageUrl(), request.imageName());

        List<Tag> tags = tagService.getOrCreate(request.tagNames());

        List<MomentTag> savedMomentTags = momentTagService.createAll(savedMoment, tags);

        rewardService.useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, savedMoment.getId());

        return savedMomentImage.map(momentImage -> MomentCreateResponse.of(savedMoment, momentImage, savedMomentTags))
                .orElseGet(() -> MomentCreateResponse.of(savedMoment, savedMomentTags));
    }

    public List<MomentComposition> getMyMoments(String nextCursor, int size, Long momenterId) {
        User momenter = userService.getUserById(momenterId);

        Cursor cursor = new Cursor(nextCursor);
        PageSize pageSize = new PageSize(size);

        List<Moment> momentsWithinCursor = momentService.getMyPage(momenter, cursor, pageSize);

        return getMyMomentPageResponse(momentsWithinCursor, pageSize);
    }

    private List<MomentComposition> getMyMomentPageResponse(
            List<Moment> momentsWithinCursor,
            PageSize pageSize
    ) {
        List<Moment> momentsWithoutCursor = removeCursor(momentsWithinCursor, pageSize);

        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTagService.getMomentTagsByMoment(momentsWithoutCursor);
        Map<Moment, MomentImage> momentImageByMoment = momentImageService.getMomentImageByMoment(momentsWithoutCursor);

        return momentsWithoutCursor.stream()
                .map(moment ->
                        MomentComposition.of(
                                moment, momentTagsByMoment.get(moment),
                                momentImageByMoment.getOrDefault(moment, null)
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
}
