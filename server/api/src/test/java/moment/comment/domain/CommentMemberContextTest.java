package moment.comment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentMemberContextTest {

    @Test
    void Comment_멤버_컨텍스트_생성_성공() {
        // Given
        User momenter = createUser(1L);
        User commenter = createUser(2L);
        Group group = createGroup(momenter);
        GroupMember member = createMember(group, commenter);
        Moment moment = createMoment(momenter, group);

        // When
        Comment comment = new Comment(moment, commenter, member, "그룹 댓글 내용");

        // Then
        assertThat(comment.getMember()).isEqualTo(member);
        assertThat(comment.getContent()).isEqualTo("그룹 댓글 내용");
        assertThat(comment.getCommenter()).isEqualTo(commenter);
        assertThat(comment.getMomentId()).isEqualTo(moment.getId());
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

    private Moment createMoment(User momenter, Group group) {
        Moment moment = new Moment("테스트 내용", momenter);
        ReflectionTestUtils.setField(moment, "id", 1L);
        ReflectionTestUtils.setField(moment, "group", group);
        return moment;
    }
}
