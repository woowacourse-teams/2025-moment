package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminGroupListResponse;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupMemberRepository;
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
class AdminGroupListApiTest {

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
    private GroupMemberRepository groupMemberRepository;

    private String sessionCookie;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();

        Admin admin = adminService.createAdmin("admin@test.com", "테스트관리자", "password123!@#");
        sessionCookie = 로그인("admin@test.com", "password123!@#");
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 그룹_목록_조회_성공_페이지네이션_기본값() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group1 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "그룹1", "설명1"));
        Group group2 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "그룹2", "설명2"));

        groupMemberRepository.save(GroupMember.createOwner(group1, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(group2, owner, "닉네임2"));

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(2),
            () -> assertThat(response.page()).isEqualTo(0),
            () -> assertThat(response.size()).isEqualTo(20),
            () -> assertThat(response.totalElements()).isEqualTo(2)
        );
    }

    @Test
    void 그룹_목록_조회_성공_키워드_검색_그룹명() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group1 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "개발자모임", "설명1"));
        Group group2 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "디자이너모임", "설명2"));

        groupMemberRepository.save(GroupMember.createOwner(group1, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(group2, owner, "닉네임2"));

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("keyword", "개발자")
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(1),
            () -> assertThat(response.content().get(0).name()).isEqualTo("개발자모임")
        );
    }

    @Test
    void 그룹_목록_조회_성공_상태필터_ACTIVE() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group activeGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "활성그룹", "설명1"));
        Group deletedGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제그룹", "설명2"));

        groupMemberRepository.save(GroupMember.createOwner(activeGroup, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(deletedGroup, owner, "닉네임2"));

        groupRepository.delete(deletedGroup);

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("status", "ACTIVE")
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(1),
            () -> assertThat(response.content().get(0).name()).isEqualTo("활성그룹"),
            () -> assertThat(response.content().get(0).isDeleted()).isFalse()
        );
    }

    @Test
    void 그룹_목록_조회_성공_상태필터_DELETED() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group activeGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "활성그룹", "설명1"));
        Group deletedGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제그룹", "설명2"));

        groupMemberRepository.save(GroupMember.createOwner(activeGroup, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(deletedGroup, owner, "닉네임2"));

        groupRepository.delete(deletedGroup);

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .queryParam("status", "DELETED")
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then
        assertAll(
            () -> assertThat(response.content()).hasSize(1),
            () -> assertThat(response.content().get(0).name()).isEqualTo("삭제그룹"),
            () -> assertThat(response.content().get(0).isDeleted()).isTrue()
        );
    }

    @Test
    void 그룹_목록_조회_성공_상태필터_ALL_기본값() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group activeGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "활성그룹", "설명1"));
        Group deletedGroup = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제그룹", "설명2"));

        groupMemberRepository.save(GroupMember.createOwner(activeGroup, owner, "닉네임1"));
        groupMemberRepository.save(GroupMember.createOwner(deletedGroup, owner, "닉네임2"));

        groupRepository.delete(deletedGroup);

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then
        assertThat(response.content()).hasSize(2);
    }

    @Test
    void 그룹_목록_조회_성공_정렬_createdAt_DESC() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group1 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "먼저생성", "설명1"));
        groupMemberRepository.save(GroupMember.createOwner(group1, owner, "닉네임1"));

        Group group2 = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "나중생성", "설명2"));
        groupMemberRepository.save(GroupMember.createOwner(group2, owner, "닉네임2"));

        // when
        AdminGroupListResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups")
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupListResponse.class);

        // then - 최신순 정렬이므로 "나중생성"이 먼저
        assertThat(response.content().get(0).name()).isEqualTo("나중생성");
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
