# Phase 4: API 구현 완료 보고서

## 개요

- **작업 기간**: 2026-01-23
- **목표**: 그룹, 멤버, 콘텐츠 REST API 구현
- **API 버전**: `/api/v2/`
- **상태**: 완료

---

## 구현된 API 엔드포인트

### 1. 그룹 CRUD (5개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups` | 그룹 생성 |
| GET | `/api/v2/groups` | 내 그룹 목록 |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 수정 |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 |

### 2. 초대/멤버 (11개)
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

### 3. 콘텐츠 (10개)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v2/groups/{groupId}/moments` | 모멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments` | 그룹 피드 |
| GET | `/api/v2/groups/{groupId}/moments/my` | 나의 모멘트 |
| GET | `/api/v2/groups/{groupId}/moments/{id}` | 모멘트 상세 |
| DELETE | `/api/v2/groups/{groupId}/moments/{id}` | 모멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/moments/{id}/comments` | 코멘트 작성 |
| GET | `/api/v2/groups/{groupId}/moments/{id}/comments` | 코멘트 목록 |
| DELETE | `/api/v2/groups/{groupId}/comments/{id}` | 코멘트 삭제 |
| POST | `/api/v2/groups/{groupId}/moments/{id}/like` | 모멘트 좋아요 |
| POST | `/api/v2/groups/{groupId}/comments/{id}/like` | 코멘트 좋아요 |

---

## 아키텍처 및 데이터 흐름

### 전체 레이어 구조

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                          │
│  ┌─────────────┐ ┌────────────────┐ ┌────────────────┐             │
│  │GroupController│ │GroupMemberCtrl │ │GroupInviteCtrl │             │
│  └──────┬──────┘ └───────┬────────┘ └───────┬────────┘             │
│  ┌──────┴────────────────┴──────────────────┴────────┐             │
│  │GroupMomentController      GroupCommentController   │             │
│  └────────────────────────┬──────────────────────────┘             │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Application Layer                            │
│  ┌────────────────────────┐  ┌─────────────────────────────┐       │
│  │GroupApplicationService │  │GroupMemberApplicationService│       │
│  └───────────┬────────────┘  └──────────────┬──────────────┘       │
│  ┌───────────┴────────────┐  ┌──────────────┴──────────────┐       │
│  │MomentApplicationService│  │CommentApplicationService    │       │
│  └───────────┬────────────┘  └──────────────┬──────────────┘       │
└──────────────┼──────────────────────────────┼───────────────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          Domain Layer                               │
│  ┌──────────────┐ ┌─────────────────┐ ┌──────────────────┐         │
│  │ GroupService │ │GroupMemberService│ │InviteLinkService│         │
│  └──────┬───────┘ └────────┬────────┘ └────────┬─────────┘         │
│  ┌──────┴───────┐ ┌────────┴────────┐ ┌────────┴─────────┐         │
│  │MomentService │ │CommentService   │ │MomentLikeService │         │
│  └──────┬───────┘ └────────┬────────┘ │CommentLikeService│         │
│         │                  │          └──────────────────┘         │
└─────────┼──────────────────┼────────────────────────────────────────┘
          │                  │
          ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       Infrastructure Layer                          │
│  ┌─────────────────┐ ┌───────────────────┐ ┌──────────────────────┐│
│  │GroupRepository  │ │GroupMemberRepository│ │InviteLinkRepository ││
│  └─────────────────┘ └───────────────────┘ └──────────────────────┘│
│  ┌─────────────────┐ ┌───────────────────┐ ┌──────────────────────┐│
│  │MomentRepository │ │CommentRepository  │ │MomentLikeRepository  ││
│  └─────────────────┘ └───────────────────┘ │CommentLikeRepository ││
│                                            └──────────────────────┘│
└─────────────────────────────────────────────────────────────────────┘
```

---

## 주요 사용자 흐름 다이어그램

### 1. 그룹 생성 흐름

