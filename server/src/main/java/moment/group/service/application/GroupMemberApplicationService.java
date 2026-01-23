package moment.group.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.dto.response.MemberResponse;
import moment.group.service.group.GroupMemberService;
import moment.group.service.group.GroupService;
import moment.group.service.invite.InviteLinkService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberApplicationService {

    private final GroupService groupService;
    private final GroupMemberService memberService;
    private final InviteLinkService inviteLinkService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public InviteInfoResponse getInviteInfo(String inviteCode) {
        GroupInviteLink link = inviteLinkService.getByCode(inviteCode);
        Group group = link.getGroup();
        long memberCount = memberService.countApprovedMembers(group.getId());

        return InviteInfoResponse.from(group, memberCount);
    }

    @Transactional
    public GroupJoinResponse joinGroup(Long userId, GroupJoinRequest request) {
        GroupInviteLink link = inviteLinkService.getByCode(request.inviteCode());
        Group group = link.getGroup();
        User user = userService.getUserBy(userId);

        GroupMember member = memberService.joinOrRestore(group, user, request.nickname());

        eventPublisher.publishEvent(new GroupJoinRequestEvent(
            group.getId(),
            group.getOwner().getId(),
            member.getId(),
            request.nickname()
        ));

        return GroupJoinResponse.from(member);
    }

    @Transactional
    public void approveMember(Long groupId, Long memberId, Long approverId) {
        validateOwner(groupId, approverId);
        memberService.approve(memberId);

        GroupMember member = memberService.getById(memberId);

        eventPublisher.publishEvent(new GroupJoinApprovedEvent(
            groupId,
            member.getUser().getId(),
            memberId
        ));
    }

    @Transactional
    public void rejectMember(Long groupId, Long memberId, Long rejecterId) {
        validateOwner(groupId, rejecterId);
        memberService.reject(memberId);
    }

    @Transactional
    public void kickMember(Long groupId, Long memberId, Long kickerId) {
        validateOwner(groupId, kickerId);
        GroupMember member = memberService.getById(memberId);
        Long kickedUserId = member.getUser().getId();

        memberService.kick(memberId);

        eventPublisher.publishEvent(new GroupKickedEvent(
            groupId,
            kickedUserId,
            memberId
        ));
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        memberService.leave(groupId, userId);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerMemberId) {
        memberService.transferOwnership(groupId, currentOwnerId, newOwnerMemberId);

        GroupMember newOwner = memberService.getById(newOwnerMemberId);
        Group group = groupService.getById(groupId);
    }

    @Transactional
    public void updateProfile(Long groupId, Long userId, String nickname) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        memberService.updateNickname(member.getId(), nickname);
    }

    public List<MemberResponse> getMembers(Long groupId) {
        List<GroupMember> members = memberService.getApprovedMembers(groupId);
        return members.stream()
            .map(MemberResponse::from)
            .toList();
    }

    public List<MemberResponse> getPendingMembers(Long groupId, Long requesterId) {
        validateOwner(groupId, requesterId);
        List<GroupMember> members = memberService.getPendingMembers(groupId);
        return members.stream()
            .map(MemberResponse::from)
            .toList();
    }

    @Transactional
    public String createInviteLink(Long groupId, Long userId) {
        validateOwner(groupId, userId);
        Group group = groupService.getById(groupId);
        GroupInviteLink link = inviteLinkService.createOrGet(group);
        return link.getCode();
    }

    private void validateOwner(Long groupId, Long userId) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        if (!member.isOwner()) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
    }
}
