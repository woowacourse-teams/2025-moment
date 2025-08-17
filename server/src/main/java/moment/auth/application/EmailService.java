package moment.auth.application;

import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.PasswordUpdateRequest;

public interface EmailService {

    void sendVerificationEmail(EmailRequest request);

    void verifyCode(EmailVerifyRequest request);

    void sendPasswordUpdateEmail(PasswordUpdateRequest request, Long id);
}
