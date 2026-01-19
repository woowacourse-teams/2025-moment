package moment.admin.presentation;

import static org.hamcrest.Matchers.containsString;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;
import moment.admin.domain.AdminSession;
import moment.admin.infrastructure.AdminRepository;
import moment.admin.infrastructure.AdminSessionRepository;
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
class AdminSessionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AdminSessionRepository adminSessionRepository;

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
                .extract().cookie("SESSION");
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
                .extract().cookie("SESSION");
    }

    @Test
    void 슈퍼_관리자가_세션_목록을_조회할_수_있다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .when().get("/admin/sessions")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 일반_관리자는_세션_목록에_접근할_수_없다() {
        // given
        String sessionId = loginAsAdmin();

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .when().get("/admin/sessions")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/error/forbidden"));
    }

    @Test
    void 슈퍼_관리자가_세션_상세_정보를_조회할_수_있다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // 세션이 생성되었으므로 가장 최근 세션을 조회
        AdminSession latestSession = adminSessionRepository.findAllByLogoutTimeIsNullOrderByLoginTimeDesc()
                .stream().findFirst().orElseThrow();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .when().get("/admin/sessions/{id}", latestSession.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("세션 상세 정보"));
    }

    @Test
    void 일반_관리자는_세션_상세에_접근할_수_없다() {
        // given
        String sessionId = loginAsAdmin();

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .when().get("/admin/sessions/{id}", 1)
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/error/forbidden"));
    }

    @Test
    void 존재하지_않는_세션_ID로_조회하면_404_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .when().get("/admin/sessions/{id}", 99999)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 슈퍼_관리자가_특정_세션을_강제_로그아웃시킬_수_있다() {
        // given
        String superAdminSession = loginAsSuperAdmin();

        // 일반 관리자 로그인
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin normalAdmin = new Admin("normal@example.com", "Normal", encodedPassword, AdminRole.ADMIN);
        normalAdmin = adminRepository.save(normalAdmin);

        String normalAdminSession = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", normalAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");

        // 일반 관리자의 세션 ID 찾기
        AdminSession targetSession = adminSessionRepository.findByAdminIdAndLogoutTimeIsNull(normalAdmin.getId())
                .stream().findFirst().orElseThrow();

        // when
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", superAdminSession)
                .when().post("/admin/sessions/{sessionId}/invalidate", targetSession.getSessionId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/sessions"));

        // then - 강제 로그아웃된 세션으로 접근 불가
        RestAssured.given()
                .redirects().follow(false)
                .cookie("SESSION", normalAdminSession)
                .when().get("/admin/users")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 슈퍼_관리자가_특정_관리자의_모든_세션을_강제_로그아웃시킬_수_있다() {
        // given
        String superAdminSession = loginAsSuperAdmin();

        // 일반 관리자 로그인 (세션 생성)
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin normalAdmin = new Admin("normal@example.com", "Normal", encodedPassword, AdminRole.ADMIN);
        normalAdmin = adminRepository.save(normalAdmin);

        // 일반 관리자 로그인
        String normalAdminSession = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", normalAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");

        // when - 특정 관리자의 모든 세션 강제 로그아웃
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", superAdminSession)
                .when().post("/admin/sessions/invalidate-by-admin/{adminId}", normalAdmin.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/sessions"));

        // then - 강제 로그아웃된 세션으로 접근 불가
        RestAssured.given()
                .redirects().follow(false)
                .cookie("SESSION", normalAdminSession)
                .when().get("/admin/users")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 자기_자신의_모든_세션을_강제_로그아웃하려고_하면_실패한다() {
        // given
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin superAdmin = new Admin("super@example.com", "SuperAdmin", encodedPassword, AdminRole.SUPER_ADMIN);
        superAdmin = adminRepository.save(superAdmin);

        String sessionId = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", superAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");

        // when - 자기 자신의 모든 세션 강제 로그아웃 시도
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .when().post("/admin/sessions/invalidate-by-admin/{adminId}", superAdmin.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/sessions"));

        // then - 본인 세션은 여전히 유효
        RestAssured.given()
                .cookie("SESSION", sessionId)
                .when().get("/admin/users")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
