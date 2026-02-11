package moment.like.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.moment.domain.Moment;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MomentLikeTest {

    @Test
    void MomentLike_생성_성공() {
        // Given
        Moment moment = createMoment();
        GroupMember member = createMember();

        // When
        MomentLike like = new MomentLike(moment, member);

        // Then
        assertThat(like.getMoment()).isEqualTo(moment);
        assertThat(like.getMember()).isEqualTo(member);
        assertThat(like.isDeleted()).isFalse();
    }

    @Test
    void MomentLike_토글_취소() {
        // Given
        MomentLike like = new MomentLike(createMoment(), createMember());
        assertThat(like.isDeleted()).isFalse();

        // When
        like.toggleDeleted();

        // Then
        assertThat(like.isDeleted()).isTrue();
    }

    @Test
    void MomentLike_토글_복구() {
        // Given
        MomentLike like = new MomentLike(createMoment(), createMember());
        ReflectionTestUtils.setField(like, "deletedAt", LocalDateTime.now());
        assertThat(like.isDeleted()).isTrue();

        // When
        like.toggleDeleted();

        // Then
        assertThat(like.isDeleted()).isFalse();
    }

    @Test
    void MomentLike_restore_성공() {
        // Given
        MomentLike like = new MomentLike(createMoment(), createMember());
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

    private Moment createMoment() {
        User momenter = createUser(2L);
        Moment moment = new Moment("테스트 내용", momenter);
        ReflectionTestUtils.setField(moment, "id", 1L);
        return moment;
    }
}
