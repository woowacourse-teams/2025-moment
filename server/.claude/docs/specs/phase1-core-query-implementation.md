# Phase 1: 핵심 조회 기능 구현 문서

## 개요
Admin 그룹 관리의 기반이 되는 조회 API를 TDD 방식으로 구현합니다.

## 대상 API

| # | 엔드포인트 | 설명 |
|---|-----------|------|
| 1 | `GET /api/admin/groups/stats` | 그룹 통계 조회 |
| 2 | `GET /api/admin/groups` | 그룹 목록 조회 |
| 3 | `GET /api/admin/groups/{groupId}` | 그룹 상세 조회 |
| 4 | `GET /api/admin/groups/{groupId}/members` | 승인된 멤버 목록 조회 |
| 5 | `GET /api/admin/groups/{groupId}/pending-members` | 대기 멤버 목록 조회 |

---

## TDD 테스트 목록

### 1. 그룹 통계 조회 API (5개)

#### E2E 테스트 (`AdminGroupStatsApiTest`)
```
[ ] 그룹_통계_조회_성공_전체_그룹_수_반환
[ ] 그룹_통계_조회_성공_활성_삭제_그룹_수_분리
[ ] 그룹_통계_조회_성공_전체_멤버_수_집계
[ ] 그룹_통계_조회_성공_오늘_생성된_그룹_수
[ ] 그룹_통계_조회_인증없이_접근시_401
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] getGroupStats_전체_그룹_수_정확히_반환
[ ] getGroupStats_활성_그룹과_삭제된_그룹_구분
[ ] getGroupStats_전체_멤버_수_APPROVED만_집계
[ ] getGroupStats_오늘_생성된_그룹_수_정확히_반환
[ ] getGroupStats_빈_데이터일_때_0_반환
```

---

### 2. 그룹 목록 조회 API (8개)

#### E2E 테스트 (`AdminGroupListApiTest`)
```
[ ] 그룹_목록_조회_성공_페이지네이션_기본값
[ ] 그룹_목록_조회_성공_키워드_검색_그룹명
[ ] 그룹_목록_조회_성공_키워드_검색_그룹장명
[ ] 그룹_목록_조회_성공_상태필터_ACTIVE
[ ] 그룹_목록_조회_성공_상태필터_DELETED
[ ] 그룹_목록_조회_성공_상태필터_ALL_기본값
[ ] 그룹_목록_조회_성공_정렬_createdAt_DESC
[ ] 그룹_목록_조회_성공_삭제된_그룹도_포함
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] getGroupList_페이지네이션_적용
[ ] getGroupList_키워드_검색_그룹명_일치
[ ] getGroupList_키워드_검색_그룹장명_일치
[ ] getGroupList_상태필터_ACTIVE_활성그룹만
[ ] getGroupList_상태필터_DELETED_삭제그룹만
[ ] getGroupList_상태필터_ALL_전체그룹
[ ] getGroupList_정렬_최신순
[ ] getGroupList_Owner_정보_포함
```

---

### 3. 그룹 상세 조회 API (5개)

#### E2E 테스트 (`AdminGroupDetailApiTest`)
```
[ ] 그룹_상세_조회_성공_기본정보
[ ] 그룹_상세_조회_성공_삭제된_그룹도_조회가능
[ ] 그룹_상세_조회_성공_멤버수_대기멤버수_정확성
[ ] 그룹_상세_조회_성공_모멘트수_코멘트수_정확성
[ ] 그룹_상세_조회_존재하지않는_그룹_404
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] getGroupDetail_기본정보_반환
[ ] getGroupDetail_삭제된_그룹도_조회
[ ] getGroupDetail_멤버수_APPROVED만_카운트
[ ] getGroupDetail_대기멤버수_PENDING만_카운트
[ ] getGroupDetail_모멘트수_삭제되지않은것만
[ ] getGroupDetail_코멘트수_삭제되지않은것만
[ ] getGroupDetail_초대링크_정보_포함
[ ] getGroupDetail_존재하지않으면_예외
```

---

### 4. 승인된 멤버 목록 조회 API (4개)

