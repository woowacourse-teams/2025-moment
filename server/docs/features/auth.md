# Auth Domain (PREFIX: AUTH)

> Last Updated: 2026-02-04
> Features: 12

## 기능 목록

### AUTH-001: 이메일 로그인

- **Status**: DONE
- **API**: `POST /api/v2/auth/login`
- **Key Classes**:
    - Controller: `AuthController`
    - Domain: `AuthService`
- **Business Rules**: 이메일 + 비밀번호 검증 후 JWT 토큰 발급 (accessToken cookie)
- **Dependencies**: user (UserService)
- **Tests**: `AuthControllerTest` (E2E)
- **Error Codes**: U-002 (로그인 실패)

### AUTH-002: 로그아웃

- **Status**: DONE
- **API**: `POST /api/v2/auth/logout`
- **Key Classes**:
    - Controller: `AuthController`
    - Domain: `AuthService`
- **Business Rules**: accessToken 쿠키 제거, refreshToken 삭제
- **Dependencies**: 없음
- **Tests**: `AuthControllerTest` (E2E)

### AUTH-003: Google OAuth

- **Status**: DONE
- **API**: `GET /api/v2/auth/login/google`, `GET /api/v2/auth/callback/google`
- **Key Classes**:
    - Controller: `AuthController`
    - Domain: `GoogleAuthService`
- **Business Rules**: Google OAuth2.0 흐름, 신규 사용자 자동 가입
- **Dependencies**: user (UserService)
- **Tests**: `AuthControllerTest` (E2E)

### AUTH-004: Apple Sign-in

- **Status**: DONE
- **API**: `POST /api/v2/auth/apple`
- **Key Classes**:
    - Controller: `AuthController`
    - Application: `AppleAuthService`
    - Infrastructure: `AppleAuthClient`
    - DTO: `AppleUserInfo`
- **Business Rules**: Apple Identity Token 검증, 실제 Apple 이메일 사용 (email 클레임), 이메일 미공유 시 MD5 해시 기반 짧은 이메일 생성 (`apple_{hash}@apple.app`), 기존 `sub@apple.user` 형식 레거시 이메일 자동 마이그레이션, 신규 사용자 자동 가입
- **Dependencies**: user (UserRepository, NicknameGenerateApplicationService)
- **Tests**: `AppleAuthServiceTest`, `ApplePublicKeyTest`, `ApplePublicKeysTest`, `AppleUserInfoTest`, `AppleAuthClientTest`
- **Error Codes**: AP-001 ~ AP-005 (Apple 인증 관련)

### AUTH-005: 로그인 상태 확인

- **Status**: DONE
- **API**: `GET /api/v2/auth/login/check`
- **Key Classes**:
    - Controller: `AuthController`
    - Domain: `AuthService`
- **Business Rules**: accessToken 쿠키 존재 및 유효성 확인
- **Dependencies**: 없음
- **Tests**: `AuthControllerTest` (E2E)

### AUTH-006: 토큰 갱신

- **Status**: DONE
- **API**: `POST /api/v2/auth/refresh`
- **Key Classes**:
    - Controller: `AuthController`
    - Domain: `AuthService`
    - Entity: `RefreshToken`
- **Business Rules**: refreshToken으로 새 accessToken 발급
- **Dependencies**: 없음
- **Tests**: `TokensIssuerTest`, `RefreshTokenTest`, `RefreshTokenRepositoryTest`
- **DB Migration**: V9 (`V9__create_refreshToken__mysql.sql`)
- **Error Codes**: T-001 ~ T-007 (토큰 관련)

### AUTH-007: 이메일 인증 요청

- **Status**: DONE
- **API**: `POST /api/v2/auth/email`
- **Key Classes**:
    - Controller: `AuthController`
    - Application: `AuthEmailService`
