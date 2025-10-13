package moment.user.service.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import moment.global.exception.MomentException;
import moment.user.domain.NicknameGenerator;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@DisplayNameGeneration(ReplaceUnderscores.class)
class NicknameGenerateApplicationServiceTest {
    private static final String RANDOM_NICKNAME = "테스트 닉네임";

    @Autowired
    private NicknameGenerateApplicationService nicknameGenerateApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 랜덤_닉네임을_생성한다() {
        MomentRandomNicknameResponse randomNickname = nicknameGenerateApplicationService.createRandomNickname();
        assertThat(randomNickname.randomNickname()).isEqualTo(RANDOM_NICKNAME);
    }

    @Test
    void 랜덤_닉네임_생성_시_이미_존재하는_닉네임인_경우_예외가_발생한다() {
        // given
        String password = "1234qwer!@";
        User user = new User("test@email.com", password, RANDOM_NICKNAME, ProviderType.EMAIL);
        userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> nicknameGenerateApplicationService.createRandomNickname())
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("message", "사용 가능한 닉네임을 생성할 수 없습니다.");
    }

    @TestConfiguration
    static class NicknameGeneratorTestConfiguration {

        @Bean
        @Primary
        public NicknameGenerator nicknameGenerator() {
            return () -> RANDOM_NICKNAME;
        }

        @Bean("momentRandomNicknameGenerator")
        public NicknameGenerator momentRandomNicknameGenerator() {
            return nicknameGenerator();
        }

        @Bean("alphanumericNicknameGenerator")
        public NicknameGenerator alphanumericNicknameGenerator() {
            return nicknameGenerator();
        }
    }
}
