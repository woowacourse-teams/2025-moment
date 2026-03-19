package moment.group.service.group;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import moment.fixture.GroupFixture;
import moment.fixture.GroupInviteLinkFixture;
import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.infrastructure.GroupInviteLinkRepository;
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
class InviteLinkServiceTest {

    @Mock
    private GroupInviteLinkRepository inviteLinkRepository;

    @InjectMocks
    private InviteLinkService inviteLinkService;

    @Test
    void 초대_링크_생성_성공() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(anyLong()))
            .thenReturn(Optional.empty());
        when(inviteLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupInviteLink link = inviteLinkService.createOrGet(group);

        // Then
        assertThat(link.getCode()).isNotNull();
        assertThat(link.isActive()).isTrue();
        verify(inviteLinkRepository).save(any(GroupInviteLink.class));
    }

    @Test
    void 기존_유효한_링크_반환() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink existingLink = GroupInviteLinkFixture.createValidLink(group);
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(anyLong()))
            .thenReturn(Optional.of(existingLink));

        // When
        GroupInviteLink link = inviteLinkService.createOrGet(group);

        // Then
        assertThat(link).isEqualTo(existingLink);
        verify(inviteLinkRepository, never()).save(any());
    }

    @Test
    void 만료된_링크가_있으면_비활성화_후_새로_생성() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink expiredLink = GroupInviteLinkFixture.createExpiredLink(group);
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(anyLong()))
            .thenReturn(Optional.of(expiredLink));
        when(inviteLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupInviteLink link = inviteLinkService.createOrGet(group);

        // Then
        assertThat(expiredLink.isActive()).isFalse();
        assertThat(link).isNotEqualTo(expiredLink);
        assertThat(link.isValid()).isTrue();
        verify(inviteLinkRepository).save(any(GroupInviteLink.class));
    }

    @Test
    void 코드로_조회_성공() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink validLink = GroupInviteLinkFixture.createValidLink(group);
        when(inviteLinkRepository.findByCode("valid-code"))
            .thenReturn(Optional.of(validLink));

        // When
        GroupInviteLink link = inviteLinkService.getByCode("valid-code");

        // Then
        assertThat(link).isEqualTo(validLink);
    }

    @Test
    void 코드로_조회_유효하지_않은_링크() {
        // Given
        when(inviteLinkRepository.findByCode("invalid"))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> inviteLinkService.getByCode("invalid"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_LINK_INVALID);
    }

    @Test
    void 코드로_조회_만료된_링크() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink expiredLink = GroupInviteLinkFixture.createExpiredLink(group);
        when(inviteLinkRepository.findByCode("expired"))
            .thenReturn(Optional.of(expiredLink));

        // When/Then
        assertThatThrownBy(() -> inviteLinkService.getByCode("expired"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_LINK_EXPIRED);
    }

    @Test
    void 링크_비활성화_성공() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink link = GroupInviteLinkFixture.createValidLink(group);
        when(inviteLinkRepository.findById(1L)).thenReturn(Optional.of(link));

        // When
        inviteLinkService.deactivate(1L);

        // Then
        assertThat(link.isActive()).isFalse();
    }

    @Test
    void 링크_비활성화_존재하지_않음() {
        // Given
        when(inviteLinkRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> inviteLinkService.deactivate(999L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_LINK_INVALID);
    }

    @Test
    void 링크_리프레시_기존_링크_비활성화() {
        // Given
        User owner = UserFixture.createUser();
        Group group = GroupFixture.createGroupWithId(1L, owner);
        GroupInviteLink existingLink = GroupInviteLinkFixture.createValidLink(group);
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(1L))
            .thenReturn(Optional.of(existingLink));

        // When
        inviteLinkService.refresh(1L);

        // Then
        assertThat(existingLink.isActive()).isFalse();
    }
}
