# Phase 3: 서비스 구현 (Day 5-7)

## 개요
- **목표**: 그룹, 멤버, 좋아요 서비스 구현 및 기존 서비스 수정
- **원칙**: TDD 기반 (테스트 → 구현 → 리팩토링)
- **패턴**: upsert/restore (Soft Delete + UNIQUE 제약 처리)
- **검증**: 각 Step 후 `./gradlew fastTest`

---

## Step 1: Group 도메인 서비스 구현

### 1.1 대상 파일

#### `src/main/java/moment/group/service/group/GroupService.java`
```java
package moment.group.service.group;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.infrastructure.GroupRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;

    @Transactional
    public Group create(String name, String description, User owner) {
        Group group = new Group(name, description, owner);
        return groupRepository.save(group);
    }

    public Group getById(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() -> new MomentException(ErrorCode.GROUP_NOT_FOUND));
    }

    @Transactional
    public void update(Long groupId, String name, String description, User requester) {
        Group group = getById(groupId);
        validateOwner(group, requester);
        group.updateInfo(name, description);
    }

    @Transactional
    public void delete(Long groupId, User requester) {
        Group group = getById(groupId);
        validateOwner(group, requester);
        groupRepository.delete(group);
    }

    private void validateOwner(Group group, User user) {
        if (!group.isOwner(user)) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
    }
}
```

