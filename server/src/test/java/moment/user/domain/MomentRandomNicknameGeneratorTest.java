package moment.user.domain;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MomentRandomNicknameGeneratorTest {

    @Test
    void Moment에_어울리는_랜덤_닉네임을_생성한다() {
        // given
        NicknameGenerator nicknameGenerator = new MomentRandomNicknameGenerator();

        for (int i = 0; i < 100; i++) {
            String test = nicknameGenerator.generateNickname();
            System.out.println(test);
        }

        // when & then
        assertThatCode(nicknameGenerator::generateNickname).doesNotThrowAnyException();
    }
}
