# Phase 4: API 구현 (Day 8-9)

## 개요
- **목표**: 그룹, 멤버, 콘텐츠 REST API 구현
- **원칙**: TDD 기반 (E2E 테스트 → Controller → DTO)
- **API 버전**: `/api/v2/` (기존 v1과 분리)
- **검증**: 각 Step 후 `./gradlew e2eTest`

---

## API 엔드포인트 요약

### 그룹 CRUD (5개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups` | 그룹 생성 |
| GET | `/api/v2/groups` | 내 그룹 목록 |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 수정 |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 |

### 초대/멤버 (11개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups/{groupId}/invite` | 초대 링크 생성 |
| GET | `/api/v2/invite/{code}` | 초대 정보 조회 |
| POST | `/api/v2/groups/join` | 가입 신청 |
| GET | `/api/v2/groups/{groupId}/members` | 멤버 목록 |
| GET | `/api/v2/groups/{groupId}/pending` | 대기자 목록 |
| POST | `/api/v2/groups/{groupId}/members/{id}/approve` | 멤버 승인 |
| POST | `/api/v2/groups/{groupId}/members/{id}/reject` | 멤버 거절 |
| DELETE | `/api/v2/groups/{groupId}/members/{id}` | 멤버 강퇴 |
| DELETE | `/api/v2/groups/{groupId}/leave` | 그룹 탈퇴 |
| POST | `/api/v2/groups/{groupId}/transfer/{memberId}` | 소유권 이전 |
| PATCH | `/api/v2/groups/{groupId}/profile` | 내 프로필 수정 |

### 콘텐츠 (10개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups/{groupId}/moments` | 모멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments` | 그룹 피드 |
| GET | `/api/v2/groups/{groupId}/my-moments` | 나의 모음집 |
| GET | `/api/v2/groups/{groupId}/moments/{id}` | 모멘트 상세 |
| DELETE | `/api/v2/groups/{groupId}/moments/{id}` | 모멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/moments/{id}/comments` | 코멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments/{id}/comments` | 코멘트 목록 |
| DELETE | `/api/v2/groups/{groupId}/comments/{id}` | 코멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/moments/{id}/like` | 모멘트 좋아요 |
| POST | `/api/v2/groups/{groupId}/comments/{id}/like` | 코멘트 좋아요 |

---

## Step 1: DTO 생성

### 1.1 Group Request DTOs

#### `src/main/java/moment/group/dto/request/GroupCreateRequest.java`
```java
package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다")
    String name,

    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다")
    String description,

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    String ownerNickname
) {}
```

#### `src/main/java/moment/group/dto/request/GroupUpdateRequest.java`
```java
package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupUpdateRequest(
    @NotBlank(message = "그룹 이름은 필수입니다")
    @Size(max = 50, message = "그룹 이름은 50자 이하여야 합니다")
    String name,

    @Size(max = 200, message = "그룹 설명은 200자 이하여야 합니다")
    String description
) {}
```

#### `src/main/java/moment/group/dto/request/GroupJoinRequest.java`
```java
package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupJoinRequest(
    @NotBlank(message = "초대 코드는 필수입니다")
    String inviteCode,

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    String nickname
) {}
```

#### `src/main/java/moment/group/dto/request/ProfileUpdateRequest.java`
```java
package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    String nickname
) {}
```

### 1.2 Group Response DTOs

#### `src/main/java/moment/group/dto/response/GroupCreateResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;

