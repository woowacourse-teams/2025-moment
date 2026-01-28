# Phase 2: 그룹 관리 기능 구현 문서

## 개요
그룹 수정, 삭제, 복원 기능과 Admin 로그 기록 시스템을 TDD 방식으로 구현합니다.

## 선행 작업
- Phase 1 완료 (기본 조회 API)
- `AdminGroupLog` 엔티티 생성 (Phase 3, 4에서 활용)

## 대상 API

| # | 엔드포인트 | 설명 |
|---|-----------|------|
| 1 | `PUT /api/admin/groups/{groupId}` | 그룹 정보 수정 |
| 2 | `DELETE /api/admin/groups/{groupId}` | 그룹 삭제 |
| 3 | `POST /api/admin/groups/{groupId}/restore` | 그룹 복원 |

---

## TDD 테스트 목록

### 0. AdminGroupLog 엔티티 테스트 (2개)

#### 도메인 단위 테스트 (`AdminGroupLogTest`)
```
[ ] AdminGroupLog_생성_성공
[ ] AdminGroupLog_beforeValue_afterValue_JSON_저장
```

---

### 1. 그룹 정보 수정 API (6개)

#### E2E 테스트 (`AdminGroupUpdateApiTest`)
```
[ ] 그룹_정보_수정_성공_이름_설명_변경
[ ] 그룹_정보_수정_삭제된_그룹_수정시_400_AG003
[ ] 그룹_정보_수정_이름_누락시_400
[ ] 그룹_정보_수정_설명_누락시_400
[ ] 그룹_정보_수정_이름_30자_초과시_400
[ ] 그룹_정보_수정_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] updateGroup_이름_설명_변경_성공
[ ] updateGroup_삭제된_그룹_수정불가_예외
[ ] updateGroup_그룹없으면_예외
[ ] updateGroup_AdminGroupLog_기록_확인
[ ] updateGroup_beforeValue_afterValue_저장
```

---

### 2. 그룹 삭제 API (6개)

#### E2E 테스트 (`AdminGroupDeleteApiTest`)
```
[ ] 그룹_삭제_성공_SoftDelete
[ ] 그룹_삭제_이미_삭제된_그룹_삭제시_400_AG003
[ ] 그룹_삭제_성공_멤버_전체_SoftDelete
[ ] 그룹_삭제_성공_모멘트_전체_SoftDelete
[ ] 그룹_삭제_성공_코멘트_전체_SoftDelete
[ ] 그룹_삭제_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] deleteGroup_그룹_SoftDelete_성공
[ ] deleteGroup_이미_삭제된_그룹_예외
[ ] deleteGroup_그룹없으면_예외
[ ] deleteGroup_멤버_전체_삭제_확인
[ ] deleteGroup_모멘트_전체_삭제_확인
[ ] deleteGroup_코멘트_전체_삭제_확인
[ ] deleteGroup_AdminGroupLog_기록_확인
```

---

### 3. 그룹 복원 API (6개)

#### E2E 테스트 (`AdminGroupRestoreApiTest`)
```
[ ] 그룹_복원_성공
[ ] 그룹_복원_삭제되지않은_그룹_복원시_400_AG002
[ ] 그룹_복원_성공_멤버_전체_복원
[ ] 그룹_복원_성공_모멘트_전체_복원
[ ] 그룹_복원_성공_코멘트_전체_복원
[ ] 그룹_복원_그룹없으면_404
```

#### 서비스 단위 테스트 (`AdminGroupServiceTest`)
```
[ ] restoreGroup_그룹_복원_성공
[ ] restoreGroup_삭제되지않은_그룹_예외
[ ] restoreGroup_그룹없으면_예외
[ ] restoreGroup_멤버_전체_복원_확인
[ ] restoreGroup_모멘트_전체_복원_확인
[ ] restoreGroup_코멘트_전체_복원_확인
[ ] restoreGroup_AdminGroupLog_기록_확인
```

---

## 생성/수정 파일 목록

