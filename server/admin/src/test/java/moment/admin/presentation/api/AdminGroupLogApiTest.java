package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminGroupLog;
import moment.admin.domain.AdminGroupLogType;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminGroupLogListResponse;
import moment.admin.infrastructure.AdminGroupLogRepository;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
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
class AdminGroupLogApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private AdminGroupLogRepository adminGroupLogRepository;

    private String sessionCookie;
    private Admin admin;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        admin = adminService.createAdmin("admin@test.com", "테스트관리자", "password123!@#");
        sessionCookie = 로그인("admin@test.com", "password123!@#");
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void Admin로그_조회_성공_그룹별_로그_반환() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.GROUP_UPDATE)
            .groupId(group.getId())
            .description("그룹 정보 수정")
            .build());

        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.MEMBER_KICK)
            .groupId(group.getId())
            .targetId(100L)
            .description("멤버 추방")
            .build());

        // when
        AdminGroupLogListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("groupId", group.getId())
            .when().get("/api/admin/groups/logs")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupLogListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(2),
            () -> assertThat(response.content()).allMatch(log -> log.groupId().equals(group.getId()))
        );
    }

    @Test
    void Admin로그_조회_성공_페이지네이션_적용() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

        for (int i = 0; i < 25; i++) {
            adminGroupLogRepository.save(AdminGroupLog.builder()
                .adminId(admin.getId())
                .adminEmail(admin.getEmail())
                .type(AdminGroupLogType.GROUP_UPDATE)
                .groupId(group.getId())
                .description("로그 " + i)
                .build());
        }

        // when
        AdminGroupLogListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when().get("/api/admin/groups/logs")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupLogListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(10),
            () -> assertThat(response.page()).isEqualTo(0),
            () -> assertThat(response.size()).isEqualTo(10),
            () -> assertThat(response.totalElements()).isEqualTo(25),
            () -> assertThat(response.totalPages()).isEqualTo(3)
        );
    }

    @Test
    void Admin로그_조회_성공_정렬_createdAt_DESC() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

        AdminGroupLog log1 = adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.GROUP_UPDATE)
            .groupId(group.getId())
            .description("첫번째 로그")
            .build());

        AdminGroupLog log2 = adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.GROUP_DELETE)
            .groupId(group.getId())
            .description("두번째 로그")
            .build());

        // when
        AdminGroupLogListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/logs")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupLogListResponse.class);

        // then - 최신순 정렬이므로 log2가 먼저
        assertThat(response.content().get(0).id()).isEqualTo(log2.getId());
        assertThat(response.content().get(1).id()).isEqualTo(log1.getId());
    }

    @Test
    void Admin로그_조회_성공_로그타입_필터() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.GROUP_UPDATE)
            .groupId(group.getId())
            .description("업데이트 로그")
            .build());

        adminGroupLogRepository.save(AdminGroupLog.builder()
            .adminId(admin.getId())
            .adminEmail(admin.getEmail())
            .type(AdminGroupLogType.MEMBER_KICK)
            .groupId(group.getId())
            .description("추방 로그")
            .build());

        // when
        AdminGroupLogListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("type", "GROUP_UPDATE")
            .when().get("/api/admin/groups/logs")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupLogListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(1),
            () -> assertThat(response.content().get(0).type()).isEqualTo("GROUP_UPDATE")
        );
    }

    private String 로그인(String email, String password) {
        AdminLoginRequest loginRequest = new AdminLoginRequest(email, password);

        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
            .when().post("/api/admin/auth/login")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .cookie("SESSION");
    }
}