public record GroupCreateResponse(
    Long groupId,
    String name,
    String description,
    Long memberId,
    String nickname,
    String inviteCode
) {
    public static GroupCreateResponse from(Group group, GroupMember member, GroupInviteLink link) {
        return new GroupCreateResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            member.getId(),
            member.getNickname(),
            link.getCode()
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/GroupDetailResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.Group;
import moment.group.domain.GroupMember;

import java.time.LocalDateTime;
import java.util.List;

public record GroupDetailResponse(
    Long id,
    String name,
    String description,
    Long ownerId,
    String ownerNickname,
    Long myMemberId,
    String myNickname,
    boolean isOwner,
    long memberCount,
    List<MemberResponse> members,
    LocalDateTime createdAt
) {
    public static GroupDetailResponse from(Group group, GroupMember myMembership,
                                            List<GroupMember> members, long memberCount) {
        GroupMember owner = members.stream()
            .filter(GroupMember::isOwner)
            .findFirst()
            .orElse(null);

        return new GroupDetailResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            owner != null ? owner.getId() : null,
            owner != null ? owner.getNickname() : null,
            myMembership.getId(),
            myMembership.getNickname(),
            myMembership.isOwner(),
            memberCount,
            members.stream().map(MemberResponse::from).toList(),
            group.getCreatedAt()
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/MyGroupResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.Group;
import moment.group.domain.GroupMember;

public record MyGroupResponse(
    Long groupId,
    String name,
    String description,
    Long memberId,
    String nickname,
    boolean isOwner,
    long memberCount
) {
    public static MyGroupResponse from(Group group, GroupMember membership, long memberCount) {
        return new MyGroupResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            membership.getId(),
            membership.getNickname(),
            membership.isOwner(),
            memberCount
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/GroupJoinResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.GroupMember;

public record GroupJoinResponse(
    Long groupId,
    Long memberId,
    String nickname,
    String status
) {
    public static GroupJoinResponse from(GroupMember member) {
        return new GroupJoinResponse(
            member.getGroup().getId(),
            member.getId(),
            member.getNickname(),
            member.getStatus().name()
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/MemberResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.GroupMember;

import java.time.LocalDateTime;

public record MemberResponse(
    Long id,
    Long userId,
    String nickname,
    String role,
    String status,
    LocalDateTime joinedAt
) {
    public static MemberResponse from(GroupMember member) {
        return new MemberResponse(
            member.getId(),
            member.getUser().getId(),
            member.getNickname(),
            member.getRole().name(),
            member.getStatus().name(),
            member.getCreatedAt()
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/InviteInfoResponse.java`
```java
package moment.group.dto.response;

import moment.group.domain.Group;

public record InviteInfoResponse(
    Long groupId,
    String groupName,
    String groupDescription,
    long memberCount
) {
    public static InviteInfoResponse from(Group group, long memberCount) {
        return new InviteInfoResponse(
            group.getId(),
            group.getName(),
            group.getDescription(),
            memberCount
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/InviteCreateResponse.java`
```java
package moment.group.dto.response;

public record InviteCreateResponse(
    String inviteCode,
    String inviteUrl
) {
    public static InviteCreateResponse from(String code, String baseUrl) {
        return new InviteCreateResponse(
            code,
            baseUrl + "/invite/" + code
        );
    }
}
```

### 1.3 Content DTOs

#### `src/main/java/moment/group/dto/request/GroupMomentCreateRequest.java`
```java
package moment.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GroupMomentCreateRequest(
    @NotBlank(message = "내용은 필수입니다")
    @Size(max = 500, message = "내용은 500자 이하여야 합니다")
    String content,

    List<String> imageUrls
) {}
```

#### `src/main/java/moment/group/dto/response/GroupMomentResponse.java`
```java
package moment.group.dto.response;

import moment.moment.domain.Moment;

import java.time.LocalDateTime;
import java.util.List;

public record GroupMomentResponse(
    Long id,
    String content,
    Long memberId,
    String memberNickname,
    List<String> imageUrls,
    long likeCount,
    boolean hasLiked,
    long commentCount,
    LocalDateTime createdAt
) {
    public static GroupMomentResponse from(Moment moment, long likeCount,
                                            boolean hasLiked, long commentCount) {
        List<String> imageUrls = moment.getMomentImages().stream()
            .map(img -> img.getImageUrl())
            .toList();

        return new GroupMomentResponse(
            moment.getId(),
            moment.getContent(),
            moment.getMember().getId(),
            moment.getMember().getNickname(),
            imageUrls,
            likeCount,
            hasLiked,
            commentCount,
            moment.getCreatedAt()
        );
    }
}
```

#### `src/main/java/moment/group/dto/response/GroupFeedResponse.java`
```java
package moment.group.dto.response;

import java.util.List;

public record GroupFeedResponse(
    List<GroupMomentResponse> moments,
    Long nextCursor
) {}
```

#### `src/main/java/moment/group/dto/response/LikeToggleResponse.java`
```java
package moment.group.dto.response;

public record LikeToggleResponse(
    boolean isLiked,
    long likeCount
) {}
```

### 1.4 검증
```bash
./gradlew compileJava
```

---

## Step 2: GroupController 구현

### 2.1 대상 파일

#### `src/main/java/moment/group/presentation/GroupController.java`
```java
package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.LoginUser;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.request.GroupUpdateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.application.GroupApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupApplicationService groupApplicationService;

    @PostMapping
    public ResponseEntity<GroupCreateResponse> createGroup(
            @LoginUser Long userId,
            @Valid @RequestBody GroupCreateRequest request) {
        GroupCreateResponse response = groupApplicationService.createGroup(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MyGroupResponse>> getMyGroups(@LoginUser Long userId) {
        List<MyGroupResponse> response = groupApplicationService.getMyGroups(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        GroupDetailResponse response = groupApplicationService.getGroupDetail(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(
            @PathVariable Long groupId,
            @LoginUser Long userId,
            @Valid @RequestBody GroupUpdateRequest request) {
        groupApplicationService.updateGroup(groupId, userId, request.name(), request.description());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        groupApplicationService.deleteGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

### 2.2 TDD E2E 테스트 케이스
```java
@Tag("e2e")
class GroupControllerTest extends AcceptanceTest {

    @Test
    void 그룹_생성_성공() {
        // Given
        String token = 로그인("user@example.com");
        GroupCreateRequest request = new GroupCreateRequest("테스트 그룹", "설명", "닉네임");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v2/groups");

        // Then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("name")).isEqualTo("테스트 그룹");
        assertThat(response.jsonPath().getString("inviteCode")).isNotBlank();
    }

    @Test
    void 내_그룹_목록_조회() {
        // Given
        String token = 로그인("user@example.com");
        그룹_생성(token, "그룹1");
        그룹_생성(token, "그룹2");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .get("/api/v2/groups");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).hasSize(2);
    }

    @Test
    void 그룹_상세_조회() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .get("/api/v2/groups/" + groupId);

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("name")).isEqualTo("테스트 그룹");
        assertThat(response.jsonPath().getBoolean("isOwner")).isTrue();
    }

    @Test
    void 그룹_수정_소유자만_가능() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        멤버_가입_및_승인(groupId, memberToken, ownerToken);

        GroupUpdateRequest request = new GroupUpdateRequest("새 이름", "새 설명");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + memberToken)
            .contentType(ContentType.JSON)
            .body(request)
            .patch("/api/v2/groups/" + groupId);

        // Then
        assertThat(response.statusCode()).isEqualTo(403);  // NOT_GROUP_OWNER
    }

    @Test
    void 그룹_삭제_멤버_있으면_실패() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        멤버_가입_및_승인(groupId, memberToken, ownerToken);

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + ownerToken)
            .delete("/api/v2/groups/" + groupId);

        // Then
        assertThat(response.statusCode()).isEqualTo(400);  // CANNOT_DELETE_GROUP_WITH_MEMBERS
    }
}
```

### 2.3 검증
```bash
./gradlew e2eTest --tests "GroupControllerTest"
```

---

## Step 3: GroupMemberController 구현

### 3.1 대상 파일

#### `src/main/java/moment/group/presentation/GroupMemberController.java`
```java
package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.LoginUser;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.request.ProfileUpdateRequest;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.MemberResponse;
import moment.group.service.application.GroupMemberApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/groups")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberApplicationService memberApplicationService;

    @PostMapping("/join")
    public ResponseEntity<GroupJoinResponse> joinGroup(
            @LoginUser Long userId,
            @Valid @RequestBody GroupJoinRequest request) {
        GroupJoinResponse response = memberApplicationService.joinGroup(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<MemberResponse>> getMembers(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        List<MemberResponse> response = memberApplicationService.getMembers(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/pending")
    public ResponseEntity<List<MemberResponse>> getPendingMembers(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        List<MemberResponse> response = memberApplicationService.getPendingMembers(groupId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/members/{memberId}/approve")
    public ResponseEntity<Void> approveMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @LoginUser Long userId) {
        memberApplicationService.approveMember(groupId, memberId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/members/{memberId}/reject")
    public ResponseEntity<Void> rejectMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @LoginUser Long userId) {
        memberApplicationService.rejectMember(groupId, memberId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @LoginUser Long userId) {
        memberApplicationService.kickMember(groupId, memberId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        memberApplicationService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/transfer/{memberId}")
    public ResponseEntity<Void> transferOwnership(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @LoginUser Long userId) {
        memberApplicationService.transferOwnership(groupId, userId, memberId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupId}/profile")
    public ResponseEntity<Void> updateProfile(
            @PathVariable Long groupId,
            @LoginUser Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        memberApplicationService.updateProfile(groupId, userId, request.nickname());
        return ResponseEntity.ok().build();
    }
}
```

### 3.2 TDD E2E 테스트 케이스
```java
@Tag("e2e")
class GroupMemberControllerTest extends AcceptanceTest {

    @Test
    void 그룹_가입_신청() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        String inviteCode = 초대_코드_조회(ownerToken, groupId);

        GroupJoinRequest request = new GroupJoinRequest(inviteCode, "새멤버");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + memberToken)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v2/groups/join");

        // Then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("status")).isEqualTo("PENDING");
    }

    @Test
    void 멤버_승인() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long memberId = 가입_신청_후_멤버ID_반환(memberToken, groupId);

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + ownerToken)
            .post("/api/v2/groups/" + groupId + "/members/" + memberId + "/approve");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void 멤버_강퇴() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long memberId = 멤버_가입_및_승인(groupId, memberToken, ownerToken);

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + ownerToken)
            .delete("/api/v2/groups/" + groupId + "/members/" + memberId);

        // Then
        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    void 소유자_강퇴_불가() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long ownerMemberId = 소유자_멤버ID_조회(ownerToken, groupId);

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + ownerToken)
            .delete("/api/v2/groups/" + groupId + "/members/" + ownerMemberId);

        // Then
        assertThat(response.statusCode()).isEqualTo(400);  // CANNOT_KICK_OWNER
    }

    @Test
    void 소유권_이전() {
        // Given
        String ownerToken = 로그인("owner@example.com");
        String memberToken = 로그인("member@example.com");
        Long groupId = 그룹_생성_후_ID_반환(ownerToken, "테스트 그룹");
        Long newOwnerMemberId = 멤버_가입_및_승인(groupId, memberToken, ownerToken);

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + ownerToken)
            .post("/api/v2/groups/" + groupId + "/transfer/" + newOwnerMemberId);

        // Then
        assertThat(response.statusCode()).isEqualTo(200);

        // 새 소유자 확인
        ExtractableResponse<Response> detailResponse = given()
            .header("Authorization", "Bearer " + memberToken)
            .get("/api/v2/groups/" + groupId);

        assertThat(detailResponse.jsonPath().getBoolean("isOwner")).isTrue();
    }
}
```

### 3.3 검증
```bash
./gradlew e2eTest --tests "GroupMemberControllerTest"
```

---

## Step 4: GroupInviteController 구현

### 4.1 대상 파일

#### `src/main/java/moment/group/presentation/GroupInviteController.java`
```java
package moment.group.presentation;

import lombok.RequiredArgsConstructor;
import moment.auth.domain.LoginUser;
import moment.group.dto.response.InviteCreateResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.service.application.GroupMemberApplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GroupInviteController {

    private final GroupMemberApplicationService memberApplicationService;

    @Value("${app.base-url:https://moment.app}")
    private String baseUrl;

    @PostMapping("/api/v2/groups/{groupId}/invite")
    public ResponseEntity<InviteCreateResponse> createInviteLink(
            @PathVariable Long groupId,
            @LoginUser Long userId) {
        String code = memberApplicationService.createInviteLink(groupId, userId);
        InviteCreateResponse response = InviteCreateResponse.from(code, baseUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v2/invite/{code}")
    public ResponseEntity<InviteInfoResponse> getInviteInfo(@PathVariable String code) {
        InviteInfoResponse response = memberApplicationService.getInviteInfo(code);
        return ResponseEntity.ok(response);
    }
}
```

### 4.2 TDD E2E 테스트 케이스
```java
@Tag("e2e")
class GroupInviteControllerTest extends AcceptanceTest {

    @Test
    void 초대_링크_생성() {
        // Given
        String token = 로그인("owner@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .post("/api/v2/groups/" + groupId + "/invite");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("inviteCode")).isNotBlank();
        assertThat(response.jsonPath().getString("inviteUrl")).contains("/invite/");
    }

    @Test
    void 초대_정보_조회() {
        // Given
        String token = 로그인("owner@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        String inviteCode = 초대_링크_생성(token, groupId);

        // When (인증 없이 조회 가능)
        ExtractableResponse<Response> response = given()
            .get("/api/v2/invite/" + inviteCode);

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("groupName")).isEqualTo("테스트 그룹");
    }

    @Test
    void 만료된_초대_링크_조회_실패() {
        // Given
        String expiredCode = "expired-invite-code";

        // When
        ExtractableResponse<Response> response = given()
            .get("/api/v2/invite/" + expiredCode);

        // Then
        assertThat(response.statusCode()).isEqualTo(400);  // INVITE_LINK_INVALID or EXPIRED
    }
}
```

### 4.3 검증
```bash
./gradlew e2eTest --tests "GroupInviteControllerTest"
```

---

## Step 5: GroupMomentController 구현

### 5.1 대상 파일

#### `src/main/java/moment/group/presentation/GroupMomentController.java`
```java
package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.LoginUser;
import moment.group.dto.request.GroupMomentCreateRequest;
import moment.group.dto.response.GroupFeedResponse;
import moment.group.dto.response.GroupMomentResponse;
import moment.group.dto.response.LikeToggleResponse;
import moment.moment.service.application.MomentApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/groups/{groupId}/moments")
@RequiredArgsConstructor
public class GroupMomentController {

    private final MomentApplicationService momentApplicationService;

    @PostMapping
    public ResponseEntity<GroupMomentResponse> createMoment(
            @PathVariable Long groupId,
            @LoginUser Long userId,
            @Valid @RequestBody GroupMomentCreateRequest request) {
        GroupMomentResponse response = momentApplicationService.createMomentInGroup(
            groupId, userId, request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<GroupFeedResponse> getGroupFeed(
            @PathVariable Long groupId,
            @LoginUser Long userId,
            @RequestParam(required = false) Long cursor) {
        GroupFeedResponse response = momentApplicationService.getGroupFeed(groupId, userId, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<GroupFeedResponse> getMyMoments(
            @PathVariable Long groupId,
            @LoginUser Long userId,
            @RequestParam(required = false) Long cursor) {
        GroupFeedResponse response = momentApplicationService.getMyMomentsInGroup(groupId, userId, cursor);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{momentId}")
    public ResponseEntity<GroupMomentResponse> getMomentDetail(
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @LoginUser Long userId) {
        GroupMomentResponse response = momentApplicationService.getGroupMomentDetail(
            groupId, momentId, userId
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{momentId}")
    public ResponseEntity<Void> deleteMoment(
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @LoginUser Long userId) {
        momentApplicationService.deleteGroupMoment(groupId, momentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{momentId}/like")
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @LoginUser Long userId) {
        LikeToggleResponse response = momentApplicationService.toggleMomentLike(
            groupId, momentId, userId
        );
        return ResponseEntity.ok(response);
    }
}
```

### 5.2 TDD E2E 테스트 케이스
```java
@Tag("e2e")
class GroupMomentControllerTest extends AcceptanceTest {

    @Test
    void 모멘트_작성() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        GroupMomentCreateRequest request = new GroupMomentCreateRequest("오늘의 순간", List.of());

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v2/groups/" + groupId + "/moments");

        // Then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("content")).isEqualTo("오늘의 순간");
    }

    @Test
    void 그룹_피드_조회() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        모멘트_작성(token, groupId, "모멘트1");
        모멘트_작성(token, groupId, "모멘트2");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .get("/api/v2/groups/" + groupId + "/moments");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("moments")).hasSize(2);
    }

    @Test
    void 그룹_피드_커서_페이지네이션() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        for (int i = 0; i < 25; i++) {
            모멘트_작성(token, groupId, "모멘트" + i);
        }

        // When (첫 페이지)
        ExtractableResponse<Response> firstPage = given()
            .header("Authorization", "Bearer " + token)
            .get("/api/v2/groups/" + groupId + "/moments");

        Long nextCursor = firstPage.jsonPath().getLong("nextCursor");

        // When (두 번째 페이지)
        ExtractableResponse<Response> secondPage = given()
            .header("Authorization", "Bearer " + token)
            .queryParam("cursor", nextCursor)
            .get("/api/v2/groups/" + groupId + "/moments");

        // Then
        assertThat(firstPage.jsonPath().getList("moments")).hasSize(20);
        assertThat(secondPage.jsonPath().getList("moments")).hasSize(5);
    }

    @Test
    void 모멘트_좋아요_토글() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        Long momentId = 모멘트_작성_후_ID_반환(token, groupId, "테스트");

        // When (좋아요)
        ExtractableResponse<Response> likeResponse = given()
            .header("Authorization", "Bearer " + token)
            .post("/api/v2/groups/" + groupId + "/moments/" + momentId + "/like");

        // Then
        assertThat(likeResponse.statusCode()).isEqualTo(200);
        assertThat(likeResponse.jsonPath().getBoolean("isLiked")).isTrue();
        assertThat(likeResponse.jsonPath().getLong("likeCount")).isEqualTo(1);

        // When (좋아요 취소)
        ExtractableResponse<Response> unlikeResponse = given()
            .header("Authorization", "Bearer " + token)
            .post("/api/v2/groups/" + groupId + "/moments/" + momentId + "/like");

        // Then
        assertThat(unlikeResponse.jsonPath().getBoolean("isLiked")).isFalse();
        assertThat(unlikeResponse.jsonPath().getLong("likeCount")).isEqualTo(0);
    }
}
```

### 5.3 검증
```bash
./gradlew e2eTest --tests "GroupMomentControllerTest"
```

---

## Step 6: GroupCommentController 구현

### 6.1 대상 파일

#### `src/main/java/moment/group/presentation/GroupCommentController.java`
```java
package moment.group.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import moment.auth.domain.LoginUser;
import moment.comment.dto.request.CommentCreateRequest;
import moment.comment.dto.response.CommentResponse;
import moment.comment.service.application.CommentApplicationService;
import moment.group.dto.response.LikeToggleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/groups/{groupId}")
@RequiredArgsConstructor
public class GroupCommentController {

    private final CommentApplicationService commentApplicationService;

    @PostMapping("/moments/{momentId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @LoginUser Long userId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentApplicationService.createCommentInGroup(
            groupId, momentId, userId, request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/moments/{momentId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long groupId,
            @PathVariable Long momentId,
            @LoginUser Long userId,
            @RequestParam(required = false) Long cursor) {
        List<CommentResponse> response = commentApplicationService.getCommentsInGroup(
            groupId, momentId, userId, cursor
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long groupId,
            @PathVariable Long commentId,
            @LoginUser Long userId) {
        commentApplicationService.deleteCommentInGroup(groupId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<LikeToggleResponse> toggleCommentLike(
            @PathVariable Long groupId,
            @PathVariable Long commentId,
            @LoginUser Long userId) {
        LikeToggleResponse response = commentApplicationService.toggleCommentLike(
            groupId, commentId, userId
        );
        return ResponseEntity.ok(response);
    }
}
```

### 6.2 TDD E2E 테스트 케이스
```java
@Tag("e2e")
class GroupCommentControllerTest extends AcceptanceTest {

    @Test
    void 코멘트_작성() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        Long momentId = 모멘트_작성_후_ID_반환(token, groupId, "테스트");
        CommentCreateRequest request = new CommentCreateRequest("좋은 글이네요!");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/api/v2/groups/" + groupId + "/moments/" + momentId + "/comments");

        // Then
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("content")).isEqualTo("좋은 글이네요!");
    }

    @Test
    void 코멘트_목록_조회() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        Long momentId = 모멘트_작성_후_ID_반환(token, groupId, "테스트");
        코멘트_작성(token, groupId, momentId, "코멘트1");
        코멘트_작성(token, groupId, momentId, "코멘트2");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .get("/api/v2/groups/" + groupId + "/moments/" + momentId + "/comments");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("$")).hasSize(2);
    }

    @Test
    void 코멘트_좋아요_토글() {
        // Given
        String token = 로그인("user@example.com");
        Long groupId = 그룹_생성_후_ID_반환(token, "테스트 그룹");
        Long momentId = 모멘트_작성_후_ID_반환(token, groupId, "테스트");
        Long commentId = 코멘트_작성_후_ID_반환(token, groupId, momentId, "코멘트");

        // When
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .post("/api/v2/groups/" + groupId + "/comments/" + commentId + "/like");

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("isLiked")).isTrue();
    }
}
```

### 6.3 검증
```bash
./gradlew e2eTest --tests "GroupCommentControllerTest"
```

---

## 최종 검증

```bash
# 1. 컴파일 확인
./gradlew compileJava

# 2. 단위 테스트
./gradlew fastTest

# 3. E2E 테스트
./gradlew e2eTest

# 4. 전체 빌드
./gradlew build
```

---

## 체크리스트

### DTO 생성 완료
- [ ] GroupCreateRequest, GroupUpdateRequest
- [ ] GroupJoinRequest, ProfileUpdateRequest
- [ ] GroupCreateResponse, GroupDetailResponse, MyGroupResponse
- [ ] GroupJoinResponse, MemberResponse
- [ ] InviteInfoResponse, InviteCreateResponse
- [ ] GroupMomentCreateRequest, GroupMomentResponse
- [ ] GroupFeedResponse, LikeToggleResponse

### Controller 생성 완료
- [ ] GroupController (CRUD)
- [ ] GroupMemberController (가입/승인/강퇴/탈퇴/이전/프로필)
- [ ] GroupInviteController (초대 링크)
- [ ] GroupMomentController (모멘트 CRUD + 좋아요)
- [ ] GroupCommentController (코멘트 CRUD + 좋아요)

### E2E 테스트 완료
- [ ] GroupControllerTest
- [ ] GroupMemberControllerTest
- [ ] GroupInviteControllerTest
- [ ] GroupMomentControllerTest
- [ ] GroupCommentControllerTest

### 최종 검증
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew fastTest` 성공
- [ ] `./gradlew e2eTest` 성공
- [ ] `./gradlew build` 성공

---

## 디렉토리 구조

```
src/main/java/moment/group/
├── domain/
├── infrastructure/
├── service/
│   ├── group/
│   ├── invite/
│   └── application/
├── presentation/
│   ├── GroupController.java
│   ├── GroupMemberController.java
│   ├── GroupInviteController.java
│   ├── GroupMomentController.java
│   └── GroupCommentController.java
└── dto/
    ├── request/
    │   ├── GroupCreateRequest.java
    │   ├── GroupUpdateRequest.java
    │   ├── GroupJoinRequest.java
    │   ├── ProfileUpdateRequest.java
    │   └── GroupMomentCreateRequest.java
    ├── response/
    │   ├── GroupCreateResponse.java
    │   ├── GroupDetailResponse.java
    │   ├── MyGroupResponse.java
    │   ├── GroupJoinResponse.java
    │   ├── MemberResponse.java
    │   ├── InviteInfoResponse.java
    │   ├── InviteCreateResponse.java
    │   ├── GroupMomentResponse.java
    │   ├── GroupFeedResponse.java
    │   └── LikeToggleResponse.java
    └── event/
        ├── GroupJoinRequestEvent.java
        ├── GroupJoinApprovedEvent.java
        └── GroupKickedEvent.java
```
