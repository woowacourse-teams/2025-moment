package moment.auth.application;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.auth.domain.VerificationInfo;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEmailService implements EmailService{

    private static final long COOL_DOWN_SECONDS = 60;
    private static final long EXPIRY_SECONDS = 300;

    private final JavaMailSender mailSender;
    private final Map<String, VerificationInfo> verificationInfos = new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(EmailRequest request) {
        String email = request.email();
        VerificationInfo existingInfo = verificationInfos.get(email);

        if (existingInfo != null && existingInfo.isCoolTime(COOL_DOWN_SECONDS)) {
            throw new MomentException(ErrorCode.EMAIL_COOL_DOWN_NOT_PASSED);
        }

        String code = createVerificationCode();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Moment] 이메일 인증 코드 안내");
        message.setText("인증 코드는 " + code + " 입니다.");
        mailSender.send(message);

        verificationInfos.put(email, new VerificationInfo(code, LocalDateTime.now().plusSeconds(EXPIRY_SECONDS)));
    }

    @Override
    public void verifyCode(EmailVerifyRequest request) {
        String email = request.email();
        String code = request.code();

        VerificationInfo info = verificationInfos.get(email);

        if (info == null || info.isExpired() || !info.hasSameCode(code)) {
            throw new MomentException(ErrorCode.EMAIL_VERIFY_FAILED);
        }

        verificationInfos.remove(email);
    }

    private String createVerificationCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    @Scheduled(fixedRate = 3600000) // 1시간마다 실행
    public void cleanExpiredVerificationInfos() {
        verificationInfos.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("만료된 이메일 인증 정보 정리 완료. 남은 정보 수: {}", verificationInfos.size());
    }
}
