# Phase 3: Service Layer 구현 보고서

## 개요

이 보고서는 그룹 기능의 Service Layer 구현 결과를 정리한 문서입니다.

- **작업 기간**: 2026-01-22
- **브랜치**: `feat/#993`
- **커밋 수**: 5개

---

## 1. ErrorCode 추가

### 파일: `src/main/java/moment/global/exception/ErrorCode.java`

| 코드 | 이름 | 설명 | HTTP 상태 |
|------|------|------|----------|
| GR-001 | GROUP_NOT_FOUND | 존재하지 않는 그룹입니다 | 404 |
| GR-002 | NOT_GROUP_OWNER | 그룹 소유자가 아닙니다 | 403 |
| GR-003 | CANNOT_DELETE_GROUP_WITH_MEMBERS | 멤버가 있는 그룹은 삭제할 수 없습니다 | 400 |
| GM-001 | GROUP_MEMBER_NOT_FOUND | 존재하지 않는 그룹 멤버입니다 | 404 |
| GM-002 | ALREADY_GROUP_MEMBER | 이미 그룹 멤버입니다 | 409 |
| GM-003 | NOT_APPROVED_MEMBER | 승인된 멤버가 아닙니다 | 403 |
| GM-004 | CANNOT_KICK_OWNER | 그룹 소유자는 추방할 수 없습니다 | 400 |
| GM-005 | OWNER_CANNOT_LEAVE | 그룹 소유자는 탈퇴할 수 없습니다 | 400 |
| GM-006 | TARGET_NOT_APPROVED | 대상 멤버가 승인 상태가 아닙니다 | 400 |
| GM-007 | NICKNAME_ALREADY_EXISTS | 이미 사용 중인 닉네임입니다 | 409 |
| GM-008 | NOT_PENDING_MEMBER | 대기 중인 멤버가 아닙니다 | 400 |
| IL-001 | INVITE_LINK_NOT_FOUND | 존재하지 않는 초대 링크입니다 | 404 |
| IL-002 | INVITE_LINK_EXPIRED | 만료된 초대 링크입니다 | 400 |

---

## 2. Domain Services

### 2.1 GroupService

**파일**: `src/main/java/moment/group/service/group/GroupService.java`

그룹의 기본 CRUD 작업을 담당하는 도메인 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `create(String name, String description, User owner)` | `Group` | 새 그룹을 생성합니다. 그룹 이름, 설명, 소유자 정보를 받아 저장합니다. |
| `getById(Long groupId)` | `Group` | ID로 그룹을 조회합니다. 없으면 `GROUP_NOT_FOUND` 예외를 발생시킵니다. |
| `update(Long groupId, String name, String description, User requester)` | `Group` | 그룹 정보를 수정합니다. 요청자가 소유자인지 검증합니다. |
| `delete(Long groupId, User requester)` | `void` | 그룹을 삭제합니다. 소유자만 삭제 가능합니다. |

---

### 2.2 GroupMemberService

**파일**: `src/main/java/moment/group/service/group/GroupMemberService.java`

