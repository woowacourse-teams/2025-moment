package moment.reply.application;

import java.util.Set;
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

    @Transactional
    public void addEchos(EchoCreateRequest request, Authentication authentication) {
        Comment comment = commentQueryService.getCommentById(request.commentId());
        User user = userQueryService.getUserById(authentication.id());

        validateMomenter(comment, user);

        Set<String> echoTypes = request.echoTypes();
        for(String echoType : echoTypes) {
            if(echoRepository.existsByCommentAndUserAndEchoType(comment, user, echoType)) {
                throw new MomentException(ErrorCode.ECHO_CONFLICT);
            }
            Echo echoWithoutId = new Echo(echoType, user, comment);
            echoRepository.save(echoWithoutId);
        }

        // TODO : 논의 필요, 여러 개의 에코 중에 어떤 에코를 기준으로 잡을지, 현재는 임시로 comment id를 저장한 상태, 특정 commentId에 echo가 처음 달릴 때만 보상을 줄 수 있다고 생각
        rewardService.rewardForEcho(comment.getCommenter(), Reason.ECHO_RECEIVED, comment.getId());

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
    }

    private void validateMomenter(Comment comment, User user) {
        Moment moment = comment.getMoment();
        if (!moment.checkMomenter(user)) {
            throw new MomentException(ErrorCode.USER_UNAUTHORIZED);
        }
    }

    public List<EchoReadResponse> getEchosByCommentId(Long commentId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        List<Echo> echoes = echoQueryService.getEmojisByComment(comment);

        return echoes.stream()
                .map(EchoReadResponse::from)
                .toList();
    }
}
