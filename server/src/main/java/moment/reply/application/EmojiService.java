package moment.reply.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.application.CommentQueryService;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;
import moment.reply.dto.request.EmojiCreateRequest;
import moment.reply.dto.response.EmojiCreateResponse;
import moment.reply.dto.response.EmojiReadResponse;
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
        Comment comment = commentQueryService.getCommentById(request.commentId());
        User user = userQueryService.getUserById(authentication.id());

        comment.checkAuthorization(user);

        Emoji emoji = new Emoji(request.emojiType(), user, comment);
        Emoji savedEmoji = emojiRepository.save(emoji);
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

        emojiRepository.delete(emoji);
    }
}
