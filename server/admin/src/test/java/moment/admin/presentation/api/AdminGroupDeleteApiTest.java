package moment.admin.presentation.api;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import moment.admin.domain.Admin;
import moment.admin.dto.request.AdminLoginRequest;
import moment.admin.service.admin.AdminService;
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
class AdminGroupDeleteApiTest {

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
    void 그룹_삭제_성공_SoftDelete() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 삭제되었지만 includingDeleted로 조회 가능
        Group deletedGroup = groupRepository.findByIdIncludingDeleted(group.getId()).orElseThrow();
        assertThat(deletedGroup.getDeletedAt()).isNotNull();
    }

    @Test
    void 그룹_삭제_이미_삭제된_그룹_삭제시_400_AG003() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장닉네임"));

        // 이미 삭제
        groupRepository.delete(group);

        // when & then
        String errorCode = RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract()
            .jsonPath()
            .getString("code");

        assertThat(errorCode).isEqualTo("AG-003");
    }

    @Test
    void 그룹_삭제_성공_멤버_전체_SoftDelete() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        User member1 = userRepository.save(UserFixture.createUser());
        User member2 = userRepository.save(UserFixture.createUser());

        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));

        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));
        GroupMember m1 = GroupMember.createPendingMember(group, member1, "멤버1");
        m1.approve();
        groupMemberRepository.save(m1);
        GroupMember m2 = GroupMember.createPendingMember(group, member2, "멤버2");
        m2.approve();
        groupMemberRepository.save(m2);

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 전체 삭제 확인 (SQLRestriction으로 deleted_at IS NULL 조건이 적용됨)
        long activeMemberCount = groupMemberRepository.countByGroupIdAndStatus(group.getId(), MemberStatus.APPROVED);
        assertThat(activeMemberCount).isZero();
    }

    @Test
    void 그룹_삭제_성공_모멘트_전체_SoftDelete() {
        // given
        User owner = userRepository.save(UserFixture.createUser());
        Group group = groupRepository.save(GroupFixture.createGroupWithNameAndDescription(owner, "삭제될그룹", "설명"));
        GroupMember ownerMember = groupMemberRepository.save(GroupMember.createOwner(group, owner, "방장"));

        // 그룹 모멘트 생성
        Moment moment1 = momentRepository.save(new Moment(owner, group, ownerMember, "모멘트1"));
        Moment moment2 = momentRepository.save(new Moment(owner, group, ownerMember, "모멘트2"));

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 모멘트 전체 삭제 확인 (SQLRestriction으로 deleted_at IS NULL 조건 적용됨)
        // 삭제 후 조회하면 빈 리스트가 반환되어야 함
        assertThat(momentRepository.findById(moment1.getId())).isEmpty();
        assertThat(momentRepository.findById(moment2.getId())).isEmpty();
    }

    @Test
    void 그룹_삭제_성공_코멘트_전체_SoftDelete() {
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

        // when
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", group.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 코멘트 전체 삭제 확인 (SQLRestriction으로 deleted_at IS NULL 조건 적용됨)
        assertThat(commentRepository.findById(comment1.getId())).isEmpty();
        assertThat(commentRepository.findById(comment2.getId())).isEmpty();
    }

    @Test
    void 그룹_삭제_그룹없으면_404() {
        // when & then
        RestAssured.given().log().all()
            .cookie("SESSION", sessionCookie)
            .when().delete("/api/admin/groups/{groupId}", 999999L)
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
