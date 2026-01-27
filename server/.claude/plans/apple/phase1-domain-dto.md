# Phase 1: 도메인 및 DTO 구현

## 목표
Apple 로그인에 필요한 기본 도메인 타입과 DTO 클래스를 구현합니다.

---

## 1. ProviderType.APPLE 추가

### 파일 위치
`src/main/java/moment/user/domain/ProviderType.java`

### 변경 내용
```java
public enum ProviderType {
    EMAIL,
    GOOGLE,
    APPLE,  // 추가
}
```

### 테스트
- 별도 테스트 불필요 (enum 값 추가만)

---

## 2. ErrorCode에 Apple 에러 코드 추가

### 파일 위치
`src/main/java/moment/global/exception/ErrorCode.java`

### 추가 내용
```java
// Apple Auth (AP)
APPLE_TOKEN_INVALID("AP-001", "유효하지 않은 Apple 토큰입니다.", HttpStatus.UNAUTHORIZED),
APPLE_TOKEN_EXPIRED("AP-002", "만료된 Apple 토큰입니다.", HttpStatus.UNAUTHORIZED),
APPLE_PUBLIC_KEY_NOT_FOUND("AP-003", "Apple 공개키를 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
APPLE_PUBLIC_KEY_GENERATION_FAILED("AP-004", "Apple 공개키 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
APPLE_AUTH_SERVER_ERROR("AP-005", "Apple 인증 서버 오류입니다.", HttpStatus.SERVICE_UNAVAILABLE),
```

### 추가 위치
- `INVITE_LINK_EXPIRED` 다음에 추가

### 테스트
- 별도 테스트 불필요 (enum 값 추가만)

---

## 3. DTO 클래스 생성

### 3.1 AppleLoginRequest

#### 파일 위치
`src/main/java/moment/auth/dto/request/AppleLoginRequest.java`

#### 구현
```java
package moment.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
    @NotBlank(message = "identityToken은 필수입니다.")
    String identityToken
) {
}
```

#### 테스트 (선택적)
- `@Valid` 검증은 Controller 테스트에서 통합 테스트

---

### 3.2 ApplePublicKey

#### 파일 위치
`src/main/java/moment/auth/dto/apple/ApplePublicKey.java`

#### 구현
```java
package moment.auth.dto.apple;

/**
 * Apple JWKS의 개별 공개키 정보
 * https://appleid.apple.com/auth/keys 응답의 keys 배열 요소
 */
public record ApplePublicKey(
    String kty,  // 키 타입 (RSA)
    String kid,  // 키 ID
    String use,  // 용도 (sig)
    String alg,  // 알고리즘 (RS256)
    String n,    // RSA modulus (Base64URL)
    String e     // RSA exponent (Base64URL)
) {
    /**
     * JWT 헤더의 kid, alg와 일치하는지 확인
     */
    public boolean matches(String targetKid, String targetAlg) {
        return this.kid.equals(targetKid) && this.alg.equals(targetAlg);
    }
}
```

#### 테스트
```java
// ApplePublicKeyTest.java
@Test
void matches_kid와_alg가_일치하면_true() {
    ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");
    assertThat(key.matches("ABC123", "RS256")).isTrue();
}

@Test
void matches_kid가_다르면_false() {
    ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");
    assertThat(key.matches("DIFFERENT", "RS256")).isFalse();
}

@Test
void matches_alg가_다르면_false() {
    ApplePublicKey key = new ApplePublicKey("RSA", "ABC123", "sig", "RS256", "n_value", "e_value");
    assertThat(key.matches("ABC123", "RS512")).isFalse();
}
```

---

### 3.3 ApplePublicKeys

#### 파일 위치
`src/main/java/moment/auth/dto/apple/ApplePublicKeys.java`

