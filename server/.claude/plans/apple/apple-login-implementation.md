# Apple 로그인 백엔드 구현 계획 (v2)

## 개요

Expo 앱에서 전달받은 Apple `identityToken`(JWT)을 검증하고 회원가입/로그인을 처리하는 백엔드 API를 구현합니다.

---

## 1. 핵심 설계 결정

### 1.1 사용자 식별 전략
- **식별자**: `{apple_sub}@apple.user` 형태로 email 필드에 저장
- **이유**: Apple sub는 사용자마다 고유하므로 UNIQUE 제약 조건 충족
- **예시**: `001234.abcd1234efgh5678.0123@apple.user`

### 1.2 조회/생성 일관성 (Blocker 해결)
```java
// 조회와 생성 모두 동일한 이메일 형식 사용
String appleEmail = appleUserId + "@apple.user";

// 조회
userRepository.findByEmailAndProviderType(appleEmail, ProviderType.APPLE);

// 생성
new User(appleEmail, encodedPassword, nickname, ProviderType.APPLE);
```

### 1.3 계정 연동 정책
- **연동 미지원**: 동일 이메일이어도 Provider별로 별도 계정
- **이유**: 계정 탈취 위험 방지

### 1.4 실제 이메일 처리
- **저장하지 않음**: Apple이 제공하는 실제 이메일은 사용하지 않음
- **이유**: 최초 로그인 후 미제공 가능, Private Relay 이메일 문제

---

## 2. 수정/추가 파일 목록

### 신규 파일 (6개)

| 파일 | 설명 |
|------|------|
| `auth/dto/apple/ApplePublicKey.java` | Apple JWKS 개별 키 DTO |
| `auth/dto/apple/ApplePublicKeys.java` | Apple JWKS 응답 DTO |
| `auth/dto/apple/AppleUserInfo.java` | 검증된 토큰에서 추출한 사용자 정보 |
| `auth/dto/request/AppleLoginRequest.java` | Apple 로그인 요청 DTO |
| `auth/infrastructure/AppleAuthClient.java` | Apple JWKS 조회 및 JWT 검증 |
| `auth/application/AppleAuthService.java` | Apple 인증 비즈니스 로직 |

### 수정 파일 (4개)

| 파일 | 수정 내용 |
|------|----------|
| `user/domain/ProviderType.java` | `APPLE` enum 추가 |
| `auth/presentation/AuthController.java` | `POST /api/v2/auth/apple` 엔드포인트 추가 |
| `global/exception/ErrorCode.java` | Apple 에러 코드 추가 (AP-001 ~ AP-005) |
| `application-dev.yml`, `application-prod.yml` | `auth.apple.client-ids` 설정 추가 |

---

## 3. 구현 상세

### 3.1 ProviderType.java
```java
public enum ProviderType {
    EMAIL,
    GOOGLE,
    APPLE,  // 추가
}
```

### 3.2 ErrorCode.java (추가)
```java
// Apple Auth (AP)
APPLE_TOKEN_INVALID("AP-001", "유효하지 않은 Apple 토큰입니다.", HttpStatus.UNAUTHORIZED),
APPLE_TOKEN_EXPIRED("AP-002", "만료된 Apple 토큰입니다.", HttpStatus.UNAUTHORIZED),
APPLE_PUBLIC_KEY_NOT_FOUND("AP-003", "Apple 공개키를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
APPLE_PUBLIC_KEY_GENERATION_FAILED("AP-004", "Apple 공개키 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
APPLE_AUTH_SERVER_ERROR("AP-005", "Apple 인증 서버 오류입니다.", HttpStatus.SERVICE_UNAVAILABLE),
```

### 3.3 AppleAuthClient.java (핵심 검증 로직)

