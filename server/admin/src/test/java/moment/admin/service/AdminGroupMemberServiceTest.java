package moment.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.admin.service.group.AdminGroupMemberService;
import moment.comment.infrastructure.CommentRepository;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminGroupMemberServiceTest {

    @InjectMocks
    private AdminGroupMemberService adminGroupMemberService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentRepository commentRepository;

    private Group group;
    private User user;
    private GroupMember pendingMember;
    private GroupMember approvedMember;
    private GroupMember ownerMember;

    @BeforeEach
    void setUp() {
        group = mock(Group.class);
        user = mock(User.class);

        pendingMember = GroupMember.createPendingMember(group, user, "테스트멤버");
        ReflectionTestUtils.setField(pendingMember, "id", 1L);

        approvedMember = GroupMember.createPendingMember(group, user, "승인멤버");
        approvedMember.approve();
        ReflectionTestUtils.setField(approvedMember, "id", 2L);

        ownerMember = GroupMember.createOwner(group, user, "방장");
        ReflectionTestUtils.setField(ownerMember, "id", 3L);
    }

    @Nested
    class 멤버_승인 {

        @Test
        void approveMember_상태변경_PENDING_to_APPROVED() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L)).willReturn(Optional.of(pendingMember));

            // when
            adminGroupMemberService.approveMember(1L, 1L);

            // then
            assertThat(pendingMember.getStatus()).isEqualTo(MemberStatus.APPROVED);
        }

        @Test
        void approveMember_이미_승인된_멤버_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.approveMember(1L, 2L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.ALREADY_APPROVED);
        }

        @Test
        void approveMember_PENDING_아닌_멤버_예외() {
            // given
            GroupMember kickedMember = GroupMember.createPendingMember(group, user, "추방멤버");
            kickedMember.approve();
            kickedMember.kick();
            ReflectionTestUtils.setField(kickedMember, "id", 4L);

            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 4L)).willReturn(Optional.of(kickedMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.approveMember(1L, 4L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_PENDING);
        }

        @Test
        void approveMember_멤버없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.approveMember(1L, 999L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        void approveMember_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.approveMember(999L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 멤버_거절 {

        @Test
        void rejectMember_멤버십_SoftDelete_성공() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L)).willReturn(Optional.of(pendingMember));

            // when
            adminGroupMemberService.rejectMember(1L, 1L);

            // then
            verify(groupMemberRepository).delete(pendingMember);
        }

        @Test
        void rejectMember_PENDING_아닌_멤버_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.rejectMember(1L, 2L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_PENDING);
        }

        @Test
        void rejectMember_멤버없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.rejectMember(1L, 999L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        void rejectMember_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.rejectMember(999L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 멤버_강제추방 {

        @Test
        void kickMember_상태변경_APPROVED_to_KICKED_및_SoftDelete() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));
            given(momentRepository.findAllIdsByMemberId(2L)).willReturn(List.of());

            // when
            adminGroupMemberService.kickMember(1L, 2L);

            // then
            assertAll(
                () -> assertThat(approvedMember.getStatus()).isEqualTo(MemberStatus.KICKED),
                () -> verify(groupMemberRepository).delete(approvedMember)
            );
        }

        @Test
        void kickMember_Owner_추방불가_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 3L)).willReturn(Optional.of(ownerMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.kickMember(1L, 3L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.CANNOT_KICK_OWNER);
        }

        @Test
        void kickMember_APPROVED_아닌_멤버_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L)).willReturn(Optional.of(pendingMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.kickMember(1L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_APPROVED);
        }

        @Test
        void kickMember_해당멤버_모멘트_전체_삭제() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));
            given(momentRepository.findAllIdsByMemberId(2L)).willReturn(List.of(10L, 11L));

            // when
            adminGroupMemberService.kickMember(1L, 2L);

            // then
            verify(momentRepository).softDeleteByMemberId(2L);
        }

        @Test
        void kickMember_해당멤버_코멘트_전체_삭제() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));
            given(momentRepository.findAllIdsByMemberId(2L)).willReturn(List.of());

            // when
            adminGroupMemberService.kickMember(1L, 2L);

            // then
            verify(commentRepository).softDeleteByMemberId(2L);
        }

        @Test
        void kickMember_멤버없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.kickMember(1L, 999L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        void kickMember_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.kickMember(999L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.GROUP_NOT_FOUND);
        }
    }

    @Nested
    class 소유권_이전 {

        @Test
        void transferOwnership_기존_Owner_역할변경_MEMBER() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));
            given(groupMemberRepository.findOwnerByGroupId(1L)).willReturn(Optional.of(ownerMember));

            // when
            adminGroupMemberService.transferOwnership(1L, 2L);

            // then
            assertThat(ownerMember.getRole()).isEqualTo(MemberRole.MEMBER);
        }

        @Test
        void transferOwnership_새_Owner_역할변경_OWNER() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 2L)).willReturn(Optional.of(approvedMember));
            given(groupMemberRepository.findOwnerByGroupId(1L)).willReturn(Optional.of(ownerMember));

            // when
            adminGroupMemberService.transferOwnership(1L, 2L);

            // then
            assertThat(approvedMember.getRole()).isEqualTo(MemberRole.OWNER);
        }

        @Test
        void transferOwnership_APPROVED_아닌_멤버_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 1L)).willReturn(Optional.of(pendingMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.transferOwnership(1L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_APPROVED);
        }

        @Test
        void transferOwnership_이미_OWNER인_멤버_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 3L)).willReturn(Optional.of(ownerMember));

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.transferOwnership(1L, 3L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.ALREADY_OWNER);
        }

        @Test
        void transferOwnership_멤버없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
            given(groupMemberRepository.findByGroupIdAndMemberId(1L, 999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.transferOwnership(1L, 999L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.MEMBER_NOT_FOUND);
        }

        @Test
        void transferOwnership_그룹없으면_예외() {
            // given
            given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminGroupMemberService.transferOwnership(999L, 1L))
                .isInstanceOf(AdminException.class)
                .extracting(e -> ((AdminException) e).getErrorCode())
                .isEqualTo(AdminErrorCode.GROUP_NOT_FOUND);
        }
    }
}