### 1.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void 그룹_생성_성공() {
        // Given
        User owner = createUser();
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        // When
        Group group = groupService.create("테스트", "설명", owner);

        // Then
        assertThat(group.getName()).isEqualTo("테스트");
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    void 그룹_조회_존재하지_않음() {
        // Given
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> groupService.getById(1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    void 그룹_수정_소유자가_아님() {
        // Given
        User owner = createUser(1L);
        User other = createUser(2L);
        Group group = new Group("그룹", "설명", owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When/Then
        assertThatThrownBy(() -> groupService.update(1L, "새이름", "새설명", other))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_OWNER);
    }

    @Test
    void 그룹_삭제_성공() {
        // Given
        User owner = createUser();
        Group group = new Group("그룹", "설명", owner);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // When
        groupService.delete(1L, owner);

        // Then
        verify(groupRepository).delete(group);
    }
}
```

### 1.3 검증
```bash
./gradlew fastTest
```

---

## Step 2: GroupMember 도메인 서비스 구현

### 2.1 대상 파일

#### `src/main/java/moment/group/service/group/GroupMemberService.java`
```java
package moment.group.service.group;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupMember;
import moment.group.domain.MemberStatus;
import moment.group.infrastructure.GroupMemberRepository;
import moment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupMemberRepository memberRepository;

    @Transactional
    public GroupMember createOwner(Group group, User user, String nickname) {
        validateNicknameNotUsed(group.getId(), nickname);
        GroupMember owner = GroupMember.createOwner(group, user, nickname);
        return memberRepository.save(owner);
    }

    @Transactional
    public GroupMember joinOrRestore(Group group, User user, String nickname) {
        validateNicknameNotUsed(group.getId(), nickname);

        // Soft delete된 기존 멤버십 확인
        Optional<GroupMember> existing = memberRepository
            .findByGroupIdAndUserIdIncludeDeleted(group.getId(), user.getId());

        if (existing.isPresent()) {
            GroupMember member = existing.get();
            if (!member.isDeleted()) {
                throw new MomentException(ErrorCode.ALREADY_GROUP_MEMBER);
            }
            member.restore(nickname);
            return member;
        }

        GroupMember member = GroupMember.createPendingMember(group, user, nickname);
        return memberRepository.save(member);
    }

    @Transactional
    public void approve(Long memberId) {
        GroupMember member = getById(memberId);
        if (!member.isPending()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_PENDING);
        }
        member.approve();
    }

    @Transactional
    public void reject(Long memberId) {
        GroupMember member = getById(memberId);
        if (!member.isPending()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_PENDING);
        }
        memberRepository.delete(member);
    }

    @Transactional
    public void kick(Long memberId) {
        GroupMember member = getById(memberId);
        if (member.isOwner()) {
            throw new MomentException(ErrorCode.CANNOT_KICK_OWNER);
        }
        member.kick();
        memberRepository.delete(member);
    }

    @Transactional
    public void leave(Long groupId, Long userId) {
        GroupMember member = getByGroupAndUser(groupId, userId);
        if (member.isOwner()) {
            throw new MomentException(ErrorCode.OWNER_CANNOT_LEAVE);
        }
        memberRepository.delete(member);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerId) {
        GroupMember currentOwner = getByGroupAndUser(groupId, currentOwnerId);
        GroupMember newOwner = getById(newOwnerId);

        if (!currentOwner.isOwner()) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
        if (!newOwner.isApproved()) {
            throw new MomentException(ErrorCode.MEMBER_NOT_APPROVED);
        }

        currentOwner.demoteToMember();
        newOwner.transferOwnership();
    }

    @Transactional
    public void updateNickname(Long memberId, String nickname) {
        GroupMember member = getById(memberId);
        validateNicknameNotUsed(member.getGroup().getId(), nickname);
        member.updateNickname(nickname);
    }

    public GroupMember getById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new MomentException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public GroupMember getByGroupAndUser(Long groupId, Long userId) {
        return memberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new MomentException(ErrorCode.NOT_GROUP_MEMBER));
    }

    public List<GroupMember> getApprovedMembers(Long groupId) {
        return memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
    }

    public List<GroupMember> getPendingMembers(Long groupId) {
        return memberRepository.findByGroupIdAndStatus(groupId, MemberStatus.PENDING);
    }

    public List<GroupMember> getMyGroups(Long userId) {
        return memberRepository.findApprovedMembershipsByUserId(userId);
    }

    public long countApprovedMembers(Long groupId) {
        return memberRepository.countByGroupIdAndStatus(groupId, MemberStatus.APPROVED);
    }

    private void validateNicknameNotUsed(Long groupId, String nickname) {
        if (memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(groupId, nickname)) {
            throw new MomentException(ErrorCode.NICKNAME_ALREADY_USED);
        }
    }
}
```

### 2.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class GroupMemberServiceTest {

    @Mock
    private GroupMemberRepository memberRepository;

    @InjectMocks
    private GroupMemberService memberService;

    @Test
    void Owner_멤버_생성_성공() {
        // Given
        Group group = createGroup();
        User user = createUser();
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupMember owner = memberService.createOwner(group, user, "닉네임");

        // Then
        assertThat(owner.isOwner()).isTrue();
        assertThat(owner.isApproved()).isTrue();
    }

    @Test
    void 가입_신청_닉네임_중복() {
        // Given
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(1L, "중복닉네임"))
            .thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            memberService.joinOrRestore(createGroup(), createUser(), "중복닉네임"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NICKNAME_ALREADY_USED);
    }

    @Test
    void 재가입_soft_delete_복구() {
        // Given
        Group group = createGroup();
        User user = createUser();
        GroupMember deletedMember = createDeletedMember();
        when(memberRepository.existsByGroupIdAndNicknameAndDeletedAtIsNull(anyLong(), anyString()))
            .thenReturn(false);
        when(memberRepository.findByGroupIdAndUserIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(deletedMember));

        // When
        GroupMember restored = memberService.joinOrRestore(group, user, "새닉네임");

        // Then
        assertThat(restored.getDeletedAt()).isNull();
        assertThat(restored.getNickname()).isEqualTo("새닉네임");
        assertThat(restored.isPending()).isTrue();
    }

    @Test
    void 멤버_승인_성공() {
        // Given
        GroupMember pendingMember = createPendingMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(pendingMember));

        // When
        memberService.approve(1L);

        // Then
        assertThat(pendingMember.isApproved()).isTrue();
    }

    @Test
    void Owner_강퇴_불가() {
        // Given
        GroupMember owner = createOwnerMember();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(owner));

        // When/Then
        assertThatThrownBy(() -> memberService.kick(1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_KICK_OWNER);
    }

    @Test
    void 소유권_이전_성공() {
        // Given
        GroupMember currentOwner = createOwnerMember();
        GroupMember newOwner = createApprovedMember();
        when(memberRepository.findByGroupIdAndUserId(1L, 1L))
            .thenReturn(Optional.of(currentOwner));
        when(memberRepository.findById(2L))
            .thenReturn(Optional.of(newOwner));

        // When
        memberService.transferOwnership(1L, 1L, 2L);

        // Then
        assertThat(currentOwner.getRole()).isEqualTo(MemberRole.MEMBER);
        assertThat(newOwner.getRole()).isEqualTo(MemberRole.OWNER);
    }
}
```

### 2.3 검증
```bash
./gradlew fastTest
```

---

## Step 3: InviteLink 서비스 구현

### 3.1 대상 파일

