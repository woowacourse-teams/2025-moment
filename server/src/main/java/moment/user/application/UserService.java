package moment.user.application;

import lombok.RequiredArgsConstructor;
import moment.auth.application.TokensIssuer;
import moment.auth.domain.Tokens;
import moment.auth.dto.google.GoogleUserInfo;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.user.domain.PendingUser;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.dto.request.BasicUserCreateRequest;
import moment.user.dto.request.GoogleOAuthUserCreateRequest;
import moment.user.dto.request.NicknameConflictCheckRequest;
import moment.user.dto.response.MomentRandomNicknameResponse;
import moment.user.dto.response.NicknameConflictCheckResponse;
import moment.user.dto.response.PendingUserResponse;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserQueryService userQueryService;
    private final UserRepository userRepository;
    private final NicknameGenerateService nicknameGenerateService;
    private final PasswordEncoder passwordEncoder;
    private final TokensIssuer tokensIssuer;
    private final PendingUserCacheService pendingUserCacheService;

    @Transactional
    public void registerUser(BasicUserCreateRequest request) {
        comparePasswordWithRepassword(request.password(), request.rePassword());
        validateEmailInBasicSignUp(request.email());
        validateNickname(request.nickname());

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), encodedPassword, request.nickname(), ProviderType.EMAIL);

        userRepository.save(user);
    }

    @Transactional
    public Tokens registerAndLoginGoogleOAuthUser(GoogleOAuthUserCreateRequest request, String authorizationEmail) {
        String email = request.email();

        if (!email.equals(authorizationEmail)) {
            throw new MomentException(ErrorCode.AUTHORIZATION_INVALID);
        }

        validateEmailInBasicSignUp(email);
        validateNickname(request.nickname());

        PendingUser pendingUser = pendingUserCacheService.getPendingUser(email);

        String encodedPassword = passwordEncoder.encode(pendingUser.getPassword());

        User user = new User(email, encodedPassword, nicknameGenerateService.createRandomNickname(),
                ProviderType.GOOGLE);

        User registeredUser = userRepository.save(user);
        pendingUserCacheService.removePendingUser(email);
        return tokensIssuer.issueTokens(registeredUser);
    }

    private void comparePasswordWithRepassword(String password, String rePassword) {
        if (!password.equals(rePassword)) {
            throw new MomentException(ErrorCode.PASSWORD_MISMATCHED);
        }
    }

    private void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new MomentException(ErrorCode.USER_NICKNAME_CONFLICT);
        }
    }

    private void validateEmailInBasicSignUp(String email) {
        if (userRepository.existsByEmailAndProviderType(email, ProviderType.EMAIL)) {
            throw new MomentException(ErrorCode.USER_CONFLICT);
        }
    }

    public PendingUserResponse registerPendingUser(GoogleUserInfo googleUserInfo) {
        PendingUser pendingUser = pendingUserCacheService.register(googleUserInfo);
        return new PendingUserResponse(pendingUser.email());
    }

    public UserProfileResponse getUserProfile(Authentication authentication) {
        User user = userQueryService.getUserById(authentication.id());
        return UserProfileResponse.from(user);
    }

    public MomentRandomNicknameResponse createRandomNickname() {
        String randomNickname = nicknameGenerateService.createRandomNickname();
        return new MomentRandomNicknameResponse(randomNickname);
    }

    public NicknameConflictCheckResponse checkNicknameConflict(NicknameConflictCheckRequest request) {
        boolean existsByNickname = userRepository.existsByNickname(request.nickname());
        return new NicknameConflictCheckResponse(existsByNickname);
    }
}
