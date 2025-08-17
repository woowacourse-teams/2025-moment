package moment.reply.application;

import java.util.List;
import moment.comment.domain.Comment;
import moment.reply.domain.Echo;

public interface EchoQueryService {

    List<Echo> getEmojisByComment(Comment comment);

    Echo getEmojiById(Long emojiId);
}
