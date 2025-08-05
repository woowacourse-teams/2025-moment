package moment.auth.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import moment.auth.application.GoogleAuthService;
import moment.auth.dto.request.LoginRequest;
import moment.auth.infrastructure.JwtTokenManager;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired
    JwtTokenManager jwtTokenManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @MockBean
    private GoogleAuthService googleAuthService;

    @Test
    void 로그인에_성공한다() {
        // given
        String encodedPassword = encoder.encode("1q2w3e4r!");
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
        String encodedPassword = encoder.encode("1q2w3e4r!");
        User user = userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago", ProviderType.EMAIL));

        String token = jwtTokenManager.createToken(1L, "ekorea623@gmail.com");

        // when
        String emptyToken = given().log().all()
                .cookie("token", token)
                .contentType(ContentType.JSON)
                .when().post("/api/v1/auth/logout")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().cookie("token");

        // then
        assertAll(
                () -> assertThat(emptyToken).isEmpty()
        );
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
        // given
        Mockito.when(googleAuthService.loginOrSignUp(Mockito.anyString()))
                .thenReturn("testToken");

        // when
        Response response = given()
                .when()
                .redirects().follow(false)
                .queryParam("code", "testAuthorizationCode")
                .get("/api/v1/auth/callback/google");

        // then
        response.then()
                .statusCode(HttpStatus.MOVED_PERMANENTLY.value());

        String setCookieHeader = response.getHeader(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);

        response.then()
                .header(HttpHeaders.SET_COOKIE, containsString("token=testToken"));

        response.then()
                .header(HttpHeaders.LOCATION, equalTo("http://www.connectingmoment.com"));
    }
}
