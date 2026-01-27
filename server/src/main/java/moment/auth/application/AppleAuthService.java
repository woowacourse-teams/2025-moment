package moment.auth.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.Tokens;
import moment.auth.dto.apple.AppleUserInfo;
import moment.auth.infrastructure.AppleAuthClient;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.application.NicknameGenerateApplicationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppleAuthClient appleAuthClient;
    private final NicknameGenerateApplicationService nicknameGenerateApplicationService;
    private final TokensIssuer tokensIssuer;

    /**
     * Apple 로그인 또는 회원가입 처리
     *
     * @param identityToken Apple에서 발급한 Identity Token (JWT)
     * @return 액세스 토큰과 리프레시 토큰
     */
    @Transactional
    public Tokens loginOrSignUp(String identityToken) {
        // 1. Identity Token 검증 및 사용자 정보 추출
        AppleUserInfo appleUserInfo = appleAuthClient.verifyAndGetUserInfo(identityToken);

        // 2. sub 기반 고유 이메일 생성
        String appleEmail = appleUserInfo.toAppleEmail();

        // 3. 기존 사용자 조회
        Optional<User> findUser = userRepository.findByEmailAndProviderType(
                appleEmail,
                ProviderType.APPLE
        );

        // 4. 기존 사용자면 토큰 발급
        if (findUser.isPresent()) {
            return tokensIssuer.issueTokens(findUser.get());
        }

        // 5. 신규 사용자 회원가입 후 토큰 발급
        User savedUser = createUser(appleUserInfo.sub(), appleEmail);
        return tokensIssuer.issueTokens(savedUser);
    }

    /**
     * 신규 Apple 사용자 생성
     */
    private User createUser(String appleUserId, String appleEmail) {
        // sub를 비밀번호로 인코딩 (OAuth 사용자는 비밀번호 로그인 불가)
        String encodedPassword = passwordEncoder.encode(appleUserId);

        User user = new User(
                appleEmail,  // {sub}@apple.user 형태
                encodedPassword,
                nicknameGenerateApplicationService.generate(),
                ProviderType.APPLE
        );

        return userRepository.save(user);
    }
}