```
검증 플로우:
1. identityToken의 JWT 헤더에서 kid, alg 추출
2. alg가 RS256이 아니면 즉시 거절 (보안)
3. Apple JWKS 조회 (https://appleid.apple.com/auth/keys)
   - 캐싱 적용 (5분)
   - kid 미일치 시 캐시 무효화 후 재조회
4. kid로 매칭되는 공개키 선택
5. RSA PublicKey 생성 (n, e 값으로)
6. JWT 서명 검증
7. Claims 검증:
   - iss == "https://appleid.apple.com"
   - aud가 허용 리스트(client-ids)에 포함
   - exp 검증 (clock skew 30초 허용)
8. 사용자 정보 반환 (sub만 사용)
```

#### 캐싱 전략
```java
@Cacheable(value = "applePublicKeys", unless = "#result == null")
public ApplePublicKeys getApplePublicKeys() {
    return restTemplate.getForObject(APPLE_JWKS_URL, ApplePublicKeys.class);
}

@CacheEvict(value = "applePublicKeys", allEntries = true)
public void evictApplePublicKeysCache() {
    // kid 미일치 시 호출
}
```

#### 보안 검증 코드
```java
// 1. 알고리즘 검증 - RS256만 허용
if (!"RS256".equals(alg)) {
    throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
}

// 2. aud 허용 리스트 검증
if (!allowedClientIds.contains(audience)) {
    throw new MomentException(ErrorCode.APPLE_TOKEN_INVALID);
}

// 3. Clock skew 허용 (30초)
Jwts.parser()
    .clockSkewSeconds(30)
    .verifyWith(publicKey)
    .build()
    .parseSignedClaims(token);
```

