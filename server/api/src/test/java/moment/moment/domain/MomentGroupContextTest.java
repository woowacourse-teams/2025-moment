package moment.moment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MomentGroupContextTest {

    @Test
    void Moment_그룹_컨텍스트_생성_성공() {
        // Given
        User momenter = createUser(1L);
        Group group = createGroup(momenter);
        GroupMember member = createMember(group, momenter);

        // When
        Moment moment = new Moment(momenter, group, member, "그룹 모멘트 내용");

        // Then
        assertThat(moment.getGroup()).isEqualTo(group);
        assertThat(moment.getMember()).isEqualTo(member);
        assertThat(moment.getContent()).isEqualTo("그룹 모멘트 내용");
        assertThat(moment.getMomenter()).isEqualTo(momenter);
    }

    private User createUser(Long id) {
        User user = new User("test" + id + "@example.com", "password", "닉네임" + id, ProviderType.EMAIL);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Group createGroup(User owner) {
        Group group = new Group("테스트 그룹", "설명", owner);
        ReflectionTestUtils.setField(group, "id", 1L);
        return group;
    }

    private GroupMember createMember(Group group, User user) {
        return GroupMember.createOwner(group, user, "닉네임");
    }
}
