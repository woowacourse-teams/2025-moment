# Admin 그룹 관리 API 스펙

## 1. 개요

### 1.1 목적
Admin 페이지에서 그룹을 관리하기 위한 REST API를 제공합니다. 프론트엔드 개발자가 Admin UI를 구현할 수 있도록 API를 설계합니다.

### 1.2 주요 기능
- 그룹 목록 조회 (활성 + 삭제된 그룹 포함)
- 그룹 상세 정보 조회 및 수정
- 그룹 강제 삭제 및 복원
- 그룹 멤버 관리 (조회, 승인, 거절, 강제 추방)
- 그룹 소유권 강제 이전
- 그룹 내 콘텐츠(모멘트/코멘트) 조회 및 삭제
- 초대 링크 조회
- 간단한 통계 제공

### 1.3 권한
- 모든 Admin (ADMIN, SUPER_ADMIN) 사용 가능
- 주요 작업에 대한 Admin 로그 기록

### 1.4 설계 원칙: 알림 미발송
Admin API는 **알림을 발송하지 않습니다**. 이 원칙의 이유:

1. **모듈 분리**: Admin 모듈과 Product 모듈(notification)의 코드 분리 유지
2. **의존성 최소화**: 향후 Admin 모듈을 별도 서비스로 분리할 때 notification 모듈에 대한 의존성 없음
3. **책임 분리**: Admin은 데이터 관리에만 집중, 알림은 Product 영역의 책임

---

## 2. API 엔드포인트

### 2.1 기본 정보
- **Base URL**: `/api/admin/groups`
- **인증**: Admin 세션 필수
- **응답 포맷**: `AdminSuccessResponse<T>`

### 2.2 날짜/시간 포맷
- **타임존**: 서버 타임존 (Asia/Seoul, KST)
- **포맷**: ISO-8601 (`yyyy-MM-dd'T'HH:mm:ss`)
- 예시: `2024-01-15T10:30:00`

### 2.3 Enum 정의

#### 그룹 상태 필터 (GroupStatusFilter)
| 값 | 설명 |
|----|------|
| ACTIVE | 활성 그룹 |
| DELETED | 삭제된 그룹 |
| ALL | 전체 (기본값) |

#### 멤버 역할 (MemberRole)
| 값 | 설명 |
|----|------|
| OWNER | 그룹장 |
| MEMBER | 일반 멤버 |

#### 멤버 상태 (MemberStatus)
| 값 | 설명 |
|----|------|
| PENDING | 가입 대기 |
| APPROVED | 승인됨 |
| KICKED | 추방됨 |

---

## 3. 그룹 API

### 3.1 그룹 통계 조회
간단한 그룹 관련 통계를 조회합니다.

```
GET /api/admin/groups/stats
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "totalGroups": 150,
    "activeGroups": 142,
    "deletedGroups": 8,
    "totalMembers": 1250,
    "totalMoments": 5600,
    "todayCreatedGroups": 3
  }
}
```

### 3.2 그룹 목록 조회
그룹 목록을 페이지네이션으로 조회합니다. 삭제된 그룹도 포함됩니다.

