package moment.comment.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentCreateResponse addComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        Comment comment = request.toComment(commenter, moment);

        Comment savedComment = commentRepository.save(comment);

        return CommentCreateResponse.from(savedComment);
    }

    public List<MyCommentsResponse> getCommentsByUserId(Long commenterId) {
        if (!userQueryService.existsById(commenterId)) {
            throw new MomentException(ErrorCode.USER_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findCommentsWithMomentAndEmojisByCommenterId(commenterId);

        return comments.stream()
                .map(MyCommentsResponse::from)
                .toList();
    }
}
