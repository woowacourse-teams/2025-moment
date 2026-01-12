package moment.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
class AdminManagementControllerTest {

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

    private String loginAsSuperAdmin() {
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin temp = AdminFixture.createAdminByRole(AdminRole.SUPER_ADMIN);
        Admin superAdmin = new Admin(temp.getEmail(), temp.getName(), encodedPassword, AdminRole.SUPER_ADMIN);
        Admin savedAdmin = adminRepository.save(superAdmin);

        return RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", savedAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("JSESSIONID");
    }

    private String loginAsAdmin() {
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin temp = AdminFixture.createAdminByRole(AdminRole.ADMIN);
        Admin admin = new Admin(temp.getEmail(), temp.getName(), encodedPassword, AdminRole.ADMIN);
        Admin savedAdmin = adminRepository.save(admin);

        return RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", savedAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("JSESSIONID");
    }

    @Test
    void 슈퍼_관리자가_관리자_등록_페이지에_접근할_수_있다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .when().get("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 일반_관리자가_관리자_등록_페이지에_접근하면_권한_없음_예외가_발생한다() {
        // given
        String sessionId = loginAsAdmin();

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .sessionId(sessionId)
                .when().get("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/error/forbidden"));
    }

    @Test
    void 세션_없이_관리자_등록_페이지에_접근하면_권한_없음_예외가_발생한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .when().get("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 슈퍼_관리자가_새로운_관리자_계정을_생성한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        String newEmail = "newadmin@test.com";
        String newName = "NewAdmin";
        String newPassword = "password123!@#";

        // when
        RestAssured.given().log().all()
                .redirects().follow(false)
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", newEmail)
                .formParam("name", newName)
                .formParam("password", newPassword)
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));

        // then
        Admin createdAdmin = adminRepository.findByEmail(newEmail).orElseThrow();
        assertAll(
                () -> assertThat(createdAdmin.getEmail()).isEqualTo(newEmail),
                () -> assertThat(createdAdmin.getName()).isEqualTo(newName),
                () -> assertThat(createdAdmin.getRole()).isEqualTo(AdminRole.ADMIN),
                () -> assertThat(passwordEncoder.matches(newPassword, createdAdmin.getPassword())).isTrue()
        );
    }

    @Test
    void 일반_관리자가_관리자_계정을_생성하면_권한_없음_예외가_발생한다() {
        // given
        String sessionId = loginAsAdmin();

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "newadmin@test.com")
                .formParam("name", "NewAdmin")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/error/forbidden"));
    }

    @Test
    void 중복된_이메일로_관리자_계정_생성_시_에러를_반환한다() {
        // given
        String duplicateEmail = "duplicate@test.com";
        Admin existingAdmin = AdminFixture.createAdminByEmail(duplicateEmail);
        adminRepository.save(existingAdmin);

        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", duplicateEmail)
                .formParam("name", "NewAdmin")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML)
                .body(containsString("이미 등록된"));
    }

    @Test
    void 빈_이메일로_관리자_계정_생성_시_검증_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "")
                .formParam("name", "NewAdmin")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 유효하지_않은_이메일_형식으로_관리자_계정_생성_시_검증_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "invalidemail")
                .formParam("name", "NewAdmin")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 빈_이름으로_관리자_계정_생성_시_검증_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "newadmin@test.com")
                .formParam("name", "")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 빈_비밀번호로_관리자_계정_생성_시_검증_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .sessionId(sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "newadmin@test.com")
                .formParam("name", "NewAdmin")
                .formParam("password", "")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }
}
