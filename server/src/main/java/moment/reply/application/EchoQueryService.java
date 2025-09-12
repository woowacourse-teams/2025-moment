package moment.reply.application;

import java.util.List;
import java.util.Map;
import moment.comment.domain.Comment;
import moment.reply.domain.Echo;

public interface EchoQueryService {

    List<Echo> getEchosByComment(Comment comment);

    Echo getEchoById(Long emojiId);

    Map<Comment, List<Echo>> getEchosOfComments(List<Comment> comments);

    List<Echo> getAllByCommentIn(List<Comment> comments);
}
