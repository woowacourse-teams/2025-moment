package moment.comment.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentCreationStatus;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.CommentCreationStatusResponse;
import moment.comment.dto.response.MyCommentPageResponse;
import moment.comment.dto.response.MyCommentResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private static final String CURSOR_PART_DELIMITER = "_";
    private static final int CURSOR_TIME_INDEX = 0;
    private static final int CURSOR_ID_INDEX = 1;

    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;
    private final CommentRepository commentRepository;
    private final EmojiRepository emojiRepository;
    private final CommentQueryService commentQueryService;
    private final RewardService rewardService;

    @Transactional
    public CommentCreateResponse addComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        if (commentQueryService.existsByMoment(moment)) {
            throw new MomentException(ErrorCode.COMMENT_CONFLICT);
        }

        Comment comment = request.toComment(commenter, moment);

        Comment savedComment = commentRepository.save(comment);

        rewardService.reward(commenter, Reason.COMMENT_CREATION, savedComment.getId());

        return CommentCreateResponse.from(savedComment);
    }

    public MyCommentPageResponse getCommentsByUserIdWithCursor(String cursor, int pageSize, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);

        if (pageSize <= 0 || pageSize > 100) {
            throw new MomentException(ErrorCode.COMMENTS_LIMIT_INVALID);
        }

        // todo : 커서 검증 필요

        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<Comment> commentsWithinCursor = new ArrayList<>();

        if (cursor == null || cursor.isBlank()) {
            commentsWithinCursor = commentRepository.findCommentsFirstPage(commenter, pageable);
        }

        if (cursor != null) {
            String[] cursorParts = cursor.split(CURSOR_PART_DELIMITER);
            LocalDateTime cursorDateTime = LocalDateTime.parse(cursorParts[CURSOR_TIME_INDEX]);
            Long cursorId = Long.valueOf(cursorParts[CURSOR_ID_INDEX]);
            commentsWithinCursor = commentRepository.findCommentsNextPage(commenter, cursorDateTime, cursorId, pageable);
        }

        String nextCursor = extractCursor(commentsWithinCursor, pageSize);
        List<Comment> comments = extractComments(commentsWithinCursor, pageSize);

        boolean hasNextPage = !nextCursor.isBlank();

        List<Emoji> emojis = emojiRepository.findAllByCommentIn(comments);

        if (emojis.isEmpty()) {
            List<MyCommentResponse> responses = comments.stream()
                    .map(MyCommentResponse::from)
                    .toList();
            return MyCommentPageResponse.of(responses, nextCursor, hasNextPage, pageSize);
        }

        Map<Comment, List<Emoji>> commentAndEmojis = emojis.stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        List<MyCommentResponse> responses = commentAndEmojis.entrySet().stream()
                .map(MyCommentResponse::from)
                .toList();

        return MyCommentPageResponse.of(responses, nextCursor, hasNextPage, pageSize);
    }

    private List<Comment> extractComments(List<Comment> comments, int pageSize) {
        return comments.subList(0, pageSize);
    }

    private String extractCursor(List<Comment> comments, int pageSize) {
        boolean hasNext = comments.size() > pageSize;

        String nextCursor = null;

        List<Comment> pagingComments = new ArrayList<>(comments);

        if (!pagingComments.isEmpty() && hasNext) {
            Comment cursor = pagingComments.getFirst();
            nextCursor = cursor.getCreatedAt().toString() + CURSOR_PART_DELIMITER + cursor.getId();
        }

        return nextCursor;
    }

    public CommentCreationStatusResponse canCreateComment(Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Optional<Moment> matchedMoment = momentQueryService.findTodayMatchedMomentByCommenter(commenter);

        if (matchedMoment.isEmpty()) {
            return CommentCreationStatusResponse.from(CommentCreationStatus.NOT_MATCHED);
        }

        if (commentRepository.existsByMoment(matchedMoment.get())) {
            return CommentCreationStatusResponse.from(CommentCreationStatus.ALREADY_COMMENTED);
        }

        return CommentCreationStatusResponse.from(CommentCreationStatus.WRITABLE);
    }
}
