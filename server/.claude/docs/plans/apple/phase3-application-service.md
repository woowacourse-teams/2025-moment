# Phase 3: Application Service 구현 (AppleAuthService)

## 목표
Apple 로그인/회원가입 비즈니스 로직을 담당하는 AppleAuthService를 구현합니다.

---

## 1. 의존성 확인

### 필요 컴포넌트 (기존 코드 참조)
- `UserRepository`: 사용자 조회/저장
- `PasswordEncoder`: 비밀번호 인코딩
- `AppleAuthClient`: Apple JWT 검증 (Phase 2)
- `NicknameGenerateApplicationService`: 닉네임 생성
- `TokensIssuer`: JWT 토큰 발급

### GoogleAuthService 참조
기존 `GoogleAuthService` 패턴을 따라 구현

---

## 2. AppleAuthService 구현

### 파일 위치
`src/main/java/moment/auth/application/AppleAuthService.java`

### 구현
```java
package moment.auth.application;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.Tokens;
import moment.auth.domain.TokensIssuer;
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
```

---

## 3. 테스트

### 파일 위치
`src/test/java/moment/auth/application/AppleAuthServiceTest.java`

### 테스트 케이스

```java
package moment.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.domain.TokensIssuer;
import moment.auth.dto.apple.AppleUserInfo;
import moment.auth.infrastructure.AppleAuthClient;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import moment.user.service.application.NicknameGenerateApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AppleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppleAuthClient appleAuthClient;

    @Mock
    private NicknameGenerateApplicationService nicknameGenerateApplicationService;

    @Mock
    private TokensIssuer tokensIssuer;

    @InjectMocks
    private AppleAuthService appleAuthService;

    @Nested
    @DisplayName("loginOrSignUp")
    class LoginOrSignUpTest {

        @Test
        @DisplayName("기존 사용자면 토큰만 발급한다")
        void existingUser_returnsTokens() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User existingUser = mock(User.class);
            Tokens expectedTokens = new Tokens("access-token", new RefreshToken("refresh-token"));

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("신규 사용자면 회원가입 후 토큰을 발급한다")
        void newUser_createsUserAndReturnsTokens() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";
            String nickname = "행복한별123";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User savedUser = mock(User.class);
            Tokens expectedTokens = new Tokens("access-token", new RefreshToken("refresh-token"));

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn("encoded-password");
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(tokensIssuer.issueTokens(savedUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            assertThat(result.getAccessToken()).isEqualTo("access-token");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("신규 사용자 생성 시 올바른 정보로 User를 생성한다")
        void newUser_createsUserWithCorrectInfo() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";
            String nickname = "행복한별123";
            String encodedPassword = "encoded-password";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            Tokens expectedTokens = new Tokens("access-token", new RefreshToken("refresh-token"));

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                .thenReturn(Optional.empty());
            when(passwordEncoder.encode(appleUserId)).thenReturn(encodedPassword);
            when(nicknameGenerateApplicationService.generate()).thenReturn(nickname);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(tokensIssuer.issueTokens(any(User.class))).thenReturn(expectedTokens);

            // when
            appleAuthService.loginOrSignUp(identityToken);

            // then
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getEmail()).isEqualTo(appleEmail);
            assertThat(capturedUser.getNickname()).isEqualTo(nickname);
            assertThat(capturedUser.getProviderType()).isEqualTo(ProviderType.APPLE);
        }

        @Test
        @DisplayName("동일 sub로 재로그인하면 같은 사용자로 인식한다")
        void sameSubReturnsExistingUser() {
            // given
            String identityToken = "valid.identity.token";
            String appleUserId = "001234.abcd1234.0123";
            String appleEmail = appleUserId + "@apple.user";

            AppleUserInfo userInfo = new AppleUserInfo(appleUserId);
            User existingUser = mock(User.class);
            Tokens expectedTokens = new Tokens("access-token", new RefreshToken("refresh-token"));

            when(appleAuthClient.verifyAndGetUserInfo(identityToken)).thenReturn(userInfo);
            when(userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE))
                .thenReturn(Optional.of(existingUser));
            when(tokensIssuer.issueTokens(existingUser)).thenReturn(expectedTokens);

            // when
            Tokens result = appleAuthService.loginOrSignUp(identityToken);

            // then
            verify(userRepository).findByEmailAndProviderType(appleEmail, ProviderType.APPLE);
            verify(tokensIssuer).issueTokens(existingUser);
        }

        @Test
        @DisplayName("AppleAuthClient에서 예외 발생 시 그대로 전파된다")
        void propagatesExceptionFromAppleAuthClient() {
            // given
            String identityToken = "invalid.token";
            when(appleAuthClient.verifyAndGetUserInfo(identityToken))
                .thenThrow(new RuntimeException("Token validation failed"));

            // when & then
            org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> appleAuthService.loginOrSignUp(identityToken)
            );
        }
    }
}
```

---

## 4. GoogleAuthService 참조

### 파일 위치 확인
`src/main/java/moment/auth/application/GoogleAuthService.java`

### 참조할 패턴
- 로그인/회원가입 분기 로직
- User 생성 로직
- TokensIssuer 사용 방식

---

## 구현 순서 (TDD)

### Step 1: GoogleAuthService 패턴 확인
1. `GoogleAuthService.java` 읽기
2. 사용된 의존성 및 패턴 파악

### Step 2: AppleAuthService 테스트 작성
1. `AppleAuthServiceTest.java` 작성
2. 테스트 실행 → 실패 확인 (Red)

### Step 3: AppleAuthService 구현
1. `AppleAuthService.java` 구현
2. 테스트 통과 확인 (Green)

### Step 4: 통합 확인
1. Phase 2의 AppleAuthClient와 연동 확인
2. 전체 테스트 실행

---

## 체크리스트

- [ ] GoogleAuthService 패턴 확인
- [ ] `AppleAuthServiceTest` 작성
  - [ ] 기존 사용자 로그인 테스트
  - [ ] 신규 사용자 회원가입 테스트
  - [ ] 올바른 User 정보 생성 테스트
  - [ ] 동일 sub 재로그인 테스트
  - [ ] 예외 전파 테스트
- [ ] `AppleAuthService` 구현
- [ ] AppleAuthClient 연동 확인
- [ ] `./gradlew fastTest` 전체 통과