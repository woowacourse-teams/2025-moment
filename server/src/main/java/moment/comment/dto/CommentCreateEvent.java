package moment.comment.dto;

import moment.comment.dto.response.CommentCreateResponse;
import moment.moment.domain.Moment;

public record CommentCreateEvent(
        Long momentId,
        Long momenterId,
        Long commentId,
        Long commenterId
) {

    public static CommentCreateEvent of(CommentCreateResponse commentResponse, Long commenterId, Moment moment) {
        return new CommentCreateEvent(
                moment.getId(),
                moment.getMomenterId(),
                commentResponse.commentId(),
                commenterId
        );
    }
}
