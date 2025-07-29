package moment.moment.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.matching.application.MatchingService;
import moment.moment.domain.Moment;
import moment.moment.domain.MomentCreatePolicy;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MatchedMomentResponse;
import moment.moment.dto.response.MomentCreateResponse;
import moment.moment.dto.response.MomentCreationStatusResponse;
import moment.moment.dto.response.MyMomentResponse;
import moment.moment.infrastructure.MomentRepository;
import moment.reply.domain.Emoji;
import moment.reply.infrastructure.EmojiRepository;
import moment.user.application.UserQueryService;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentService {

    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final EmojiRepository emojiRepository;

    private final UserQueryService userQueryService;
    private final MatchingService matchingService;

    private final MomentCreatePolicy momentCreatePolicy;

    @Transactional
    public MomentCreateResponse addMomentAndMatch(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);

        if (!momentCreatePolicy.canCreate(momenter)) {
            throw new MomentException(ErrorCode.MOMENT_ALREADY_EXIST);
        }

        Moment momentWithoutId = new Moment(request.content(), momenter);
        Moment savedMoment = momentRepository.save(momentWithoutId);

        matchingService.match(savedMoment.getId());

        return MomentCreateResponse.of(savedMoment);
    }

    public List<MyMomentResponse> getMyMoments(Long userId) {
        User momenter = userQueryService.getUserById(userId);
        List<Moment> moments = momentRepository.findMomentByMomenterOrderByCreatedAtDesc(momenter);

        if (moments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Moment, Comment> commentByMoment = commentRepository.findAllByMomentIn(moments).stream()
                .collect(Collectors.toMap(Comment::getMoment, comment -> comment));

        if (commentByMoment.isEmpty()) {
            return moments.stream()
                    .map(moment -> MyMomentResponse.of(moment, null, Collections.emptyList()))
                    .toList();
        }

        List<Comment> comments = new ArrayList<>(commentByMoment.values());
        Map<Comment, List<Emoji>> emojisByComment = emojiRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        return moments.stream()
                .map(moment -> {
                    Comment comment = commentByMoment.get(moment);
                    List<Emoji> relatedEmojis = emojisByComment.getOrDefault(comment, Collections.emptyList());
                    return MyMomentResponse.of(moment, comment, relatedEmojis);
                })
                .toList();
    }

    public MatchedMomentResponse getMatchedMoment(Long commenterId) {
        User commenter = userQueryService.getUserById(commenterId);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

        Optional<Moment> matchedMoment = momentRepository.findMatchedMomentByCommenter(commenter, startOfDay, endOfDay);

        return matchedMoment.map(MatchedMomentResponse::from).orElseGet(MatchedMomentResponse::createEmpty);
    }

    @Transactional(readOnly = true)
    public MomentCreationStatusResponse canCreateMoment(Long id) {
        User user = userQueryService.getUserById(id);

        if (momentCreatePolicy.canCreate(user)) {
            return MomentCreationStatusResponse.createAllowedStatus();
        }

        return MomentCreationStatusResponse.createDeniedStatus();
    }
}
