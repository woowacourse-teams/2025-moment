package moment.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.admin.domain.Admin;
import moment.admin.infrastructure.AdminRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.AdminFixture;
import moment.fixture.UserFixture;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
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
import org.springframework.test.context.ActiveProfiles;

@Tag(TestTags.E2E)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class AdminUserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private String adminSessionId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        String rawPassword = "password123!@#";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        Admin temp = AdminFixture.createAdmin();
        Admin admin = new Admin(temp.getEmail(), temp.getName(), encodedPassword, temp.getRole());
        Admin savedAdmin = adminRepository.save(admin);

        adminSessionId = RestAssured.given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("email", savedAdmin.getEmail())
                .formParam("password", rawPassword)
                .when().post("/admin/login")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .extract().cookie("SESSION");
    }

    @AfterEach
    void down() {
        databaseCleaner.clean();
    }

    @Test
    void 사용자_목록을_조회한다() {
        // given
        List<User> users = UserFixture.createUsersByAmount(5);
        userRepository.saveAll(users);

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 사용자_목록을_페이지와_크기를_지정하여_조회한다() {
        // given
        List<User> users = UserFixture.createUsersByAmount(25);
        userRepository.saveAll(users);

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .param("page", "1")
                .param("size", "10")
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 사용자가_없을_때_빈_목록을_조회한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 세션_없이_사용자_목록_조회_시_권한_없음_예외가_발생한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .when().get("/admin/users")
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }

    @Test
    void 사용자_수정_페이지를_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().get("/admin/users/{id}/edit", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 존재하지_않는_사용자_수정_페이지_조회_시_에러_페이지로_이동한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().get("/admin/users/{id}/edit", 999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 사용자_정보를_수정한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        String newNickname = "updatedNickname";

        // when
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .contentType(ContentType.URLENC)
                .formParam("nickname", newNickname)
                .when().post("/admin/users/{id}/edit", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
    }

    @Test
    void 사용자_정보_수정_시_닉네임을_변경한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        String newNickname = "newNickname";

        // when
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .contentType(ContentType.URLENC)
                .formParam("nickname", newNickname)
                .when().post("/admin/users/{id}/edit", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
    }

    @Test
    void 빈_닉네임으로_사용자_수정_시_검증_에러가_발생한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .contentType(ContentType.URLENC)
                .formParam("nickname", "")
                .when().post("/admin/users/{id}/edit", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 존재하지_않는_사용자_수정_시_에러_페이지로_이동한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .contentType(ContentType.URLENC)
                .formParam("nickname", "nickname")
                .when().post("/admin/users/{id}/edit", 999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .contentType(ContentType.HTML);
    }

    @Test
    void 사용자를_차단한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().post("/admin/users/{id}/delete", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));

        // then
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void 존재하지_않는_사용자_차단_시_예외가_발생한다() {
        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .cookie("SESSION", adminSessionId)
                .when().post("/admin/users/{id}/delete", 999L)
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/users"));
    }

    @Test
    void 세션_없이_사용자_차단_시_권한_없음_예외가_발생한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);

        // when & then
        RestAssured.given().log().all()
                .redirects().follow(false)
                .when().post("/admin/users/{id}/delete", savedUser.getId())
                .then().log().all()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", containsString("/admin/login"));
    }
}