```
┌────────┐      ┌─────────────────┐      ┌─────────────────────────┐
│ Client │      │ GroupController │      │ GroupApplicationService │
└────┬───┘      └────────┬────────┘      └────────────┬────────────┘
     │                   │                            │
     │ POST /api/v2/groups                            │
     │ ─────────────────►│                            │
     │                   │                            │
     │                   │ createGroup(userId, req)   │
     │                   │ ───────────────────────────►
     │                   │                            │
     │                   │        ┌───────────────────┼───────────────────┐
     │                   │        │ Application Layer │                   │
     │                   │        │                   ▼                   │
     │                   │        │   ┌───────────────────────────────┐   │
     │                   │        │   │ 1. UserService.getUserBy()   │   │
     │                   │        │   │ 2. GroupService.create()     │   │
     │                   │        │   │ 3. MemberService.createOwner()│   │
     │                   │        │   │ 4. InviteLinkService.create() │   │
     │                   │        │   └───────────────────────────────┘   │
     │                   │        └───────────────────┬───────────────────┘
     │                   │                            │
     │                   │◄───────────────────────────│
     │                   │   GroupCreateResponse      │
     │◄──────────────────│                            │
     │ 201 Created       │                            │
     │ {groupId, name,   │                            │
     │  inviteCode, ...} │                            │
```

### 2. 그룹 가입 흐름

```
┌────────┐      ┌─────────────────────┐      ┌───────────────────────────────┐
│ Client │      │GroupMemberController│      │GroupMemberApplicationService  │
└────┬───┘      └──────────┬──────────┘      └───────────────┬───────────────┘
     │                     │                                 │
     │ POST /api/v2/groups/join                              │
     │ {inviteCode, nickname}                                │
     │ ────────────────────►│                                │
     │                     │                                 │
     │                     │ joinGroup(userId, request)      │
     │                     │ ────────────────────────────────►
     │                     │                                 │
     │                     │        ┌────────────────────────┼────────────────────────┐
     │                     │        │   Application Layer    │                        │
     │                     │        │                        ▼                        │
     │                     │        │ ┌──────────────────────────────────────────────┐│
     │                     │        │ │ 1. InviteLinkService.getByCode(inviteCode)  ││
     │                     │        │ │ 2. UserService.getUserBy(userId)            ││
     │                     │        │ │ 3. GroupMemberService.joinOrRestore()       ││
     │                     │        │ │ 4. EventPublisher.publish(GroupJoinEvent)   ││
     │                     │        │ └──────────────────────────────────────────────┘│
     │                     │        └────────────────────────┬────────────────────────┘
     │                     │                                 │
     │                     │◄────────────────────────────────│
     │                     │   GroupJoinResponse             │
     │◄────────────────────│   {memberId, status: PENDING}   │
     │ 201 Created         │                                 │
     │                     │                                 │
```

### 3. 모멘트 작성 흐름

```
┌────────┐      ┌───────────────────────┐      ┌───────────────────────────┐
│ Client │      │GroupMomentController  │      │MomentApplicationService   │
└────┬───┘      └───────────┬───────────┘      └─────────────┬─────────────┘
     │                      │                                │
     │ POST /api/v2/groups/{groupId}/moments                 │
     │ {content, imageUrls}                                  │
     │ ─────────────────────►│                               │
     │                      │                                │
     │                      │ createMomentInGroup(...)       │
     │                      │ ───────────────────────────────►
     │                      │                                │
     │                      │     ┌──────────────────────────┼──────────────────────────┐
     │                      │     │   Application Layer      │                          │
     │                      │     │                          ▼                          │
     │                      │     │ ┌──────────────────────────────────────────────────┐│
     │                      │     │ │ 1. UserService.getUserBy(userId)                ││
     │                      │     │ │ 2. GroupMemberService.getByGroupAndUser()       ││
     │                      │     │ │    → 멤버 권한 검증 (APPROVED 상태인지)          ││
     │                      │     │ │ 3. MomentService.createInGroup(user, group,     ││
     │                      │     │ │                               member, content)   ││
     │                      │     │ └──────────────────────────────────────────────────┘│
     │                      │     └──────────────────────────┬──────────────────────────┘
     │                      │                                │
     │                      │◄───────────────────────────────│
     │                      │   GroupMomentResponse          │
     │◄─────────────────────│   {momentId, content, ...}     │
     │ 201 Created          │                                │
```

### 4. 좋아요 토글 흐름