그룹 멤버의 생명주기를 관리하는 도메인 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `createOwner(Group group, User user, String nickname)` | `GroupMember` | 그룹 생성 시 소유자 멤버를 생성합니다. 자동으로 APPROVED 상태입니다. |
| `joinOrRestore(Group group, User user, String nickname)` | `GroupMember` | 그룹에 가입 신청합니다. 기존에 탈퇴한 멤버면 복구합니다. |
| `approve(Long memberId)` | `GroupMember` | 가입 대기 멤버를 승인합니다. |
| `reject(Long memberId)` | `void` | 가입 대기 멤버를 거절합니다 (soft delete). |
| `kick(Long memberId)` | `GroupMember` | 멤버를 추방합니다. 소유자는 추방할 수 없습니다. |
| `leave(Long groupId, Long userId)` | `GroupMember` | 그룹에서 탈퇴합니다. 소유자는 탈퇴할 수 없습니다. |
| `transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerMemberId)` | `void` | 그룹 소유권을 다른 멤버에게 양도합니다. |
| `updateNickname(Long memberId, String nickname)` | `GroupMember` | 멤버 닉네임을 변경합니다. 중복 검사를 수행합니다. |
| `getByGroupAndUser(Long groupId, Long userId)` | `GroupMember` | 그룹과 사용자로 멤버를 조회합니다. |
| `getById(Long memberId)` | `GroupMember` | ID로 멤버를 조회합니다. |
| `getApprovedMembers(Long groupId)` | `List<GroupMember>` | 그룹의 승인된 멤버 목록을 조회합니다. |
| `getPendingMembers(Long groupId)` | `List<GroupMember>` | 그룹의 가입 대기 멤버 목록을 조회합니다. |
| `countApprovedMembers(Long groupId)` | `long` | 그룹의 승인된 멤버 수를 반환합니다. |

---

### 2.3 InviteLinkService

**파일**: `src/main/java/moment/group/service/group/InviteLinkService.java`

그룹 초대 링크를 관리하는 도메인 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `createOrGet(Group group)` | `GroupInviteLink` | 유효한 초대 링크가 있으면 반환하고, 없으면 새로 생성합니다. |
| `getByCode(String code)` | `GroupInviteLink` | 초대 코드로 링크를 조회합니다. 만료 여부를 검증합니다. |
| `deactivate(Long linkId)` | `void` | 초대 링크를 비활성화합니다 (soft delete). |
| `refresh(Long groupId)` | `GroupInviteLink` | 기존 링크를 비활성화하고 새 링크를 생성합니다. |

---

## 3. Application Services

### 3.1 GroupApplicationService

**파일**: `src/main/java/moment/group/service/application/GroupApplicationService.java`

그룹 관련 유스케이스를 조율하는 애플리케이션 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `createGroup(Long userId, GroupCreateRequest request)` | `GroupCreateResponse` | 그룹을 생성하고 소유자 멤버와 초대 링크를 함께 생성합니다. |
| `getGroupDetail(Long groupId, Long userId)` | `GroupDetailResponse` | 그룹 상세 정보를 조회합니다. 멤버 수, 초대 코드, 현재 멤버 역할 포함. |
| `getMyGroups(Long userId)` | `List<MyGroupResponse>` | 사용자가 속한 그룹 목록을 조회합니다. |
| `updateGroup(Long groupId, Long userId, GroupUpdateRequest request)` | `GroupDetailResponse` | 그룹 정보를 수정합니다. |
| `deleteGroup(Long groupId, Long userId)` | `void` | 그룹을 삭제합니다. 멤버가 본인만 있어야 가능합니다. |

---

### 3.2 GroupMemberApplicationService

**파일**: `src/main/java/moment/group/service/application/GroupMemberApplicationService.java`

그룹 멤버 관련 유스케이스를 조율하고 이벤트를 발행하는 애플리케이션 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `getInviteInfo(String inviteCode)` | `InviteInfoResponse` | 초대 코드로 그룹 정보를 조회합니다 (가입 전 미리보기). |
| `joinGroup(Long userId, GroupJoinRequest request)` | `GroupJoinResponse` | 그룹 가입을 신청합니다. `GroupJoinRequestEvent` 발행. |
| `approveMember(Long groupId, Long ownerId, Long memberId)` | `void` | 가입 신청을 승인합니다. `GroupJoinApprovedEvent` 발행. |
| `rejectMember(Long groupId, Long ownerId, Long memberId)` | `void` | 가입 신청을 거절합니다. |
| `kickMember(Long groupId, Long ownerId, Long memberId)` | `void` | 멤버를 추방합니다. `GroupKickedEvent` 발행. |
| `leaveGroup(Long groupId, Long userId)` | `void` | 그룹에서 탈퇴합니다. |
| `transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerMemberId)` | `void` | 소유권을 양도합니다. |
| `updateProfile(Long memberId, Long userId, String nickname)` | `MemberResponse` | 멤버 프로필(닉네임)을 수정합니다. |
| `getMembers(Long groupId, Long userId)` | `List<MemberResponse>` | 그룹의 승인된 멤버 목록을 조회합니다. |
| `getPendingMembers(Long groupId, Long userId)` | `List<MemberResponse>` | 그룹의 가입 대기 멤버 목록을 조회합니다 (소유자만). |
| `createInviteLink(Long groupId, Long userId)` | `String` | 새 초대 링크를 생성합니다 (기존 링크 비활성화). |