#### `src/main/java/moment/group/service/invite/InviteLinkService.java`
```java
package moment.group.service.invite;

import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.infrastructure.GroupInviteLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteLinkService {

    private static final int DEFAULT_VALID_DAYS = 7;

    private final GroupInviteLinkRepository inviteLinkRepository;

    @Transactional
    public GroupInviteLink createOrGet(Group group) {
        // 기존 활성 링크가 있으면 반환
        Optional<GroupInviteLink> existing = inviteLinkRepository
            .findByGroupIdAndIsActiveTrue(group.getId());

        if (existing.isPresent()) {
            GroupInviteLink link = existing.get();
            if (link.isValid()) {
                return link;
            }
            // 만료된 경우 비활성화하고 새로 생성
            link.deactivate();
        }

        GroupInviteLink newLink = new GroupInviteLink(group, DEFAULT_VALID_DAYS);
        return inviteLinkRepository.save(newLink);
    }

    public GroupInviteLink getByCode(String code) {
        GroupInviteLink link = inviteLinkRepository.findByCode(code)
            .orElseThrow(() -> new MomentException(ErrorCode.INVITE_LINK_INVALID));

        if (!link.isValid()) {
            throw new MomentException(ErrorCode.INVITE_LINK_EXPIRED);
        }

        return link;
    }

    @Transactional
    public void deactivate(Long linkId) {
        GroupInviteLink link = inviteLinkRepository.findById(linkId)
            .orElseThrow(() -> new MomentException(ErrorCode.INVITE_LINK_INVALID));
        link.deactivate();
    }

    @Transactional
    public void refresh(Long groupId) {
        inviteLinkRepository.findByGroupIdAndIsActiveTrue(groupId)
            .ifPresent(GroupInviteLink::deactivate);
    }
}
```

### 3.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class InviteLinkServiceTest {

    @Mock
    private GroupInviteLinkRepository inviteLinkRepository;

    @InjectMocks
    private InviteLinkService inviteLinkService;

    @Test
    void 초대_링크_생성_성공() {
        // Given
        Group group = createGroup();
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(anyLong()))
            .thenReturn(Optional.empty());
        when(inviteLinkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        GroupInviteLink link = inviteLinkService.createOrGet(group);

        // Then
        assertThat(link.getCode()).isNotNull();
        assertThat(link.isActive()).isTrue();
    }

    @Test
    void 기존_유효한_링크_반환() {
        // Given
        Group group = createGroup();
        GroupInviteLink existingLink = new GroupInviteLink(group, 7);
        when(inviteLinkRepository.findByGroupIdAndIsActiveTrue(anyLong()))
            .thenReturn(Optional.of(existingLink));

        // When
        GroupInviteLink link = inviteLinkService.createOrGet(group);

        // Then
        assertThat(link).isEqualTo(existingLink);
        verify(inviteLinkRepository, never()).save(any());
    }

    @Test
    void 코드로_조회_유효하지_않은_링크() {
        // Given
        when(inviteLinkRepository.findByCode("invalid"))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> inviteLinkService.getByCode("invalid"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_LINK_INVALID);
    }

    @Test
    void 코드로_조회_만료된_링크() {
        // Given
        GroupInviteLink expiredLink = createExpiredLink();
        when(inviteLinkRepository.findByCode("expired"))
            .thenReturn(Optional.of(expiredLink));

        // When/Then
        assertThatThrownBy(() -> inviteLinkService.getByCode("expired"))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_LINK_EXPIRED);
    }
}
```

### 3.3 검증
```bash
./gradlew fastTest
```

---

## Step 4: Group Application 서비스 구현

### 4.1 대상 파일

#### `src/main/java/moment/group/service/application/GroupApplicationService.java`
```java
package moment.group.service.application;

