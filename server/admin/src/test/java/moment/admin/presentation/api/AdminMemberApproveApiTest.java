package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
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
class AdminMemberApproveApiTest {

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
    class 멤버_승인 {

        @Test
        void 멤버_승인_성공_PENDING에서_APPROVED로_변경() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User pendingUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember pendingMember = groupMemberRepository.save(GroupMember.createPendingMember(group, pendingUser, "대기멤버"));

            // when
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/approve", group.getId(), pendingMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.OK.value());

            // then
            GroupMember approvedMember = groupMemberRepository.findById(pendingMember.getId()).orElseThrow();
            assertThat(approvedMember.getStatus()).isEqualTo(MemberStatus.APPROVED);
        }

        @Test
        void 멤버_승인_이미_승인된_멤버_승인시_400_AM006() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User approvedUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, approvedUser, "멤버");
            member.approve();
            GroupMember approvedMember = groupMemberRepository.save(member);

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/approve", group.getId(), approvedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 멤버_승인_PENDING_아닌_멤버_승인시_400_AM003() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            User kickedUser = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
            GroupMember member = GroupMember.createPendingMember(group, kickedUser, "멤버");
            member.approve();
            member.kick();
            GroupMember kickedMember = groupMemberRepository.save(member);

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/approve", group.getId(), kickedMember.getId())
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
        }

        @Test
        void 멤버_승인_멤버없으면_404_AM001() {
            // given
            User owner = userRepository.save(UserFixture.createUser());
            Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "설명"));
            groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/approve", group.getId(), 999999L)
                .then().log().all()
                .statusCode(HttpStatus.NOT_FOUND.value());
        }

        @Test
        void 멤버_승인_그룹없으면_404_AG001() {
            // when & then
            RestAssured.given().log().all()
                .cookie("SESSION", sessionCookie)
                .when().post("/api/admin/groups/{groupId}/members/{memberId}/approve", 999999L, 1L)
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