### 신규 생성

#### Entity
```
src/main/java/moment/admin/domain/
├── AdminGroupLog.java
└── AdminGroupLogType.java
```

#### Infrastructure
```
src/main/java/moment/admin/infrastructure/
└── AdminGroupLogRepository.java
```

#### DTO
```
src/main/java/moment/admin/dto/request/
└── AdminGroupUpdateRequest.java
```

#### Service
```
src/main/java/moment/admin/service/
└── AdminGroupLogService.java
```

#### Migration
```
src/main/resources/db/migration/mysql/
└── V{version}__create_admin_group_logs.sql
```

#### Test
```
src/test/java/moment/admin/
├── domain/
│   └── AdminGroupLogTest.java
├── presentation/
│   ├── AdminGroupUpdateApiTest.java
│   ├── AdminGroupDeleteApiTest.java
│   └── AdminGroupRestoreApiTest.java
└── service/
    ├── AdminGroupServiceTest.java (추가)
    └── AdminGroupLogServiceTest.java
```

### 수정

```
src/main/java/moment/admin/service/
└── AdminGroupService.java  (수정/삭제/복원 메서드 추가)

src/main/java/moment/admin/presentation/
└── AdminGroupApiController.java  (수정/삭제/복원 엔드포인트 추가)

src/main/java/moment/global/exception/
└── ErrorCode.java  (AG-002, AG-003 추가)
```

---

## 시그니처 정의

### Entity

```java
// AdminGroupLog.java
@Entity
@Table(name = "admin_group_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminGroupLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminGroupLogType type;

    @Column(nullable = false)
    private Long groupId;

    private Long targetId;  // 멤버, 모멘트, 코멘트 ID 등

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "JSON")
    private String beforeValue;

    @Column(columnDefinition = "JSON")
    private String afterValue;

    @Builder
    public AdminGroupLog(Long adminId, String adminEmail, AdminGroupLogType type,
                         Long groupId, Long targetId, String description,
                         String beforeValue, String afterValue) {
        // ...
    }
}

// AdminGroupLogType.java
public enum AdminGroupLogType {
    GROUP_UPDATE,
    GROUP_DELETE,
    GROUP_RESTORE,
    MEMBER_APPROVE,
    MEMBER_REJECT,
    MEMBER_KICK,
    OWNERSHIP_TRANSFER,
    MOMENT_DELETE,
    COMMENT_DELETE
}
```

### Repository

```java
// AdminGroupLogRepository.java
public interface AdminGroupLogRepository extends JpaRepository<AdminGroupLog, Long> {

    Page<AdminGroupLog> findByGroupId(Long groupId, Pageable pageable);

    List<AdminGroupLog> findByGroupIdAndType(Long groupId, AdminGroupLogType type);
}
```

### Request DTO

```java
// AdminGroupUpdateRequest.java
public record AdminGroupUpdateRequest(
    @NotBlank(message = "그룹명은 필수입니다")
    @Size(max = 30, message = "그룹명은 30자를 초과할 수 없습니다")
    String name,

    @NotBlank(message = "그룹 설명은 필수입니다")
    @Size(max = 200, message = "그룹 설명은 200자를 초과할 수 없습니다")
    String description
) {}
```

### Service

```java
// AdminGroupService.java (추가 메서드)
@Transactional
public void updateGroup(Long groupId, AdminGroupUpdateRequest request, Long adminId);

@Transactional
public void deleteGroup(Long groupId, Long adminId);

@Transactional
public void restoreGroup(Long groupId, Long adminId);

// AdminGroupLogService.java
@Service
@RequiredArgsConstructor
public class AdminGroupLogService {

    private final AdminGroupLogRepository adminGroupLogRepository;

    @Transactional
    public void log(Long adminId, String adminEmail, AdminGroupLogType type,
                    Long groupId, Long targetId, String description,
                    Object beforeValue, Object afterValue);
}
```

### Controller (추가 엔드포인트)

