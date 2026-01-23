# Phase 4: API 구현 완료 보고서 - 데이터 플로우 다이어그램

## 구현 완료 현황

| Controller | 엔드포인트 수 | 테스트 케이스 | 상태 |
|------------|-------------|-------------|------|
| GroupController | 5개 | 5개 | ✅ 완료 |
| GroupMemberController | 8개 | 8개 | ✅ 완료 |
| GroupInviteController | 3개 | 3개 | ✅ 완료 |
| GroupMomentController | 5개 | 5개 | ✅ 완료 |
| GroupCommentController | 4개 | 4개 | ✅ 완료 |
| **총계** | **25개** | **25개** | ✅ |

---

## 1. 그룹 생성 플로우

```
┌─────────────┐    POST /api/v2/groups    ┌───────────────────────┐
│   Client    │ ──────────────────────────▶│   GroupController     │
└─────────────┘   GroupCreateRequest       └───────────────────────┘
                  (name, description,                 │
                   ownerNickname)                     │
                                                      ▼
                                          ┌───────────────────────┐
                                          │ GroupApplicationService│
                                          └───────────────────────┘
                                                      │
                     ┌────────────────────────────────┼────────────────────────────────┐
                     ▼                                ▼                                ▼
         ┌───────────────────┐          ┌───────────────────┐          ┌───────────────────┐
         │   UserService     │          │   GroupService    │          │ InviteLinkService │
         │   getUserBy()     │          │   create()        │          │   create()        │
         └───────────────────┘          └───────────────────┘          └───────────────────┘
                                                  │                              │
                                                  ▼                              ▼
                                        ┌───────────────┐              ┌───────────────┐
                                        │ GroupMember   │              │ GroupInviteLink│
                                        │ (OWNER 생성)  │              │ (Code 생성)    │
                                        └───────────────┘              └───────────────┘
                                                  │                              │
                                                  └──────────────┬───────────────┘
                                                                 ▼
                                                    ┌───────────────────────┐
                                                    │  GroupCreateResponse  │
                                                    │  (groupId, name,      │
                                                    │   memberId, nickname, │
                                                    │   inviteCode)         │
                                                    └───────────────────────┘
```

---

## 2. 그룹 가입 플로우

```
┌─────────────┐    GET /api/v2/invite/{code}    ┌───────────────────────┐
│   Client    │ ────────────────────────────────▶│ GroupInviteController │
└─────────────┘                                  └───────────────────────┘
      │                                                     │
      │                                                     ▼
      │                                          ┌───────────────────────────┐
      │                                          │GroupMemberApplicationService│
      │                                          │   getInviteInfo()          │
      │                                          └───────────────────────────┘
      │                                                     │
      │                                                     ▼
      │                                          ┌───────────────────────┐
      │                                          │  InviteInfoResponse   │
      │                                          │  (groupId, groupName, │
      │◀────────────────────────────────────────│   memberCount)        │
      │                                          └───────────────────────┘
      │
      │    POST /api/v2/groups/join
      │    GroupJoinRequest(inviteCode, nickname)
      │
      ▼
┌───────────────────────┐          ┌───────────────────────────┐
│ GroupMemberController │────────▶│GroupMemberApplicationService│
└───────────────────────┘          │   joinGroup()               │
                                   └───────────────────────────┘
                                              │
                                              ▼
                                   ┌───────────────────┐
                                   │ GroupMemberService │
                                   │ requestJoin()     │
                                   └───────────────────┘
                                              │
                                              ▼
                                   ┌───────────────────┐
                                   │   GroupMember     │
                                   │ (PENDING 상태)   │
                                   └───────────────────┘
```

---

## 3. 멤버 승인/거절 플로우

```
┌─────────────┐     POST /approve or /reject     ┌───────────────────────┐
│   Owner     │ ──────────────────────────────▶│ GroupMemberController │
└─────────────┘                                  └───────────────────────┘
                                                           │
                                                           ▼
                                              ┌───────────────────────────┐
                                              │GroupMemberApplicationService│
                                              └───────────────────────────┘
                                                           │
                              ┌─────────────────────────────┴─────────────────────────────┐
                              ▼                                                           ▼
                   ┌───────────────────┐                                       ┌───────────────────┐
                   │GroupMemberService │                                       │GroupMemberService │
                   │ approve()         │                                       │ reject()          │
                   └───────────────────┘                                       └───────────────────┘
                              │                                                           │
                              ▼                                                           ▼
                   ┌───────────────────┐                                       ┌───────────────────┐
                   │   GroupMember     │                                       │   GroupMember     │
                   │ PENDING → ACTIVE  │                                       │     (삭제)        │
                   └───────────────────┘                                       └───────────────────┘
```

---

## 4. 모멘트 작성/조회 플로우

