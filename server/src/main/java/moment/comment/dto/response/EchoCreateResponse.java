package moment.comment.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record EchoCreateResponse(Set<String> echoTypes, Long commentId, Long userId, LocalDateTime createdAt) {
}
