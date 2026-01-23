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
    void 슈퍼_관리자가_관리자_등록_페이지에_접근할_수_있다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
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
                .cookie("SESSION", sessionId)
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
                .cookie("SESSION", sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", newEmail)
                .formParam("name", newName)
                .formParam("password", newPassword)
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/accounts"));

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
                .cookie("SESSION", sessionId)
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
        // 템플릿이 구현되기 전이므로 상태 코드와 content type만 검증
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", duplicateEmail)
                .formParam("name", "NewAdmin")
                .formParam("password", "password123!@#")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
                // TODO: Phase 5 (템플릿 구현 후) body에 에러 메시지 포함 확인 추가
                // .body(containsString("이미 등록된"));
    }

    @Test
    void 빈_이메일로_관리자_계정_생성_시_검증_에러가_발생한다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
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
                .cookie("SESSION", sessionId)
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
                .cookie("SESSION", sessionId)
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
                .cookie("SESSION", sessionId)
                .contentType(ContentType.URLENC)
                .formParam("email", "newadmin@test.com")
                .formParam("name", "NewAdmin")
                .formParam("password", "")
                .when().post("/admin/accounts/new")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 슈퍼_관리자가_관리자_목록을_볼_수_있다() {
        // given
        String sessionId = loginAsSuperAdmin();

        // when & then
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .when().get("/admin/accounts")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("관리자 관리"));
    }

    @Test
    void 일반_ADMIN은_관리자_목록을_볼_수_없다() {
        // given
        String sessionId = loginAsAdmin();

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .when().get("/admin/accounts")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/error/forbidden"));
    }

    @Test
    void 관리자_차단_후_세션_무효화() {
        // given
        String superAdminSession = loginAsSuperAdmin();

        // Target Admin setup
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin targetAdmin = new Admin("target@example.com", "Target", encodedPassword, AdminRole.ADMIN);
        targetAdmin = adminRepository.save(targetAdmin);

        // Target Admin Login
        String targetAdminSession = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", targetAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");

        // when - SUPER_ADMIN blocks Target Admin
        RestAssured.given().log().all()
                .redirects().follow(false)
                .sessionId(superAdminSession)
                .post("/admin/accounts/{id}/block", targetAdmin.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value());

        // then - Target Admin cannot access protected resource
        RestAssured.given().log().all()
                .redirects().follow(false)
                .sessionId(targetAdminSession)
                .get("/admin/accounts") // or any protected URL
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 자기_자신을_차단하려고_하면_차단이_실패하고_리다이렉트된다() {
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

        // when - 자기 자신을 차단하려고 시도
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .post("/admin/accounts/{id}/block", superAdmin.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/accounts"));

        // then - 본인은 여전히 차단되지 않음 (로그인 상태 유지)
        RestAssured.given()
                .cookie("SESSION", sessionId)
                .when().get("/admin/accounts")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("관리자 관리"));
    }

    @Test
    void 마지막_SUPER_ADMIN을_차단하려고_하면_차단이_실패하고_리다이렉트된다() {
        // given - 단 하나의 SUPER_ADMIN만 존재
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin superAdmin = new Admin("super@example.com", "SuperAdmin", encodedPassword, AdminRole.SUPER_ADMIN);
        superAdmin = adminRepository.save(superAdmin);

        // 일반 ADMIN 추가 (SUPER_ADMIN이 아닌)
        Admin normalAdmin = new Admin("normal@example.com", "NormalAdmin", encodedPassword, AdminRole.ADMIN);
        adminRepository.save(normalAdmin);

        String sessionId = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", superAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");

        // when - 마지막 SUPER_ADMIN을 차단하려고 시도
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", sessionId)
                .post("/admin/accounts/{id}/block", superAdmin.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/accounts"));

        // then - 본인은 여전히 차단되지 않음 (로그인 상태 유지)
        RestAssured.given()
                .cookie("SESSION", sessionId)
                .when().get("/admin/accounts")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("관리자 관리"));
    }

    @Test
    void 차단_해제_후_재로그인이_가능하다() {
        // given
        String superAdminSession = loginAsSuperAdmin();

        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin targetAdmin = new Admin("target@example.com", "Target", encodedPassword, AdminRole.ADMIN);
        targetAdmin = adminRepository.save(targetAdmin);

        // 먼저 차단
        RestAssured.given()
                .redirects().follow(false)
                .cookie("SESSION", superAdminSession)
                .post("/admin/accounts/{id}/block", targetAdmin.getId())
                .then()
                .statusCode(HttpStatus.FOUND.value());

        // 차단된 상태에서 로그인 시도 - 실패해야 함 (리다이렉트로 로그인 페이지 유지)
        RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", targetAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));

        // when - 차단 해제
        RestAssured.given()
                .redirects().follow(false)
                .cookie("SESSION", superAdminSession)
                .post("/admin/accounts/{id}/unblock", targetAdmin.getId())
                .then()
                .statusCode(HttpStatus.FOUND.value());

        // then - 재로그인 성공
        RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", targetAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));
    }

    @Test
    void 페이징_동작_검증_21개_이상의_관리자() {
        // given - 21개의 관리자 생성 (기본 페이지 크기 20을 초과)
        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        for (int i = 1; i <= 21; i++) {
            Admin admin = new Admin("admin" + i + "@example.com", "Admin" + i, encodedPassword, AdminRole.ADMIN);
            adminRepository.save(admin);
        }

        String sessionId = loginAsSuperAdmin();

        // when & then - 첫 번째 페이지 (20개)
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .queryParam("page", 0)
                .queryParam("size", 20)
                .when().get("/admin/accounts")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("다음"));

        // when & then - 두 번째 페이지 (나머지)
        RestAssured.given().log().all()
                .cookie("SESSION", sessionId)
                .queryParam("page", 1)
                .queryParam("size", 20)
                .when().get("/admin/accounts")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .body(containsString("이전"));
    }
}