---

## 4. Like Services

### 4.1 MomentLikeService

**파일**: `src/main/java/moment/like/service/MomentLikeService.java`

모먼트 좋아요 기능을 담당하는 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `toggle(Moment moment, GroupMember member)` | `boolean` | 좋아요를 토글합니다. 좋아요 추가 시 `MomentLikeEvent` 발행 (본인 제외). 반환값: 좋아요 상태. |
| `getCount(Long momentId)` | `long` | 모먼트의 좋아요 수를 반환합니다. |
| `hasLiked(Long momentId, Long memberId)` | `boolean` | 해당 멤버가 좋아요를 눌렀는지 확인합니다. |

---

### 4.2 CommentLikeService

**파일**: `src/main/java/moment/like/service/CommentLikeService.java`

댓글 좋아요 기능을 담당하는 서비스입니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `toggle(Comment comment, GroupMember member)` | `boolean` | 좋아요를 토글합니다. 좋아요 추가 시 `CommentLikeEvent` 발행 (본인 제외). 반환값: 좋아요 상태. |
| `getCount(Long commentId)` | `long` | 댓글의 좋아요 수를 반환합니다. |
| `hasLiked(Long commentId, Long memberId)` | `boolean` | 해당 멤버가 좋아요를 눌렀는지 확인합니다. |

---

## 5. 기존 서비스 수정

### 5.1 MomentService (수정)

**파일**: `src/main/java/moment/moment/service/moment/MomentService.java`

그룹 컨텍스트 내 모먼트 기능이 추가되었습니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `createInGroup(User momenter, Group group, GroupMember member, String content)` | `Moment` | 그룹 내에서 모먼트를 생성합니다. |
| `getByGroup(Long groupId, Long cursor, int limit)` | `List<Moment>` | 그룹의 모먼트 피드를 커서 기반으로 조회합니다. |
| `getMyMomentsInGroup(Long groupId, Long memberId, Long cursor, int limit)` | `List<Moment>` | 그룹 내 본인 모먼트를 커서 기반으로 조회합니다. |

---

### 5.2 CommentService (수정)

**파일**: `src/main/java/moment/comment/service/comment/CommentService.java`

그룹 멤버 컨텍스트 내 댓글 기능이 추가되었습니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `createWithMember(Moment moment, User commenter, GroupMember member, String content)` | `Comment` | 그룹 멤버로 댓글을 생성합니다. |
| `countByMomentId(Long momentId)` | `long` | 모먼트의 댓글 수를 반환합니다. |

---

### 5.3 MomentApplicationService (수정)

**파일**: `src/main/java/moment/moment/service/application/MomentApplicationService.java`

그룹 피드 조회 유스케이스가 추가되었습니다.

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `getGroupFeed(Long groupId, Long userId, Long cursor)` | `GroupFeedResponse` | 그룹 피드를 조회합니다. 좋아요 수, 좋아요 여부, 댓글 수 포함. |
| `getMyMomentsInGroup(Long groupId, Long userId, Long cursor)` | `GroupFeedResponse` | 그룹 내 본인 모먼트를 조회합니다. |

---

## 6. DTOs

### 6.1 Request DTOs

| DTO | 필드 | 설명 |
|-----|------|------|
| `GroupCreateRequest` | name, description | 그룹 생성 요청 |
| `GroupJoinRequest` | inviteCode, nickname | 그룹 가입 요청 |
| `GroupUpdateRequest` | name, description | 그룹 수정 요청 |