import lombok.RequiredArgsConstructor;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.dto.request.GroupCreateRequest;
import moment.group.dto.response.GroupCreateResponse;
import moment.group.dto.response.GroupDetailResponse;
import moment.group.dto.response.MyGroupResponse;
import moment.group.service.group.GroupMemberService;
import moment.group.service.group.GroupService;
import moment.group.service.invite.InviteLinkService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupApplicationService {

    private final GroupService groupService;
    private final GroupMemberService memberService;
    private final InviteLinkService inviteLinkService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GroupCreateResponse createGroup(Long userId, GroupCreateRequest request) {
        User owner = userService.getUserBy(userId);

        // 그룹 생성
        Group group = groupService.create(request.name(), request.description(), owner);

        // 소유자를 첫 번째 멤버로 등록
        GroupMember ownerMember = memberService.createOwner(group, owner, request.ownerNickname());

        // 초대 링크 생성
        GroupInviteLink inviteLink = inviteLinkService.createOrGet(group);

        return GroupCreateResponse.from(group, ownerMember, inviteLink);
    }

    public GroupDetailResponse getGroupDetail(Long groupId, Long userId) {
        Group group = groupService.getById(groupId);
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        List<GroupMember> members = memberService.getApprovedMembers(groupId);
        long memberCount = memberService.countApprovedMembers(groupId);

        return GroupDetailResponse.from(group, member, members, memberCount);
    }

    public List<MyGroupResponse> getMyGroups(Long userId) {
        List<GroupMember> memberships = memberService.getMyGroups(userId);
        return memberships.stream()
            .map(membership -> {
                Group group = membership.getGroup();
                long memberCount = memberService.countApprovedMembers(group.getId());
                return MyGroupResponse.from(group, membership, memberCount);
            })
            .toList();
    }

    @Transactional
    public void updateGroup(Long groupId, Long userId, String name, String description) {
        User user = userService.getUserBy(userId);
        groupService.update(groupId, name, description, user);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        // 멤버가 소유자 외에 있는지 확인
        long memberCount = memberService.countApprovedMembers(groupId);
        if (memberCount > 1) {
            throw new MomentException(ErrorCode.CANNOT_DELETE_GROUP_WITH_MEMBERS);
        }

        User user = userService.getUserBy(userId);
        groupService.delete(groupId, user);
    }
}
```

### 4.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class GroupApplicationServiceTest {

    @Mock private GroupService groupService;
    @Mock private GroupMemberService memberService;
    @Mock private InviteLinkService inviteLinkService;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GroupApplicationService groupApplicationService;

    @Test
    void 그룹_생성_성공() {
        // Given
        User owner = createUser();
        Group group = createGroup();
        GroupMember ownerMember = createOwnerMember();
        GroupInviteLink inviteLink = createInviteLink();

        when(userService.getUserBy(1L)).thenReturn(owner);
        when(groupService.create(anyString(), anyString(), any())).thenReturn(group);
        when(memberService.createOwner(any(), any(), anyString())).thenReturn(ownerMember);
        when(inviteLinkService.createOrGet(any())).thenReturn(inviteLink);

        GroupCreateRequest request = new GroupCreateRequest("그룹명", "설명", "닉네임");

        // When
        GroupCreateResponse response = groupApplicationService.createGroup(1L, request);

        // Then
        assertThat(response).isNotNull();
        verify(groupService).create("그룹명", "설명", owner);
        verify(memberService).createOwner(group, owner, "닉네임");
        verify(inviteLinkService).createOrGet(group);
    }

    @Test
    void 내_그룹_목록_조회() {
        // Given
        List<GroupMember> memberships = List.of(createMembership());
        when(memberService.getMyGroups(1L)).thenReturn(memberships);
        when(memberService.countApprovedMembers(anyLong())).thenReturn(5L);

        // When
        List<MyGroupResponse> response = groupApplicationService.getMyGroups(1L);

        // Then
        assertThat(response).hasSize(1);
    }

    @Test
    void 그룹_삭제_멤버_있으면_실패() {
        // Given
        when(memberService.countApprovedMembers(1L)).thenReturn(3L);

        // When/Then
        assertThatThrownBy(() -> groupApplicationService.deleteGroup(1L, 1L))
            .isInstanceOf(MomentException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_DELETE_GROUP_WITH_MEMBERS);
    }
}
```

### 4.3 검증
```bash
./gradlew fastTest
```

---

## Step 5: GroupMember Application 서비스 구현

### 5.1 대상 파일