#### E2E 테스트 (`AdminGroupMemberListApiTest`)
```
[ ] 승인된_멤버_목록_조회_성공_APPROVED만
[ ] 승인된_멤버_목록_조회_성공_페이지네이션
[ ] 승인된_멤버_목록_조회_성공_정렬_joinedAt_DESC
[ ] 승인된_멤버_목록_조회_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] getApprovedMembers_APPROVED_상태만_반환
[ ] getApprovedMembers_페이지네이션_적용
[ ] getApprovedMembers_정렬_최근가입순
[ ] getApprovedMembers_유저정보_포함
[ ] getApprovedMembers_그룹없으면_예외
```

---

### 5. 대기 멤버 목록 조회 API (3개)

#### E2E 테스트 (`AdminGroupPendingMemberListApiTest`)
```
[ ] 대기_멤버_목록_조회_성공_PENDING만
[ ] 대기_멤버_목록_조회_성공_정렬_createdAt_ASC
[ ] 대기_멤버_목록_조회_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupMemberServiceTest`)
```
[ ] getPendingMembers_PENDING_상태만_반환
[ ] getPendingMembers_정렬_오래된요청우선
[ ] getPendingMembers_유저정보_포함
[ ] getPendingMembers_그룹없으면_예외
```

---

## 생성/수정 파일 목록

### 신규 생성

#### DTO
```
src/main/java/moment/admin/dto/response/
├── AdminGroupStatsResponse.java
├── AdminGroupListResponse.java
├── AdminGroupDetailResponse.java
├── AdminGroupMemberResponse.java
├── AdminGroupOwnerInfo.java
├── AdminMemberUserInfo.java
└── AdminInviteLinkInfo.java
```

#### Enum
```
src/main/java/moment/admin/domain/
└── GroupStatusFilter.java
```

#### Service
```
src/main/java/moment/admin/service/
├── AdminGroupService.java
└── AdminGroupMemberService.java
```

#### Controller
```
src/main/java/moment/admin/presentation/
└── AdminGroupApiController.java
```

#### Test
```
src/test/java/moment/admin/
├── presentation/
│   ├── AdminGroupStatsApiTest.java
│   ├── AdminGroupListApiTest.java
│   ├── AdminGroupDetailApiTest.java
│   ├── AdminGroupMemberListApiTest.java
│   └── AdminGroupPendingMemberListApiTest.java
└── service/
    ├── AdminGroupServiceTest.java
    └── AdminGroupMemberServiceTest.java
```

### 수정

```
src/main/java/moment/group/infrastructure/
└── GroupRepository.java  (메서드 추가)

src/main/java/moment/group/infrastructure/
└── GroupMemberRepository.java  (메서드 추가)
```

---

## 시그니처 정의

### Response DTO

```java
// AdminGroupStatsResponse.java
public record AdminGroupStatsResponse(
    long totalGroups,
    long activeGroups,
    long deletedGroups,
    long totalMembers,
    long totalMoments,
    long todayCreatedGroups
) {}

// AdminGroupListResponse.java
public record AdminGroupListResponse(
    List<AdminGroupSummary> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public record AdminGroupSummary(
        Long groupId,
        String name,
        String description,
        int memberCount,
        int momentCount,
        AdminGroupOwnerInfo owner,
        LocalDateTime createdAt,
        LocalDateTime deletedAt,
        boolean isDeleted
    ) {}
}

// AdminGroupDetailResponse.java
public record AdminGroupDetailResponse(
    Long groupId,
    String name,
    String description,
    int memberCount,
    int pendingMemberCount,
    int momentCount,
    int commentCount,
    AdminGroupOwnerInfo owner,
    AdminInviteLinkInfo inviteLink,
    LocalDateTime createdAt,
    LocalDateTime deletedAt,
    boolean isDeleted
) {}

// AdminGroupMemberResponse.java (페이지네이션 래퍼)
public record AdminGroupMemberListResponse(
    List<AdminGroupMemberResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}

public record AdminGroupMemberResponse(
    Long memberId,
    String nickname,
    String role,
    String status,
    LocalDateTime joinedAt,
    AdminMemberUserInfo user
) {}

// 공통 DTO
public record AdminGroupOwnerInfo(
    Long memberId,
    String nickname,
    Long userId,
    String userEmail
) {}

public record AdminMemberUserInfo(
    Long userId,
    String email,
    String nickname
) {}

public record AdminInviteLinkInfo(
    String code,
    LocalDateTime expiresAt,
    boolean isActive,
    boolean isExpired
) {}
```