```
GET /api/admin/groups
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20, 최대: 100) |
| keyword | string | N | 그룹명 또는 그룹장명 검색어 |
| status | string | N | 상태 필터 (ACTIVE, DELETED, ALL) - 기본값: ALL |

**기본 정렬**: `createdAt DESC` (최신순)

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "groupId": 1,
        "name": "개발자 모임",
        "description": "개발자들의 일상 공유",
        "memberCount": 15,
        "momentCount": 120,
        "owner": {
          "memberId": 1,
          "nickname": "방장닉네임",
          "userId": 100,
          "userEmail": "owner@example.com"
        },
        "createdAt": "2024-01-15T10:30:00",
        "deletedAt": null,
        "isDeleted": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 3.3 그룹 상세 조회
그룹의 상세 정보를 조회합니다.

```
GET /api/admin/groups/{groupId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "groupId": 1,
    "name": "개발자 모임",
    "description": "개발자들의 일상을 공유하는 그룹입니다.",
    "memberCount": 15,
    "pendingMemberCount": 3,
    "momentCount": 120,
    "commentCount": 450,
    "owner": {
      "memberId": 1,
      "nickname": "방장닉네임",
      "userId": 100,
      "userEmail": "owner@example.com"
    },
    "inviteLink": {
      "code": "abc123-def456",
      "expiresAt": "2024-01-22T10:30:00",
      "isActive": true,
      "isExpired": false
    },
    "createdAt": "2024-01-15T10:30:00",
    "deletedAt": null,
    "isDeleted": false
  }
}
```

### 3.4 그룹 정보 수정
그룹의 기본 정보를 수정합니다.

```
PUT /api/admin/groups/{groupId}
```

**Request Body**
```json
{
  "name": "수정된 그룹명",
  "description": "수정된 그룹 설명"
}
```

**Validation**
- name: 1-30자, 필수
- description: 1-200자, 필수

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

### 3.5 그룹 삭제
그룹을 soft delete 합니다. 그룹 삭제 시 해당 그룹의 멤버, 모멘트, 코멘트도 함께 soft delete 됩니다.

```
DELETE /api/admin/groups/{groupId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 그룹 soft delete
- 그룹 멤버 전체 soft delete
- 그룹 모멘트 전체 soft delete
- 그룹 코멘트 전체 soft delete
- Admin 로그 기록

### 3.6 그룹 복원
삭제된 그룹을 복원합니다. 그룹과 함께 멤버, 모멘트, 코멘트도 복원됩니다.

```
POST /api/admin/groups/{groupId}/restore
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 그룹 복원 (deletedAt = null)
- 그룹 멤버 전체 복원
- 그룹 모멘트 전체 복원
- 그룹 코멘트 전체 복원
- Admin 로그 기록

**Error Cases**
- GROUP_NOT_FOUND: 그룹이 존재하지 않음
- GROUP_NOT_DELETED: 삭제되지 않은 그룹은 복원 불가

---

## 4. 멤버 API

### 4.1 승인된 멤버 목록 조회
그룹의 승인된 멤버 목록을 조회합니다.

```
GET /api/admin/groups/{groupId}/members
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

**기본 정렬**: `joinedAt DESC` (최근 가입순)

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "memberId": 1,
        "nickname": "멤버닉네임",
        "role": "OWNER",
        "status": "APPROVED",
        "joinedAt": "2024-01-15T10:30:00",
        "user": {
          "userId": 100,
          "email": "user@example.com",
          "nickname": "유저전체닉네임"
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

### 4.2 대기 멤버 목록 조회
그룹의 가입 대기 중인 멤버 목록을 조회합니다.

```
GET /api/admin/groups/{groupId}/pending-members
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

**기본 정렬**: `createdAt ASC` (오래된 요청 우선)

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "memberId": 5,
        "nickname": "신규멤버",
        "role": "MEMBER",
        "status": "PENDING",
        "createdAt": "2024-01-20T14:00:00",
        "user": {
          "userId": 105,
          "email": "newuser@example.com",
          "nickname": "신규유저"
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 3,
    "totalPages": 1
  }
}
```

### 4.3 멤버 가입 승인
대기 중인 멤버를 승인합니다.

```
POST /api/admin/groups/{groupId}/members/{memberId}/approve
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 멤버 상태 변경 (PENDING → APPROVED)
- Admin 로그 기록

### 4.4 멤버 가입 거절
대기 중인 멤버의 가입을 거절합니다.

```
POST /api/admin/groups/{groupId}/members/{memberId}/reject
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 멤버십 soft delete
- Admin 로그 기록

### 4.5 멤버 강제 추방
승인된 멤버를 강제로 추방합니다. Owner는 추방할 수 없습니다.

```
DELETE /api/admin/groups/{groupId}/members/{memberId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 멤버 상태 변경 (APPROVED → KICKED) 및 soft delete
- 해당 멤버의 모멘트 전체 soft delete
- 해당 멤버의 코멘트 전체 soft delete
- Admin 로그 기록

**Error Cases**
- CANNOT_KICK_OWNER: Owner는 추방 불가

**복원 정책**
- 멤버 강제 추방으로 삭제된 모멘트/코멘트는 **그룹 복원 시 함께 복원되지 않음**
- 추방된 멤버의 콘텐츠는 영구적으로 비활성화됨 (별도 복원 API 미제공)

### 4.6 그룹 소유권 강제 이전
그룹 소유권을 다른 멤버에게 강제로 이전합니다.

```
POST /api/admin/groups/{groupId}/transfer-ownership/{newOwnerMemberId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 기존 Owner → MEMBER로 역할 변경
- 새 Owner → OWNER로 역할 변경
- Admin 로그 기록

**Error Cases**
- MEMBER_NOT_FOUND: 멤버를 찾을 수 없음
- MEMBER_NOT_APPROVED: 승인된 멤버만 Owner가 될 수 있음
- ALREADY_OWNER: 이미 Owner인 멤버에게 이전 불가

---

## 5. 콘텐츠(모멘트/코멘트) API

### 5.1 그룹 모멘트 목록 조회
그룹의 모멘트 목록을 조회합니다.

```
GET /api/admin/groups/{groupId}/moments
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

**기본 정렬**: `createdAt DESC` (최신순)

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "momentId": 1,
        "content": "오늘 하루도 열심히!",
        "imageUrl": "https://...",
        "commentCount": 5,
        "likeCount": 10,
        "author": {
          "memberId": 2,
          "groupNickname": "작성자닉네임",
          "userId": 101,
          "userEmail": "author@example.com",
          "userNickname": "작성자전체닉네임"
        },
        "createdAt": "2024-01-20T15:00:00",
        "deletedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 120,
    "totalPages": 6
  }
}
```

### 5.2 모멘트 삭제
모멘트를 soft delete 합니다. 해당 모멘트의 코멘트도 함께 삭제됩니다.

```
DELETE /api/admin/groups/{groupId}/moments/{momentId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 모멘트 soft delete
- 해당 모멘트의 코멘트 전체 soft delete
- Admin 로그 기록

### 5.3 모멘트 코멘트 목록 조회
특정 모멘트의 코멘트 목록을 조회합니다.

```
GET /api/admin/groups/{groupId}/moments/{momentId}/comments
```

**Query Parameters**
| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | N | 페이지 번호 (기본값: 0) |
| size | int | N | 페이지 크기 (기본값: 20) |

**기본 정렬**: `createdAt ASC` (시간순)

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "content": [
      {
        "commentId": 1,
        "content": "응원해요!",
        "author": {
          "memberId": 3,
          "groupNickname": "댓글작성자",
          "userId": 102,
          "userEmail": "commenter@example.com",
          "userNickname": "댓글작성자전체"
        },
        "createdAt": "2024-01-20T16:00:00",
        "deletedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1
  }
}
```

### 5.4 코멘트 삭제
코멘트를 soft delete 합니다.

```
DELETE /api/admin/groups/{groupId}/comments/{commentId}
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": null
}
```

**Side Effects**
- 코멘트 soft delete
- Admin 로그 기록

---

## 6. 초대 링크 API

### 6.1 초대 링크 조회
그룹의 현재 초대 링크 정보를 조회합니다.

```
GET /api/admin/groups/{groupId}/invite-link
```

**Response**
```json
{
  "code": 200,
  "status": "OK",
  "data": {
    "code": "abc123-def456",
    "fullUrl": "https://moment.com/invite/abc123-def456",
    "expiresAt": "2024-01-22T10:30:00",
    "isActive": true,
    "isExpired": false,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 7. Admin 로그

### 7.1 로그 기록 대상 작업
다음 작업은 Admin 로그에 기록됩니다:

| 작업 | 로그 타입 |
|------|----------|
| 그룹 정보 수정 | GROUP_UPDATE |
| 그룹 삭제 | GROUP_DELETE |
| 그룹 복원 | GROUP_RESTORE |
| 멤버 가입 승인 | MEMBER_APPROVE |
| 멤버 가입 거절 | MEMBER_REJECT |
| 멤버 강제 추방 | MEMBER_KICK |
| 소유권 강제 이전 | OWNERSHIP_TRANSFER |
| 모멘트 삭제 | MOMENT_DELETE |
| 코멘트 삭제 | COMMENT_DELETE |

### 7.2 로그 구조
```java
public class AdminGroupLog extends BaseEntity {
    private Long id;
    private Long adminId;           // 작업 수행 Admin
    private String adminEmail;      // Admin 이메일 (스냅샷)
    private AdminGroupLogType type; // 로그 타입
    private Long groupId;           // 대상 그룹
    private Long targetId;          // 대상 ID (멤버, 모멘트, 코멘트 등)
    private String description;     // 상세 설명
    private String beforeValue;     // 변경 전 값 (JSON)
    private String afterValue;      // 변경 후 값 (JSON)
    private LocalDateTime createdAt;
}
```

---

## 8. Error Codes

### 8.1 그룹 관련
| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| AG-001 | 그룹을 찾을 수 없습니다. | 404 |
| AG-002 | 삭제되지 않은 그룹은 복원할 수 없습니다. | 400 |
| AG-003 | 이미 삭제된 그룹입니다. | 400 |

### 8.2 멤버 관련
| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| AM-001 | 멤버를 찾을 수 없습니다. | 404 |
| AM-002 | 그룹장은 추방할 수 없습니다. | 400 |
| AM-003 | 승인 대기 중인 멤버가 아닙니다. | 400 |
| AM-004 | 승인된 멤버만 그룹장이 될 수 있습니다. | 400 |
| AM-005 | 이미 그룹장인 멤버입니다. | 400 |
| AM-006 | 이미 승인된 멤버입니다. | 400 |
| AM-007 | 이미 거절/삭제된 멤버입니다. | 400 |

### 8.3 콘텐츠 관련
| 코드 | 메시지 | HTTP Status |
|------|--------|-------------|
| AC-001 | 모멘트를 찾을 수 없습니다. | 404 |
| AC-002 | 코멘트를 찾을 수 없습니다. | 404 |

---

## 9. 구현 우선순위

### Phase 1: 핵심 조회 기능
1. 그룹 통계 조회 API
2. 그룹 목록 조회 API (검색, 필터, 페이지네이션)
3. 그룹 상세 조회 API
4. 멤버 목록 조회 API
5. 대기 멤버 목록 조회 API

### Phase 2: 그룹 관리 기능
1. 그룹 정보 수정 API
2. 그룹 삭제 API
3. 그룹 복원 API
4. Admin 로그 엔티티 및 기록 로직

### Phase 3: 멤버 관리 기능
1. 멤버 가입 승인 API
2. 멤버 가입 거절 API
3. 멤버 강제 추방 API
4. 소유권 강제 이전 API

### Phase 4: 콘텐츠 관리 기능
1. 모멘트 목록 조회 API
2. 모멘트 삭제 API
3. 코멘트 목록 조회 API
4. 코멘트 삭제 API

### Phase 5: 부가 기능
1. 초대 링크 조회 API
2. Admin 로그 조회 API (선택)

---

## 10. 기술적 고려사항

### 10.1 트랜잭션 관리
- 그룹 삭제/복원 시 연관 데이터(멤버, 모멘트, 코멘트) 처리는 단일 트랜잭션에서 수행
- 대량 데이터 처리 시 배치 업데이트 사용 검토

### 10.2 성능
- 그룹 목록 조회 시 N+1 문제 방지를 위한 fetch join 또는 별도 쿼리 사용
- 통계 조회는 캐싱 검토 (빈번한 조회 예상)

### 10.3 보안
- Admin 세션 인증 필수
- 모든 수정/삭제 작업에 Admin 로그 기록
- 민감한 작업(삭제, 추방 등)에 대한 확인 절차는 프론트엔드에서 처리

### 10.4 Soft Delete 처리
- 삭제된 그룹 조회를 위해 `@SQLRestriction` 우회 쿼리 필요
- Repository에 `findAllIncludingDeleted`, `findByIdIncludingDeleted` 메서드 추가
- **삭제된 그룹에 대한 API 동작**:
  - 조회 API: 삭제된 그룹도 조회 가능 (`isDeleted` 필드로 구분)
  - 수정/삭제/멤버관리 API: 삭제된 그룹 대상 불가 → AG-003 에러 반환
  - 복원 API: 삭제된 그룹만 대상 가능

### 10.5 모듈 분리 원칙
- **Admin API는 알림을 발송하지 않음** (1.4 설계 원칙 참조)
- Product 도메인(notification 모듈)에 대한 의존성 없음
- 향후 Admin 모듈을 별도 서비스/모듈로 분리 시 독립적 배포 가능
- Admin 작업으로 인한 알림이 필요한 경우, Product 측에서 별도 이벤트 구독 방식으로 처리

---

## 11. 변경 이력

| 버전 | 날짜 | 변경 내용 |
|-----|------|----------|
| 1.0 | 2024-01-27 | 최초 작성 |
| 1.1 | 2026-01-27 | 알림 발송 기능 제거 (Admin/Product 모듈 분리 원칙 적용) |
| 1.2 | 2026-01-27 | 날짜/시간 포맷 규칙, Enum 정의, 중복 호출 에러코드, 목록 조회 기본 정렬, 삭제된 그룹 API 동작 규정, 멤버 추방 복원 정책 추가 |