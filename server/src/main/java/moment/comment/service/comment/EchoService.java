package moment.comment.service.comment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.Echo;
import moment.comment.infrastructure.EchoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EchoService {
    
    private final EchoRepository echoRepository;
    
    public Map<Comment, List<Echo>> getEchosOfComments(List<Comment> comments) {
        return echoRepository.findAllByCommentIn(comments).stream()
                .collect(Collectors.groupingBy(Echo::getComment));
    }

    @Transactional
    public void deleteBy(Long commentId) {
        echoRepository.deleteByCommentId(commentId);
    }
}
