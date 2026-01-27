# Phase 3: 멤버 관리 기능 구현 문서

## 개요
멤버 승인, 거절, 강제 추방, 소유권 이전 기능을 TDD 방식으로 구현합니다.

## 선행 작업
- Phase 1 완료 (기본 조회 API)
- Phase 2 완료 (`AdminGroupLog` 엔티티)

## 대상 API

| # | 엔드포인트 | 설명 |
|---|-----------|------|
| 1 | `POST /api/admin/groups/{groupId}/members/{memberId}/approve` | 멤버 승인 |
| 2 | `POST /api/admin/groups/{groupId}/members/{memberId}/reject` | 멤버 거절 |
| 3 | `DELETE /api/admin/groups/{groupId}/members/{memberId}` | 멤버 강제 추방 |
| 4 | `POST /api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}` | 소유권 이전 |

---

## TDD 테스트 목록

### 1. 멤버 승인 API (5개)

#### E2E 테스트 (`AdminMemberApproveApiTest`)
```
[ ] 멤버_승인_성공_PENDING에서_APPROVED로_변경
[ ] 멤버_승인_이미_승인된_멤버_승인시_400_AM006
[ ] 멤버_승인_PENDING_아닌_멤버_승인시_400_AM003
[ ] 멤버_승인_멤버없으면_404_AM001
[ ] 멤버_승인_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] approveMember_상태변경_PENDING_to_APPROVED
[ ] approveMember_이미_승인된_멤버_예외
[ ] approveMember_PENDING_아닌_멤버_예외
[ ] approveMember_멤버없으면_예외
[ ] approveMember_그룹없으면_예외
[ ] approveMember_AdminGroupLog_기록_확인
```

---

### 2. 멤버 거절 API (5개)

#### E2E 테스트 (`AdminMemberRejectApiTest`)
```
[ ] 멤버_거절_성공_멤버십_SoftDelete
[ ] 멤버_거절_PENDING_아닌_멤버_거절시_400_AM003
[ ] 멤버_거절_이미_거절된_멤버_거절시_400_AM007
[ ] 멤버_거절_멤버없으면_404_AM001
[ ] 멤버_거절_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] rejectMember_멤버십_SoftDelete_성공
[ ] rejectMember_PENDING_아닌_멤버_예외
[ ] rejectMember_이미_거절된_멤버_예외
[ ] rejectMember_멤버없으면_예외
[ ] rejectMember_그룹없으면_예외
[ ] rejectMember_AdminGroupLog_기록_확인
```

---

### 3. 멤버 강제 추방 API (7개)

#### E2E 테스트 (`AdminMemberKickApiTest`)
```
[ ] 멤버_강제추방_성공_상태변경_및_SoftDelete
[ ] 멤버_강제추방_Owner_추방시_400_AM002
[ ] 멤버_강제추방_APPROVED_아닌_멤버_추방시_400
[ ] 멤버_강제추방_성공_해당멤버_모멘트_SoftDelete
[ ] 멤버_강제추방_성공_해당멤버_코멘트_SoftDelete
[ ] 멤버_강제추방_멤버없으면_404_AM001
[ ] 멤버_강제추방_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] kickMember_상태변경_APPROVED_to_KICKED_및_SoftDelete
[ ] kickMember_Owner_추방불가_예외
[ ] kickMember_APPROVED_아닌_멤버_예외
[ ] kickMember_해당멤버_모멘트_전체_삭제
[ ] kickMember_해당멤버_코멘트_전체_삭제
[ ] kickMember_멤버없으면_예외
[ ] kickMember_그룹없으면_예외
[ ] kickMember_AdminGroupLog_기록_확인
```

---

### 4. 소유권 이전 API (5개)

#### E2E 테스트 (`AdminOwnershipTransferApiTest`)
```
[ ] 소유권_이전_성공_기존Owner_MEMBER로_새멤버_OWNER로
[ ] 소유권_이전_APPROVED_아닌_멤버에게_이전시_400_AM004
[ ] 소유권_이전_이미_OWNER인_멤버에게_이전시_400_AM005
[ ] 소유권_이전_멤버없으면_404_AM001
[ ] 소유권_이전_그룹없으면_404_AG001
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] transferOwnership_기존_Owner_역할변경_MEMBER
[ ] transferOwnership_새_Owner_역할변경_OWNER
[ ] transferOwnership_APPROVED_아닌_멤버_예외
[ ] transferOwnership_이미_OWNER인_멤버_예외
[ ] transferOwnership_멤버없으면_예외
[ ] transferOwnership_그룹없으면_예외
[ ] transferOwnership_AdminGroupLog_기록_확인
```

