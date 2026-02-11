package moment.like.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import moment.comment.domain.Comment;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CommentLikeTest {

    @Test
    void CommentLike_생성_성공() {
        // Given
        Comment comment = createComment();
        GroupMember member = createMember();

        // When
        CommentLike like = new CommentLike(comment, member);

        // Then
        assertThat(like.getComment()).isEqualTo(comment);
        assertThat(like.getMember()).isEqualTo(member);
        assertThat(like.isDeleted()).isFalse();
    }

    @Test
    void CommentLike_토글_취소() {
        // Given
        CommentLike like = new CommentLike(createComment(), createMember());
        assertThat(like.isDeleted()).isFalse();

        // When
        like.toggleDeleted();

        // Then
        assertThat(like.isDeleted()).isTrue();
    }

    @Test
    void CommentLike_토글_복구() {
        // Given
        CommentLike like = new CommentLike(createComment(), createMember());
        ReflectionTestUtils.setField(like, "deletedAt", LocalDateTime.now());
        assertThat(like.isDeleted()).isTrue();

        // When
        like.toggleDeleted();

        // Then
        assertThat(like.isDeleted()).isFalse();
    }

    @Test
    void CommentLike_restore_성공() {
        // Given
        CommentLike like = new CommentLike(createComment(), createMember());
        ReflectionTestUtils.setField(like, "deletedAt", LocalDateTime.now());

        // When
        like.restore();

        // Then
        assertThat(like.isDeleted()).isFalse();
        assertThat(like.getDeletedAt()).isNull();
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

    private GroupMember createMember() {
        return GroupMember.createOwner(createGroup(), createUser(1L), "닉네임");
    }

    private Comment createComment() {
        User commenter = createUser(2L);
        Comment comment = new Comment("테스트 댓글", commenter, 1L);
        ReflectionTestUtils.setField(comment, "id", 1L);
        return comment;
    }
}
