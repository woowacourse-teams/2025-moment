package moment.comment.dto;

import moment.moment.domain.Moment;

public record CommentCreateEvent(
        Long momentId,
        Long momenterId
) {

    public static CommentCreateEvent of(Moment moment) {
        return new CommentCreateEvent(moment.getId(), moment.getMomenterId());
    }
}
