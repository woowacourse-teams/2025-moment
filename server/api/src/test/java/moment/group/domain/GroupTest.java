package moment.group.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupTest {

    @Test
    void Group_생성_성공() {
        // Given
        User owner = createUser(1L);

        // When
        Group group = new Group("테스트 그룹", "설명", owner);

        // Then
        assertThat(group.getName()).isEqualTo("테스트 그룹");
        assertThat(group.getDescription()).isEqualTo("설명");
        assertThat(group.getOwner()).isEqualTo(owner);
    }

    @Test
    void Group_정보_수정_성공() {
        // Given
        User owner = createUser(1L);
        Group group = new Group("원래 이름", "원래 설명", owner);

        // When
        group.updateInfo("새 이름", "새 설명");

        // Then
        assertThat(group.getName()).isEqualTo("새 이름");
        assertThat(group.getDescription()).isEqualTo("새 설명");
    }

    @Test
    void Group_소유자_확인_성공() {
        // Given
        User owner = createUser(1L);
        User other = createUser(2L);
        Group group = new Group("그룹", "설명", owner);

        // When/Then
        assertThat(group.isOwner(owner)).isTrue();
        assertThat(group.isOwner(other)).isFalse();
    }

    private User createUser(Long id) {
        User user = new User("test" + id + "@example.com", "password", "닉네임" + id, ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
