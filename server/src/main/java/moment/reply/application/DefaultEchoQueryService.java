package moment.reply.application;

import java.util.List;
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
    public List<Echo> getEmojisByComment(Comment comment) {
        return echoRepository.findAllByComment(comment);
    }

    @Override
    public Echo getEmojiById(Long emojiId) {
        return echoRepository.findById(emojiId)
                .orElseThrow(() -> new MomentException(ErrorCode.ECHO_NOT_FOUND));
    }
}
