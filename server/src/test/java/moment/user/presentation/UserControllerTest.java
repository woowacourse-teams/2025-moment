package moment.user.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import moment.auth.application.TokenManager;
import moment.global.dto.response.SuccessResponse;
import moment.user.domain.User;
import moment.user.dto.request.UserCreateRequest;
import moment.user.dto.response.UserProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Test
    void 유저_생성에_성공한다() {
        // given
        UserCreateRequest request = new UserCreateRequest("mimi@icloud.com", "mimi1234!", "mimi1234!", "mimi");

        // when
        SuccessResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/users/signup")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(SuccessResponse.class);

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(userRepository.existsByEmail("mimi@icloud.com")).isTrue()
        );
    }

    @Test
    void 유저_프로필_조회에_성공한다() {
        // given
        String nickname = "mimi";
        User user = userRepository.save(new User("mimi@icloud.com", "password", nickname));
        String token = tokenManager.createToken(user.getId(), user.getEmail());
        UserProfileResponse expect = new UserProfileResponse(nickname);

        // when
        SuccessResponse<UserProfileResponse> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/api/v1/users/me")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.data()).isEqualTo(expect)
        );
    }
}
