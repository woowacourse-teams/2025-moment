package moment.user.application;

import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.NicknameGenerator;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NicknameGenerateServiceTest {

    @Mock
    UserQueryService userQueryService;
    @Mock
    NicknameGenerator nicknameGenerator;
    @InjectMocks
    private NicknameGenerateService nicknameGenerateService;

    @Test
    void 닉네임을_랜덤으로_생성한다() {
        // given
        String nickname = "깊은 물속의 하마";
        given(nicknameGenerator.generateNickname()).willReturn(nickname);
        given(userQueryService.existsByNickname(any(String.class))).willReturn(false);

        // when & then
        assertAll(
                () -> assertThat(nicknameGenerateService.createRandomNickname()).isEqualTo(nickname),
                () -> then(userQueryService).should(times(1)).existsByNickname(any(String.class)),
                () -> then(nicknameGenerator).should(times(1)).generateNickname()
        );
    }

    @Test
    void 랜덤으로_생성한_닉네임이_존재하는_경우_임계치_이후_예외가_발생합니다() {
        // given
        String nickname = "깊은 물속의 하마";
        given(nicknameGenerator.generateNickname()).willReturn(nickname);
        given(userQueryService.existsByNickname(any(String.class))).willReturn(true);

        // when & then
        assertThatThrownBy(() -> nicknameGenerateService.createRandomNickname())
                .isInstanceOf(MomentException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NICKNAME_GENERATION_FAILED);
    }

}