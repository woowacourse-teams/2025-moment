package moment.user.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import moment.config.TestTags;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(TestTags.UNIT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MomentRandomNicknameGeneratorTest {

    @Test
    void Moment에_어울리는_랜덤_닉네임을_생성한다() {
        // given
        NicknameGenerator nicknameGenerator = new MomentRandomNicknameGenerator();

        // when & then
        assertThatCode(nicknameGenerator::generateNickname).doesNotThrowAnyException();
    }
}
