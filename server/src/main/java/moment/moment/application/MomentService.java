package moment.moment.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentTag;
import moment.moment.domain.Tag;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.CommentableMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.moment.infrastructure.MomentTagRepository;
import moment.reply.domain.Echo;
import moment.reply.infrastructure.EchoRepository;
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

    private static final String CURSOR_PART_DELIMITER = "_";
    private static final int CURSOR_TIME_INDEX = 0;
    private static final int CURSOR_ID_INDEX = 1;

    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final EchoRepository echoRepository;
    private final MomentTagRepository momentTagRepository;
    private final UserQueryService userQueryService;
    private final RewardService rewardService;
    private final TagService tagService;

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

        Tag registeredTag = tagService.getOrRegister(request.tagName());
        MomentTag momentTag = new MomentTag(savedMoment, registeredTag);
        momentTagRepository.save(momentTag);

        rewardService.rewardForMoment(momenter, Reason.MOMENT_CREATION, savedMoment.getId());

        return MomentCreateResponse.of(savedMoment, momentTag);
    }

    @Transactional
    public MomentCreateResponse addExtraMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        if (!extraMomentCreatePolicy.canCreate(momenter)) {
            throw new MomentException(ErrorCode.USER_NOT_ENOUGH_STAR);
        }

        Moment momentWithoutId = new Moment(request.content(), momenter, WriteType.EXTRA);
        Moment savedMoment = momentRepository.save(momentWithoutId);

        Tag registeredTag = tagService.getOrRegister(request.tagName());
        MomentTag momentTag = new MomentTag(savedMoment, registeredTag);
        momentTagRepository.save(momentTag);

        rewardService.useReward(momenter, Reason.MOMENT_ADDITIONAL_USE, savedMoment.getId());

        return MomentCreateResponse.of(savedMoment, momentTag);
    }

    public MyMomentPageResponse getMyMoments(String cursor, int pageSize, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        // todo : 커서 검증 필요

        if (pageSize <= 0 || pageSize > 100) {
            throw new MomentException(ErrorCode.MOMENTS_LIMIT_INVALID);
        }

        PageRequest pageable = PageRequest.of(0, pageSize + 1);

        List<Moment> momentsWithinCursor = new ArrayList<>();

        if (cursor == null || cursor.isBlank()) {
            momentsWithinCursor = momentRepository.findMyMomentFirstPage(momenter, pageable);
        }

        if (cursor != null) {
            String[] cursorParts = cursor.split(CURSOR_PART_DELIMITER);
            LocalDateTime cursorDateTime = LocalDateTime.parse(cursorParts[CURSOR_TIME_INDEX]);
            Long cursorId = Long.valueOf(cursorParts[CURSOR_ID_INDEX]);
            momentsWithinCursor = momentRepository.findMyMomentsNextPage(momenter, cursorDateTime, cursorId, pageable);
        }

        List<Comment> comments = commentRepository.findAllByMomentIn(momentsWithinCursor);

        boolean hasNextPage = momentsWithinCursor.size() > pageSize;
        String nextCursor = extractCursor(momentsWithinCursor, hasNextPage);
        List<Moment> moments = extractMoments(momentsWithinCursor, pageSize);

        List<MomentTag> momentTags = momentTagRepository.findAllByMomentIn(moments);

        Map<Moment, List<MomentTag>> momentTagsByMoment = momentTags.stream()
                .collect(Collectors.groupingBy(MomentTag::getMoment));

        if (comments.isEmpty()) {
            List<MyMomentResponse> responses = moments.stream()
                    .map(moment -> MyMomentResponse.of(
                            moment,
                            Collections.emptyList(),
                            Collections.emptyMap(),
                            momentTagsByMoment.getOrDefault(moment, Collections.emptyList())))
                    .toList();

            return MyMomentPageResponse.of(responses, nextCursor, hasNextPage, responses.size());
        }

        Map<Moment, List<Comment>> commentsByMoment = comments.stream()
                .collect(Collectors.groupingBy(Comment::getMoment));

        Map<Comment, List<Echo>> echosByComment = echoRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Echo::getComment));

        List<MyMomentResponse> responses = moments.stream()
                .map(moment -> {
                    List<Comment> momentComments = commentsByMoment.getOrDefault(moment, List.of());

                    Map<Long, List<Echo>> commentEchos = momentComments.stream()
                            .collect(Collectors.toMap(Comment::getId,
                                    comment -> echosByComment.getOrDefault(comment, List.of())));

                    List<MomentTag> momentTag = momentTagsByMoment.getOrDefault(moment, List.of());

                    return MyMomentResponse.of(moment, momentComments, commentEchos, momentTag);
                })
                .toList();

        return MyMomentPageResponse.of(responses, nextCursor, hasNextPage, responses.size());
    }

    private List<Moment> extractMoments(List<Moment> momentsWithinCursor, int pageSize) {
        if (momentsWithinCursor.size() > pageSize) {
            return momentsWithinCursor.subList(0, pageSize);
        }
        return momentsWithinCursor;
    }

    private String extractCursor(List<Moment> moments, boolean hasNext) {
        String nextCursor = null;

        List<Moment> pagingMoments = new ArrayList<>(moments);

        if (!pagingMoments.isEmpty() && hasNext) {
            Moment cursor = pagingMoments.get(moments.size() - 2);
            nextCursor = cursor.getCreatedAt().toString() + CURSOR_PART_DELIMITER + cursor.getId();
        }

        return nextCursor;
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
        if(tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMoments(user, threeDaysAgo);
        }
        if(!tagNames.isEmpty()) {
            commentableMoments = momentRepository.findCommentableMomentsByTagNames(user, threeDaysAgo, tagNames);
        }

        if (commentableMoments.isEmpty()) {
            return CommentableMomentResponse.empty();
        }

        Moment moment = commentableMoments.get(new Random().nextInt(commentableMoments.size()));

        return CommentableMomentResponse.from(moment);
    }
}
