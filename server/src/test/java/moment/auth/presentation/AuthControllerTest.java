package moment.auth.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import moment.auth.application.GoogleAuthService;
import moment.auth.dto.request.EmailRequest;
import moment.auth.dto.request.EmailVerifyRequest;
import moment.auth.dto.request.LoginRequest;
import moment.auth.dto.response.LoginCheckResponse;
import moment.auth.infrastructure.JwtTokenManager;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    private JwtTokenManager jwtTokenManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private GoogleAuthService googleAuthService;

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
        String token = given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/auth/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");

        // then
        Authentication authentication = jwtTokenManager.extractAuthentication(token);
        assertThat(authentication.id()).isEqualTo(user.getId());
    }

    @Test
    void 로그아웃에_성공한다() {
        // given
        String encodedPassword = passwordEncoder.encode("1q2w3e4r!");
        userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago", ProviderType.EMAIL));

        String token = jwtTokenManager.createAccessToken(1L, "ekorea623@gmail.com");

        // when
        String emptyToken = given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/api/v1/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");

        // then
        assertThat(emptyToken).isEmpty();
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
        given()
                .when()
                .redirects().follow(false)
                .queryParam("code", "testAuthorizationCode")
                .get("/api/v1/auth/callback/google")
                .then()
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value())
                .header(HttpHeaders.LOCATION, equalTo("http://www.connectingmoment.com/auth/callback?success=true"))
                .header(HttpHeaders.SET_COOKIE, containsString("token="));
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
}
