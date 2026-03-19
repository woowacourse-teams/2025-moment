package moment.group.service.group;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.infrastructure.GroupInviteLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteLinkService {

    private static final int DEFAULT_VALID_DAYS = 7;

    private final GroupInviteLinkRepository inviteLinkRepository;

    @Transactional
    public GroupInviteLink createOrGet(Group group) {
        Optional<GroupInviteLink> existing = inviteLinkRepository
            .findByGroupIdAndIsActiveTrue(group.getId());

        if (existing.isPresent()) {
            GroupInviteLink link = existing.get();
            if (link.isValid()) {
                return link;
            }
            link.deactivate();
        }

        GroupInviteLink newLink = new GroupInviteLink(group, DEFAULT_VALID_DAYS);
        return inviteLinkRepository.save(newLink);
    }

    public GroupInviteLink getByCode(String code) {
        GroupInviteLink link = inviteLinkRepository.findByCode(code)
            .orElseThrow(() -> new MomentException(ErrorCode.INVITE_LINK_INVALID));

        if (!link.isValid()) {
            throw new MomentException(ErrorCode.INVITE_LINK_EXPIRED);
        }

        return link;
    }

    @Transactional
    public void deactivate(Long linkId) {
        GroupInviteLink link = inviteLinkRepository.findById(linkId)
            .orElseThrow(() -> new MomentException(ErrorCode.INVITE_LINK_INVALID));
        link.deactivate();
    }

    @Transactional
    public void refresh(Long groupId) {
        inviteLinkRepository.findByGroupIdAndIsActiveTrue(groupId)
            .ifPresent(GroupInviteLink::deactivate);
    }
}
