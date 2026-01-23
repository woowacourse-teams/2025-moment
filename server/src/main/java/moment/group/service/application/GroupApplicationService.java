package moment.group.service.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.group.GroupMemberService;
import moment.group.service.group.GroupService;
import moment.group.service.invite.InviteLinkService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupApplicationService {

    private final GroupService groupService;
    private final GroupMemberService memberService;
    private final InviteLinkService inviteLinkService;
    private final UserService userService;

    @Transactional
    public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
        User owner = userService.getUserBy(userId);

        Group group = groupService.create(request.name(), request.description(), owner);

        GroupMember ownerMember = memberService.createOwner(group, owner, request.ownerNickname());

        GroupInviteLink inviteLink = inviteLinkService.createOrGet(group);

        return GroupCreateResponse.from(group, ownerMember, inviteLink);
    }

    public GroupDetailResponse getGroupDetail(Long groupId, Long userId) {
        Group group = groupService.getById(groupId);
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        List<GroupMember> members = memberService.getApprovedMembers(groupId);
        long memberCount = memberService.countApprovedMembers(groupId);

        return GroupDetailResponse.from(group, member, members, memberCount);
    }

    public List<MyGroupResponse> getMyGroups(Long userId) {
        List<GroupMember> memberships = memberService.getMyGroups(userId);
        return memberships.stream()
            .map(membership -> {
                Group group = membership.getGroup();
                long memberCount = memberService.countApprovedMembers(group.getId());
                return MyGroupResponse.from(group, membership, memberCount);
            })
            .toList();
    }

    @Transactional
    public void updateGroup(Long groupId, Long userId, String name, String description) {
        User user = userService.getUserBy(userId);
        groupService.update(groupId, name, description, user);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        long memberCount = memberService.countApprovedMembers(groupId);
        if (memberCount > 1) {
            throw new MomentException(ErrorCode.CANNOT_DELETE_GROUP_WITH_MEMBERS);
        }

        User user = userService.getUserBy(userId);
        GroupMember owner = memberService.getByGroupAndUser(groupId, userId);
        memberService.deleteOwner(owner);
        groupService.delete(groupId, user);
    }
}
