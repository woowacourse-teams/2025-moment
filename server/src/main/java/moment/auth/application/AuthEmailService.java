package moment.auth.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.auth.domain.EmailVerification;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.PasswordResetRequest;
import moment.auth.dto.request.PasswordUpdateRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.application.UserQueryService;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEmailService implements EmailService{

    private static final long COOL_DOWN_SECONDS = 60;
    private static final long EXPIRY_SECONDS = 300;

    private final UserQueryService userQueryService;
    private final JavaMailSender mailSender;
    private final Map<String, EmailVerification> verificationInfos = new ConcurrentHashMap<>();
    private final Map<String, EmailVerification> passwordUpdateInfos = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(EmailRequest request) {
        String email = request.email();
        EmailVerification existingInfo = verificationInfos.get(email);

        validateCoolTimePassed(existingInfo);

        String code = createVerificationCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Moment] 이메일 인증 코드 안내");
        message.setText("인증 코드는 " + code + " 입니다.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("인증 이메일 전송 실패: ", e);
        }

        verificationInfos.put(email, new EmailVerification(code, LocalDateTime.now(), EXPIRY_SECONDS));
    }

    @Override
    public void verifyCode(EmailVerifyRequest request) {
        String email = request.email();
        String code = request.code();

        EmailVerification info = verificationInfos.get(email);

        if (info == null || info.isExpired() || !info.hasSameCode(code)) {
            throw new MomentException(ErrorCode.EMAIL_VERIFY_FAILED);
        }

        verificationInfos.remove(email);
    }

    private String createVerificationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    @Scheduled(fixedRate = 3600_000) // 1시간마다 실행
    public void cleanExpiredVerificationInfos() {
        verificationInfos.entrySet().removeIf(entry -> entry.getValue().isExpired());
        passwordUpdateInfos.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("만료된 이메일 인증 정보 정리 완료. 남은 정보 수: {}", verificationInfos.size());
        log.info("만료된 비밀번호 변경 정보 정리 완료. 남은 정보 수: {}", passwordUpdateInfos.size());
    }

    @Override
    public void sendPasswordUpdateEmail(PasswordUpdateRequest request) {
        String email = request.email();
        Optional<User> findUser = userQueryService.findUserByEmailAndProviderType(email, ProviderType.EMAIL);

        if (findUser.isPresent()) {
            EmailVerification existingInfo = passwordUpdateInfos.get(email);
            validateCoolTimePassed(existingInfo);

            String token = UUID.randomUUID().toString();

            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                sendUpdateMail(helper, email, token);
            } catch (MessagingException | MailException e) {
                log.error("비밀번호 재설정 이메일 전송 실패: ", e);
                throw new MomentException(ErrorCode.EMAIL_SEND_FAILURE);
            }

            passwordUpdateInfos.put(email, new EmailVerification(token, LocalDateTime.now(), EXPIRY_SECONDS));
        }
    }

    private void sendUpdateMail(MimeMessageHelper helper, String email, String token)
            throws MessagingException {
        helper.setTo(email);
        helper.setSubject("[Moment] 비밀번호 재설정 안내");
        String htmlContent = "<p>비밀번호를 재설정하려면 다음 링크를 클릭하세요:</p>"
                + "<a href=\"https://connectingmoment.com/passwordUpdate?token=" + token + "\">비밀번호 재설정하기</a>";
        helper.setText(htmlContent, true);

        mailSender.send(helper.getMimeMessage());
    }

    private void validateCoolTimePassed(EmailVerification existingInfo) {
        if (existingInfo != null && existingInfo.isCoolTime(COOL_DOWN_SECONDS)) {
            throw new MomentException(ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
        }
    }

    @Override
    public void verifyPasswordResetToken(PasswordResetRequest request) {
        EmailVerification info = passwordUpdateInfos.get(request.email());

        if (info == null || info.isExpired() || !info.hasSameCode(request.token())) {
            throw new MomentException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }

        passwordUpdateInfos.remove(request.email());
    }
}