```
┌────────┐      ┌───────────────────────┐      ┌───────────────────────────┐
│ Client │      │GroupMomentController  │      │MomentApplicationService   │
└────┬───┘      └───────────┬───────────┘      └─────────────┬─────────────┘
     │                      │                                │
     │ POST /api/v2/groups/{groupId}/moments/{momentId}/like │
     │ ─────────────────────►│                               │
     │                      │                                │
     │                      │ toggleMomentLike(...)          │
     │                      │ ───────────────────────────────►
     │                      │                                │
     │                      │     ┌──────────────────────────┼──────────────────────────┐
     │                      │     │   Application Layer      │                          │
     │                      │     │                          ▼                          │
     │                      │     │ ┌──────────────────────────────────────────────────┐│
     │                      │     │ │ 1. GroupMemberService.getByGroupAndUser()       ││
     │                      │     │ │ 2. MomentService.getMomentBy(momentId)          ││
     │                      │     │ │ 3. 그룹 소속 검증 (moment.group == groupId)      ││
     │                      │     │ │ 4. MomentLikeService.toggle(moment, member)     ││
     │                      │     │ │    → 기존 좋아요 존재 시 soft toggle             ││
     │                      │     │ │    → 신규 시 좋아요 생성                         ││
     │                      │     │ │ 5. EventPublisher.publish(MomentLikeEvent)      ││
     │                      │     │ │    → 본인 모멘트가 아닐 경우 알림 발송           ││
     │                      │     │ └──────────────────────────────────────────────────┘│
     │                      │     └──────────────────────────┬──────────────────────────┘
     │                      │                                │
     │                      │◄───────────────────────────────│
     │                      │   LikeToggleResponse           │
     │◄─────────────────────│   {isLiked: true, likeCount: 5}│
     │ 200 OK               │                                │
```

### 5. 그룹 피드 조회 흐름

```
┌────────┐      ┌───────────────────────┐      ┌───────────────────────────┐
│ Client │      │GroupMomentController  │      │MomentApplicationService   │
└────┬───┘      └───────────┬───────────┘      └─────────────┬─────────────┘
     │                      │                                │
     │ GET /api/v2/groups/{groupId}/moments?cursor=123       │
     │ ─────────────────────►│                               │
     │                      │                                │
     │                      │ getGroupFeed(groupId, userId, cursor)
     │                      │ ───────────────────────────────►
     │                      │                                │
     │                      │     ┌──────────────────────────┼──────────────────────────┐
     │                      │     │   Application Layer      │                          │
     │                      │     │                          ▼                          │
     │                      │     │ ┌──────────────────────────────────────────────────┐│
     │                      │     │ │ 1. GroupMemberService.getByGroupAndUser()       ││
     │                      │     │ │    → 멤버 권한 검증                              ││
     │                      │     │ │ 2. MomentService.getByGroup(groupId, cursor, 20)││
     │                      │     │ │    → 커서 기반 페이지네이션 (최신순)             ││
     │                      │     │ │ 3. 각 모멘트에 대해:                             ││
     │                      │     │ │    - MomentLikeService.getCount()               ││
     │                      │     │ │    - MomentLikeService.hasLiked()               ││
     │                      │     │ │    - CommentService.countByMomentId()           ││
     │                      │     │ └──────────────────────────────────────────────────┘│
     │                      │     └──────────────────────────┬──────────────────────────┘
     │                      │                                │
     │                      │◄───────────────────────────────│
     │                      │   GroupFeedResponse            │
     │◄─────────────────────│   {moments: [...], nextCursor} │
     │ 200 OK               │                                │
```

---

