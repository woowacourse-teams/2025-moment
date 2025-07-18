package moment.moment.presentation;

import moment.auth.application.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class MomentControllerTest {

    @Autowired
    private TokenManager tokenManager;

//    @Test
//    void 모멘트를_등록한다() {
//        // given
//        MomentCreateRequest request = new MomentCreateRequest("재미있는 내용이네요~~?");
//        String token = tokenManager.createToken(1L, "lebron@gmail.com");
//
//        // when
//        SuccessResponse response = RestAssured.given().log().all()
//                .contentType(ContentType.JSON)
//                .cookie("token", token)
//                .body(request)
//                .when().post("/api/v1/users/moments")
//                .then().log().all()
//                .statusCode(HttpStatus.CREATED.value())
//                .extract().as(SuccessResponse.class);
//
//        // then
//        assertAll(() -> assertThat(response.status()).isEqualTo(HttpStatus.CREATED.value())
//        );
//    }
}
