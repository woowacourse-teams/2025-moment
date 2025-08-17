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
import java.util.Map;
import moment.auth.domain.EmailVerification;
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
    private final long expirySeconds = 300L;

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
        String verificationCode = "123456";
        Map<String, EmailVerification> verificationInfos = getVerificationInfosMap();
        verificationInfos.put(email, new EmailVerification(verificationCode, LocalDateTime.now(), expirySeconds));

        EmailVerifyRequest verifyRequest = new EmailVerifyRequest(email, verificationCode);

        // when & then
        assertThatCode(() -> authEmailService.verifyCode(verifyRequest))
            .doesNotThrowAnyException();
    }

    @Test
    void 쿨다운이_지나지_않았으면_인증_메일_재전송시_예외가_발생한다() {
        // given
        EmailRequest request = new EmailRequest(email);
        Map<String, EmailVerification> verificationInfos = getVerificationInfosMap();
        verificationInfos.put(email, new EmailVerification("123456", LocalDateTime.now().minusSeconds(1), expirySeconds));

        // when & then
        assertThatThrownBy(() -> authEmailService.sendVerificationEmail(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
    }

    @Test
    void 인증번호의_유효시간이_만료되면_예외가_발생한다() {
        // given
        String verificationCode = "123456";
        Map<String, EmailVerification> verificationInfos = getVerificationInfosMap();
        LocalDateTime expiredTime = LocalDateTime.now().minusSeconds(expirySeconds + 1);
        verificationInfos.put(email, new EmailVerification(verificationCode, expiredTime, expirySeconds));

        EmailVerifyRequest request = new EmailVerifyRequest(email, verificationCode);

        // when & then
        assertThatThrownBy(() -> authEmailService.verifyCode(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFY_FAILED);
    }

    @Test
    void 인증번호가_일치하지_않으면_예외가_발생한다() {
        // given
        Map<String, EmailVerification> verificationInfos = getVerificationInfosMap();
        verificationInfos.put(email, new EmailVerification("123456", LocalDateTime.now(), expirySeconds));

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

        when(userQueryService.getUserByEmailAndProviderType(email, ProviderType.EMAIL)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        authEmailService.sendPasswordUpdateEmail(request);

        // then
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void 비밀번호_재설정_요청_쿨다운이_지나지_않았으면_예외가_발생한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User(email, "password", "nickname", ProviderType.EMAIL);
        Map<String, EmailVerification> passwordUpdateInfos = getPasswordUpdateInfosMap();
        passwordUpdateInfos.put(email, new EmailVerification("some-token", LocalDateTime.now().minusSeconds(1), expirySeconds));

        when(userQueryService.getUserByEmailAndProviderType(email, ProviderType.EMAIL)).thenReturn(user);

        // when & then
        assertThatThrownBy(() -> authEmailService.sendPasswordUpdateEmail(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
    }

    @Test
    void 비밀번호_재설정_메일_전송에_실패하면_예외가_발생한다() {
        // given
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);
        User user = new User(email, "password", "nickname", ProviderType.EMAIL);
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(userQueryService.getUserByEmailAndProviderType(email, ProviderType.EMAIL)).thenReturn(user);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> authEmailService.sendPasswordUpdateEmail(request))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_SEND_FAILURE);
    }

    @SuppressWarnings("unchecked")
    private Map<String, EmailVerification> getVerificationInfosMap() {
        return (Map<String, EmailVerification>) ReflectionTestUtils.getField(authEmailService, "verificationInfos");
    }

    @SuppressWarnings("unchecked")
    private Map<String, EmailVerification> getPasswordUpdateInfosMap() {
        return (Map<String, EmailVerification>) ReflectionTestUtils.getField(authEmailService, "passwordUpdateInfos");
    }
}