### 3.4 AppleAuthService.java (로그인/회원가입 로직)

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppleAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppleAuthClient appleAuthClient;
    private final NicknameGenerateApplicationService nicknameGenerateApplicationService;
    private final TokensIssuer tokensIssuer;

    @Transactional
    public Tokens loginOrSignUp(String identityToken) {
        // 1. identityToken 검증 및 sub 추출
        AppleUserInfo appleUserInfo = appleAuthClient.verifyAndGetUserInfo(identityToken);
        String appleUserId = appleUserInfo.getSub();

        // 2. sub 기반 고유 이메일 생성 (조회/생성 일관성)
        String appleEmail = appleUserId + "@apple.user";

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
        User savedUser = addUser(appleUserId, appleEmail);
        return tokensIssuer.issueTokens(savedUser);
    }

    private User addUser(String appleUserId, String appleEmail) {
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

### 3.5 AuthController.java (엔드포인트 추가)

```java
@Operation(summary = "Apple 로그인", description = "Apple Identity Token을 검증하고 로그인/회원가입을 처리합니다.")
@PostMapping("/apple")
public ResponseEntity<SuccessResponse<Void>> appleLogin(
    @Valid @RequestBody AppleLoginRequest request
) {
    Tokens tokens = appleAuthService.loginOrSignUp(request.identityToken());

    // 쿠키 설정 (기존 Google 로그인과 동일)
    ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_HEADER, tokens.getAccessToken())
        .sameSite("none")
        .secure(true)
        .httpOnly(true)
        .path("/")
        .maxAge(ACCESS_TOKEN_TIME)
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_HEADER, tokens.getRefreshToken().getTokenValue())
        .sameSite("none")
        .secure(true)
        .httpOnly(true)
        .path("/")
        .maxAge(REFRESH_TOKEN_TIME)
        .build();

    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status)
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .body(SuccessResponse.of(status, null));
}
```

---

## 4. 환경 변수 설정

### application-dev.yml
```yaml
auth:
  google:
    # ... 기존 설정 유지
  apple:
    client-ids: ${APPLE_CLIENT_IDS}  # 쉼표로 구분된 허용 리스트
    # 예: com.moment.app,com.moment.app.service
```

| 환경 변수 | 설명 | 예시 |
|----------|------|------|
| `APPLE_CLIENT_IDS` | 허용된 Apple Client ID 목록 | `com.moment.app,com.moment.app.dev` |

---

## 5. Apple vs Google 비교

| 항목 | Google OAuth | Apple Sign In |
|------|-------------|---------------|
| 사용자 식별 | email + GOOGLE | {sub}@apple.user + APPLE |
| 이메일 저장 | 실제 이메일 | sub 기반 더미 이메일 |
| 토큰 검증 | Access Token으로 UserInfo API 호출 | Identity Token 직접 검증 (JWKS) |
| Client Secret | 필요 | **불필요** (네이티브 앱 id_token 검증만 해당) |

> **주의**: 향후 웹 기반 authorization code 플로우 확장 시에는 Client Secret과 token endpoint 교환이 필요합니다.

---

## 6. 에러 응답

| 코드 | 메시지 | HTTP Status | 발생 상황 |
|------|--------|-------------|----------|
| AP-001 | 유효하지 않은 Apple 토큰입니다. | 401 | JWT 형식 오류, 서명 검증 실패, Claims 검증 실패, 잘못된 알고리즘 |
| AP-002 | 만료된 Apple 토큰입니다. | 401 | JWT exp 만료 |
| AP-003 | Apple 공개키를 찾을 수 없습니다. | 500 | JWKS에서 kid 매칭 실패 (재조회 후에도) |
| AP-004 | Apple 공개키 생성에 실패했습니다. | 500 | RSA PublicKey 생성 오류 |
| AP-005 | Apple 인증 서버 오류입니다. | 503 | Apple JWKS 조회 실패 |

---

## 7. 구현 순서 (TDD 기반)

### Phase 1: 도메인 및 DTO
- [ ] `ProviderType.APPLE` 추가
- [ ] `ErrorCode`에 Apple 에러 코드 추가
- [ ] DTO 클래스 생성 (AppleLoginRequest, ApplePublicKey, ApplePublicKeys, AppleUserInfo)

### Phase 2: Infrastructure
- [ ] `AppleAuthClient` 테스트 작성
- [ ] `AppleAuthClient` 구현 (JWKS 조회, 캐싱, JWT 검증)

### Phase 3: Application Service
- [ ] `AppleAuthService` 테스트 작성
- [ ] `AppleAuthService` 구현

### Phase 4: Presentation
- [ ] `AuthController` Apple 엔드포인트 테스트 작성
- [ ] `AuthController`에 `/api/v2/auth/apple` 추가

### Phase 5: Configuration
- [ ] `application-dev.yml`, `application-prod.yml` 수정
- [ ] 캐시 설정 추가 (applePublicKeys)

---

## 8. 검증 방법

### 단위 테스트
```bash
cd server && ./gradlew fastTest
```

### E2E 테스트 (curl)
```bash
curl -X POST https://dev.connectingmoment.com/api/v2/auth/apple \
  -H "Content-Type: application/json" \
  -d '{"identityToken": "eyJhbGciOiJSUzI1NiIs..."}'
```

### 확인 사항
- [ ] 유효한 identityToken으로 로그인 성공 (쿠키에 accessToken, refreshToken 설정)
- [ ] RS256 외 알고리즘 사용 시 AP-001 에러
- [ ] 잘못된 aud 사용 시 AP-001 에러
- [ ] 만료된 토큰 사용 시 AP-002 에러
- [ ] 신규 사용자: 회원가입 후 토큰 발급
- [ ] 기존 사용자: 바로 토큰 발급
- [ ] 동일 sub로 재로그인 시 같은 사용자로 인식

---

## 9. 보안 체크리스트

- [x] **알고리즘 제한**: RS256만 허용, 다른 알고리즘 즉시 거절
- [x] **aud 검증**: 허용 리스트 기반 검증 (환경별 다중 지원)
- [x] **iss 검증**: `https://appleid.apple.com` 고정
- [x] **exp 검증**: clock skew 30초 허용
- [x] **JWKS 캐싱**: 5분 캐시, kid 미일치 시 재조회
- [x] **계정 분리**: Provider별 별도 계정 (연동 미지원)
- [ ] **nonce 검증**: 현재 미적용 (프론트에서 미사용). 향후 보안 강화 시 추가 가능

---

## 10. 향후 확장 고려사항

1. **웹 기반 Sign in with Apple**: authorization code 플로우 추가 시 Client Secret 필요
2. **계정 연동**: 명시적 연결 플로우 (설정 페이지에서 수동 연결)
3. **nonce 검증**: 리플레이 공격 방지 강화
