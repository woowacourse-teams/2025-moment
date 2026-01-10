package moment.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.infrastructure.AdminRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.AdminFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminAuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    void 로그인_페이지를_정상적으로_렌더링한다() {
        // when & then
        RestAssured.given().log().all()
                .when().get("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 로그인_페이지에서_에러_파라미터를_전달하면_모델에_에러가_포함된다() {
        // given
        String errorMessage = "로그인 실패";

        // when & then
        RestAssured.given().log().all()
                .param("error", errorMessage)
                .when().get("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML)
                .body(containsString(errorMessage));
    }

    @Test
    void 정상적인_관리자_로그인에_성공한다() {
        // given
        String rawPassword = "password123!@#";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Admin admin = AdminFixture.createAdminByEmailAndPassword("admin@test.com", hashedPassword);
        Admin savedAdmin = adminRepository.save(admin);

        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.URLENC)
                .formParam("email", "admin@test.com")
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"))
                .extract().response();

        // then
        String sessionId = response.getCookie("JSESSIONID");
        assertThat(sessionId).isNotNull();

        // 세션 ID로 인증된 요청 검증
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void 잘못된_비밀번호로_로그인_시_로그인_페이지로_리다이렉트된다() {
        // given
        String rawPassword = "password123!@#";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Admin admin = AdminFixture.createAdminByEmailAndPassword("admin@test.com", hashedPassword);
        adminRepository.save(admin);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.URLENC)
                .formParam("email", "admin@test.com")
                .formParam("password", "wrongpassword")
                .when().post("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login?error="));
    }

    @Test
    void 존재하지_않는_이메일로_로그인_시_로그인_페이지로_리다이렉트된다() {
        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.URLENC)
                .formParam("email", "nonexistent@test.com")
                .formParam("password", "password123!@#")
                .when().post("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login?error="));
    }

    @Test
    void 로그아웃에_성공하고_로그인_페이지로_리다이렉트된다() {
        // given
        String rawPassword = "password123!@#";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Admin admin = AdminFixture.createAdminByEmailAndPassword("admin@test.com", hashedPassword);
        adminRepository.save(admin);

        // 로그인하여 세션 ID 획득
        String sessionId = RestAssured.given()
                .contentType(ContentType.URLENC)
                .formParam("email", "admin@test.com")
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("JSESSIONID");

        // when
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().post("/admin/logout")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));

        // then - 로그아웃 후 세션 무효화 확인
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 슈퍼_관리자_로그인에_성공한다() {
        // given
        String rawPassword = "password123!@#";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        Admin superAdmin = new Admin("super@test.com", "SuperAdmin", hashedPassword, AdminRole.SUPER_ADMIN);
        Admin savedAdmin = adminRepository.save(superAdmin);

        // when
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.URLENC)
                .formParam("email", "super@test.com")
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"))
                .extract().response();

        // then
        String sessionId = response.getCookie("JSESSIONID");
        assertThat(sessionId).isNotNull();

        // 세션 ID로 인증된 요청 검증
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value());
    }
}