#### `src/main/java/moment/group/service/application/GroupMemberApplicationService.java`
```java
package moment.group.service.application;

import lombok.RequiredArgsConstructor;
import moment.group.domain.Group;
import moment.group.domain.GroupInviteLink;
import moment.group.domain.GroupMember;
import moment.group.dto.request.GroupJoinRequest;
import moment.group.dto.response.GroupJoinResponse;
import moment.group.dto.response.InviteInfoResponse;
import moment.group.dto.response.MemberResponse;
import moment.group.dto.event.GroupJoinRequestEvent;
import moment.group.dto.event.GroupJoinApprovedEvent;
import moment.group.dto.event.GroupKickedEvent;
import moment.group.service.group.GroupMemberService;
import moment.group.service.group.GroupService;
import moment.group.service.invite.InviteLinkService;
import moment.user.domain.User;
import moment.user.service.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberApplicationService {

    private final GroupService groupService;
    private final GroupMemberService memberService;
    private final InviteLinkService inviteLinkService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public InviteInfoResponse getInviteInfo(String inviteCode) {
        GroupInviteLink link = inviteLinkService.getByCode(inviteCode);
        Group group = link.getGroup();
        long memberCount = memberService.countApprovedMembers(group.getId());

        return InviteInfoResponse.from(group, memberCount);
    }

    @Transactional
    public GroupJoinResponse joinGroup(Long userId, GroupJoinRequest request) {
        GroupInviteLink link = inviteLinkService.getByCode(request.inviteCode());
        Group group = link.getGroup();
        User user = userService.getUserBy(userId);

        GroupMember member = memberService.joinOrRestore(group, user, request.nickname());

        // 가입 신청 이벤트 발행
        eventPublisher.publishEvent(new GroupJoinRequestEvent(
            group.getId(),
            group.getOwner().getId(),
            member.getId(),
            request.nickname()
        ));

        return GroupJoinResponse.from(member);
    }

    @Transactional
    public void approveMember(Long groupId, Long memberId, Long approverId) {
        validateOwner(groupId, approverId);
        memberService.approve(memberId);

        GroupMember member = memberService.getById(memberId);

        // 승인 이벤트 발행
        eventPublisher.publishEvent(new GroupJoinApprovedEvent(
            groupId,
            member.getUser().getId(),
            memberId
        ));
    }

    @Transactional
    public void rejectMember(Long groupId, Long memberId, Long rejecterId) {
        validateOwner(groupId, rejecterId);
        memberService.reject(memberId);
    }

    @Transactional
    public void kickMember(Long groupId, Long memberId, Long kickerId) {
        validateOwner(groupId, kickerId);
        GroupMember member = memberService.getById(memberId);
        Long kickedUserId = member.getUser().getId();

        memberService.kick(memberId);

        // 강퇴 이벤트 발행
        eventPublisher.publishEvent(new GroupKickedEvent(
            groupId,
            kickedUserId,
            memberId
        ));
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        memberService.leave(groupId, userId);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long currentOwnerId, Long newOwnerMemberId) {
        memberService.transferOwnership(groupId, currentOwnerId, newOwnerMemberId);

        // 그룹의 owner_id도 업데이트
        GroupMember newOwner = memberService.getById(newOwnerMemberId);
        Group group = groupService.getById(groupId);
        // Group 엔티티에 owner 변경 메서드 필요
    }

    @Transactional
    public void updateProfile(Long groupId, Long userId, String nickname) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        memberService.updateNickname(member.getId(), nickname);
    }

    public List<MemberResponse> getMembers(Long groupId) {
        List<GroupMember> members = memberService.getApprovedMembers(groupId);
        return members.stream()
            .map(MemberResponse::from)
            .toList();
    }

    public List<MemberResponse> getPendingMembers(Long groupId, Long requesterId) {
        validateOwner(groupId, requesterId);
        List<GroupMember> members = memberService.getPendingMembers(groupId);
        return members.stream()
            .map(MemberResponse::from)
            .toList();
    }

    @Transactional
    public String createInviteLink(Long groupId, Long userId) {
        validateOwner(groupId, userId);
        Group group = groupService.getById(groupId);
        GroupInviteLink link = inviteLinkService.createOrGet(group);
        return link.getCode();
    }

    private void validateOwner(Long groupId, Long userId) {
        GroupMember member = memberService.getByGroupAndUser(groupId, userId);
        if (!member.isOwner()) {
            throw new MomentException(ErrorCode.NOT_GROUP_OWNER);
        }
    }
}
```

### 5.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class GroupMemberApplicationServiceTest {

    @Mock private GroupService groupService;
    @Mock private GroupMemberService memberService;
    @Mock private InviteLinkService inviteLinkService;
    @Mock private UserService userService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GroupMemberApplicationService memberApplicationService;

    @Test
    void 그룹_가입_신청_성공() {
        // Given
        GroupInviteLink link = createValidInviteLink();
        Group group = link.getGroup();
        User user = createUser();
        GroupMember pendingMember = createPendingMember();

        when(inviteLinkService.getByCode("invite-code")).thenReturn(link);
        when(userService.getUserBy(1L)).thenReturn(user);
        when(memberService.joinOrRestore(any(), any(), anyString())).thenReturn(pendingMember);

        GroupJoinRequest request = new GroupJoinRequest("invite-code", "닉네임");

        // When
        GroupJoinResponse response = memberApplicationService.joinGroup(1L, request);

        // Then
        assertThat(response).isNotNull();
        verify(eventPublisher).publishEvent(any(GroupJoinRequestEvent.class));
    }

    @Test
    void 멤버_승인_이벤트_발행() {
        // Given
        GroupMember owner = createOwnerMember();
        GroupMember pending = createPendingMember();

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(owner);
        when(memberService.getById(2L)).thenReturn(pending);

        // When
        memberApplicationService.approveMember(1L, 2L, 1L);

        // Then
        verify(memberService).approve(2L);
        verify(eventPublisher).publishEvent(any(GroupJoinApprovedEvent.class));
    }

    @Test
    void 멤버_강퇴_이벤트_발행() {
        // Given
        GroupMember owner = createOwnerMember();
        GroupMember member = createApprovedMember();

        when(memberService.getByGroupAndUser(1L, 1L)).thenReturn(owner);
        when(memberService.getById(2L)).thenReturn(member);

        // When
        memberApplicationService.kickMember(1L, 2L, 1L);

        // Then
        verify(memberService).kick(2L);
        verify(eventPublisher).publishEvent(any(GroupKickedEvent.class));
    }
}
```

### 5.3 검증
```bash
./gradlew fastTest
```

---

## Step 6: Like 서비스 구현

### 6.1 대상 파일

#### `src/main/java/moment/like/service/MomentLikeService.java`
```java
package moment.like.service;

