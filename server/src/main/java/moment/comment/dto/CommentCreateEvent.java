package moment.comment.dto;

import moment.moment.domain.Moment;

public record CommentCreateEvent(
        Long momentId,
        Long momenterId,
        Long commenterId
) {

    public static CommentCreateEvent of(Moment moment, Long commenterId) {
        return new CommentCreateEvent(moment.getId(), moment.getMomenterId(), commenterId);
    }
}
