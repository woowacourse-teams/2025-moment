package moment.comment.dto;

public record EchoCreateEvent(
        Long commentId,
        Long commenterId
) {
}
