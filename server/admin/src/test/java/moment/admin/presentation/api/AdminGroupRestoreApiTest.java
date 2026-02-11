package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.service.admin.AdminService;
import moment.admin.service.group.AdminGroupService;
import moment.comment.domain.Comment;
import moment.comment.infrastructure.CommentRepository;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.GroupFixture;
import moment.fixture.UserFixture;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
import moment.moment.domain.Moment;
import moment.moment.infrastructure.MomentRepository;
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
class AdminGroupRestoreApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminGroupService adminGroupService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private CommentRepository commentRepository;

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
    void 그룹_복원_성공() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // 그룹 삭제
        adminGroupService.deleteGroup(group.getId());

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then - 복원되었는지 확인
        Group restoredGroup = groupRepository.findById(group.getId()).orElseThrow();
        assertThat(restoredGroup.getDeletedAt()).isNull();
    }

    @Test
    void 그룹_복원_삭제되지않은_그룹_복원시_400_AG002() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "활성그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("AG-002");
    }

    @Test
    void 그룹_복원_성공_멤버_전체_복원() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User member1 = userRepository.save(UserFixture.createUser());
        User member2 = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));

        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember m1 = GroupMember.createPendingMember(group, member1, "멤버1");
        m1.approve();
        groupMemberRepository.save(m1);
        GroupMember m2 = GroupMember.createPendingMember(group, member2, "멤버2");
        m2.approve();
        groupMemberRepository.save(m2);

        // 그룹 삭제
        adminGroupService.deleteGroup(group.getId());

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then - 멤버 전체 복원 확인
        long activeMemberCount = groupMemberRepository.countByGroupIdAndStatus(group.getId(), MemberStatus.APPROVED);
        assertThat(activeMemberCount).isEqualTo(3);
    }

    @Test
    void 그룹_복원_성공_모멘트_전체_복원() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        Moment moment1 = momentRepository.save(new Moment(owner, group, ownerMember, "모멘트1"));
        Moment moment2 = momentRepository.save(new Moment(owner, group, ownerMember, "모멘트2"));

        Long moment1Id = moment1.getId();
        Long moment2Id = moment2.getId();

        // 그룹 삭제
        adminGroupService.deleteGroup(group.getId());

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then - 모멘트 전체 복원 확인
        assertThat(momentRepository.findById(moment1Id)).isPresent();
        assertThat(momentRepository.findById(moment2Id)).isPresent();
    }

    @Test
    void 그룹_복원_성공_코멘트_전체_복원() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User commenter = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember commenterMember = GroupMember.createPendingMember(group, commenter, "댓글러");
        commenterMember.approve();
        groupMemberRepository.save(commenterMember);

        Moment moment = momentRepository.save(new Moment(owner, group, ownerMember, "모멘트"));
        Comment comment1 = commentRepository.save(new Comment(moment, commenter, commenterMember, "코멘트1"));
        Comment comment2 = commentRepository.save(new Comment(moment, commenter, commenterMember, "코멘트2"));

        Long comment1Id = comment1.getId();
        Long comment2Id = comment2.getId();

        // 그룹 삭제
        adminGroupService.deleteGroup(group.getId());

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value());

        // then - 코멘트 전체 복원 확인
        assertThat(commentRepository.findById(comment1Id)).isPresent();
        assertThat(commentRepository.findById(comment2Id)).isPresent();
    }

    @Test
    void 그룹_복원_그룹없으면_404() {
        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().post("/api/admin/groups/{groupId}/restore", 999999L)
            .then().log().all()
            .statusCode(HttpStatus.NOT_FOUND.value());
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