```java
// AdminGroupApiController.java (추가)
@PutMapping("/{groupId}")
public ResponseEntity<SuccessResponse<Void>> updateGroup(
    @PathVariable Long groupId,
    @Valid @RequestBody AdminGroupUpdateRequest request,
    @AdminAuth Long adminId
);

@DeleteMapping("/{groupId}")
public ResponseEntity<SuccessResponse<Void>> deleteGroup(
    @PathVariable Long groupId,
    @AdminAuth Long adminId
);

@PostMapping("/{groupId}/restore")
public ResponseEntity<SuccessResponse<Void>> restoreGroup(
    @PathVariable Long groupId,
    @AdminAuth Long adminId
);
```

### ErrorCode 추가

```java
// ErrorCode.java (추가)
ADMIN_GROUP_NOT_DELETED("AG-002", "삭제되지 않은 그룹은 복원할 수 없습니다.", HttpStatus.BAD_REQUEST),
ADMIN_GROUP_ALREADY_DELETED("AG-003", "이미 삭제된 그룹입니다.", HttpStatus.BAD_REQUEST),
```

---

## Flyway 마이그레이션

```sql
-- V{version}__create_admin_group_logs.sql
CREATE TABLE admin_group_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    group_id BIGINT NOT NULL,
    target_id BIGINT,
    description TEXT,
    before_value JSON,
    after_value JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    INDEX idx_admin_group_logs_group_id (group_id),
    INDEX idx_admin_group_logs_admin_id (admin_id),
    INDEX idx_admin_group_logs_type (type),
    INDEX idx_admin_group_logs_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 구현 순서

1. **ErrorCode 추가** → `AG-002`, `AG-003`
2. **Enum 생성** → `AdminGroupLogType`
3. **Entity 생성** → `AdminGroupLog`
4. **Flyway 마이그레이션** 추가
5. **Repository 생성** → `AdminGroupLogRepository`
6. **Service 구현** → `AdminGroupLogService`
7. **AdminGroupService 확장** → 수정/삭제/복원 메서드
8. **Controller 확장** → 수정/삭제/복원 엔드포인트
9. **테스트 작성 및 통과**

---

## Side Effects 처리

### 그룹 삭제 시
```java
@Transactional
public void deleteGroup(Long groupId, Long adminId) {
    Group group = findGroupIncludingDeleted(groupId);
    validateNotDeleted(group);

    // 1. 그룹 soft delete
    group.delete();

    // 2. 멤버 전체 soft delete
    groupMemberRepository.softDeleteByGroupId(groupId);

    // 3. 모멘트 전체 soft delete
    momentRepository.softDeleteByGroupId(groupId);

    // 4. 코멘트 전체 soft delete
    commentRepository.softDeleteByGroupId(groupId);

    // 5. 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.GROUP_DELETE, ...);
}
```

### 그룹 복원 시
```java
@Transactional
public void restoreGroup(Long groupId, Long adminId) {
    Group group = findGroupIncludingDeleted(groupId);
    validateIsDeleted(group);

    // 1. 그룹 복원
    group.restore();

    // 2. 멤버 전체 복원
    groupMemberRepository.restoreByGroupId(groupId);

    // 3. 모멘트 전체 복원
    momentRepository.restoreByGroupId(groupId);

    // 4. 코멘트 전체 복원
    commentRepository.restoreByGroupId(groupId);

    // 5. 로그 기록
    adminGroupLogService.log(..., AdminGroupLogType.GROUP_RESTORE, ...);
}
```

---

## 테스트 총 개수: ~20개

| 카테고리 | E2E | 단위 | 합계 |
|---------|-----|-----|------|
| AdminGroupLog 엔티티 | 0 | 2 | 2 |
| 그룹 정보 수정 | 6 | 5 | 11 |
| 그룹 삭제 | 6 | 7 | 13 |
| 그룹 복원 | 6 | 7 | 13 |
| **총계** | **18** | **21** | **39** |

> 참고: 실제 구현 시 테스트 케이스는 조정될 수 있습니다.
