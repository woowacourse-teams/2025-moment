package moment.comment.application;

import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;
    private final CommentRepository commentRepository;

    public CommentCreateResponse addComment(CommentCreateRequest request, Long userId) {
        User commenter = userQueryService.getUserById(userId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        Comment comment = request.toComment(commenter, moment);

        Comment savedComment = commentRepository.save(comment);

        return CommentCreateResponse.from(savedComment);
    }
}
