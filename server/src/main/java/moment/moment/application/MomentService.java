package moment.moment.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.moment.domain.Moment;
import moment.moment.dto.request.MomentCreateRequest;
import moment.moment.dto.response.MomentCreateResponse;
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

    @Transactional
    public MomentCreateResponse addMoment(MomentCreateRequest request, Long momenterId) {
        User momenter = userQueryService.getUserById(momenterId);
        Moment momentWithoutId = new Moment(request.content(), momenter);
        Moment moment = momentRepository.save(momentWithoutId);

        return MomentCreateResponse.of(moment);
    }

    public List<MyMomentResponse> getMyMoments(Long userId) {
        // 1. 모멘트 조회 (쿼리 1)
        List<Moment> moments = momentRepository.findMomentByMomenter_Id(userId);

        // 모멘트가 없는 경우 대비
        if (moments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 위 모멘트에 달린 모든 코멘트 조회 (쿼리 2)
        // 현재는 1:1
        Map<Moment, Comment> commentByMoment = commentRepository.findAllByMomentIn(moments).stream()
                .collect(Collectors.toMap(Comment::getMoment, comment -> comment));

        // 코멘트가 없는 경우 대비
        if (commentByMoment.isEmpty()) {
            return moments.stream()
                    .map(moment -> MyMomentResponse.of(moment, null, Collections.emptyList()))
                    .toList();
        }

        // 3. 위 코멘트들에 달린 모든 이모지 조회 (쿼리 3)
        List<Comment> comments = new ArrayList<>(commentByMoment.values());
        Map<Comment, List<Emoji>> emojisByComment = emojiRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Emoji::getComment));

        // 4. 최종 데이터 조립
        return moments.stream()
                .map(moment -> {
                    Comment comment = commentByMoment.get(moment);
                    List<Emoji> relatedEmojis = emojisByComment.getOrDefault(comment, Collections.emptyList());
                    return MyMomentResponse.of(moment, comment, relatedEmojis);
                })
                .toList();
    }
}