- **Business Rules**: 인증 코드 생성 후 이메일 발송
- **Dependencies**: 없음
- **Tests**: `EmailVerificationTest`
- **Error Codes**: V-001 ~ V-004 (이메일 인증 관련)

### AUTH-008: 이메일 인증 확인

- **Status**: DONE
- **API**: `POST /api/v2/auth/email/verify`
- **Key Classes**:
    - Controller: `AuthController`
    - Application: `AuthEmailService`
- **Business Rules**: 인증 코드 검증
- **Dependencies**: 없음
- **Tests**: `EmailVerificationTest`
- **Error Codes**: V-001 ~ V-004

### AUTH-009: 비밀번호 재설정 요청

- **Status**: DONE
- **API**: `POST /api/v2/auth/email/password`
- **Key Classes**:
    - Controller: `AuthController`
    - Application: `AuthEmailService`
- **Business Rules**: 비밀번호 재설정 링크 이메일 발송
- **Dependencies**: user (UserService)
- **Tests**: `AuthControllerTest` (E2E)

### AUTH-010: 비밀번호 재설정 실행

- **Status**: DONE
- **API**: `POST /api/v2/auth/email/password/reset`
- **Key Classes**:
    - Controller: `AuthController`
    - Application: `AuthEmailService`
- **Business Rules**: 토큰 검증 후 비밀번호 변경
- **Dependencies**: user (UserService)
- **Tests**: `AuthControllerTest` (E2E)

### AUTH-011: JWT Cookie 인증 체계

- **Status**: DONE
- **API**: N/A (인프라)
- **Key Classes**:
    - Infrastructure: `LoginUserArgumentResolver`, `JwtTokenManager`, `Authentication`
- **Business Rules**: accessToken 쿠키에서 JWT 추출 → `@AuthenticationPrincipal` 주입
- **Dependencies**: 없음
- **Tests**: `JwtTokenManagerTest`

### AUTH-012: Apple 로그인 이메일 개선 및 레거시 마이그레이션

- **Status**: DONE
- **API**: N/A (AUTH-004 내부 로직 개선)
- **Key Classes**:
    - Application: `AppleAuthService`
    - Infrastructure: `AppleAuthClient`
    - DTO: `AppleUserInfo` (`resolveDisplayEmail()`, `toLegacyEmail()`)
- **Business Rules**:
    - Identity Token의 `email` 클레임에서 실제 Apple 이메일 추출
    - 이메일 미공유 시 sub의 MD5 해시 앞 8자로 `apple_{hash}@apple.app` 형식 생성
    - 기존 `sub@apple.user` 형식 사용자는 새 이메일 형식으로 자동 마이그레이션 (`User.updateEmail()`)
    - 동일 sub는 항상 동일한 해시 이메일 생성 (결정적)
- **Dependencies**: user (`User.updateEmail()`)
- **Tests**: `AppleAuthServiceTest` (7개 테스트), `AppleUserInfoTest` (6개 테스트), `AppleAuthClientTest` (10개 테스트)

## 관련 엔티티

- `RefreshToken` (@Entity: "refresh_tokens")
- `EmailVerification` (Value Object)

## 관련 테스트 클래스 (11개)

- `AppleAuthServiceTest` (7개: 이메일 있는/없는 기존 사용자, 레거시 fallback 마이그레이션, 신규 사용자 이메일/해시, 재로그인, 예외 전파)
- `AppleUserInfoTest` (6개: resolveDisplayEmail, toLegacyEmail)
- `AppleAuthClientTest` (10개: 토큰 검증, email 클레임 추출, 공개키 조회)
- `TokensIssuerTest`, `EmailVerificationTest`, `RefreshTokenTest`
- `ApplePublicKeyTest`, `ApplePublicKeysTest`
- `JwtTokenManagerTest`, `RefreshTokenRepositoryTest`
- `AuthControllerTest` (E2E)

## DB 마이그레이션

- V9: `V9__create_refreshToken__mysql.sql`
