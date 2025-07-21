package moment.reply.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reply.domain.Emoji;
import moment.reply.domain.EmojiType;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.reply.dto.response.EmojiCreateResponse;
import moment.reply.dto.response.EmojisResponse;
import moment.reply.infrastructure.EmojiRepository;
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

    @Transactional
    public EmojiCreateResponse addEmoji(EmojiCreateRequest request, Authentication authentication) {
        EmojiType emojiType = convertToEmojiType(request.emojiType());

        Comment comment = commentQueryService.getCommentById(request.commentId());
        User user = userQueryService.getUserById(authentication.id());

        Emoji emoji = new Emoji(emojiType, user, comment);
        Emoji savedEmoji = emojiRepository.save(emoji);
        return EmojiCreateResponse.from(savedEmoji);
    }

    private EmojiType convertToEmojiType(String emojiType) {
        try {
            return EmojiType.valueOf(emojiType);
        } catch (IllegalArgumentException e) {
            throw new MomentException(ErrorCode.EMOJI_NOT_FOUND);
        }
    }

    public List<EmojisResponse> getEmojisByCommentId(Long commentId) {
        Comment comment = commentQueryService.getCommentById(commentId);
        List<Emoji> emojis = emojiQueryService.getEmojisByComment(comment);

        return emojis.stream()
                .map(EmojisResponse::from)
                .toList();
    }
}