import lombok.RequiredArgsConstructor;
import moment.group.domain.GroupMember;
import moment.like.domain.MomentLike;
import moment.like.dto.event.MomentLikeEvent;
import moment.like.infrastructure.MomentLikeRepository;
import moment.moment.domain.Moment;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MomentLikeService {

    private final MomentLikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean toggle(Moment moment, GroupMember member) {
        Optional<MomentLike> existing = likeRepository
            .findByMomentIdAndMemberIdIncludeDeleted(moment.getId(), member.getId());

        boolean isNowLiked;

        if (existing.isPresent()) {
            MomentLike like = existing.get();
            like.toggleDeleted();
            isNowLiked = !like.isDeleted();
        } else {
            MomentLike newLike = new MomentLike(moment, member);
            likeRepository.save(newLike);
            isNowLiked = true;
        }

        // 좋아요 추가 시에만 이벤트 발행 (작성자에게 알림)
        if (isNowLiked && !moment.getMomenter().getId().equals(member.getUser().getId())) {
            eventPublisher.publishEvent(new MomentLikeEvent(
                moment.getId(),
                moment.getMomenter().getId(),
                member.getId(),
                member.getNickname()
            ));
        }

        return isNowLiked;
    }

    public long getCount(Long momentId) {
        return likeRepository.countByMomentId(momentId);
    }

    public boolean hasLiked(Long momentId, Long memberId) {
        return likeRepository.existsByMomentIdAndMemberId(momentId, memberId);
    }
}
```

#### `src/main/java/moment/like/service/CommentLikeService.java`
```java
package moment.like.service;

import lombok.RequiredArgsConstructor;
import moment.comment.domain.Comment;
import moment.group.domain.GroupMember;
import moment.like.domain.CommentLike;
import moment.like.dto.event.CommentLikeEvent;
import moment.like.infrastructure.CommentLikeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean toggle(Comment comment, GroupMember member) {
        Optional<CommentLike> existing = likeRepository
            .findByCommentIdAndMemberIdIncludeDeleted(comment.getId(), member.getId());

        boolean isNowLiked;

        if (existing.isPresent()) {
            CommentLike like = existing.get();
            like.toggleDeleted();
            isNowLiked = !like.isDeleted();
        } else {
            CommentLike newLike = new CommentLike(comment, member);
            likeRepository.save(newLike);
            isNowLiked = true;
        }

        // 좋아요 추가 시에만 이벤트 발행 (작성자에게 알림)
        if (isNowLiked && !comment.getCommenter().getId().equals(member.getUser().getId())) {
            eventPublisher.publishEvent(new CommentLikeEvent(
                comment.getId(),
                comment.getCommenter().getId(),
                member.getId(),
                member.getNickname()
            ));
        }

        return isNowLiked;
    }

    public long getCount(Long commentId) {
        return likeRepository.countByCommentId(commentId);
    }

    public boolean hasLiked(Long commentId, Long memberId) {
        return likeRepository.existsByCommentIdAndMemberId(commentId, memberId);
    }
}
```

### 6.2 TDD 테스트 케이스
```java
@ExtendWith(MockitoExtension.class)
class MomentLikeServiceTest {

    @Mock private MomentLikeRepository likeRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MomentLikeService likeService;

    @Test
    void 좋아요_토글_새로_생성() {
        // Given
        Moment moment = createMoment();
        GroupMember member = createMember();
        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean isLiked = likeService.toggle(moment, member);

        // Then
        assertThat(isLiked).isTrue();
        verify(likeRepository).save(any(MomentLike.class));
    }

    @Test
    void 좋아요_토글_취소() {
        // Given
        MomentLike existingLike = createMomentLike();
        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(existingLike));

        // When
        boolean isLiked = likeService.toggle(createMoment(), createMember());

