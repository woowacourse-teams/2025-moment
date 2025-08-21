package moment.moment.domain;

import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentTest {

    @ParameterizedTest
    @NullSource
    @EmptySource
    void 내용이_없는_경우_예외가_발생한다(String content) {
        // given
        User user = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);

        // when & then
        assertThatThrownBy(() -> new Moment(content, user, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("moment의 content는 null이거나 빈 값이어서는 안 됩니다.");
    }

    @Test
    void 모멘트_내용_길이가_200자가_넘는_경우_예외가_발생한다() {
        //given
        User user = new User("lebron@gmail.com", "1234", "르브론", ProviderType.EMAIL);
        String longContent = "=".repeat(201);

        // when & then
        assertThatThrownBy(() -> new Moment(longContent, user, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("모멘트는 1자 이상, 200자 이하로만 작성 가능합니다.");
    }

    @Test
    void 사용자가_없는_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> new Moment("굿", null, WriteType.BASIC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("momenter가 null이 되어서는 안 됩니다.");
    }

    @Test
    void 모멘트_작성자인지_확인한다() {
        // given
        User momenter = new User("drago@email.com", "1234", "drago", ProviderType.EMAIL);
        ReflectionTestUtils.setField(momenter, "id", 1L);

        User unAuthorizedUser = new User("unAuth@email.com", "1234", "unAuth", ProviderType.EMAIL);
        ReflectionTestUtils.setField(unAuthorizedUser, "id", 2L);

        Moment moment = new Moment("오늘 달리기 완료!", momenter, WriteType.BASIC);
        ReflectionTestUtils.setField(moment, "id", 1L);

        // when & then
        assertAll(
                () -> assertThat(moment.checkMomenter(momenter)).isTrue(),
                () -> assertThat(moment.checkMomenter(unAuthorizedUser)).isFalse()
        );
    }
}