---

## 생성/수정 파일 목록

### 신규 생성

#### Test
```
src/test/java/moment/admin/presentation/
├── AdminMemberApproveApiTest.java
├── AdminMemberRejectApiTest.java
├── AdminMemberKickApiTest.java
└── AdminOwnershipTransferApiTest.java

src/test/java/moment/admin/service/
└── AdminGroupMemberServiceTest.java (확장)
```

### 수정

```
src/main/java/moment/admin/service/
└── AdminGroupMemberService.java  (승인/거절/추방/이전 메서드 추가)

src/main/java/moment/admin/presentation/
└── AdminGroupApiController.java  (엔드포인트 추가)

src/main/java/moment/group/infrastructure/
└── GroupMemberRepository.java  (메서드 추가)

src/main/java/moment/moment/infrastructure/
└── MomentRepository.java  (메서드 추가)

src/main/java/moment/comment/infrastructure/
└── CommentRepository.java  (메서드 추가)

src/main/java/moment/global/exception/
└── ErrorCode.java  (AM-001 ~ AM-007 추가)
```

---

## 시그니처 정의

### Service (AdminGroupMemberService 확장)

```java
// AdminGroupMemberService.java (추가 메서드)
@Transactional
public void approveMember(Long groupId, Long memberId, Long adminId);

@Transactional
public void rejectMember(Long groupId, Long memberId, Long adminId);

@Transactional
public void kickMember(Long groupId, Long memberId, Long adminId);

@Transactional
public void transferOwnership(Long groupId, Long newOwnerMemberId, Long adminId);
```

### Controller (추가 엔드포인트)

```java
// AdminGroupApiController.java (추가)
@PostMapping("/{groupId}/members/{memberId}/approve")
public ResponseEntity<SuccessResponse<Void>> approveMember(
    @PathVariable Long groupId,
    @PathVariable Long memberId,
    @AdminAuth Long adminId
);

@PostMapping("/{groupId}/members/{memberId}/reject")
public ResponseEntity<SuccessResponse<Void>> rejectMember(
    @PathVariable Long groupId,
    @PathVariable Long memberId,
    @AdminAuth Long adminId
);

@DeleteMapping("/{groupId}/members/{memberId}")
public ResponseEntity<SuccessResponse<Void>> kickMember(
    @PathVariable Long groupId,
    @PathVariable Long memberId,
    @AdminAuth Long adminId
);

@PostMapping("/{groupId}/transfer-ownership/{newOwnerMemberId}")
public ResponseEntity<SuccessResponse<Void>> transferOwnership(
    @PathVariable Long groupId,
    @PathVariable Long newOwnerMemberId,
    @AdminAuth Long adminId
);
```

### ErrorCode 추가

```java
// ErrorCode.java (추가)
ADMIN_MEMBER_NOT_FOUND("AM-001", "멤버를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
ADMIN_CANNOT_KICK_OWNER("AM-002", "그룹장은 추방할 수 없습니다.", HttpStatus.BAD_REQUEST),
ADMIN_MEMBER_NOT_PENDING("AM-003", "승인 대기 중인 멤버가 아닙니다.", HttpStatus.BAD_REQUEST),
ADMIN_MEMBER_NOT_APPROVED("AM-004", "승인된 멤버만 그룹장이 될 수 있습니다.", HttpStatus.BAD_REQUEST),
ADMIN_ALREADY_OWNER("AM-005", "이미 그룹장인 멤버입니다.", HttpStatus.BAD_REQUEST),
ADMIN_ALREADY_APPROVED("AM-006", "이미 승인된 멤버입니다.", HttpStatus.BAD_REQUEST),
ADMIN_ALREADY_REJECTED("AM-007", "이미 거절/삭제된 멤버입니다.", HttpStatus.BAD_REQUEST),
```

### Repository 메서드 추가

```java
// GroupMemberRepository.java (추가)
@Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.id = :memberId")
Optional<GroupMember> findByGroupIdAndMemberId(
    @Param("groupId") Long groupId,
    @Param("memberId") Long memberId
);

@Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role = 'OWNER' AND gm.deletedAt IS NULL")
Optional<GroupMember> findOwnerByGroupId(@Param("groupId") Long groupId);

// MomentRepository.java (추가)
@Modifying
@Query("UPDATE Moment m SET m.deletedAt = CURRENT_TIMESTAMP WHERE m.member.id = :memberId AND m.deletedAt IS NULL")
int softDeleteByMemberId(@Param("memberId") Long memberId);

@Modifying
@Query("UPDATE Moment m SET m.deletedAt = NULL WHERE m.member.id = :memberId")
int restoreByMemberId(@Param("memberId") Long memberId);

// CommentRepository.java (추가)
@Modifying
@Query("UPDATE Comment c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.member.id = :memberId AND c.deletedAt IS NULL")
int softDeleteByMemberId(@Param("memberId") Long memberId);

@Modifying
@Query("UPDATE Comment c SET c.deletedAt = NULL WHERE c.member.id = :memberId")
int restoreByMemberId(@Param("memberId") Long memberId);
```

