package moment.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.auth.dto.request.LoginRequest;
import moment.auth.infrastructure.JwtTokenManager;
import moment.user.domain.User;
import moment.user.dto.request.Authentication;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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

    @Test
    void 로그인에_성공한다() {
        // given
        String encodedPassword = encoder.encode("1q2w3e4r!");
        User user = userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago"));
        LoginRequest request = new LoginRequest("ekorea623@gmail.com", "1q2w3e4r!");

        // when
        String token = RestAssured.given().log().all()
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
        User user = userRepository.save(new User("ekorea623@gmail.com", encodedPassword, "drago"));

        String token = jwtTokenManager.createToken(1L, "ekorea623@gmail.com");

        // when
        String emptyToken = RestAssured.given().log().all()
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
}
