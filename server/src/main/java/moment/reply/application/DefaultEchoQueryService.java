package moment.reply.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.reply.domain.Echo;
import moment.reply.infrastructure.EchoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultEchoQueryService implements EchoQueryService {

    private final EchoRepository echoRepository;

    @Override
    public List<Echo> getEchosByComment(Comment comment) {
        return echoRepository.findAllByComment(comment);
    }

    @Override
    public Echo getEchoById(Long echoId) {
        return echoRepository.findById(echoId)
                .orElseThrow(() -> new MomentException(ErrorCode.ECHO_NOT_FOUND));
    }

    @Override
    public Map<Comment, List<Echo>> getEchosOfComments(List<Comment> comments) {
        return echoRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Echo::getComment));
    }

    @Override
    public List<Echo> getAllByCommentIn(List<Comment> comments) {
        return echoRepository.findAllByCommentIn(comments);
    }
}