---

## 구현 순서

1. **ErrorCode 추가** → `AM-001` ~ `AM-007`
2. **Repository 메서드 추가**
3. **AdminGroupMemberService 확장** → 승인/거절/추방/이전 메서드
4. **Controller 확장** → 멤버 관리 엔드포인트
5. **테스트 작성 및 통과**

---

## 비즈니스 로직 상세

### 멤버 승인
```java
@Transactional
public void approveMember(Long groupId, Long memberId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    GroupMember member = findMemberOrThrow(groupId, memberId);

    // 검증
    if (member.getStatus() == MemberStatus.APPROVED) {
        throw new MomentException(ErrorCode.ADMIN_ALREADY_APPROVED);
    }
    if (member.getStatus() != MemberStatus.PENDING) {
        throw new MomentException(ErrorCode.ADMIN_MEMBER_NOT_PENDING);
    }

    // 상태 변경
    member.approve();

    // 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.MEMBER_APPROVE, ...);
}
```

### 멤버 거절
```java
@Transactional
public void rejectMember(Long groupId, Long memberId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    GroupMember member = findMemberOrThrow(groupId, memberId);

    // 검증
    if (member.isDeleted()) {
        throw new MomentException(ErrorCode.ADMIN_ALREADY_REJECTED);
    }
    if (member.getStatus() != MemberStatus.PENDING) {
        throw new MomentException(ErrorCode.ADMIN_MEMBER_NOT_PENDING);
    }

    // Soft Delete
    member.delete();

    // 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.MEMBER_REJECT, ...);
}
```

### 멤버 강제 추방
```java
@Transactional
public void kickMember(Long groupId, Long memberId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    GroupMember member = findMemberOrThrow(groupId, memberId);

    // 검증
    if (member.getRole() == MemberRole.OWNER) {
        throw new MomentException(ErrorCode.ADMIN_CANNOT_KICK_OWNER);
    }
    if (member.getStatus() != MemberStatus.APPROVED) {
        throw new MomentException(ErrorCode.ADMIN_MEMBER_NOT_APPROVED);
    }

    // 상태 변경 및 Soft Delete
    member.kick();  // status = KICKED, deletedAt = now

    // 해당 멤버의 콘텐츠 삭제
    momentRepository.softDeleteByMemberId(memberId);
    commentRepository.softDeleteByMemberId(memberId);

    // 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.MEMBER_KICK, ...);
}
```

### 소유권 이전
```java
@Transactional
public void transferOwnership(Long groupId, Long newOwnerMemberId, Long adminId) {
    Group group = findGroupOrThrow(groupId);
    GroupMember newOwner = findMemberOrThrow(groupId, newOwnerMemberId);
    GroupMember currentOwner = findOwnerByGroupId(groupId);

    // 검증
    if (newOwner.getStatus() != MemberStatus.APPROVED) {
        throw new MomentException(ErrorCode.ADMIN_MEMBER_NOT_APPROVED);
    }
    if (newOwner.getRole() == MemberRole.OWNER) {
        throw new MomentException(ErrorCode.ADMIN_ALREADY_OWNER);
    }

    // 역할 변경
    currentOwner.changeRole(MemberRole.MEMBER);
    newOwner.changeRole(MemberRole.OWNER);

    // 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.OWNERSHIP_TRANSFER, ...);
}
```

---

## 복원 정책 참고

> **중요**: 멤버 강제 추방으로 삭제된 모멘트/코멘트는 **그룹 복원 시 함께 복원되지 않습니다**.
>
> 추방된 멤버의 콘텐츠는 영구적으로 비활성화되며, 별도 복원 API가 제공되지 않습니다.

---

## 테스트 총 개수: ~22개

| 카테고리 | E2E | 단위 | 합계 |
|---------|-----|-----|------|
| 멤버 승인 | 5 | 6 | 11 |
| 멤버 거절 | 5 | 6 | 11 |
| 멤버 강제 추방 | 7 | 8 | 15 |
| 소유권 이전 | 5 | 7 | 12 |
| **총계** | **22** | **27** | **49** |

> 참고: 실제 구현 시 테스트 케이스는 조정될 수 있습니다.