### Enum

```java
// GroupStatusFilter.java
public enum GroupStatusFilter {
    ACTIVE,
    DELETED,
    ALL
}
```

### Service

```java
// AdminGroupService.java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupService {

    public AdminGroupStatsResponse getGroupStats();

    public AdminGroupListResponse getGroupList(int page, int size, String keyword, GroupStatusFilter status);

    public AdminGroupDetailResponse getGroupDetail(Long groupId);
}

// AdminGroupMemberService.java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminGroupMemberService {

    public AdminGroupMemberListResponse getApprovedMembers(Long groupId, int page, int size);

    public AdminGroupMemberListResponse getPendingMembers(Long groupId, int page, int size);
}
```

### Controller

```java
// AdminGroupApiController.java
@RestController
@RequestMapping("/api/admin/groups")
@RequiredArgsConstructor
public class AdminGroupApiController {

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse<AdminGroupStatsResponse>> getGroupStats();

    @GetMapping
    public ResponseEntity<SuccessResponse<AdminGroupListResponse>> getGroupList(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "ALL") GroupStatusFilter status
    );

    @GetMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<AdminGroupDetailResponse>> getGroupDetail(
        @PathVariable Long groupId
    );

    @GetMapping("/{groupId}/members")
    public ResponseEntity<SuccessResponse<AdminGroupMemberListResponse>> getApprovedMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );

    @GetMapping("/{groupId}/pending-members")
    public ResponseEntity<SuccessResponse<AdminGroupMemberListResponse>> getPendingMembers(
        @PathVariable Long groupId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    );
}
```

### Repository 메서드 추가

```java
// GroupRepository.java 추가 메서드
@Query("SELECT g FROM Group g WHERE g.deletedAt IS NULL OR g.deletedAt IS NOT NULL")
Page<Group> findAllIncludingDeleted(Pageable pageable);

@Query("SELECT g FROM Group g WHERE g.id = :id")
Optional<Group> findByIdIncludingDeleted(@Param("id") Long id);

@Query("SELECT COUNT(g) FROM Group g WHERE g.deletedAt IS NULL")
long countActiveGroups();

@Query("SELECT COUNT(g) FROM Group g WHERE g.deletedAt IS NOT NULL")
long countDeletedGroups();

@Query("SELECT COUNT(g) FROM Group g WHERE DATE(g.createdAt) = CURRENT_DATE")
long countTodayCreatedGroups();

// GroupMemberRepository.java 추가 메서드
@Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.status = 'APPROVED' AND gm.deletedAt IS NULL")
long countTotalApprovedMembers();

Page<GroupMember> findByGroupIdAndStatusAndDeletedAtIsNull(
    Long groupId, MemberStatus status, Pageable pageable
);
```

---

## 구현 순서

1. **ErrorCode 추가** → `AG-001` (그룹 미존재)
2. **Enum 생성** → `GroupStatusFilter`
3. **DTO 생성** → Response DTO들
4. **Repository 메서드 추가**
5. **Service 구현** → `AdminGroupService`, `AdminGroupMemberService`
6. **Controller 구현** → `AdminGroupApiController`
7. **테스트 작성 및 통과**

---

## 테스트 총 개수: ~25개

| 카테고리 | E2E | 단위 | 합계 |
|---------|-----|-----|------|
| 그룹 통계 | 5 | 5 | 10 |
| 그룹 목록 | 8 | 8 | 16 |
| 그룹 상세 | 5 | 8 | 13 |
| 승인 멤버 | 4 | 5 | 9 |
| 대기 멤버 | 3 | 4 | 7 |
| **총계** | **25** | **30** | **55** |

> 참고: 실제 구현 시 테스트 케이스는 조정될 수 있습니다.
