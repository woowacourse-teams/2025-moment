# Apple 로그인 구현 계획 (TDD)

## 개요
Expo 앱에서 전달받은 Apple `identityToken`(JWT)을 검증하고 회원가입/로그인을 처리하는 백엔드 API를 구현합니다.

---

## Phase 목록

| Phase | 파일 | 설명 | 상태 |
|-------|------|------|------|
| 1 | [phase1-domain-dto.md](./phase1-domain-dto.md) | 도메인 및 DTO 구현 | ⬜ |
| 2 | [phase2-infrastructure.md](./phase2-infrastructure.md) | AppleAuthClient 구현 | ⬜ |
| 3 | [phase3-application-service.md](./phase3-application-service.md) | AppleAuthService 구현 | ⬜ |
| 4 | [phase4-presentation.md](./phase4-presentation.md) | AuthController 엔드포인트 추가 | ⬜ |
| 5 | [phase5-configuration.md](./phase5-configuration.md) | 환경 설정 | ⬜ |

---

## 구현 순서

```
Phase 1 (도메인/DTO)
    ↓
Phase 2 (Infrastructure)
    ↓
Phase 3 (Application Service)
    ↓
Phase 4 (Presentation)
    ↓
Phase 5 (Configuration)
```

---

## 신규 파일 (6개)

| 파일 | Phase | 설명 |
|------|-------|------|
| `auth/dto/apple/ApplePublicKey.java` | 1 | Apple JWKS 개별 키 DTO |
| `auth/dto/apple/ApplePublicKeys.java` | 1 | Apple JWKS 응답 DTO |
| `auth/dto/apple/AppleUserInfo.java` | 1 | 검증된 토큰에서 추출한 사용자 정보 |
| `auth/dto/request/AppleLoginRequest.java` | 1 | Apple 로그인 요청 DTO |
| `auth/infrastructure/AppleAuthClient.java` | 2 | Apple JWKS 조회 및 JWT 검증 |
| `auth/application/AppleAuthService.java` | 3 | Apple 인증 비즈니스 로직 |

---

## 수정 파일 (4개)

| 파일 | Phase | 수정 내용 |
|------|-------|----------|
| `user/domain/ProviderType.java` | 1 | `APPLE` enum 추가 |
| `global/exception/ErrorCode.java` | 1 | Apple 에러 코드 추가 (AP-001 ~ AP-005) |
| `auth/presentation/AuthController.java` | 4 | `POST /api/v2/auth/apple` 엔드포인트 추가 |
| `application-*.yml` | 5 | `auth.apple.client-ids` 설정 추가 |

---

## 테스트 파일 (4개)

| 파일 | Phase | 설명 |
|------|-------|------|
| `auth/dto/apple/ApplePublicKeyTest.java` | 1 | ApplePublicKey 단위 테스트 |
| `auth/dto/apple/ApplePublicKeysTest.java` | 1 | ApplePublicKeys 단위 테스트 |
| `auth/dto/apple/AppleUserInfoTest.java` | 1 | AppleUserInfo 단위 테스트 |
| `auth/infrastructure/AppleAuthClientTest.java` | 2 | AppleAuthClient 단위 테스트 |
| `auth/application/AppleAuthServiceTest.java` | 3 | AppleAuthService 단위 테스트 |
| `auth/presentation/AuthControllerAppleTest.java` | 4 | Apple 로그인 E2E 테스트 |

---

## TDD 진행 방법

각 Phase에서:

1. **Red**: 테스트 먼저 작성 → 실패 확인
2. **Green**: 최소한의 코드로 테스트 통과
3. **Refactor**: 필요시 리팩토링 (별도 커밋)

### 테스트 실행 명령어

```bash
cd server

# 빠른 테스트 (e2e 제외)
./gradlew fastTest

# 전체 테스트
./gradlew test

# E2E 테스트만
./gradlew e2eTest
```

---

## API 스펙

### POST /api/v2/auth/apple

**Request**
```json
{
  "identityToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**Response (200 OK)**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Cookies**
- `accessToken`: JWT 액세스 토큰 (30분)
- `refreshToken`: JWT 리프레시 토큰 (7일)

**에러 코드**

| 코드 | HTTP | 설명 |
|------|------|------|
| AP-001 | 401 | 유효하지 않은 Apple 토큰 |
| AP-002 | 401 | 만료된 Apple 토큰 |
| AP-003 | 500 | Apple 공개키를 찾을 수 없음 |
| AP-004 | 500 | Apple 공개키 생성 실패 |
| AP-005 | 503 | Apple 인증 서버 오류 |

---

## 환경 변수

| 변수 | 설명 | 예시 |
|------|------|------|
| `APPLE_CLIENT_IDS` | 허용된 Client ID 목록 | `com.moment.app,com.moment.app.dev` |

---

## 핵심 설계 결정

### 사용자 식별
- **식별자**: `{apple_sub}@apple.user` 형태로 email 필드에 저장
- **이유**: Apple sub는 사용자마다 고유하므로 UNIQUE 제약 조건 충족

### 보안
- **알고리즘 제한**: RS256만 허용
- **aud 검증**: 환경 변수로 허용 리스트 관리
- **JWKS 캐싱**: 5분 캐시, kid 미일치 시 재조회

### 계정 정책
- **연동 미지원**: 동일 이메일이어도 Provider별 별도 계정
- **실제 이메일 미저장**: Apple이 제공하는 실제 이메일은 사용하지 않음

---

## 참고 문서

- [Apple Sign In 공식 문서](https://developer.apple.com/documentation/sign_in_with_apple)
- [Apple JWKS 엔드포인트](https://appleid.apple.com/auth/keys)
- [원본 계획서](../apple-login-implementation.md)
