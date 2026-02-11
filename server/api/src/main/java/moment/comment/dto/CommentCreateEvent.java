package moment.comment.dto;

import moment.moment.domain.Moment;

public record CommentCreateEvent(
        Long momentId,
        Long momenterId,
        Long commenterId,
        Long groupId
) {

    public static CommentCreateEvent of(Moment moment, Long commenterId) {
        Long groupId = moment.getGroup() != null ? moment.getGroup().getId() : null;
        return new CommentCreateEvent(moment.getId(), moment.getMomenterId(), commenterId, groupId);
    }
}
