package moment.comment.service.comment;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.comment.domain.Echo;
import moment.comment.infrastructure.EchoRepository;
import moment.user.domain.User;
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

    @Transactional
    public void saveIfNotExisted(Comment comment, User momenter, Set<String> echoTypes) {
        List<Echo> existingEchos = echoRepository.findByCommentAndUserAndEchoTypeIn(comment, momenter, echoTypes);

        List<String> existingTypes = existingEchos.stream()
                .map(Echo::getEchoType)
                .toList();

        List<Echo> newEchos = echoTypes.stream()
                .filter(type -> !existingTypes.contains(type))
                .map(type -> new Echo(type, momenter, comment))
                .toList();

        if (!newEchos.isEmpty()) {
            echoRepository.saveAll(newEchos);
        }
    }

    public List<Echo> getEchosBy(Comment comment) {
        return echoRepository.findAllByComment(comment);
    }
}