### 6.2 Response DTOs

| DTO | 필드 | 설명 |
|-----|------|------|
| `GroupCreateResponse` | groupId, inviteCode | 그룹 생성 응답 |
| `GroupDetailResponse` | id, name, description, memberCount, inviteCode, myRole | 그룹 상세 응답 |
| `MyGroupResponse` | groupId, groupName, myNickname, memberCount | 내 그룹 목록 응답 |
| `MemberResponse` | memberId, userId, nickname, role, status, joinedAt | 멤버 정보 응답 |
| `GroupJoinResponse` | memberId, status | 그룹 가입 응답 |
| `InviteInfoResponse` | groupId, groupName, description, memberCount | 초대 링크 정보 응답 |
| `GroupMomentResponse` | momentId, content, memberNickname, memberId, likeCount, hasLiked, commentCount, createdAt | 그룹 모먼트 응답 |
| `GroupFeedResponse` | moments, nextCursor, hasNextPage | 그룹 피드 응답 |

### 6.3 Event DTOs

| Event | 필드 | 발행 시점 |
|-------|------|----------|
| `GroupJoinRequestEvent` | groupId, requesterId, requesterNickname | 그룹 가입 신청 시 |
| `GroupJoinApprovedEvent` | groupId, groupName, memberId | 가입 승인 시 |
| `GroupKickedEvent` | groupId, groupName, memberId | 멤버 추방 시 |
| `MomentLikeEvent` | momentId, momenterId, likerNickname | 모먼트 좋아요 시 (본인 제외) |
| `CommentLikeEvent` | commentId, commenterId, likerNickname | 댓글 좋아요 시 (본인 제외) |

---

## 7. 테스트

### 7.1 단위 테스트

모든 서비스에 대해 Mockito 기반 단위 테스트가 작성되었습니다.

| 테스트 파일 | 테스트 수 |
|------------|----------|
| `GroupServiceTest.java` | 7개 |
| `GroupMemberServiceTest.java` | 18개 |
| `InviteLinkServiceTest.java` | 6개 |
| `GroupApplicationServiceTest.java` | 7개 |
| `GroupMemberApplicationServiceTest.java` | 14개 |
| `MomentLikeServiceTest.java` | 7개 |
| `CommentLikeServiceTest.java` | 7개 |

### 7.2 테스트 픽스처

| 픽스처 | 설명 |
|--------|------|
| `GroupFixture` | 그룹 테스트 데이터 생성 |
| `GroupMemberFixture` | 그룹 멤버 테스트 데이터 생성 (자동 ID 할당) |
| `GroupInviteLinkFixture` | 초대 링크 테스트 데이터 생성 |
| `MomentLikeFixture` | 모먼트 좋아요 테스트 데이터 생성 |
| `CommentLikeFixture` | 댓글 좋아요 테스트 데이터 생성 |

---

## 8. 커밋 이력

| 순서 | 커밋 메시지 | 설명 |
|------|------------|------|
| 1 | `feat: Group, GroupMember, InviteLink 관련 ErrorCode 추가` | 14개 ErrorCode 추가 |
| 2 | `feat: GroupService, GroupMemberService, InviteLinkService 구현` | 도메인 서비스 3개 구현 |
| 3 | `feat: GroupApplicationService, GroupMemberApplicationService 구현` | Application 서비스 2개 구현 |
| 4 | `feat: MomentLikeService, CommentLikeService 구현` | Like 서비스 2개 구현 |
| 5 | `feat: MomentService, CommentService, MomentApplicationService 그룹 기능 추가` | 기존 서비스 그룹 기능 확장 |

---

## 9. 다음 단계

- **Phase 4**: Presentation Layer (Controller) 구현
  - REST API 엔드포인트 설계
  - 요청 검증 및 인증/인가
  - API 문서화

---

*Generated: 2026-01-22*
