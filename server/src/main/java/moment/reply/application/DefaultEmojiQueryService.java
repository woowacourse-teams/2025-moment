package moment.reply.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultEmojiQueryService implements EmojiQueryService {

    private final EmojiRepository emojiRepository;

    public List<Emoji> getEmojisByComment(Comment comment) {
        return emojiRepository.findAllByComment(comment);
    }
}
