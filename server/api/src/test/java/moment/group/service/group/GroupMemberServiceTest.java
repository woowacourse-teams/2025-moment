package moment.group.service.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import moment.fixture.GroupFixture;
import moment.fixture.GroupMemberFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class GroupMemberServiceTest {

    @Mock
    private GroupMemberRepository memberRepository;

    @InjectMocks
    private GroupMemberService memberService;

    @Test
    void Owner_멤버_생성_성공() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupMember ownerMember = memberService.createOwner(group, owner, "닉네임");

        // Then
        assertThat(ownerMember.isOwner()).isTrue();
        assertThat(ownerMember.isApproved()).isTrue();
    }

    @Test
    void Owner_생성_닉네임_중복() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(1L, "중복닉네임"))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> memberService.createOwner(group, owner, "중복닉네임"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_ALREADY_USED);
    }

    @Test
    void 가입_신청_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User newUser = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.findByGroupIdAndUserIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupMember member = memberService.joinOrRestore(group, newUser, "신규닉네임");

        // Then
        assertThat(member.isPending()).isTrue();
        assertThat(member.getNickname()).isEqualTo("신규닉네임");
    }

    @Test
    void 가입_신청_닉네임_중복() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User newUser = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(1L, "중복닉네임"))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> memberService.joinOrRestore(group, newUser, "중복닉네임"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_ALREADY_USED);
    }

    @Test
    void 가입_신청_이미_멤버() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User existingUser = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember existingMember = GroupMemberFixture.createApprovedMember(group, existingUser, "기존닉네임");
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.findByGroupIdAndUserIdIncludeDeleted(1L, 2L))
            .thenReturn(Optional.of(existingMember));

        // When/Then
        assertThatThrownBy(() -> memberService.joinOrRestore(group, existingUser, "새닉네임"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_GROUP_MEMBER);
    }

    @Test
    void 재가입_soft_delete_복구() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember deletedMember = GroupMemberFixture.createDeletedMember(group, user, "이전닉네임");
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.findByGroupIdAndUserIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(deletedMember));

        // When
        GroupMember restored = memberService.joinOrRestore(group, user, "새닉네임");

        // Then
        assertThat(restored.getDeletedAt()).isNull();
        assertThat(restored.getNickname()).isEqualTo("새닉네임");
        assertThat(restored.isPending()).isTrue();
    }

    @Test
    void 멤버_승인_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember pendingMember = GroupMemberFixture.createPendingMember(group, user, "닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(pendingMember));

        // When
        memberService.approve(1L);

        // Then
        assertThat(pendingMember.isApproved()).isTrue();
    }

    @Test
    void 멤버_승인_대기중이_아님() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember approvedMember = GroupMemberFixture.createApprovedMember(group, user, "닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(approvedMember));

        // When/Then
        assertThatThrownBy(() -> memberService.approve(1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_PENDING);
    }

    @Test
    void 멤버_거절_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember pendingMember = GroupMemberFixture.createPendingMember(group, user, "닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(pendingMember));

        // When
        memberService.reject(1L);

        // Then
        verify(memberRepository).delete(pendingMember);
    }

    @Test
    void 멤버_강퇴_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember approvedMember = GroupMemberFixture.createApprovedMember(group, user, "닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(approvedMember));

        // When
        memberService.kick(1L);

        // Then
        verify(memberRepository).delete(approvedMember);
    }

    @Test
    void Owner_강퇴_불가() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(ownerMember));

        // When/Then
        assertThatThrownBy(() -> memberService.kick(1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_KICK_OWNER);
    }

    @Test
    void 멤버_탈퇴_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember member = GroupMemberFixture.createApprovedMember(group, user, "닉네임");
        when(memberRepository.findByGroupIdAndUserId(1L, 2L)).thenReturn(Optional.of(member));

        // When
        memberService.leave(1L, 2L);

        // Then
        verify(memberRepository).delete(member);
    }

    @Test
    void Owner_탈퇴_불가() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember ownerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너닉네임");
        when(memberRepository.findByGroupIdAndUserId(1L, 1L)).thenReturn(Optional.of(ownerMember));

        // When/Then
        assertThatThrownBy(() -> memberService.leave(1L, 1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OWNER_CANNOT_LEAVE);
    }

    @Test
    void 소유권_이전_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User newOwner = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember currentOwnerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너닉네임");
        GroupMember newOwnerMember = GroupMemberFixture.createApprovedMember(group, newOwner, "새오너닉네임");
        when(memberRepository.findByGroupIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(currentOwnerMember));
        when(memberRepository.findById(2L))
            .thenReturn(Optional.of(newOwnerMember));

        // When
        memberService.transferOwnership(1L, 1L, 2L);

        // Then
        assertThat(currentOwnerMember.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(newOwnerMember.getRole()).isEqualTo(MemberRole.OWNER);
    }

    @Test
    void 소유권_이전_승인된_멤버가_아님() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User pendingUser = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember currentOwnerMember = GroupMemberFixture.createOwnerMember(group, owner, "오너닉네임");
        GroupMember pendingMember = GroupMemberFixture.createPendingMember(group, pendingUser, "대기닉네임");
        when(memberRepository.findByGroupIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(currentOwnerMember));
        when(memberRepository.findById(2L))
            .thenReturn(Optional.of(pendingMember));

        // When/Then
        assertThatThrownBy(() -> memberService.transferOwnership(1L, 1L, 2L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_APPROVED);
    }

    @Test
    void 닉네임_수정_성공() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user = UserFixture.createUserWithId(2L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupMember member = GroupMemberFixture.createApprovedMember(group, user, "이전닉네임");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(1L, "새닉네임"))
            .thenReturn(false);

        // When
        memberService.updateNickname(1L, "새닉네임");

        // Then
        assertThat(member.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    void 승인된_멤버_목록_조회() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User user1 = UserFixture.createUserWithId(2L);
        User user2 = UserFixture.createUserWithId(3L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        List<GroupMember> approvedMembers = List.of(
            GroupMemberFixture.createOwnerMember(group, owner, "오너"),
            GroupMemberFixture.createApprovedMember(group, user1, "멤버1"),
            GroupMemberFixture.createApprovedMember(group, user2, "멤버2")
        );
        when(memberRepository.findByGroupIdAndStatus(1L, MemberStatus.APPROVED))
            .thenReturn(approvedMembers);

        // When
        List<GroupMember> result = memberService.getApprovedMembers(1L);

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    void 대기중_멤버_목록_조회() {
        // Given
        User owner = UserFixture.createUserWithId(1L);
        User pending1 = UserFixture.createUserWithId(2L);
        User pending2 = UserFixture.createUserWithId(3L);
        Group group = GroupFixture.createGroupWithId(1L, owner);
        List<GroupMember> pendingMembers = List.of(
            GroupMemberFixture.createPendingMember(group, pending1, "대기1"),
            GroupMemberFixture.createPendingMember(group, pending2, "대기2")
        );
        when(memberRepository.findByGroupIdAndStatus(1L, MemberStatus.PENDING))
            .thenReturn(pendingMembers);

        // When
        List<GroupMember> result = memberService.getPendingMembers(1L);

        // Then
        assertThat(result).hasSize(2);
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
        when(memberRepository.findApprovedMembershipsByUserId(1L))
            .thenReturn(memberships);

        // When
        List<GroupMember> result = memberService.getMyGroups(1L);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void 승인된_멤버_수_조회() {
        // Given
        when(memberRepository.countByGroupIdAndStatus(1L, MemberStatus.APPROVED))
            .thenReturn(5L);

        // When
        long count = memberService.countApprovedMembers(1L);

        // Then
        assertThat(count).isEqualTo(5L);
    }
}
