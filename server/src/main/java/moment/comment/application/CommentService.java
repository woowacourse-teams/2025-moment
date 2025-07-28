package moment.comment.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.dto.response.CommentStatusResponse;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentCreateResponse;
import moment.comment.dto.response.MyCommentsResponse;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.application.MomentQueryService;
import moment.moment.domain.Moment;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final UserQueryService userQueryService;
    private final MomentQueryService momentQueryService;
    private final CommentRepository commentRepository;
    private final EmojiRepository emojiRepository;
    private final CommentQueryService commentQueryService;

    @Transactional
    public CommentCreateResponse addComment(CommentCreateRequest request, Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Moment moment = momentQueryService.getMomentById(request.momentId());

        if (commentQueryService.existsByMoment(moment)) {
            throw new MomentException(ErrorCode.COMMENT_CONFLICT);
        }

        Comment comment = request.toComment(commenter, moment);

        Comment savedComment = commentRepository.save(comment);

        return CommentCreateResponse.from(savedComment);
    }

    public List<MyCommentsResponse> getCommentsByUserId(Long commenterId) {
        if (!userQueryService.existsById(commenterId)) {
            throw new MomentException(ErrorCode.USER_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findCommentsByCommenterIdOrderByCreatedAtDesc(commenterId);

        List<Emoji> emojis = emojiRepository.findAllByCommentIn(comments);

        if (emojis.isEmpty()) {
            return comments.stream()
                    .map(MyCommentsResponse::from)
                    .toList();
        }

        Map<Comment, List<Emoji>> commentAndEmojis = emojis.stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        return commentAndEmojis.entrySet().stream()
                .map(MyCommentsResponse::from)
                .toList();
    }

    public CommentStatusResponse checkCommentStatus(Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);
        Optional<Moment> matchedMoment = momentQueryService.findTodayMatchedMomentByCommenter(commenter);

        if(matchedMoment.isEmpty()) {
            return CommentStatusResponse.createNotMatchedStatus();
        }

        if(commentRepository.existsByMoment(matchedMoment.get())) {
            return CommentStatusResponse.createAlreadyCommentedStatus();
        }

        return CommentStatusResponse.createWritingAvailableStatus();
    }
}
