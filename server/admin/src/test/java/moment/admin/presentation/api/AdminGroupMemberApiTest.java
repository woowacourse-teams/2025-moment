package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminGroupMemberListResponse;
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
import org.junit.jupiter.api.Nested;
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
class AdminGroupMemberApiTest {

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

    @Nested
    class 승인된_멤버_목록_조회 {

        @Test
        void 승인된_멤버_목록_조회_성공() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User member1 = userRepository.save(UserFixture.createUser());
            User member2 = userRepository.save(UserFixture.createUser());

            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember m1 = GroupMember.createPendingMember(group, member1, "멤버1");
            m1.approve();
            groupMemberRepository.save(m1);
            GroupMember m2 = GroupMember.createPendingMember(group, member2, "멤버2");
            m2.approve();
            groupMemberRepository.save(m2);

            // when
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(3),
                () -> assertThat(response.page()).isEqualTo(0),
                () -> assertThat(response.totalElements()).isEqualTo(3)
            );
        }

        @Test
        void 승인된_멤버_목록_조회_성공_페이지네이션() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // 추가 멤버 생성 (5명)
            for (int i = 1; i <= 5; i++) {
                User member = userRepository.save(UserFixture.createUser());
                GroupMember gm = GroupMember.createPendingMember(group, member, "멤버" + i);
                gm.approve();
                groupMemberRepository.save(gm);
            }

            // when - size=3으로 첫 페이지 조회
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .queryParam("size", 3)
                .when().get("/api/admin/groups/{groupId}/members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(3),
                () -> assertThat(response.totalElements()).isEqualTo(6), // owner + 5명
                () -> assertThat(response.totalPages()).isEqualTo(2)
            );
        }

        @Test
        void 승인된_멤버_목록_조회_성공_멤버_정보_포함() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

            // when
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(1),
                () -> assertThat(response.content().get(0).nickname()).isEqualTo("방장닉네임"),
                () -> assertThat(response.content().get(0).role()).isEqualTo("OWNER"),
                () -> assertThat(response.content().get(0).status()).isEqualTo("APPROVED")
            );
        }

        @Test
        void 승인된_멤버_목록_조회_실패_존재하지_않는_그룹() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/members", 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 승인된_멤버_목록_조회_실패_인증없이_접근() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트", "설명"));

            // when & then
            RestAssured.given().log().all()
                .when().get("/api/admin/groups/{groupId}/members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.UNAUTHORIZED.value());
        }
    }

    @Nested
    class 대기중_멤버_목록_조회 {

        @Test
        void 대기중_멤버_목록_조회_성공() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pending1 = userRepository.save(UserFixture.createUser());
            User pending2 = userRepository.save(UserFixture.createUser());

            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            groupMemberRepository.save(GroupMember.createPendingMember(group, pending1, "대기1"));
            groupMemberRepository.save(GroupMember.createPendingMember(group, pending2, "대기2"));

            // when
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/pending-members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(2),
                () -> assertThat(response.totalElements()).isEqualTo(2)
            );
        }

        @Test
        void 대기중_멤버_목록_조회_성공_멤버_정보_포함() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pending = userRepository.save(UserFixture.createUser());

            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));

            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            groupMemberRepository.save(GroupMember.createPendingMember(group, pending, "대기닉네임"));

            // when
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/pending-members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).hasSize(1),
                () -> assertThat(response.content().get(0).nickname()).isEqualTo("대기닉네임"),
                () -> assertThat(response.content().get(0).role()).isEqualTo("MEMBER"),
                () -> assertThat(response.content().get(0).status()).isEqualTo("PENDING")
            );
        }

        @Test
        void 대기중_멤버_목록_조회_성공_대기멤버_없을때_빈_목록() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when
            AdminGroupMemberListResponse response = RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/pending-members", group.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getObject("data", AdminGroupMemberListResponse.class);

            // then
            assertAll(
                () -> assertThat(response.content()).isEmpty(),
                () -> assertThat(response.totalElements()).isEqualTo(0)
            );
        }

        @Test
        void 대기중_멤버_목록_조회_실패_존재하지_않는_그룹() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().get("/api/admin/groups/{groupId}/pending-members", 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }
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
