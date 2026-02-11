package moment.moment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.test.util.ReflectionTestUtils;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentTest {

    @ParameterizedTest
    @NullSource
    @EmptySource
    void 내용이_없는_경우_예외가_발생한다(String content) {
        // given
        User user = UserFixture.createUser();

        // when & then
        assertThatThrownBy(() -> new Moment(content, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("moment의 content는 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void 모멘트_내용_길이가_200자가_넘는_경우_예외가_발생한다() {
        //given
        User user = UserFixture.createUser();
        String longContent = "=".repeat(201);

        // when & then
        assertThatThrownBy(() -> new Moment(longContent, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모멘트는 1자 이상, 200자 이하로만 작성 가능합니다.");
    }

    @Test
    void 사용자가_없는_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new Moment("굿", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("momenter가 null이 되어서는 안 됩니다.");
    }

    @Test
    void 모멘트_작성자인지_확인한다() {
        // given
        User momenter = UserFixture.createUser();
        ReflectionTestUtils.setField(momenter, "id", 1L);

        User unAuthorizedUser = UserFixture.createUser();
        ReflectionTestUtils.setField(unAuthorizedUser, "id", 2L);

        Moment moment = new Moment("오늘 달리기 완료!", momenter);
        ReflectionTestUtils.setField(moment, "id", 1L);

        // when & then
        assertAll(
                () -> assertThat(moment.isNotSame(momenter)).isFalse(),
                () -> assertThat(moment.isNotSame(unAuthorizedUser)).isTrue()
        );
    }
}
