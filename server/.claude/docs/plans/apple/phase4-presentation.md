# Phase 4: Presentation 구현 (AuthController)

## 목표
Apple 로그인 API 엔드포인트를 AuthController에 추가합니다.

---

## 1. 엔드포인트 스펙

### API 정보
- **Method**: POST
- **Path**: `/api/v2/auth/apple`
- **Request Body**: `AppleLoginRequest`
- **Response**: `SuccessResponse<Void>` + 쿠키 (accessToken, refreshToken)

### 요청 예시
```json
{
  "identityToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMzQ1In0..."
}
```

### 응답 예시
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

### 쿠키 설정
- `accessToken`: 30분 유효
- `refreshToken`: 7일 유효
- SameSite=none, Secure=true, HttpOnly=true

---

## 2. AuthController 수정

### 파일 위치
`src/main/java/moment/auth/presentation/AuthController.java`

### 추가할 의존성
```java
private final AppleAuthService appleAuthService;
```

### 추가할 엔드포인트
```java
@Operation(summary = "Apple 로그인", description = "Apple Identity Token을 검증하고 로그인/회원가입을 처리합니다.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Apple 로그인 성공"),
    @ApiResponse(responseCode = "401", description = """
        - [AP-001] 유효하지 않은 Apple 토큰입니다.
        - [AP-002] 만료된 Apple 토큰입니다.
        """,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(responseCode = "500", description = """
        - [AP-003] Apple 공개키를 찾을 수 없습니다.
        - [AP-004] Apple 공개키 생성에 실패했습니다.
        """,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(responseCode = "503", description = """
        - [AP-005] Apple 인증 서버 오류입니다.
        """,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
})
@PostMapping("/apple")
public ResponseEntity<SuccessResponse<Void>> appleLogin(
    @Valid @RequestBody AppleLoginRequest request
) {
    Tokens tokens = appleAuthService.loginOrSignUp(request.identityToken());
    String accessToken = tokens.getAccessToken();
    String refreshToken = tokens.getRefreshToken().getTokenValue();

    ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_HEADER, accessToken)
        .sameSite("none")
        .secure(true)
        .httpOnly(true)
        .path("/")
        .maxAge(ACCESS_TOKEN_TIME)
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_HEADER, refreshToken)
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

## 3. 테스트

### 파일 위치
`src/test/java/moment/auth/presentation/AuthControllerAppleTest.java`

또는 기존 `AuthControllerTest.java`에 추가

### 테스트 케이스 (E2E)

```java
package moment.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import moment.auth.application.AppleAuthService;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.request.AppleLoginRequest;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.support.AcceptanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Tag("e2e")
class AuthControllerAppleTest extends AcceptanceTest {

    @MockBean
    private AppleAuthService appleAuthService;

    @Nested
    @DisplayName("POST /api/v2/auth/apple")
    class AppleLoginTest {

