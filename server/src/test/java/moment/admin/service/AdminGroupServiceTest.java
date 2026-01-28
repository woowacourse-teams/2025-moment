package moment.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import moment.admin.dto.response.AdminGroupInviteLinkResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import moment.admin.service.group.AdminGroupService;
import moment.comment.infrastructure.CommentRepository;
import moment.fixture.GroupFixture;
import moment.fixture.GroupInviteLinkFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.infrastructure.GroupInviteLinkRepository;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.infrastructure.MomentRepository;
import moment.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminGroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private MomentRepository momentRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private GroupInviteLinkRepository groupInviteLinkRepository;

    private AdminGroupService adminGroupService;

    private User owner;
    private Group group;

    @BeforeEach
    void setUp() {
        adminGroupService = new AdminGroupService(
            groupRepository,
            groupMemberRepository,
            momentRepository,
            commentRepository,
            groupInviteLinkRepository
        );
        ReflectionTestUtils.setField(adminGroupService, "baseUrl", "https://moment.com");

        owner = UserFixture.createUser();
        ReflectionTestUtils.setField(owner, "id", 1L);
        group = GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명");
        ReflectionTestUtils.setField(group, "id", 1L);
    }

    @Test
    void getInviteLink_활성_초대링크_반환() {
        // given
        GroupInviteLink inviteLink = GroupInviteLinkFixture.createValidLink(group);
        given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
        given(groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(1L))
            .willReturn(Optional.of(inviteLink));

        // when
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo(inviteLink.getCode());
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void getInviteLink_fullUrl_도메인_포함() {
        // given
        GroupInviteLink inviteLink = GroupInviteLinkFixture.createValidLinkWithCode(group, "test-code-123");
        given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
        given(groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(1L))
            .willReturn(Optional.of(inviteLink));

        // when
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(1L);

        // then
        assertThat(response.fullUrl()).isEqualTo("https://moment.com/invite/test-code-123");
    }

    @Test
    void getInviteLink_만료된_링크_isExpired_true() {
        // given
        GroupInviteLink expiredLink = GroupInviteLinkFixture.createExpiredLink(group);
        given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
        given(groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(1L))
            .willReturn(Optional.of(expiredLink));

        // when
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(1L);

        // then
        assertThat(response.isExpired()).isTrue();
    }

    @Test
    void getInviteLink_만료되지않은_링크_isExpired_false() {
        // given
        GroupInviteLink validLink = GroupInviteLinkFixture.createValidLink(group);
        given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
        given(groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(1L))
            .willReturn(Optional.of(validLink));

        // when
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(1L);

        // then
        assertThat(response.isExpired()).isFalse();
    }

    @Test
    void getInviteLink_그룹없으면_예외() {
        // given
        given(groupRepository.findByIdIncludingDeleted(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminGroupService.getInviteLink(999L))
            .isInstanceOf(AdminException.class)
            .hasFieldOrPropertyWithValue("errorCode", AdminErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    void getInviteLink_초대링크없으면_null_반환() {
        // given
        given(groupRepository.findByIdIncludingDeleted(1L)).willReturn(Optional.of(group));
        given(groupInviteLinkRepository.findFirstByGroupIdOrderByCreatedAtDesc(1L))
            .willReturn(Optional.empty());

        // when
        AdminGroupInviteLinkResponse response = adminGroupService.getInviteLink(1L);

        // then
        assertThat(response).isNull();
    }
}
