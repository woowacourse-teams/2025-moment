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
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.group.GroupMemberService;
import moment.group.service.group.GroupService;
import moment.group.service.group.InviteLinkService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupApplicationServiceTest {

    @Mock
    private GroupService groupService;

    @Mock
    private GroupMemberService memberService;

    @Mock
    private InviteLinkService inviteLinkService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GroupApplicationService groupApplicationService;

    @Test
    void 그룹_생성_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "닉네임");
        GroupInviteLink inviteLink = GroupInviteLinkFixture.createValidLink(group);

        when(userService.getUserBy(1L)).thenReturn(owner);
        when(groupService.create(anyString(), anyString(), any())).thenReturn(group);
        when(memberService.createOwner(any(), any(), anyString())).thenReturn(ownerMember);
        when(inviteLinkService.createOrGet(any())).thenReturn(inviteLink);

        GroupCreateRequest request = new GroupCreateRequest("그룹명", "설명", "닉네임");

        // When
        GroupCreateResponse response = groupApplicationService.createGroup(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 그룹");
        verify(groupService).create("그룹명", "설명", owner);
        verify(memberService).createOwner(group, owner, "닉네임");
        verify(inviteLinkService).createOrGet(group);
    }

    @Test
    void 그룹_상세_조회_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User member1 = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너");
        GroupMember approvedMember = GroupMemberFixture.createApprovedMember(group, member1, "멤버1");
        List<GroupMember> members = List.of(ownerMember, approvedMember);

        when(groupService.getById(1L)).thenReturn(group);
        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(ownerMember);
        when(memberService.getApprovedMembers(1L)).thenReturn(members);
        when(memberService.countApprovedMembers(1L)).thenReturn(2L);

        // When
        GroupDetailResponse response = groupApplicationService.getGroupDetail(1L, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.groupId()).isEqualTo(1L);
        assertThat(response.isOwner()).isTrue();
        assertThat(response.memberCount()).isEqualTo(2L);
        assertThat(response.members()).hasSize(2);
    }

    @Test
    void 내_그룹_목록_조회() {
        // Given
        User user = UserFixture.createUserWithId(1L);
        User owner1 = UserFixture.createUserWithId(2L);
        User owner2 = UserFixture.createUserWithId(3L);
        Group group1 = GroupFixture.createGroupWithId(1L, owner1);
        Group group2 = GroupFixture.createGroupWithId(2L, owner2);
        List<GroupMember> memberships = List.of(
            GroupMemberFixture.createApprovedMember(group1, user, "닉네임1"),
            GroupMemberFixture.createApprovedMember(group2, user, "닉네임2")
        );

        when(memberService.getMyGroups(1L)).thenReturn(memberships);
        when(memberService.countApprovedMembers(1L)).thenReturn(5L);
        when(memberService.countApprovedMembers(2L)).thenReturn(3L);

        // When
        List<MyGroupResponse> response = groupApplicationService.getMyGroups(1L);

        // Then
        assertThat(response).hasSize(2);
    }

    @Test
    void 그룹_수정_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        when(userService.getUserBy(1L)).thenReturn(owner);

        // When
        groupApplicationService.updateGroup(1L, 1L, "새이름", "새설명");

        // Then
        verify(groupService).update(1L, "새이름", "새설명", owner);
    }

    @Test
    void 그룹_삭제_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        when(memberService.countApprovedMembers(1L)).thenReturn(1L);
        when(userService.getUserBy(1L)).thenReturn(owner);

        // When
        groupApplicationService.deleteGroup(1L, 1L);

        // Then
        verify(groupService).delete(1L, owner);
    }

    @Test
    void 그룹_삭제_멤버_있으면_실패() {
        // Given
        when(memberService.countApprovedMembers(1L)).thenReturn(3L);

        // When/Then
        assertThatThrownBy(() -> groupApplicationService.deleteGroup(1L, 1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_DELETE_GROUP_WITH_MEMBERS);
    }
}
