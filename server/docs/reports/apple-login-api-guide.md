# Apple 로그인 API 프론트엔드 연동 가이드

## 1. 개요

### 1.1 배경
Expo 앱에서 Apple Sign In을 통해 받은 `identityToken`을 백엔드로 전송하여 회원가입/로그인을 처리합니다.

### 1.2 API 엔드포인트
```
POST /api/v2/auth/apple
```

---

## 2. 요청 스펙

### 2.1 Request

#### URL
```
POST https://{server-domain}/api/v2/auth/apple
```

#### Headers
```http
Content-Type: application/json
```

#### Request Body
```json
{
  "identityToken": "eyJraWQiOiJXNldjT0tCIiwiYWxnIjoiUlMyNTYifQ..."
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `identityToken` | String | O | Apple Sign In에서 받은 Identity Token (JWT) |

### 2.2 Expo에서 identityToken 획득하기

```typescript
import * as AppleAuthentication from 'expo-apple-authentication';

const handleAppleLogin = async () => {
  try {
    const credential = await AppleAuthentication.signInAsync({
      requestedScopes: [
        AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
        AppleAuthentication.AppleAuthenticationScope.EMAIL,
      ],
    });

    // 이 identityToken을 백엔드로 전송
    const identityToken = credential.identityToken;

    // 백엔드 API 호출
    const response = await fetch('https://api.example.com/api/v2/auth/apple', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // 쿠키 수신을 위해 필수
      body: JSON.stringify({ identityToken }),
    });

    if (response.ok) {
      // 로그인 성공 - 쿠키에 토큰이 자동 저장됨
      console.log('Apple 로그인 성공');
    }
  } catch (error) {
    console.error('Apple 로그인 실패:', error);
  }
};
```

---

## 3. 응답 스펙

### 3.1 성공 응답 (200 OK)

#### Response Body
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

#### Response Cookies
| 쿠키명 | 유효기간 | 설명 |
|--------|----------|------|
| `accessToken` | 30분 | JWT 액세스 토큰 |
| `refreshToken` | 7일 | JWT 리프레시 토큰 |

**쿠키 속성:**
- `SameSite=None`
- `Secure=true`
- `HttpOnly=true`
- `Path=/`

### 3.2 에러 응답

#### 400 Bad Request - 유효성 검증 실패
```json
{
  "code": "G-002",
  "status": "BAD_REQUEST",
  "message": "유효하지 않은 요청 값입니다."
}
```
**원인:** `identityToken`이 비어있거나 null인 경우

---

#### 401 Unauthorized - 유효하지 않은 토큰 (AP-001)
```json
{
  "code": "AP-001",
  "status": "UNAUTHORIZED",
  "message": "유효하지 않은 Apple 토큰입니다."
}
```
**원인:**
- 토큰 형식이 올바르지 않음
- 토큰 서명이 유효하지 않음
- RS256 이외의 알고리즘 사용
- 허용되지 않은 Client ID (aud)

---

#### 401 Unauthorized - 만료된 토큰 (AP-002)
```json
{
  "code": "AP-002",
  "status": "UNAUTHORIZED",
  "message": "만료된 Apple 토큰입니다."
}
```
**원인:** Identity Token이 만료됨 (Apple 토큰은 약 10분간 유효)

**대응:** Apple Sign In을 다시 수행하여 새 토큰 발급 필요

---

#### 500 Internal Server Error - 공개키 없음 (AP-003)
```json
{
  "code": "AP-003",
  "status": "INTERNAL_SERVER_ERROR",
  "message": "Apple 공개키를 찾을 수 없습니다."
}
```
**원인:** Apple JWKS에서 일치하는 공개키를 찾을 수 없음 (드문 경우)

---

#### 500 Internal Server Error - 공개키 생성 실패 (AP-004)
```json
{
  "code": "AP-004",
  "status": "INTERNAL_SERVER_ERROR",
  "message": "Apple 공개키 생성에 실패했습니다."
}
```
**원인:** RSA 공개키 생성 중 오류 발생 (드문 경우)

---

#### 503 Service Unavailable - Apple 서버 오류 (AP-005)
```json
{
  "code": "AP-005",
  "status": "SERVICE_UNAVAILABLE",
  "message": "Apple 인증 서버 오류입니다."
}
```
**원인:** Apple 인증 서버(https://appleid.apple.com/auth/keys)에 연결할 수 없음

**대응:** 잠시 후 재시도 필요

---

## 4. 프론트엔드 구현 예시

### 4.1 React Native / Expo 전체 예시

```typescript
import * as AppleAuthentication from 'expo-apple-authentication';
import { Platform } from 'react-native';

const API_BASE_URL = 'https://api.example.com';

interface AppleLoginResponse {
  code: number;
  status: string;
  data: null;
}

interface ErrorResponse {
  code: string;
  status: string;
  message: string;
}

