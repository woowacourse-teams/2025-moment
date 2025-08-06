package moment.auth.application;

import lombok.RequiredArgsConstructor;
import moment.auth.dto.request.LoginRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;

    public String login(LoginRequest request) {
        User user = userRepository.findByEmailAndProviderType(request.email(), ProviderType.EMAIL)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new MomentException(ErrorCode.USER_LOGIN_FAILED);
        }

        return tokenManager.createToken(user.getId(), user.getEmail());
    }

    public Authentication getAuthenticationByToken(String token) {
        return tokenManager.extractAuthentication(token);
    }
}