```
┌─────────────┐   POST /api/v2/groups/{id}/moments   ┌───────────────────────┐
│   Member    │ ──────────────────────────────────▶ │ GroupMomentController │
└─────────────┘   GroupMomentCreateRequest           └───────────────────────┘
                  (content)                                      │
                                                                 ▼
                                                    ┌───────────────────────────┐
                                                    │ MomentApplicationService  │
                                                    │  createMomentInGroup()    │
                                                    └───────────────────────────┘
                                                                 │
                              ┌───────────────────────────────────┼───────────────────────────────────┐
                              ▼                                   ▼                                   ▼
                   ┌───────────────────┐              ┌───────────────────┐              ┌───────────────────┐
                   │   UserService     │              │ GroupMemberService│              │  MomentService    │
                   │   getUserBy()     │              │ getByGroupAndUser()│              │  createWithMember()│
                   └───────────────────┘              └───────────────────┘              └───────────────────┘
                                                                                                   │
                                                                                                   ▼
                                                                                        ┌───────────────────┐
                                                                                        │     Moment        │
                                                                                        │ (Group에 연결됨)  │
                                                                                        └───────────────────┘
                                                                                                   │
                                                                                                   ▼
                                                                                        ┌───────────────────────┐
                                                                                        │ GroupMomentResponse   │
                                                                                        └───────────────────────┘

────────────────────────────────────────────────────────────────────────────────────────────────────────────────

                  GET /api/v2/groups/{id}/moments
┌─────────────┐   (cursor pagination)              ┌───────────────────────┐
│   Member    │ ─────────────────────────────────▶│ GroupMomentController │
└─────────────┘                                    └───────────────────────┘
                                                             │
                                                             ▼
                                                  ┌───────────────────────────┐
                                                  │ MomentApplicationService  │
                                                  │  getGroupFeed()           │
                                                  └───────────────────────────┘
                                                             │
                         ┌───────────────────────────────────┼───────────────────────────────────┐
                         ▼                                   ▼                                   ▼
              ┌───────────────────┐              ┌───────────────────┐              ┌───────────────────┐
              │ GroupMemberService│              │  MomentService    │              │ MomentLikeService │
              │ getByGroupAndUser()│              │  getMomentsInGroup()│            │  getCount()       │
              └───────────────────┘              └───────────────────┘              │  hasLiked()       │
                                                                                    └───────────────────┘
                                                             │
                                                             ▼
                                                  ┌───────────────────────┐
                                                  │   GroupFeedResponse   │
                                                  │ (moments, nextCursor) │
                                                  └───────────────────────┘
```

---

## 5. 코멘트 작성/좋아요 플로우

```
┌─────────────┐   POST .../moments/{id}/comments   ┌───────────────────────┐
│   Member    │ ─────────────────────────────────▶│ GroupCommentController│
└─────────────┘   GroupCommentCreateRequest        └───────────────────────┘
                  (content)                                    │
                                                               ▼
                                                  ┌───────────────────────────┐
                                                  │ CommentApplicationService │
                                                  │  createCommentInGroup()   │
                                                  └───────────────────────────┘
                                                               │
                         ┌─────────────────────────────────────┼─────────────────────────────────────┐
                         ▼                                     ▼                                     ▼
              ┌───────────────────┐                 ┌───────────────────┐                 ┌───────────────────┐
              │   UserService     │                 │ GroupMemberService│                 │  CommentService   │
              │   getUserBy()     │                 │ getByGroupAndUser()│                 │ createWithMember()│
              └───────────────────┘                 └───────────────────┘                 └───────────────────┘
                                                                                                    │
                                                                                                    ▼
                                                                                         ┌───────────────────┐
                                                                                         │     Comment       │
                                                                                         │ (Member 연결됨)   │
                                                                                         └───────────────────┘

────────────────────────────────────────────────────────────────────────────────────────────────────────────────

               POST .../comments/{id}/like
┌─────────────┐                                   ┌───────────────────────┐
│   Member    │ ────────────────────────────────▶│ GroupCommentController│
└─────────────┘                                   └───────────────────────┘
                                                            │
                                                            ▼
                                                 ┌───────────────────────────┐
                                                 │ CommentApplicationService │
                                                 │  toggleCommentLike()      │
                                                 └───────────────────────────┘
                                                            │
                         ┌──────────────────────────────────┼──────────────────────────────────┐
                         ▼                                  ▼                                  ▼
              ┌───────────────────┐              ┌───────────────────┐              ┌───────────────────┐
              │  CommentService   │              │ GroupMemberService│              │CommentLikeService │
              │  getCommentBy()   │              │ getByGroupAndUser()│              │  toggle()         │
              └───────────────────┘              └───────────────────┘              └───────────────────┘
                                                                                             │
                                                                                             ▼
                                                                                  ┌───────────────────┐
                                                                                  │ LikeToggleResponse│
                                                                                  │ (liked, likeCount)│
                                                                                  └───────────────────┘
```