        // Then
        assertThat(isLiked).isFalse();
        assertThat(existingLike.isDeleted()).isTrue();
    }

    @Test
    void 좋아요_토글_재활성화() {
        // Given
        MomentLike deletedLike = createDeletedMomentLike();
        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.of(deletedLike));

        // When
        boolean isLiked = likeService.toggle(createMoment(), createMember());

        // Then
        assertThat(isLiked).isTrue();
        assertThat(deletedLike.isDeleted()).isFalse();
    }

    @Test
    void 좋아요_시_이벤트_발행() {
        // Given
        Moment moment = createMomentWithOwner(1L);
        GroupMember member = createMemberWithUser(2L);  // 다른 사용자
        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(moment, member);

        // Then
        verify(eventPublisher).publishEvent(any(MomentLikeEvent.class));
    }

    @Test
    void 자기_글_좋아요_시_이벤트_미발행() {
        // Given
        Moment moment = createMomentWithOwner(1L);
        GroupMember member = createMemberWithUser(1L);  // 같은 사용자
        when(likeRepository.findByMomentIdAndMemberIdIncludeDeleted(anyLong(), anyLong()))
            .thenReturn(Optional.empty());
        when(likeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        likeService.toggle(moment, member);

        // Then
        verify(eventPublisher, never()).publishEvent(any());
    }
}
```

### 6.3 검증
```bash
./gradlew fastTest
```

---

## Step 7: 기존 Moment/Comment 서비스 수정

### 7.1 대상 파일

#### `moment/service/moment/MomentService.java` 수정
```java
// 기존 메서드 유지 + 그룹 컨텍스트 메서드 추가

@Transactional
public Moment createInGroup(User momenter, Group group, GroupMember member, String content) {
    Moment moment = new Moment(momenter, group, member, content);
    return momentRepository.save(moment);
}

public List<Moment> getByGroup(Long groupId, Long cursor, int limit) {
    // 커서 기반 페이지네이션
    if (cursor == null) {
        return momentRepository.findByGroupIdOrderByIdDesc(groupId, PageRequest.of(0, limit));
    }
    return momentRepository.findByGroupIdAndIdLessThanOrderByIdDesc(groupId, cursor, PageRequest.of(0, limit));
}

public List<Moment> getMyMomentsInGroup(Long groupId, Long memberId, Long cursor, int limit) {
    if (cursor == null) {
        return momentRepository.findByGroupIdAndMemberIdOrderByIdDesc(groupId, memberId, PageRequest.of(0, limit));
    }
    return momentRepository.findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc(
        groupId, memberId, cursor, PageRequest.of(0, limit)
    );
}
```

#### `comment/service/comment/CommentService.java` 수정
```java
// 기존 메서드 유지 + 멤버 컨텍스트 메서드 추가

@Transactional
public Comment createWithMember(Moment moment, User commenter, GroupMember member, String content) {
    Comment comment = new Comment(moment, commenter, member, content);
    return commentRepository.save(comment);
}
```

### 7.2 MomentRepository 수정
```java
// 그룹 피드 조회 쿼리 추가
List<Moment> findByGroupIdOrderByIdDesc(Long groupId, Pageable pageable);

List<Moment> findByGroupIdAndIdLessThanOrderByIdDesc(Long groupId, Long cursor, Pageable pageable);

List<Moment> findByGroupIdAndMemberIdOrderByIdDesc(Long groupId, Long memberId, Pageable pageable);

List<Moment> findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc(
    Long groupId, Long memberId, Long cursor, Pageable pageable
);
```

### 7.3 TDD 테스트 케이스
```java
@Test
void 그룹_내_모멘트_생성() {
    // Given
    User momenter = createUser();
    Group group = createGroup();
    GroupMember member = createMember();

    // When
    Moment moment = momentService.createInGroup(momenter, group, member, "내용");

    // Then
    assertThat(moment.getGroup()).isEqualTo(group);
    assertThat(moment.getMember()).isEqualTo(member);
}

@Test
void 그룹_피드_조회_커서_기반() {
    // Given
    Long groupId = 1L;

    // When
    List<Moment> moments = momentService.getByGroup(groupId, null, 20);

    // Then
    assertThat(moments).isNotNull();
}
```

### 7.4 검증
```bash
./gradlew fastTest
```

---

## Step 8: Application 서비스 수정

### 8.1 대상 파일

#### `moment/service/application/MomentApplicationService.java` 수정
```java
// 그룹 피드 조회 메서드 추가
public GroupFeedResponse getGroupFeed(Long groupId, Long userId, Long cursor) {
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);
    List<Moment> moments = momentService.getByGroup(groupId, cursor, 20);

    // 각 모멘트의 좋아요 수, 본인 좋아요 여부 조회
    List<GroupMomentResponse> responses = moments.stream()
        .map(moment -> {
            long likeCount = momentLikeService.getCount(moment.getId());
            boolean hasLiked = momentLikeService.hasLiked(moment.getId(), member.getId());
            long commentCount = commentService.countByMomentId(moment.getId());
            return GroupMomentResponse.from(moment, likeCount, hasLiked, commentCount);
        })
        .toList();

    Long nextCursor = moments.isEmpty() ? null : moments.get(moments.size() - 1).getId();
    return new GroupFeedResponse(responses, nextCursor);
}

