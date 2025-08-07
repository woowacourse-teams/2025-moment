package moment.reply.application;

import java.util.List;
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
import moment.reply.domain.Emoji;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.reply.dto.response.EmojiCreateResponse;
import moment.reply.dto.response.EmojiReadResponse;
import moment.reply.infrastructure.EmojiRepository;
import moment.reward.application.RewardService;
import moment.reward.domain.Reason;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmojiService {

    private final EmojiRepository emojiRepository;
    private final CommentQueryService commentQueryService;
    private final UserQueryService userQueryService;
    private final EmojiQueryService emojiQueryService;
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
    public EmojiCreateResponse addEmoji(EmojiCreateRequest request, Authentication authentication) {
        Comment comment = commentQueryService.getCommentById(request.commentId());
        User user = userQueryService.getUserById(authentication.id());

        validateMomenter(comment, user);

        Emoji emojiWithoutId = new Emoji(request.emojiType(), user, comment);

        Emoji savedEmoji = emojiRepository.save(emojiWithoutId);

        rewardService.reward(comment.getCommenter(), Reason.POSITIVE_EMOJI_RECEIVED, comment.getId());

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

        //sseNotificationService.sendToClient(comment.getCommenter().getId(), "notification", response);
        notificationRepository.save(notificationWithoutId);

        return EmojiCreateResponse.from(savedEmoji);
    }

    public List<EmojiReadResponse> getEmojisByCommentId(Long commentId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        List<Emoji> emojis = emojiQueryService.getEmojisByComment(comment);

        return emojis.stream()
                .map(EmojiReadResponse::from)
                .toList();
    }

    @Transactional
    public void removeEmojiById(Long emojiId, Long userId) {
        Emoji emoji = emojiQueryService.getEmojiById(emojiId);
        User user = userQueryService.getUserById(userId);

        emoji.checkWriter(user);

        Comment comment = emoji.getComment();
        emojiRepository.delete(emoji);

        cancelRewardIfLastEmoji(comment);
    }

    private void cancelRewardIfLastEmoji(Comment comment) {
        if (!emojiRepository.existsByComment(comment)) {
            rewardService.reward(comment.getCommenter(), Reason.CANCEL_POSITIVE_EMOJI_RECEIVED, comment.getId());
        }
    }
}
