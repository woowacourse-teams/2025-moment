package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthEmailServiceTest {

    private final String email = "ekorea623@gmail.com";
    private final String wrongCode = "111111";

    @InjectMocks
    private AuthEmailService authEmailService;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void 인증_메일_전송에_성공한다() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        EmailRequest request = new EmailRequest(email);

        // when
        authEmailService.sendVerificationEmail(request);

        // then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void 인증번호_검증에_성공한다() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        authEmailService.sendVerificationEmail(new EmailRequest(email));
        String code = getVerificationCode(email);
        EmailVerifyRequest request = new EmailVerifyRequest(email, code);

        // when & then
        assertThatCode(() -> authEmailService.verifyCode(request))
            .doesNotThrowAnyException();
    }

    @Test
    void 쿨다운이_지나지_않았으면_인증_메일_재전송시_예외가_발생한다() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        EmailRequest request = new EmailRequest(email);
        authEmailService.sendVerificationEmail(request);

        // when & then
        assertThatThrownBy(() -> authEmailService.sendVerificationEmail(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
    }

    @Test
    void 인증번호의_유효시간이_만료되면_예외가_발생한다() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        authEmailService.sendVerificationEmail(new EmailRequest(email));
        String code = getVerificationCode(email);
        EmailVerifyRequest request = new EmailVerifyRequest(email, code);

        // 만료 시간 조작
        AuthEmailService.VerificationInfo info = getVerificationInfo(email);
        ReflectionTestUtils.setField(info, "expiryTime", LocalDateTime.now().minusSeconds(1));

        // when & then
        assertThatThrownBy(() -> authEmailService.verifyCode(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_FAILED);
    }

    @Test
    void 인증번호가_일치하지_않으면_예외가_발생한다() {
        // given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        authEmailService.sendVerificationEmail(new EmailRequest(email));
        EmailVerifyRequest request = new EmailVerifyRequest(email, wrongCode);

        // when & then
        assertThatThrownBy(() -> authEmailService.verifyCode(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_FAILED);
    }

    private String getVerificationCode(String email) {
        return getVerificationInfo(email).getCode();
    }

    private AuthEmailService.VerificationInfo getVerificationInfo(String email) {
        try {
            java.lang.reflect.Field field = AuthEmailService.class.getDeclaredField("verificationInfos");
            field.setAccessible(true);
            java.util.Map<String, AuthEmailService.VerificationInfo> verificationInfos =
                (java.util.Map<String, AuthEmailService.VerificationInfo>) field.get(authEmailService);
            return verificationInfos.get(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}