---

## 6. 레이어별 의존성 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    PRESENTATION LAYER                                        │
│  ┌────────────────┐ ┌────────────────────┐ ┌──────────────────┐ ┌───────────────────────┐  │
│  │GroupController │ │GroupMemberController│ │GroupInviteController│ │GroupMomentController│  │
│  └────────────────┘ └────────────────────┘ └──────────────────┘ └───────────────────────┘  │
│                          ┌───────────────────────┐                                          │
│                          │GroupCommentController │                                          │
│                          └───────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    APPLICATION LAYER                                         │
│  ┌─────────────────────────┐  ┌──────────────────────────────┐  ┌────────────────────────┐  │
│  │GroupApplicationService  │  │GroupMemberApplicationService │  │MomentApplicationService│  │
│  └─────────────────────────┘  └──────────────────────────────┘  └────────────────────────┘  │
│                          ┌─────────────────────────┐                                         │
│                          │CommentApplicationService│                                         │
│                          └─────────────────────────┘                                         │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                      DOMAIN LAYER                                            │
│  ┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────────┐   │
│  │ GroupService │  │GroupMemberService│  │InviteLinkService │  │    MomentService      │   │
│  └──────────────┘  └──────────────────┘  └──────────────────┘  └───────────────────────┘   │
│  ┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────────┐   │
│  │CommentService│  │MomentLikeService │  │CommentLikeService│  │     UserService       │   │
│  └──────────────┘  └──────────────────┘  └──────────────────┘  └───────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                                  INFRASTRUCTURE LAYER                                        │
│  ┌─────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐                   │
│  │ GroupRepository │  │GroupMemberRepository │  │GroupInviteLinkRepository│                  │
│  └─────────────────┘  └──────────────────────┘  └───────────────────────┘                   │
│  ┌─────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐                   │
│  │MomentRepository │  │ CommentRepository    │  │  MomentLikeRepository │                   │
│  └─────────────────┘  └──────────────────────┘  └───────────────────────┘                   │
│  ┌─────────────────┐  ┌──────────────────────┐                                              │
│  │CommentLikeRepository│ │   UserRepository    │                                              │
│  └─────────────────┘  └──────────────────────┘                                              │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. 엔티티 관계 다이어그램

```
┌─────────────────┐       1:N        ┌──────────────────┐
│      User       │──────────────────│   GroupMember    │
│  - id           │                  │  - id            │
│  - email        │                  │  - nickname      │
│  - nickname     │                  │  - role          │
└─────────────────┘                  │  - status        │
                                     └──────────────────┘
                                              │ N:1
                                              │
                                              ▼
┌─────────────────┐       1:N        ┌──────────────────┐       1:N        ┌──────────────────┐
│     Group       │──────────────────│   GroupMember    │──────────────────│     Moment       │
│  - id           │                  │                  │                  │  - id            │
│  - name         │                  │                  │                  │  - content       │
│  - description  │                  └──────────────────┘                  │  - member_id     │
└─────────────────┘                                                        └──────────────────┘
        │ 1:N                                                                       │ 1:N
        │                                                                           │
        ▼                                                                           ▼
┌──────────────────┐                                                       ┌──────────────────┐
│GroupInviteLink   │                                                       │    Comment       │
│  - id            │                                                       │  - id            │
│  - code          │                                                       │  - content       │
│  - expires_at    │                                                       │  - member_id     │
└──────────────────┘                                                       │  - moment_id     │
                                                                           └──────────────────┘
                         ┌──────────────────┐                                       │
                         │   MomentLike     │◀──────────────────────────────────────┘
                         │  - member_id     │             1:N
                         │  - moment_id     │
                         └──────────────────┘

                         ┌──────────────────┐
                         │   CommentLike    │◀──────────────────────────────────────
                         │  - member_id     │             1:N (Comment에서)
                         │  - comment_id    │
                         └──────────────────┘
```

---

## 8. API 엔드포인트 목록

### Group CRUD
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v2/groups` | 그룹 생성 |
| GET | `/api/v2/groups` | 내 그룹 목록 조회 |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 조회 |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 수정 |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 |

### Invite & Join
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v2/groups/{groupId}/invite` | 초대 링크 생성 |
| GET | `/api/v2/invite/{code}` | 초대 정보 조회 |
| POST | `/api/v2/groups/join` | 그룹 가입 신청 |

