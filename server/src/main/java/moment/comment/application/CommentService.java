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
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.notification.application.NotificationService;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
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
    private final EmojiRepository emojiRepository;
    private final NotificationRepository notificationRepository;
    private final CommentQueryService commentQueryService;
    private final NotificationService notificationService;

    @Transactional
    public CommentCreateResponse addComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        if (commentQueryService.existsByMoment(moment)) {
            throw new MomentException(ErrorCode.COMMENT_CONFLICT);
        }

        Comment commentWithoutId = request.toComment(commenter, moment);
        Comment savedComment = commentRepository.save(commentWithoutId);

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                moment.getId()
        );

        Notification notificationWithoutId = new Notification(
                moment.getMomenter(),
                NotificationType.NEW_COMMENT_ON_MOMENT,
                TargetType.MOMENT,
                moment.getId());

        notificationService.sendToClient(moment.getMomenterId(), "notification", response);

        notificationRepository.save(notificationWithoutId);

        return CommentCreateResponse.from(savedComment);
    }

    public List<MyCommentsResponse> getCommentsByUserId(Long commenterId) {
        if (!userQueryService.existsById(commenterId)) {
            throw new MomentException(ErrorCode.USER_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findCommentsByCommenterIdOrderByCreatedAtDesc(commenterId);

        List<Emoji> emojis = emojiRepository.findAllByCommentIn(comments);

        if (emojis.isEmpty()) {
            return comments.stream()
                    .map(MyCommentsResponse::from)
                    .toList();
        }

        Map<Comment, List<Emoji>> commentAndEmojis = emojis.stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        return commentAndEmojis.entrySet().stream()
                .map(MyCommentsResponse::from)
                .toList();
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
