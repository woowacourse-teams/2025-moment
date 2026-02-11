package moment.like.dto.event;

import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;

public record MomentLikeEvent(
    Long momentId,
    Long momentOwnerId,
    Long likeMemberId,
    String likerNickname,
    Long groupId,
    Long likerUserId
) {
    public static MomentLikeEvent of(Moment moment, GroupMember member) {
        return new MomentLikeEvent(
            moment.getId(),
            moment.getMomenter().getId(),
            member.getId(),
            member.getNickname(),
            moment.getGroup().getId(),
            member.getUser().getId()
        );
    }
}
