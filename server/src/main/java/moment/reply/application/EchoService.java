package moment.reply.application;

import lombok.RequiredArgsConstructor;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.notification.application.SseNotificationService;
import moment.notification.domain.Notification;
import moment.notification.domain.NotificationType;
import moment.notification.domain.TargetType;
import moment.notification.dto.response.NotificationSseResponse;
import moment.notification.infrastructure.NotificationRepository;
import moment.reply.domain.Echo;
import moment.reply.dto.request.EchoCreateRequest;
import moment.reply.dto.response.EchoCreateResponse;
import moment.reply.dto.response.EchoReadResponse;
import moment.reply.infrastructure.EchoRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EchoService {

    private final EchoRepository echoRepository;
    private final CommentQueryService commentQueryService;
    private final UserQueryService userQueryService;
    private final EchoQueryService echoQueryService;
    private final SseNotificationService sseNotificationService;
    private final NotificationRepository notificationRepository;
    private final RewardService rewardService;

    private static void validateMomenter(Comment comment, User user) {
        Moment moment = comment.getMoment();
        if (!moment.checkMomenter(user)) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }
    }

    @Transactional
    public EchoCreateResponse addEcho(EchoCreateRequest request, Authentication authentication) {
        Comment comment = commentQueryService.getCommentById(request.commentId());
        User user = userQueryService.getUserById(authentication.id());

        validateMomenter(comment, user);

        Echo echoWithoutId = new Echo(request.echoType(), user, comment);

        Echo savedEcho = echoRepository.save(echoWithoutId);

        rewardService.rewardForEcho(comment.getCommenter(), Reason.ECHO_RECEIVED, savedEcho.getId());

        NotificationSseResponse response = NotificationSseResponse.createSseResponse(
                NotificationType.NEW_REPLY_ON_COMMENT,
                TargetType.COMMENT,
                comment.getId()
        );

        Notification notificationWithoutId = new Notification(
                comment.getCommenter(),
                NotificationType.NEW_REPLY_ON_COMMENT,
                TargetType.COMMENT,
                comment.getId());

        sseNotificationService.sendToClient(comment.getCommenter().getId(), "notification", response);
        notificationRepository.save(notificationWithoutId);

        return EchoCreateResponse.from(savedEcho);
    }

    public List<EchoReadResponse> getEchosByCommentId(Long commentId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        List<Echo> echoes = echoQueryService.getEmojisByComment(comment);

        return echoes.stream()
                .map(EchoReadResponse::from)
                .toList();
    }
}
