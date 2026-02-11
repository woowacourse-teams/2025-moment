package moment.group.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import moment.fixture.GroupFixture;
import moment.fixture.GroupInviteLinkFixture;
import moment.fixture.GroupMemberFixture;
import moment.fixture.UserFixture;
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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupMemberApplicationServiceTest {

    @Mock
    private GroupService groupService;

    @Mock
    private GroupMemberService memberService;

    @Mock
    private InviteLinkService inviteLinkService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GroupMemberApplicationService memberApplicationService;

    @Test
    void 초대_정보_조회_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink link = GroupInviteLinkFixture.createValidLinkWithCode(group, "invite-code");

        when(inviteLinkService.getByCode("invite-code")).thenReturn(link);
        when(memberService.countApprovedMembers(1L)).thenReturn(5L);

        // When
        InviteInfoResponse response = memberApplicationService.getInviteInfo("invite-code");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.groupId()).isEqualTo(1L);
        assertThat(response.memberCount()).isEqualTo(5L);
    }

    @Test
    void 그룹_가입_신청_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User newUser = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink link = GroupInviteLinkFixture.createValidLinkWithCode(group, "invite-code");
        GroupMember pendingMember = GroupMemberFixture.createPendingMember(group, newUser, "닉네임");

        when(inviteLinkService.getByCode("invite-code")).thenReturn(link);
        when(userService.getUserBy(2L)).thenReturn(newUser);
        when(memberService.joinOrRestore(any(), any(), anyString())).thenReturn(pendingMember);

        GroupJoinRequest request = new GroupJoinRequest("invite-code", "닉네임");

        // When
        GroupJoinResponse response = memberApplicationService.joinGroup(2L, request);

        // Then
        assertThat(response).isNotNull();
        verify(eventPublisher).publishEvent(any(GroupJoinRequestEvent.class));
    }

    @Test
    void 멤버_승인_이벤트_발행() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User pending = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");
        GroupMember pendingMember = GroupMemberFixture.createPendingMember(group, pending, "대기멤버");

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);
        when(memberService.getById(2L)).thenReturn(pendingMember);

        // When
        memberApplicationService.approveMember(1L, 2L, 1L);

        // Then
        verify(memberService).approve(2L);
        verify(eventPublisher).publishEvent(any(GroupJoinApprovedEvent.class));
    }

    @Test
    void 멤버_승인_권한_없음() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User nonOwner = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember nonOwnerMember = GroupMemberFixture.createApprovedMember(group, nonOwner, "일반멤버");

        when(memberService.getByGroupAndUser(1L, 2L)).thenReturn(nonOwnerMember);

        // When/Then
        assertThatThrownBy(() -> memberApplicationService.approveMember(1L, 3L, 2L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_OWNER);
    }

    @Test
    void 멤버_거절_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);

        // When
        memberApplicationService.rejectMember(1L, 2L, 1L);

        // Then
        verify(memberService).reject(2L);
    }

    @Test
    void 멤버_강퇴_이벤트_발행() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User member = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");
        GroupMember targetMember = GroupMemberFixture.createApprovedMember(group, member, "타겟멤버");

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);
        when(memberService.getById(2L)).thenReturn(targetMember);

        // When
        memberApplicationService.kickMember(1L, 2L, 1L);

        // Then
        verify(memberService).kick(2L);
        verify(eventPublisher).publishEvent(any(GroupKickedEvent.class));
    }

    @Test
    void 그룹_탈퇴_성공() {
        // When
        memberApplicationService.leaveGroup(1L, 2L);

        // Then
        verify(memberService).leave(1L, 2L);
    }

    @Test
    void 소유권_이전_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User newOwner = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember newOwnerMember = GroupMemberFixture.createApprovedMember(group, newOwner, "새오너");

        when(memberService.getById(2L)).thenReturn(newOwnerMember);
        when(groupService.getById(1L)).thenReturn(group);

        // When
        memberApplicationService.transferOwnership(1L, 1L, 2L);

        // Then
        verify(memberService).transferOwnership(1L, 1L, 2L);
        assertThat(group.getOwner().getId()).isEqualTo(2L);
    }

    @Test
    void 프로필_수정_성공() {
        // Given
        User user = UserFixture.createUserWithId(1L);
        User owner = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember member = GroupMemberFixture.createMemberWithId(1L, group, user, "이전닉네임");

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(member);

        // When
        memberApplicationService.updateProfile(1L, 1L, "새닉네임");

        // Then
        verify(memberService).updateNickname(1L, "새닉네임");
    }

    @Test
    void 멤버_목록_조회() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User member1 = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        List<GroupMember> members = List.of(
            GroupMemberFixture.createOwnerMember(group, owner, "오너"),
            GroupMemberFixture.createApprovedMember(group, member1, "멤버1")
        );

        when(memberService.getApprovedMembers(1L)).thenReturn(members);

        // When
        List<MemberResponse> response = memberApplicationService.getMembers(1L);

        // Then
        assertThat(response).hasSize(2);
    }

    @Test
    void 대기중_멤버_목록_조회() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User pending1 = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");
        List<GroupMember> pendingMembers = List.of(
            GroupMemberFixture.createPendingMember(group, pending1, "대기1")
        );

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);
        when(memberService.getPendingMembers(1L)).thenReturn(pendingMembers);

        // When
        List<MemberResponse> response = memberApplicationService.getPendingMembers(1L, 1L);

        // Then
        assertThat(response).hasSize(1);
    }

    @Test
    void 초대_링크_생성() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");
        GroupInviteLink link = GroupInviteLinkFixture.createValidLinkWithCode(group, "new-invite-code");

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);
        when(groupService.getById(1L)).thenReturn(group);
        when(inviteLinkService.createOrGet(group)).thenReturn(link);

        // When
        String inviteCode = memberApplicationService.createInviteLink(1L, 1L);

        // Then
        assertThat(inviteCode).isEqualTo("new-invite-code");
    }
}
