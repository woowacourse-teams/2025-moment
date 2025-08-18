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
import moment.auth.application.GoogleAuthService;
import moment.auth.application.TokenManager;
import moment.auth.domain.RefreshToken;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.request.PasswordUpdateRequest;
import moment.auth.dto.request.RefreshTokenRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.JwtTokenManager;
import moment.auth.infrastructure.RefreshTokenRepository;
import moment.common.DatabaseCleaner;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
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
    @Autowired
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 이메일_인증_요청에_성공한다() {
        // given
        EmailRequest request = new EmailRequest("ekorea6gamil.com");

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/email")
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
                .when().post("/api/v1/auth/email")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/email/verify")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 로그인에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        User user = userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago", ProviderType.EMAIL));
        LoginRequest request = new LoginRequest("ekorea623@gmail.com", "1q2w3e4r!");

        // when
        Response response = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/login");
        response.then().log().all().statusCode(HttpStatus.OK.value());

        // then
        String accessToken = response.getCookie("accessToken");
        String refreshToken = response.getCookie("refreshToken");

        Authentication authentication = jwtTokenManager.extractAuthentication(accessToken);

        assertAll(
                () -> assertThat(accessToken).isNotNull().isNotBlank(),
                () -> assertThat(refreshToken).isNotNull().isNotBlank(),
                () -> assertThat(authentication.id()).isEqualTo(user.getId()));
    }

    @Test
    void 로그아웃에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago", ProviderType.EMAIL));

        String accessToken = jwtTokenManager.createAccessToken(1L, "ekorea623@gmail.com");
        String refreshToken = jwtTokenManager.createRefreshToken(1L, "ekorea623@gmail.com"); // Refresh Token 생성 로직이 필요

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshToken)
                .contentType(ContentType.JSON)
                .when().post("/api/v1/auth/logout");

        // then
        response.then().log().all().statusCode(HttpStatus.OK.value());
        assertAll(
                () -> assertThat(response.getCookie("accessToken")).isEmpty(),
                () -> assertThat(response.getCookie("refreshToken")).isEmpty());
    }

    @Test
    void 구글_로그인_시도_시_구글_OAuth_페이지로_리디렉션한다() {
        given()
                .when()
                .redirects().follow(false)
                .get("/api/v1/auth/login/google")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header(HttpHeaders.LOCATION, containsString("https://accounts.google.com/o/oauth2/v2/auth"));
    }

    @Test
    void 구글로부터_인증_코드를_받으면_토큰을_발급하고_메인페이지로_리디렉션한다() {
        //given & when
        Response response = given()
                .when()
                .redirects().follow(false)
                .queryParam("code", "testAuthorizationCode")
                .get("/api/v1/auth/callback/google");

        // then
        response.then()
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value())
                .header(HttpHeaders.LOCATION, equalTo("http://www.connectingmoment.com/auth/callback?success=true"));

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
                .cookie("token", token)
                .when().get("/api/v1/auth/login/check")
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
                .when().get("/api/v1/auth/login/check")
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
                .when().get("/api/v1/auth/login/check")
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
        User user = userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago", ProviderType.EMAIL));

        String accessToken = createExpiredToken(user.getId(), user.getEmail());
        String refreshTokenValue = jwtTokenManager.createRefreshToken(user.getId(), user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                user,
                tokenManager.getIssuedAtFromToken(refreshTokenValue),
                tokenManager.getExpirationTimeFromToken(refreshTokenValue));

        refreshTokenRepository.save(refreshToken);

        RefreshTokenRequest request = new RefreshTokenRequest(refreshTokenValue);
        sleep(1001);

        // when
        Response response = given().log().all()
                .cookie("accessToken", accessToken)
                .cookie("refreshToken", refreshTokenValue)
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/refresh");

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
        String email = "ekorea623@gmail.com";
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        userRepository.save(new User(email, encodedPassword, "drago", ProviderType.EMAIL));
        PasswordUpdateRequest request = new PasswordUpdateRequest(email);

        // when & then
        given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/email/password")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