#### 구현
```java
package moment.auth.dto.apple;

import java.util.List;
import java.util.Optional;

/**
 * Apple JWKS 응답
 * https://appleid.apple.com/auth/keys
 */
public record ApplePublicKeys(
    List<ApplePublicKey> keys
) {
    /**
     * kid와 alg가 일치하는 공개키 찾기
     */
    public Optional<ApplePublicKey> findMatchingKey(String kid, String alg) {
        return keys.stream()
            .filter(key -> key.matches(kid, alg))
            .findFirst();
    }
}
```

#### 테스트
```java
// ApplePublicKeysTest.java
@Test
void findMatchingKey_일치하는_키가_있으면_반환() {
    ApplePublicKey key1 = new ApplePublicKey("RSA", "key1", "sig", "RS256", "n1", "e1");
    ApplePublicKey key2 = new ApplePublicKey("RSA", "key2", "sig", "RS256", "n2", "e2");
    ApplePublicKeys keys = new ApplePublicKeys(List.of(key1, key2));

    Optional<ApplePublicKey> result = keys.findMatchingKey("key2", "RS256");

    assertThat(result).isPresent();
    assertThat(result.get().kid()).isEqualTo("key2");
}

@Test
void findMatchingKey_일치하는_키가_없으면_empty() {
    ApplePublicKey key1 = new ApplePublicKey("RSA", "key1", "sig", "RS256", "n1", "e1");
    ApplePublicKeys keys = new ApplePublicKeys(List.of(key1));

    Optional<ApplePublicKey> result = keys.findMatchingKey("nonexistent", "RS256");

    assertThat(result).isEmpty();
}
```

---

### 3.4 AppleUserInfo

#### 파일 위치
`src/main/java/moment/auth/dto/apple/AppleUserInfo.java`

#### 구현
```java
package moment.auth.dto.apple;

/**
 * Apple Identity Token에서 추출한 사용자 정보
 */
public record AppleUserInfo(
    String sub  // Apple 사용자 고유 ID (필수)
) {
    /**
     * sub 기반 이메일 생성
     * 예: 001234.abcd1234.0123 -> 001234.abcd1234.0123@apple.user
     */
    public String toAppleEmail() {
        return sub + "@apple.user";
    }
}
```

#### 테스트
```java
// AppleUserInfoTest.java
@Test
void toAppleEmail_sub_기반_이메일_생성() {
    AppleUserInfo userInfo = new AppleUserInfo("001234.abcd1234efgh5678.0123");

    String email = userInfo.toAppleEmail();

    assertThat(email).isEqualTo("001234.abcd1234efgh5678.0123@apple.user");
}
```

---

## 구현 순서 (TDD)

### Step 1: ProviderType.APPLE 추가
1. `ProviderType.java`에 `APPLE` 추가

### Step 2: ErrorCode 추가
1. `ErrorCode.java`에 Apple 에러 코드 5개 추가

### Step 3: ApplePublicKey 테스트 & 구현
1. `ApplePublicKeyTest.java` 작성
2. `ApplePublicKey.java` 구현
3. 테스트 통과 확인

### Step 4: ApplePublicKeys 테스트 & 구현
1. `ApplePublicKeysTest.java` 작성
2. `ApplePublicKeys.java` 구현
3. 테스트 통과 확인

### Step 5: AppleUserInfo 테스트 & 구현
1. `AppleUserInfoTest.java` 작성
2. `AppleUserInfo.java` 구현
3. 테스트 통과 확인

### Step 6: AppleLoginRequest 구현
1. `AppleLoginRequest.java` 구현 (테스트는 Controller 통합 테스트에서)

---

## 체크리스트

- [ ] `ProviderType.APPLE` 추가
- [ ] `ErrorCode` Apple 에러 코드 5개 추가
- [ ] `ApplePublicKeyTest` 작성 및 통과
- [ ] `ApplePublicKey` 구현
- [ ] `ApplePublicKeysTest` 작성 및 통과
- [ ] `ApplePublicKeys` 구현
- [ ] `AppleUserInfoTest` 작성 및 통과
- [ ] `AppleUserInfo` 구현
- [ ] `AppleLoginRequest` 구현
- [ ] `./gradlew fastTest` 전체 통과
