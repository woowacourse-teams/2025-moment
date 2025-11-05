package moment.comment.domain;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.fixture.UserFixture;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.moment.domain.Moment;
import moment.moment.domain.WriteType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(ReplaceUnderscores.class)
class EchoTest {

    @Test
    void 코멘트가_없는_경우_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();

        // when & then
        assertThatThrownBy(() -> new Echo("HEART", user, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 에코_작성자가_없는_경우_예외가_발생한다() {
        // given
        User commenter = UserFixture.createUser();
        Comment comment = new Comment("오운완!", commenter, 1L);

        // when & then
        assertThatThrownBy(() -> new Echo("HEART", null, comment))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 에코_작성자를_확인한다() {
        // given
        User momenter = UserFixture.createUser();
        User commenter = UserFixture.createUser();
        Comment comment = new Comment("오운완!", commenter, 1L);
        Echo echo = new Echo("HEART", momenter, comment);

        // when & then
        assertThatCode(() -> echo.checkWriter(momenter))
                .doesNotThrowAnyException();
    }

    @Test
    void 에코_작성자가_아닌_경우_예외가_발생한다() {
        // given
        User momenter = UserFixture.createUser();
        Moment writer = new Moment("오운완!", false, momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(writer, "id", 1L);

        User commenter = UserFixture.createUser();
        ReflectionTestUtils.setField(commenter, "id", 2L);

        Comment comment = new Comment("오운완!", commenter, writer.getId());
        Echo echo = new Echo("HEART", momenter, comment);

        // when & then
        assertThatThrownBy(() -> echo.checkWriter(commenter))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }
}