## 이벤트 기반 비동기 처리 흐름

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          도메인 이벤트 발행                                    │
│                                                                              │
│  ┌─────────────────┐     ┌─────────────────────────────────────────────────┐ │
│  │ 가입 신청       │────► │ GroupJoinRequestEvent                          │ │
│  │ (joinGroup)     │     │ → 그룹 소유자에게 알림 전송                      │ │
│  └─────────────────┘     └─────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌─────────────────┐     ┌─────────────────────────────────────────────────┐ │
│  │ 가입 승인       │────► │ GroupJoinApprovedEvent                         │ │
│  │ (approveMember) │     │ → 승인된 멤버에게 알림 전송                      │ │
│  └─────────────────┘     └─────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌─────────────────┐     ┌─────────────────────────────────────────────────┐ │
│  │ 멤버 강퇴       │────► │ GroupKickedEvent                               │ │
│  │ (kickMember)    │     │ → 강퇴된 멤버에게 알림 전송                      │ │
│  └─────────────────┘     └─────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌─────────────────┐     ┌─────────────────────────────────────────────────┐ │
│  │ 모멘트 좋아요   │────► │ MomentLikeEvent                                │ │
│  │ (toggleLike)    │     │ → 모멘트 작성자에게 알림 전송 (본인 제외)        │ │
│  └─────────────────┘     └─────────────────────────────────────────────────┘ │
│                                                                              │
│  ┌─────────────────┐     ┌─────────────────────────────────────────────────┐ │
│  │ 코멘트 좋아요   │────► │ CommentLikeEvent                               │ │
│  │ (toggleLike)    │     │ → 코멘트 작성자에게 알림 전송 (본인 제외)        │ │
│  └─────────────────┘     └─────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    @TransactionalEventListener (AFTER_COMMIT)                │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                      NotificationFacadeService                          │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐   │ │
│  │  │ 1. NotificationApplicationService.createNotification()           │   │ │
│  │  │    → DB에 알림 저장                                              │   │ │
│  │  │                                                                  │   │ │
│  │  │ 2. SSE 실시간 알림 전송                                          │   │ │
│  │  │    → 연결된 클라이언트에 즉시 푸시                                │   │ │
│  │  │                                                                  │   │ │
│  │  │ 3. PushNotificationApplicationService.sendPush()                 │   │ │
│  │  │    → Firebase를 통한 모바일 푸시 알림                             │   │ │
│  │  └──────────────────────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 생성된 파일 목록

### Controllers (5개)
```
src/main/java/moment/group/presentation/
├── GroupController.java           # 그룹 CRUD API
├── GroupMemberController.java     # 멤버 관리 API
├── GroupInviteController.java     # 초대 링크 API
├── GroupMomentController.java     # 모멘트 API
└── GroupCommentController.java    # 코멘트 API
```

### DTOs (6개 신규)
```
src/main/java/moment/group/dto/
├── request/
│   ├── ProfileUpdateRequest.java       # 프로필 수정 요청
│   └── GroupMomentCreateRequest.java   # 모멘트 생성 요청
└── response/
    ├── InviteCreateResponse.java       # 초대 링크 생성 응답
    └── LikeToggleResponse.java         # 좋아요 토글 응답

src/main/java/moment/comment/dto/response/
└── GroupCommentResponse.java           # 그룹 코멘트 응답
```

### 수정된 서비스 파일
```
src/main/java/moment/moment/service/application/
└── MomentApplicationService.java
    - createMomentInGroup()
    - getGroupMomentDetail()
    - deleteGroupMoment()
    - toggleMomentLike()

src/main/java/moment/comment/service/application/
└── CommentApplicationService.java
    - createCommentInGroup()
    - getCommentsInGroup()
    - deleteCommentInGroup()
    - toggleCommentLike()

src/main/java/moment/comment/service/comment/
└── CommentService.java
    - getByMomentId()

src/main/java/moment/comment/infrastructure/
└── CommentRepository.java
    - findByMomentIdOrderByCreatedAtAsc()
    - findByMomentIdAndIdGreaterThanOrderByCreatedAtAsc()
```

---

## 커밋 히스토리

```
8f5d7aeb feat: GroupCommentController API 구현
8cedebee feat: GroupMomentController API 구현
97b301a0 feat: GroupMemberController, GroupInviteController API 구현
120c9ef7 feat: GroupController CRUD API 구현
```

---

## 검증 결과

- `./gradlew compileJava` - **SUCCESS**
- `./gradlew fastTest` - **SUCCESS**
- `./gradlew build` - **SUCCESS**

---

## 다음 단계

1. E2E 테스트 작성 및 실행
2. API 문서화 (Swagger/OpenAPI 어노테이션 추가)
3. 성능 테스트 및 최적화
4. 프론트엔드 연동 테스트
