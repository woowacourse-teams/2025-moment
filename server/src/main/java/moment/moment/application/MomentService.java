package moment.moment.application;

import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.ExtraMomentCreatePolicy;
import moment.moment.domain.Moment;
import moment.moment.domain.BasicMomentCreatePolicy;
import moment.moment.domain.WriteType;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentPageResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {

    private static final String CURSOR_PART_DELIMITER = "_";
    private static final int CURSOR_TIME_INDEX = 0;
    private static final int CURSOR_ID_INDEX = 1;

    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final EmojiRepository emojiRepository;

    private final UserQueryService userQueryService;

    private final BasicMomentCreatePolicy basicMomentCreatePolicy;
    private final ExtraMomentCreatePolicy extraMomentCreatePolicy;

    private final RewardService rewardService;

    @Transactional
    public MomentCreateResponse addBasicMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        if (!basicMomentCreatePolicy.canCreate(momenter)) {
            throw new MomentException(ErrorCode.MOMENT_ALREADY_EXIST);
        }

        Moment momentWithoutId = new Moment(request.content(), momenter, WriteType.BASIC);
        Moment savedMoment = momentRepository.save(momentWithoutId);
        rewardService.rewardForMoment(momenter, Reason.MOMENT_CREATION, savedMoment.getId());

        return MomentCreateResponse.of(savedMoment);
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

        if (comments.isEmpty()) {
            List<MyMomentResponse> responses = moments.stream()
                    .map(moment -> MyMomentResponse.of(moment, null, Collections.emptyList()))
                    .toList();

            return MyMomentPageResponse.of(responses, nextCursor, hasNextPage, responses.size());
        }

        Map<Moment, Comment> commentByMoment = comments.stream()
                .collect(Collectors.toMap(Comment::getMoment, comment -> comment));

        Map<Comment, List<Emoji>> emojisByComment = emojiRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        List<MyMomentResponse> responses = moments.stream()
                .map(moment -> {
                    Comment comment = commentByMoment.get(moment);
                    List<Emoji> relatedEmojis = emojisByComment.getOrDefault(comment, Collections.emptyList());
                    return MyMomentResponse.of(moment, comment, relatedEmojis);
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
            return  MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }
}