// 내 모멘트 조회 메서드 추가
public GroupFeedResponse getMyMomentsInGroup(Long groupId, Long userId, Long cursor) {
    GroupMember member = memberService.getByGroupAndUser(groupId, userId);
    List<Moment> moments = momentService.getMyMomentsInGroup(groupId, member.getId(), cursor, 20);

    List<GroupMomentResponse> responses = moments.stream()
        .map(moment -> {
            long likeCount = momentLikeService.getCount(moment.getId());
            boolean hasLiked = momentLikeService.hasLiked(moment.getId(), member.getId());
            long commentCount = commentService.countByMomentId(moment.getId());
            return GroupMomentResponse.from(moment, likeCount, hasLiked, commentCount);
        })
        .toList();

    Long nextCursor = moments.isEmpty() ? null : moments.get(moments.size() - 1).getId();
    return new GroupFeedResponse(responses, nextCursor);
}
```

### 8.2 검증
```bash
./gradlew fastTest
```

---

## 최종 검증

```bash
# 1. 컴파일 확인
./gradlew compileJava

# 2. 단위 테스트
./gradlew fastTest

# 3. 전체 테스트
./gradlew test

# 4. 빌드
./gradlew build
```

---

## 체크리스트

### 도메인 서비스 생성 완료
- [ ] GroupService (CRUD)
- [ ] GroupMemberService (가입/승인/강퇴/탈퇴/소유권이전)
- [ ] InviteLinkService (생성/조회/비활성화)

### Application 서비스 생성 완료
- [ ] GroupApplicationService
- [ ] GroupMemberApplicationService

### Like 서비스 생성 완료
- [ ] MomentLikeService (토글, 카운트, 여부 확인)
- [ ] CommentLikeService (토글, 카운트, 여부 확인)

### 기존 서비스 수정 완료
- [ ] MomentService (그룹 컨텍스트 메서드 추가)
- [ ] CommentService (멤버 컨텍스트 메서드 추가)
- [ ] MomentApplicationService (그룹 피드 조회 메서드 추가)
- [ ] CommentApplicationService (그룹 내 코멘트 조회 메서드 추가)

### 이벤트 클래스 생성 완료
- [ ] GroupJoinRequestEvent
- [ ] GroupJoinApprovedEvent
- [ ] GroupKickedEvent
- [ ] MomentLikeEvent
- [ ] CommentLikeEvent

### 테스트 완료
- [ ] GroupService 단위 테스트
- [ ] GroupMemberService 단위 테스트
- [ ] InviteLinkService 단위 테스트
- [ ] GroupApplicationService 단위 테스트
- [ ] GroupMemberApplicationService 단위 테스트
- [ ] MomentLikeService 단위 테스트
- [ ] CommentLikeService 단위 테스트

### 최종 검증
- [ ] `./gradlew compileJava` 성공
- [ ] `./gradlew fastTest` 성공
- [ ] `./gradlew test` 성공
- [ ] `./gradlew build` 성공

---

## 디렉토리 구조

```
src/main/java/moment/
├── group/
│   ├── domain/
│   ├── infrastructure/
│   ├── service/
│   │   ├── group/
│   │   │   ├── GroupService.java
│   │   │   └── GroupMemberService.java
│   │   ├── invite/
│   │   │   └── InviteLinkService.java
│   │   └── application/
│   │       ├── GroupApplicationService.java
│   │       └── GroupMemberApplicationService.java
│   └── dto/
│       ├── request/
│       ├── response/
│       └── event/
│           ├── GroupJoinRequestEvent.java
│           ├── GroupJoinApprovedEvent.java
│           └── GroupKickedEvent.java
├── like/
│   ├── domain/
│   ├── infrastructure/
│   ├── service/
│   │   ├── MomentLikeService.java
│   │   └── CommentLikeService.java
│   └── dto/
│       └── event/
│           ├── MomentLikeEvent.java
│           └── CommentLikeEvent.java
├── moment/
│   └── service/
│       ├── moment/
│       │   └── MomentService.java  (수정됨)
│       └── application/
│           └── MomentApplicationService.java  (수정됨)
└── comment/
    └── service/
        ├── comment/
        │   └── CommentService.java  (수정됨)
        └── application/
            └── CommentApplicationService.java  (수정됨)
```
