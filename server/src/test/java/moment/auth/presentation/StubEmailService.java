package moment.auth.presentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import moment.auth.application.EmailService;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("test")
@Primary
public class StubEmailService implements EmailService {

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void sendVerificationEmail(EmailRequest request) {
        String code = "123456"; // 테스트용 고정 인증 코드
        verificationCodes.put(request.email(), code);
    }

    @Override
    public void verifyCode(EmailVerifyRequest request) {
        String storedCode = verificationCodes.get(request.email());
        boolean isValid = storedCode != null && storedCode.equals(request.code());

        if (!isValid) {
            throw new MomentException(ErrorCode.EMAIL_VERIFY_FAILED);
        }

        verificationCodes.remove(request.email());
    }
}
