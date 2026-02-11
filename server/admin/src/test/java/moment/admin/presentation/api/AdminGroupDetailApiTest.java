package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.dto.response.AdminGroupDetailResponse;
import moment.admin.service.admin.AdminService;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.GroupInviteLinkFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.infrastructure.GroupInviteLinkRepository;
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
class AdminGroupDetailApiTest {

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

    @Autowired
    private GroupInviteLinkRepository groupInviteLinkRepository;

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
    void 그룹_상세_조회_성공_활성_그룹() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트그룹", "테스트 설명입니다"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // when
        AdminGroupDetailResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupDetailResponse.class);

        // then
        assertAll(
            () -> assertThat(response.groupId()).isEqualTo(group.getId()),
            () -> assertThat(response.name()).isEqualTo("테스트그룹"),
            () -> assertThat(response.description()).isEqualTo("테스트 설명입니다"),
            () -> assertThat(response.isDeleted()).isFalse(),
            () -> assertThat(response.deletedAt()).isNull(),
            () -> assertThat(response.owner()).isNotNull(),
            () -> assertThat(response.owner().nickname()).isEqualTo("방장닉네임")
        );
    }

    @Test
    void 그룹_상세_조회_성공_삭제된_그룹_조회_가능() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "삭제 테스트"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // 그룹 삭제 (Soft Delete)
        groupRepository.delete(group);

        // when
        AdminGroupDetailResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupDetailResponse.class);

        // then
        assertAll(
            () -> assertThat(response.groupId()).isEqualTo(group.getId()),
            () -> assertThat(response.name()).isEqualTo("삭제될그룹"),
            () -> assertThat(response.isDeleted()).isTrue(),
            () -> assertThat(response.deletedAt()).isNotNull()
        );
    }

    @Test
    void 그룹_상세_조회_성공_멤버_수_표시() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User member1 = userRepository.save(UserFixture.createUser());
        User member2 = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "멤버테스트", "설명"));

        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember m1 = GroupMember.createPendingMember(group, member1, "멤버1");
        m1.approve();
        groupMemberRepository.save(m1);
        GroupMember m2 = GroupMember.createPendingMember(group, member2, "멤버2");
        m2.approve();
        groupMemberRepository.save(m2);

        // when
        AdminGroupDetailResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupDetailResponse.class);

        // then
        assertThat(response.memberCount()).isEqualTo(3);
    }

    @Test
    void 그룹_상세_조회_성공_대기중_멤버_수_표시() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User pending1 = userRepository.save(UserFixture.createUser());
        User pending2 = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "대기멤버테스트", "설명"));

        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        groupMemberRepository.save(GroupMember.createPendingMember(group, pending1, "대기1"));
        groupMemberRepository.save(GroupMember.createPendingMember(group, pending2, "대기2"));

        // when
        AdminGroupDetailResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupDetailResponse.class);

        // then
        assertAll(
            () -> assertThat(response.memberCount()).isEqualTo(1),  // owner만
            () -> assertThat(response.pendingMemberCount()).isEqualTo(2)
        );
    }

    @Test
    void 그룹_상세_조회_성공_초대링크_정보_포함() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "초대링크테스트", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        GroupInviteLink inviteLink = GroupInviteLinkFixture.createValidLink(group);
        groupInviteLinkRepository.save(inviteLink);

        // when
        AdminGroupDetailResponse response = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getObject("data", AdminGroupDetailResponse.class);

        // then
        assertAll(
            () -> assertThat(response.inviteLink()).isNotNull(),
            () -> assertThat(response.inviteLink().code()).isEqualTo(inviteLink.getCode())
        );
    }

    @Test
    void 그룹_상세_조회_실패_존재하지_않는_그룹() {
        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().get("/api/admin/groups/{groupId}", 999999L)
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void 그룹_상세_조회_실패_인증없이_접근() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "테스트", "설명"));

        // when & then
        RestAssured.given().log().all()
            .when().get("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
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