export const appleLogin = async (): Promise<boolean> => {
  // iOS에서만 Apple 로그인 가능
  if (Platform.OS !== 'ios') {
    console.warn('Apple 로그인은 iOS에서만 지원됩니다.');
    return false;
  }

  // Apple 로그인 가능 여부 확인
  const isAvailable = await AppleAuthentication.isAvailableAsync();
  if (!isAvailable) {
    console.warn('이 기기에서 Apple 로그인을 사용할 수 없습니다.');
    return false;
  }

  try {
    // Step 1: Apple Sign In 수행
    const credential = await AppleAuthentication.signInAsync({
      requestedScopes: [
        AppleAuthentication.AppleAuthenticationScope.FULL_NAME,
        AppleAuthentication.AppleAuthenticationScope.EMAIL,
      ],
    });

    const { identityToken } = credential;

    if (!identityToken) {
      throw new Error('Identity Token을 받지 못했습니다.');
    }

    // Step 2: 백엔드 API 호출
    const response = await fetch(`${API_BASE_URL}/api/v2/auth/apple`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // 쿠키 수신 필수
      body: JSON.stringify({ identityToken }),
    });

    // Step 3: 응답 처리
    if (response.ok) {
      // 성공 - 쿠키에 accessToken, refreshToken 자동 저장
      return true;
    }

    // 에러 처리
    const errorData: ErrorResponse = await response.json();
    handleAppleLoginError(errorData.code, errorData.message);
    return false;

  } catch (error) {
    if (error.code === 'ERR_REQUEST_CANCELED') {
      // 사용자가 로그인 취소
      console.log('사용자가 Apple 로그인을 취소했습니다.');
    } else {
      console.error('Apple 로그인 오류:', error);
    }
    return false;
  }
};

const handleAppleLoginError = (code: string, message: string) => {
  switch (code) {
    case 'AP-001':
      console.error('유효하지 않은 Apple 토큰입니다.');
      break;
    case 'AP-002':
      console.error('Apple 토큰이 만료되었습니다. 다시 시도해주세요.');
      break;
    case 'AP-005':
      console.error('Apple 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
      break;
    default:
      console.error(`Apple 로그인 실패: ${message}`);
  }
};
```

### 4.2 Apple 로그인 버튼 컴포넌트

```tsx
import * as AppleAuthentication from 'expo-apple-authentication';
import { View, StyleSheet, Platform } from 'react-native';

interface AppleLoginButtonProps {
  onSuccess: () => void;
  onError: (error: string) => void;
}

export const AppleLoginButton: React.FC<AppleLoginButtonProps> = ({
  onSuccess,
  onError,
}) => {
  if (Platform.OS !== 'ios') {
    return null; // iOS에서만 렌더링
  }

  const handlePress = async () => {
    const success = await appleLogin();
    if (success) {
      onSuccess();
    } else {
      onError('Apple 로그인에 실패했습니다.');
    }
  };

  return (
    <View style={styles.container}>
      <AppleAuthentication.AppleAuthenticationButton
        buttonType={AppleAuthentication.AppleAuthenticationButtonType.SIGN_IN}
        buttonStyle={AppleAuthentication.AppleAuthenticationButtonStyle.BLACK}
        cornerRadius={8}
        style={styles.button}
        onPress={handlePress}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    marginVertical: 10,
  },
  button: {
    width: 280,
    height: 44,
  },
});
```

---

## 5. 주의사항

### 5.1 플랫폼 제한
- Apple Sign In은 **iOS에서만** 지원됩니다.
- Android에서는 별도의 Apple 로그인 구현이 필요합니다 (웹 기반).

### 5.2 토큰 유효 시간
- Apple Identity Token은 **약 10분** 동안 유효합니다.
- 토큰 발급 후 즉시 백엔드로 전송해야 합니다.

### 5.3 쿠키 설정
- `credentials: 'include'` 옵션을 반드시 설정해야 쿠키가 저장됩니다.
- CORS 설정이 올바르게 되어 있어야 합니다.

### 5.4 계정 정책
- Apple 사용자는 `{apple_sub}@apple.user` 형태의 고유 이메일로 저장됩니다.
- 동일한 실제 이메일을 사용해도 Google 로그인과 Apple 로그인은 별도 계정으로 처리됩니다.
- 계정 연동 기능은 현재 지원하지 않습니다.

### 5.5 재로그인
- 동일한 Apple 계정으로 재로그인 시 기존 계정으로 인식됩니다.
- 새 토큰이 발급되며 기존 세션은 유지됩니다.

---

## 6. 테스트 시나리오

### 6.1 정상 시나리오
1. Apple Sign In 버튼 클릭
2. Apple 인증 완료
3. Identity Token 획득
4. 백엔드 API 호출
5. 200 응답 + 쿠키 저장
6. 메인 화면으로 이동

### 6.2 에러 시나리오
| 시나리오 | 예상 에러 코드 | 대응 |
|----------|----------------|------|
| 빈 토큰 전송 | 400 (G-002) | 토큰 검증 후 재요청 |
| 10분 후 토큰 전송 | 401 (AP-002) | Apple Sign In 재수행 |
| 네트워크 오류 | 503 (AP-005) | 재시도 버튼 표시 |
| 사용자 취소 | - | 로그인 화면 유지 |

---

## 7. 관련 문서

- [Apple Sign In with Expo 공식 문서](https://docs.expo.dev/versions/latest/sdk/apple-authentication/)
- [Apple Sign In REST API](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api)
- 백엔드 구현 계획: `/server/.claude/plans/apple/README.md`

---

## 8. 변경 이력

| 날짜 | 버전 | 설명 |
|------|------|------|
| 2025-01-26 | 1.0 | 최초 작성 |