        @Test
        @DisplayName("유효한 identityToken으로 로그인 성공")
        void success() {
            // given
            String identityToken = "valid.identity.token";
            AppleLoginRequest request = new AppleLoginRequest(identityToken);
            Tokens tokens = new Tokens("access-token", new RefreshToken("refresh-token"));

            when(appleAuthService.loginOrSignUp(identityToken)).thenReturn(tokens);

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
            assertThat(response.cookie("accessToken")).isEqualTo("access-token");
            assertThat(response.cookie("refreshToken")).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("identityToken이 비어있으면 400 에러")
        void emptyToken_returns400() {
            // given
            AppleLoginRequest request = new AppleLoginRequest("");

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("identityToken이 null이면 400 에러")
        void nullToken_returns400() {
            // given
            String requestBody = "{}";

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        @DisplayName("유효하지 않은 토큰이면 401 에러 (AP-001)")
        void invalidToken_returns401() {
            // given
            String identityToken = "invalid.token";
            AppleLoginRequest request = new AppleLoginRequest(identityToken);

            when(appleAuthService.loginOrSignUp(identityToken))
                .thenThrow(new MomentException(ErrorCode.APPLE_TOKEN_INVALID));

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.jsonPath().getString("code")).isEqualTo("AP-001");
        }

        @Test
        @DisplayName("만료된 토큰이면 401 에러 (AP-002)")
        void expiredToken_returns401() {
            // given
            String identityToken = "expired.token";
            AppleLoginRequest request = new AppleLoginRequest(identityToken);

            when(appleAuthService.loginOrSignUp(identityToken))
                .thenThrow(new MomentException(ErrorCode.APPLE_TOKEN_EXPIRED));

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(response.jsonPath().getString("code")).isEqualTo("AP-002");
        }

        @Test
        @DisplayName("Apple 공개키를 찾을 수 없으면 500 에러 (AP-003)")
        void publicKeyNotFound_returns500() {
            // given
            String identityToken = "valid.token.but.key.not.found";
            AppleLoginRequest request = new AppleLoginRequest(identityToken);

            when(appleAuthService.loginOrSignUp(identityToken))
                .thenThrow(new MomentException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(response.jsonPath().getString("code")).isEqualTo("AP-003");
        }

        @Test
        @DisplayName("Apple 인증 서버 오류면 503 에러 (AP-005)")
        void appleServerError_returns503() {
            // given
            String identityToken = "valid.token";
            AppleLoginRequest request = new AppleLoginRequest(identityToken);

            when(appleAuthService.loginOrSignUp(identityToken))
                .thenThrow(new MomentException(ErrorCode.APPLE_AUTH_SERVER_ERROR));

            // when
            ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when()
                .post("/api/v2/auth/apple")
                .then().log().all()
                .extract();

            // then
            assertThat(response.statusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
            assertThat(response.jsonPath().getString("code")).isEqualTo("AP-005");
        }
    }
}
```

### 단위 테스트 (선택)

```java
package moment.auth.presentation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import moment.auth.application.AppleAuthService;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.request.AppleLoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerAppleUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppleAuthService appleAuthService;

    // 다른 의존성들도 MockBean 필요...

    @Test
    @DisplayName("Apple 로그인 성공 시 쿠키에 토큰이 설정된다")
    void appleLogin_success_setsCookies() throws Exception {
        // given
        String identityToken = "valid.identity.token";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);
        Tokens tokens = new Tokens("access-token", new RefreshToken("refresh-token"));

        when(appleAuthService.loginOrSignUp(identityToken)).thenReturn(tokens);

        // when & then
        mockMvc.perform(post("/api/v2/auth/apple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(cookie().value("accessToken", "access-token"))
            .andExpect(cookie().value("refreshToken", "refresh-token"));
    }
}
```

---

## 4. import 추가

### AuthController.java에 추가할 import
```java
import moment.auth.application.AppleAuthService;
import moment.auth.dto.request.AppleLoginRequest;
```

---

## 구현 순서 (TDD)

### Step 1: AppleLoginRequest 확인
1. Phase 1에서 생성한 `AppleLoginRequest` 확인

### Step 2: E2E 테스트 작성
1. `AuthControllerAppleTest.java` 작성
2. 테스트 실행 → 실패 확인 (Red)

### Step 3: AuthController 수정
1. `AppleAuthService` 의존성 추가
2. `/apple` 엔드포인트 추가
3. 테스트 통과 확인 (Green)

### Step 4: Swagger 문서 확인
1. `/swagger-ui` 접속하여 API 문서 확인

---

## 체크리스트

- [ ] `AppleLoginRequest` 확인 (Phase 1)
- [ ] E2E 테스트 작성 (`AuthControllerAppleTest`)
  - [ ] 로그인 성공 테스트
  - [ ] 빈 토큰 400 에러 테스트
  - [ ] null 토큰 400 에러 테스트
  - [ ] 유효하지 않은 토큰 401 에러 테스트
  - [ ] 만료된 토큰 401 에러 테스트
  - [ ] 공개키 미발견 500 에러 테스트
  - [ ] Apple 서버 오류 503 에러 테스트
- [ ] `AuthController`에 `AppleAuthService` 의존성 추가
- [ ] `POST /api/v2/auth/apple` 엔드포인트 추가
- [ ] Swagger API 문서 확인
- [ ] `./gradlew fastTest` 전체 통과
- [ ] `./gradlew e2eTest` 전체 통과