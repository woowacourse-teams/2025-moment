package moment.auth.presentation;

import static io.jsonwebtoken.Jwts.SIG.HS256;
import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import java.util.Date;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import moment.auth.service.application.AppleAuthService;
import moment.auth.service.application.GoogleAuthService;
import moment.auth.service.auth.TokenManager;
import moment.auth.domain.RefreshToken;
import moment.auth.domain.Tokens;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.AppleLoginRequest;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.LogoutRequest;
import moment.auth.dto.request.PasswordUpdateRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.JwtTokenManager;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.common.DatabaseCleaner;
import moment.notification.domain.PushNotification;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;
    @Autowired
    private JwtTokenManager jwtTokenManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @MockitoBean
    private GoogleAuthService googleAuthService;
    @MockitoBean
    private AppleAuthService appleAuthService;
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 이메일_인증_요청에_성공한다() {
        // given
        EmailRequest request = new EmailRequest("ekorea@gamil.com");

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 이메일_인증_코드_검증에_성공한다() {
        // given
        EmailVerifyRequest request = new EmailVerifyRequest("ekorea623@gmail.com", "123456");

        given().log().all()
                .contentType(ContentType.JSON)
                .body(new EmailRequest(request.email()))
                .when().post("/api/v2/auth/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/email/verify")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 로그인에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = UserFixture.createUserByPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        LoginRequest request = new LoginRequest(user.getEmail(), "1q2w3e4r!");

        // when
        Response response = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/login");
        response.then().log().all().statusCode(HttpStatus.OK.value());

        // then
        String accessToken = response.getCookie("accessToken");
        String refreshToken = response.getCookie("refreshToken");

        Authentication authentication = jwtTokenManager.extractAuthentication(accessToken);

        assertAll(
                () -> assertThat(accessToken).isNotNull().isNotBlank(),
                () -> assertThat(refreshToken).isNotNull().isNotBlank(),
                () -> assertThat(authentication.id()).isEqualTo(savedUser.getId()));
    }

    @Test
    void 로그아웃에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = UserFixture.createUserByPassword(encodedPassword);
        userRepository.save(user);

        String accessToken = jwtTokenManager.createAccessToken(1L, user.getEmail());
        String refreshToken = jwtTokenManager.createRefreshToken(1L, user.getEmail()); // Refresh Token 생성 로직이 필요

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .contentType(ContentType.JSON)
                .when().post("/api/v2/auth/logout");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());
        assertAll(
                () -> assertThat(response.getCookie("accessToken")).isEmpty(),
                () -> assertThat(response.getCookie("refreshToken")).isEmpty());
    }

    @Test
    void 로그아웃_시_디바이스_엔드포인트를_전달하면_해당_토큰이_삭제된다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = UserFixture.createUserByPassword(encodedPassword);
        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenManager.createAccessToken(savedUser.getId(), user.getEmail());
        String deviceEndpoint = "test-device-token";

        pushNotificationRepository.save(new PushNotification(savedUser, deviceEndpoint));

        LogoutRequest logoutRequest = new LogoutRequest(deviceEndpoint);

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .contentType(ContentType.JSON)
                .body(logoutRequest)
                .when().post("/api/v2/auth/logout");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());

        List<PushNotification> notifications = pushNotificationRepository.findByUserId(savedUser.getId());
        assertThat(notifications).isEmpty();
    }

    @Test
    void 로그아웃_시_body_없이_요청하면_푸시_토큰은_유지된다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = UserFixture.createUserByPassword(encodedPassword);
        User savedUser = userRepository.save(user);

        String accessToken = jwtTokenManager.createAccessToken(savedUser.getId(), user.getEmail());
        String deviceEndpoint = "test-device-token";
        pushNotificationRepository.save(new PushNotification(savedUser, deviceEndpoint));

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .contentType(ContentType.JSON)
                .when().post("/api/v2/auth/logout");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());

        List<PushNotification> notifications = pushNotificationRepository.findByUserId(savedUser.getId());
        assertThat(notifications).hasSize(1);
    }

    @Test
    void 구글_로그인_시도_시_구글_OAuth_페이지로_리디렉션한다() {
        given()
                .when()
                .redirects().follow(false)
                .get("/api/v2/auth/login/google")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header(HttpHeaders.LOCATION, containsString("https://accounts.google.com/o/oauth2/v2/auth"));
    }

    @Test
    void 구글로부터_인증_코드를_받으면_토큰을_발급하고_메인페이지로_리디렉션한다() {
        // given
        User user = userRepository.save(UserFixture.createGoogleUser());
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user, new Date(),
                new Date(System.currentTimeMillis() + 100000));
        Tokens tokens = new Tokens("access-token-value", refreshToken);

        org.mockito.BDDMockito.given(googleAuthService.loginOrSignUp("testAuthorizationCode")).willReturn(tokens);

        //when
        Response response = given()
                .when()
                .redirects().follow(false)
                .queryParam("code", "testAuthorizationCode")
                .get("/api/v2/auth/callback/google");

        // then
        response.then()
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value())
                .header(HttpHeaders.LOCATION, equalTo("test-client-uri/auth/callback?success=true"));

        Headers headers = response.getHeaders();

        List<String> setCookieHeaders = headers.getValues(HttpHeaders.SET_COOKIE);

        // 3. AssertJ를 사용해 리스트를 검증합니다.
        assertThat(setCookieHeaders).hasSize(2);
        assertThat(setCookieHeaders).anyMatch(cookie -> cookie.startsWith("accessToken="));
        assertThat(setCookieHeaders).anyMatch(cookie -> cookie.startsWith("refreshToken="));
    }

    @Test
    void 쿠키로_토큰을_가지고_있으면_로그인_상태로_참을_반환한다() {
        // given
        String token = jwtTokenManager.createAccessToken(1L, "ekorea623@gmail.com");

        // when
        LoginCheckResponse response = given().log().all()
                .cookie("accessToken", token)
                .when().get("/api/v2/auth/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", LoginCheckResponse.class);

        // then
        assertThat(response.isLogged()).isTrue();
    }

    @Test
    void 쿠키가_없으면_로그인_상태로_거짓을_반환한다() {
        // when
        LoginCheckResponse response = given().log().all()
                .when().get("/api/v2/auth/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", LoginCheckResponse.class);

        // then
        assertThat(response.isLogged()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void 쿠키에_빈_토큰이_있으면_로그인_상태로_거짓을_반환한다(String token) {
        // when
        LoginCheckResponse response = given().log().all()
                .cookie("token", token) // This will send a cookie with an empty or blank value
                .when().get("/api/v2/auth/login/check")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", LoginCheckResponse.class);

        // then
        assertThat(response.isLogged()).isFalse();
    }

    @Test
    void 엑세스_토큰을_재발급한다() throws InterruptedException {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = userRepository.save(UserFixture.createUserByPassword(encodedPassword));

        String accessToken = createExpiredToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtTokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue));

        refreshTokenRepository.save(refreshToken);

        sleep(1001);

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshTokenValue)
                .when().post("/api/v2/auth/refresh");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());
        Headers headers = response.getHeaders();
        List<String> setCookieHeaders = headers.getValues(HttpHeaders.SET_COOKIE);

        String newAccessToken = response.getCookie("accessToken");
        String newRefreshTokenValue = response.getCookie("refreshToken");

        boolean newTokenExists = refreshTokenRepository.findByTokenValue(newRefreshTokenValue).isPresent();
        boolean oldTokenExists = refreshTokenRepository.findByTokenValue(refreshTokenValue).isPresent();

        assertAll(
                () -> assertThat(setCookieHeaders).hasSize(2),
                () -> assertThat(newAccessToken).isNotNull().isNotEqualTo(accessToken),
                () -> assertThat(newRefreshTokenValue).isNotNull().isNotEqualTo(refreshTokenValue),
                () -> assertThat(oldTokenExists).isFalse(),
                () -> assertThat(newTokenExists).isTrue()
        );
    }

    private String createExpiredToken(Long id, String email) {
        SecretKeySpec key = new SecretKeySpec("test-access-key-for-jwt-token-generation".getBytes(), "HmacSHA256");
        Date expiryDate = new Date(System.currentTimeMillis() - 3600 * 1000);

        return Jwts.builder()
                .expiration(expiryDate)
                .subject(id.toString())
                .claim("email", email)
                .issuedAt(new Date())
                .signWith(key, HS256)
                .compact();
    }

    @Test
    void 비밀번호_변경_요청에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = UserFixture.createUserByPassword(encodedPassword);
        userRepository.save(user);
        PasswordUpdateRequest request = new PasswordUpdateRequest(user.getEmail());

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/email/password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void Apple_로그인에_성공한다() {
        // given
        String identityToken = "valid.identity.token";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);
        User user = userRepository.save(UserFixture.createGoogleUser());
        RefreshToken refreshToken = new RefreshToken("refresh-token-value", user, new Date(),
                new Date(System.currentTimeMillis() + 100000));
        Tokens tokens = new Tokens("access-token-value", refreshToken);

        org.mockito.BDDMockito.given(appleAuthService.loginOrSignUp(identityToken)).willReturn(tokens);

        // when
        Response response = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());
        Headers headers = response.getHeaders();
        List<String> setCookieHeaders = headers.getValues(HttpHeaders.SET_COOKIE);

        assertAll(
                () -> assertThat(setCookieHeaders).hasSize(2),
                () -> assertThat(setCookieHeaders).anyMatch(cookie -> cookie.startsWith("accessToken=")),
                () -> assertThat(setCookieHeaders).anyMatch(cookie -> cookie.startsWith("refreshToken="))
        );
    }

    @Test
    void Apple_로그인_시_identityToken이_비어있으면_400_에러를_반환한다() {
        // given
        AppleLoginRequest request = new AppleLoginRequest("");

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple")
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void Apple_로그인_시_유효하지_않은_토큰이면_401_에러를_반환한다() {
        // given
        String identityToken = "invalid.token";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);

        org.mockito.BDDMockito.given(appleAuthService.loginOrSignUp(identityToken))
                .willThrow(new MomentException(ErrorCode.APPLE_TOKEN_INVALID));

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AP-001"));
    }

    @Test
    void Apple_로그인_시_만료된_토큰이면_401_에러를_반환한다() {
        // given
        String identityToken = "expired.token";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);

        org.mockito.BDDMockito.given(appleAuthService.loginOrSignUp(identityToken))
                .willThrow(new MomentException(ErrorCode.APPLE_TOKEN_EXPIRED));

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple")
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AP-002"));
    }

    @Test
    void Apple_로그인_시_공개키를_찾을_수_없으면_500_에러를_반환한다() {
        // given
        String identityToken = "valid.token.but.key.not.found";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);

        org.mockito.BDDMockito.given(appleAuthService.loginOrSignUp(identityToken))
                .willThrow(new MomentException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple")
                .then().log().all()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("code", equalTo("AP-003"));
    }

    @Test
    void Apple_로그인_시_Apple_서버_오류이면_503_에러를_반환한다() {
        // given
        String identityToken = "valid.token";
        AppleLoginRequest request = new AppleLoginRequest(identityToken);

        org.mockito.BDDMockito.given(appleAuthService.loginOrSignUp(identityToken))
                .willThrow(new MomentException(ErrorCode.APPLE_AUTH_SERVER_ERROR));

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v2/auth/apple")
                .then().log().all()
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .body("code", equalTo("AP-005"));
    }
}