### Member Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v2/groups/{groupId}/members` | 멤버 목록 조회 |
| GET | `/api/v2/groups/{groupId}/pending` | 대기 멤버 목록 조회 |
| PATCH | `/api/v2/groups/{groupId}/profile` | 내 프로필 수정 |
| DELETE | `/api/v2/groups/{groupId}/leave` | 그룹 탈퇴 |
| DELETE | `/api/v2/groups/{groupId}/members/{memberId}` | 멤버 강퇴 |
| POST | `/api/v2/groups/{groupId}/members/{memberId}/approve` | 멤버 승인 |
| POST | `/api/v2/groups/{groupId}/members/{memberId}/reject` | 멤버 거절 |
| POST | `/api/v2/groups/{groupId}/transfer/{memberId}` | 소유권 이전 |

### Moment
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v2/groups/{groupId}/moments` | 모멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments` | 그룹 피드 조회 |
| GET | `/api/v2/groups/{groupId}/my-moments` | 나의 모멘트 조회 |
| DELETE | `/api/v2/groups/{groupId}/moments/{momentId}` | 모멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/moments/{momentId}/like` | 모멘트 좋아요 토글 |

### Comment
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | 코멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments/{momentId}/comments` | 코멘트 목록 조회 |
| DELETE | `/api/v2/groups/{groupId}/comments/{commentId}` | 코멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/comments/{commentId}/like` | 코멘트 좋아요 토글 |

---

## 9. 구현 파일 목록

### Controllers
- `moment/group/presentation/GroupController.java`
- `moment/group/presentation/GroupMemberController.java`
- `moment/group/presentation/GroupInviteController.java`
- `moment/group/presentation/GroupMomentController.java`
- `moment/group/presentation/GroupCommentController.java`

### Application Services (수정됨)
- `moment/group/service/application/GroupApplicationService.java`
- `moment/group/service/application/GroupMemberApplicationService.java`
- `moment/moment/service/application/MomentApplicationService.java`
- `moment/comment/service/application/CommentApplicationService.java`

### Domain Services (수정됨)
- `moment/group/service/group/GroupMemberService.java` (deleteOwner 추가)
- `moment/moment/service/moment/MomentService.java` (Group 관련 메서드 추가)
- `moment/comment/service/comment/CommentService.java` (Group 관련 메서드 추가)
- `moment/like/service/MomentLikeService.java`
- `moment/like/service/CommentLikeService.java`

### DTOs
- `moment/group/dto/request/GroupCreateRequest.java`
- `moment/group/dto/request/GroupUpdateRequest.java`
- `moment/group/dto/request/GroupJoinRequest.java`
- `moment/group/dto/request/ProfileUpdateRequest.java`
- `moment/group/dto/response/GroupCreateResponse.java`
- `moment/group/dto/response/GroupDetailResponse.java`
- `moment/group/dto/response/MyGroupResponse.java`
- `moment/group/dto/response/GroupJoinResponse.java`
- `moment/group/dto/response/MemberResponse.java`
- `moment/group/dto/response/InviteInfoResponse.java`
- `moment/group/dto/response/InviteCreateResponse.java`
- `moment/moment/dto/request/GroupMomentCreateRequest.java`
- `moment/moment/dto/response/GroupMomentResponse.java`
- `moment/moment/dto/response/GroupFeedResponse.java`
- `moment/like/dto/response/LikeToggleResponse.java`
- `moment/comment/dto/request/GroupCommentCreateRequest.java`
- `moment/comment/dto/response/GroupCommentResponse.java`

### Tests
- `moment/group/presentation/GroupControllerTest.java`
- `moment/group/presentation/GroupMemberControllerTest.java`
- `moment/group/presentation/GroupInviteControllerTest.java`
- `moment/group/presentation/GroupMomentControllerTest.java`
- `moment/group/presentation/GroupCommentControllerTest.java`

---

## 10. 커밋 히스토리

| Commit | Description |
|--------|-------------|
| `f00fc9b0` | feat: GroupController REST API 구현 (TDD) |
| `f9b0b0fe` | feat: GroupMemberController REST API 구현 (TDD) |
| `35d39a5f` | feat: GroupInviteController REST API 구현 (TDD) |
| `ab2674d0` | feat: GroupMomentController REST API 구현 (TDD) |
| `5f56a012` | feat: GroupCommentController REST API 구현 (TDD) |

---

## 11. 발견된 버그 및 수정 사항

### 버그 1: 그룹 삭제 후 내 그룹 목록 조회 시 NPE
- **원인**: 그룹 삭제 시 소유자 멤버가 삭제되지 않아, Soft Delete된 그룹을 참조하는 멤버가 남아있음
- **수정**: `GroupMemberService.deleteOwner()` 메서드 추가, `GroupApplicationService.deleteGroup()`에서 그룹 삭제 전 소유자 멤버 삭제

### 버그 2: MomentApplicationService 컴파일 오류
- **원인**: 로컬 변수명 `moment`가 패키지명 `moment.global.exception`과 충돌
- **수정**: 로컬 변수명을 `momentToDelete`로 변경

---

**작성일**: 2026-01-23
**Phase 4 완료**: ✅
