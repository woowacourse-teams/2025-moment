package moment.reply.application;

import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Emoji;

public interface EmojiQueryService {

    List<Emoji> getEmojisByComment(Comment comment);
}
