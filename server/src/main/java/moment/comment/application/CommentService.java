package moment.comment.application;

import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;

    public void addComment(CommentCreateRequest request, Long userId) {
        // todo: USER NOT FOUND error code 의미 변경 논의 필요
        User commenter = userRepository.findById(userId)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
        Moment moment = momentRepository.findById(request.momentId())
                .orElseThrow(() -> new MomentException(ErrorCode.MOMENT_NOT_FOUND));

        Comment comment = request.toComment(commenter, moment);

        commentRepository.save(comment);
    }
}
