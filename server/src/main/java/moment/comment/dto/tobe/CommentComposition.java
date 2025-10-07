package moment.comment.dto.tobe;

import java.time.LocalDateTime;
import java.util.List;
import moment.comment.domain.Comment;
import moment.comment.domain.CommentImage;
import moment.comment.domain.Echo;
import moment.user.domain.Level;
import moment.user.domain.User;

public record CommentComposition(
        Long id,
        String content,
        String nickname,
        Level level,
        String imageUrl,
        LocalDateTime commentCreatedAt,
        List<EchoDetail> echoDetails,
        Long momentId
) {
    public static CommentComposition of(Comment comment,
                                        User commenter,
                                        CommentImage commentImage,
                                        List<Echo> echos) {

        return new CommentComposition(
                comment.getId(),
                comment.getContent(),
                commenter.getNickname(),
                commenter.getLevel(),
                commentImage.getImageUrl(),
                comment.getCreatedAt(),
                echos.stream()
                        .map(EchoDetail::from)
                        .toList(),
                comment.getMomentId()
        );
    }
}
