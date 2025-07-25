package moment.moment.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentTest {

    @ParameterizedTest
    @NullSource
    @EmptySource
    void 내용이_잆는_경우_예외가_발생한다(String content) {
        // given
        User user = new User("lebron@gmail.com", "1234", "르브론");

        // when & then
        assertThatThrownBy(() -> new Moment(content, user))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MOMENT_CONTENT_EMPTY);
    }

    @Test
    void 사용자가_없는_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new Moment("굿", null))
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }
}
