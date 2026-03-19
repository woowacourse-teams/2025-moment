package moment.moment.dto.response.tobe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import moment.fixture.MomentFixture;
import moment.fixture.UserFixture;
import moment.moment.domain.Moment;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentCompositionTest {

    @Test
    void momenter가_null인_경우_탈퇴한_사용자로_표시된다() {
        // given
        User momenter = UserFixture.createUserWithId(1L);
        Moment moment = MomentFixture.createMomentWithId(1L, momenter);
        setMomenterNull(moment);

        // when
        MomentComposition composition = MomentComposition.of(moment, "imageUrl");

        // then
        assertAll(
                () -> assertThat(composition.momenterId()).isNull(),
                () -> assertThat(composition.nickname()).isEqualTo("탈퇴한 사용자")
        );
    }

    @Test
    void momenter가_존재하는_경우_닉네임이_정상_표시된다() {
        // given
        User momenter = UserFixture.createUserWithId(1L);
        Moment moment = MomentFixture.createMomentWithId(1L, momenter);

        // when
        MomentComposition composition = MomentComposition.of(moment, "imageUrl");

        // then
        assertAll(
                () -> assertThat(composition.momenterId()).isEqualTo(1L),
                () -> assertThat(composition.nickname()).isEqualTo(momenter.getNickname())
        );
    }

    private void setMomenterNull(Moment moment) {
        try {
            java.lang.reflect.Field momenterField = Moment.class.getDeclaredField("momenter");
            momenterField.setAccessible(true);
            momenterField.set(moment, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set momenter to null", e);
        }
    }
}
