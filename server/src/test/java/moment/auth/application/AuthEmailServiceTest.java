package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.PasswordUpdateRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthEmailServiceTest {

    private final String email = "ekorea623@gmail.com";
    private final String wrongCode = "111111";
    private final Long userId = 1L;

    @InjectMocks
    private AuthEmailService authEmailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserQueryService userQueryService;

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
        Object verificationInfo = getVerificationInfo(email);
        ReflectionTestUtils.setField(verificationInfo, "expiryTime", LocalDateTime.now().minusSeconds(1));

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

    @Test
    void 비밀번호_재설정_메일_전송에_성공한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User(email, "1q2w3e4r!", "drago", ProviderType.EMAIL);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(userQueryService.getUserById(userId)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        authEmailService.sendPasswordUpdateEmail(request, userId);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void 비밀번호_재설정_요청_시_사용자_이메일과_일치하지_않으면_예외가_발생한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User("other-email@gmail.com", "password", "nickname", ProviderType.GOOGLE);

        when(userQueryService.getUserById(userId)).thenReturn(user);

        // when & then
        assertThatThrownBy(() -> authEmailService.sendPasswordUpdateEmail(request, userId))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_UNAUTHORIZED);
    }

    @Test
    void 비밀번호_재설정_요청_쿨다운이_지나지_않았으면_예외가_발생한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User(email, "password", "nickname", ProviderType.GOOGLE);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(userQueryService.getUserById(userId)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        authEmailService.sendPasswordUpdateEmail(request, userId);

        // when & then
        assertThatThrownBy(() -> authEmailService.sendPasswordUpdateEmail(request, userId))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
    }

    @Test
    void 비밀번호_재설정_메일_전송에_실패하면_예외가_발생한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User(email, "password", "nickname", ProviderType.GOOGLE);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(userQueryService.getUserById(userId)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> authEmailService.sendPasswordUpdateEmail(request, userId))
            .isInstanceOf(MailSendException.class);
    }

    private String getVerificationCode(String email) {
        Object verificationInfo = getVerificationInfo(email);
        return (String) ReflectionTestUtils.getField(verificationInfo, "code");
    }

    private Object getVerificationInfo(String email) {
        try {
            java.lang.reflect.Field field = AuthEmailService.class.getDeclaredField("verificationInfos");
            field.setAccessible(true);
            java.util.Map<String, Object> verificationInfos =
                    (java.util.Map<String, Object>) field.get(authEmailService);
            return verificationInfos.get(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

