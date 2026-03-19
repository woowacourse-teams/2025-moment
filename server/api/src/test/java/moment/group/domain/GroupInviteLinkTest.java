package moment.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupInviteLinkTest {

    @Test
    void GroupInviteLink_생성_성공() {
        // Given
        Group group = createGroup();

        // When
        GroupInviteLink link = new GroupInviteLink(group, 7);

        // Then
        assertThat(link.getCode()).isNotNull();
        assertThat(link.getCode()).hasSize(36); // UUID format
        assertThat(link.isActive()).isTrue();
        assertThat(link.getExpiredAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void GroupInviteLink_유효한_링크() {
        // Given
        GroupInviteLink link = new GroupInviteLink(createGroup(), 7);

        // When/Then
        assertThat(link.isValid()).isTrue();
    }

    @Test
    void GroupInviteLink_비활성화된_링크() {
        // Given
        GroupInviteLink link = new GroupInviteLink(createGroup(), 7);

        // When
        link.deactivate();

        // Then
        assertThat(link.isValid()).isFalse();
        assertThat(link.isActive()).isFalse();
    }

    @Test
    void GroupInviteLink_재활성화_성공() {
        // Given
        GroupInviteLink link = new GroupInviteLink(createGroup(), 7);
        link.deactivate();

        // When
        link.activate();

        // Then
        assertThat(link.isActive()).isTrue();
        assertThat(link.isValid()).isTrue();
    }

    @Test
    void GroupInviteLink_만료_연장_성공() {
        // Given
        GroupInviteLink link = new GroupInviteLink(createGroup(), 1);
        LocalDateTime originalExpiry = link.getExpiredAt();

        // When
        link.extendExpiration(7);

        // Then
        assertThat(link.getExpiredAt()).isAfter(originalExpiry);
    }

    private User createUser(Long id) {
        User user = new User("test" + id + "@example.com", "password", "닉네임" + id, ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Group createGroup() {
        User owner = createUser(1L);
        Group group = new Group("테스트 그룹", "설명", owner);
        ReflectionTestUtils.setField(group, "id", 1L);
        return group;
    }
}
