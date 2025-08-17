package moment.user.presentation;

import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import moment.auth.application.TokenManager;
import moment.global.dto.response.SuccessResponse;
import moment.user.domain.Level;
import moment.user.domain.ProviderType;
import moment.user.domain.User;
import moment.user.dto.response.MyPageProfileResponse;
import moment.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MyPageControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenManager tokenManager;

    @Test
    void 유저_프로필_정보를_조회한다() {
        // given
        User user = new User("test@gmail.com", "qwer1234!", "신비로운 행성의 지구", ProviderType.EMAIL);
        userRepository.save(user);

        String token = tokenManager.createToken(user.getId(), user.getEmail());

        // when
        SuccessResponse<MyPageProfileResponse> response = RestAssured.given().log().all()
                .cookie("token", token)
                .when().get("/api/v1/my/profile")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(new TypeRef<>() {
                });

        // then
        MyPageProfileResponse profile = response.data();
        assertAll(
                () -> assertThat(response.status()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(profile.nickname()).isEqualTo(user.getNickname()),
                () -> assertThat(profile.email()).isEqualTo(user.getEmail()),
                () -> assertThat(profile.level()).isEqualTo(Level.ASTEROID_WHITE),
                () -> assertThat(profile.availableStar()).isEqualTo(0),
                () -> assertThat(profile.expStar()).isEqualTo(0),
                () -> assertThat(profile.nextStepExp()).isEqualTo(5)
        );
    }
}
