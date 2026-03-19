package moment.group.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import moment.auth.service.auth.TokenManager;
import moment.common.DatabaseCleaner;
import moment.config.TestTags;
import moment.fixture.UserFixture;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.ProfileUpdateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.MemberResponse;
import moment.user.domain.User;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberRole;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.group.infrastructure.GroupRepository;
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
class GroupMemberControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @AfterEach
    void tearDown() {
        databaseCleaner.clean();
    }

    @Test
    void 그룹_멤버_목록을_조회한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "그룹장닉네임");

        // when
        List<MemberResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/members", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        // then
        assertAll(
            () -> assertThat(response).hasSize(1),
            () -> assertThat(response.get(0).nickname()).isEqualTo("그룹장닉네임")
        );
    }

    @Test
    void 대기자_목록을_조회한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse group = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // when
        List<MemberResponse> response = RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/pending", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        // then
        assertThat(response).isEmpty();
    }

    @Test
    void 내_프로필을_수정한다() {
        // given
        User user = UserFixture.createUser();
        User savedUser = userRepository.save(user);
        String token = tokenManager.createAccessToken(savedUser.getId(), savedUser.getEmail());

        GroupCreateResponse group = 그룹_생성(token, "테스트 그룹", "설명", "기존닉네임");

        ProfileUpdateRequest updateRequest = new ProfileUpdateRequest("변경된닉네임");

        // when
        RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(updateRequest)
            .when().patch("/api/v2/groups/{groupId}/profile", group.groupId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 목록에서 변경된 닉네임 확인
        List<MemberResponse> members = RestAssured.given()
            .cookie("accessToken", token)
            .when().get("/api/v2/groups/{groupId}/members", group.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        assertThat(members.get(0).nickname()).isEqualTo("변경된닉네임");
    }

    @Test
    void 그룹을_탈퇴한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse groupResponse = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // 일반 멤버 추가 (DB 직접 생성)
        User member = UserFixture.createUserByEmail("member@test.com");
        User savedMember = userRepository.save(member);
        String memberToken = tokenManager.createAccessToken(savedMember.getId(), savedMember.getEmail());

        Group group = groupRepository.findById(groupResponse.groupId()).orElseThrow();
        GroupMember groupMember = new GroupMember(group, savedMember, "멤버닉네임", MemberRole.MEMBER, MemberStatus.APPROVED);
        groupMemberRepository.save(groupMember);

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", memberToken)
            .when().delete("/api/v2/groups/{groupId}/leave", groupResponse.groupId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 목록에서 탈퇴한 멤버가 없어야 함
        List<MemberResponse> members = RestAssured.given()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/members", groupResponse.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        assertThat(members).hasSize(1);
        assertThat(members.get(0).nickname()).isEqualTo("그룹장닉네임");
    }

    @Test
    void 멤버를_강퇴한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse groupResponse = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // 일반 멤버 추가
        User member = UserFixture.createUserByEmail("member@test.com");
        User savedMember = userRepository.save(member);

        Group group = groupRepository.findById(groupResponse.groupId()).orElseThrow();
        GroupMember groupMember = new GroupMember(group, savedMember, "멤버닉네임", MemberRole.MEMBER, MemberStatus.APPROVED);
        GroupMember savedGroupMember = groupMemberRepository.save(groupMember);

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().delete("/api/v2/groups/{groupId}/members/{memberId}", groupResponse.groupId(), savedGroupMember.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 목록에서 강퇴된 멤버가 없어야 함
        List<MemberResponse> members = RestAssured.given()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/members", groupResponse.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        assertThat(members).hasSize(1);
    }

    @Test
    void 가입_신청을_승인한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse groupResponse = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // 대기 멤버 추가
        User pendingUser = UserFixture.createUserByEmail("pending@test.com");
        User savedPendingUser = userRepository.save(pendingUser);

        Group group = groupRepository.findById(groupResponse.groupId()).orElseThrow();
        GroupMember pendingMember = new GroupMember(group, savedPendingUser, "대기멤버", MemberRole.MEMBER, MemberStatus.PENDING);
        GroupMember savedPendingMember = groupMemberRepository.save(pendingMember);

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().post("/api/v2/groups/{groupId}/members/{memberId}/approve", groupResponse.groupId(), savedPendingMember.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 목록에 승인된 멤버가 포함되어야 함
        List<MemberResponse> members = RestAssured.given()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/members", groupResponse.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        assertThat(members).hasSize(2);
    }

    @Test
    void 가입_신청을_거절한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse groupResponse = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // 대기 멤버 추가
        User pendingUser = UserFixture.createUserByEmail("pending@test.com");
        User savedPendingUser = userRepository.save(pendingUser);

        Group group = groupRepository.findById(groupResponse.groupId()).orElseThrow();
        GroupMember pendingMember = new GroupMember(group, savedPendingUser, "대기멤버", MemberRole.MEMBER, MemberStatus.PENDING);
        GroupMember savedPendingMember = groupMemberRepository.save(pendingMember);

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().post("/api/v2/groups/{groupId}/members/{memberId}/reject", groupResponse.groupId(), savedPendingMember.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 대기자 목록이 비어있어야 함
        List<MemberResponse> pendingMembers = RestAssured.given()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/pending", groupResponse.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        assertThat(pendingMembers).isEmpty();
    }

    @Test
    void 소유권을_이전한다() {
        // given
        User owner = UserFixture.createUser();
        User savedOwner = userRepository.save(owner);
        String ownerToken = tokenManager.createAccessToken(savedOwner.getId(), savedOwner.getEmail());

        GroupCreateResponse groupResponse = 그룹_생성(ownerToken, "테스트 그룹", "설명", "그룹장닉네임");

        // 일반 멤버 추가
        User member = UserFixture.createUserByEmail("member@test.com");
        User savedMember = userRepository.save(member);

        Group group = groupRepository.findById(groupResponse.groupId()).orElseThrow();
        GroupMember groupMember = new GroupMember(group, savedMember, "멤버닉네임", MemberRole.MEMBER, MemberStatus.APPROVED);
        GroupMember savedGroupMember = groupMemberRepository.save(groupMember);

        // when
        RestAssured.given().log().all()
            .cookie("accessToken", ownerToken)
            .when().post("/api/v2/groups/{groupId}/transfer/{memberId}", groupResponse.groupId(), savedGroupMember.getId())
            .then().log().all()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // then - 멤버 목록에서 새 오너 확인
        List<MemberResponse> members = RestAssured.given()
            .cookie("accessToken", ownerToken)
            .when().get("/api/v2/groups/{groupId}/members", groupResponse.groupId())
            .then()
            .extract()
            .jsonPath()
            .getList("data", MemberResponse.class);

        MemberResponse newOwner = members.stream()
            .filter(m -> m.nickname().equals("멤버닉네임"))
            .findFirst()
            .orElseThrow();

        assertThat(newOwner.role()).isEqualTo(MemberRole.OWNER);
    }

    private GroupCreateResponse 그룹_생성(String token, String name, String description, String nickname) {
        GroupCreateRequest request = new GroupCreateRequest(name, description, nickname);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .cookie("accessToken", token)
            .body(request)
            .when().post("/api/v2/groups")
            .then()
            .statusCode(HttpStatus.CREATED.value())
            .extract()
            .jsonPath()
            .getObject("data", GroupCreateResponse.class);
    }
}
