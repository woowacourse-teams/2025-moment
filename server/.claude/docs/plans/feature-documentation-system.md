# Feature Implementation Status Documentation System

> Status: DRAFT
> Date: 2026-02-03

## 목표

프로젝트의 모든 구현된 기능을 도메인별로 **상세하게** 문서화하여, Claude Code가 새 기능 추가 시 해당 문서를 읽고/업데이트하면서 맥락을 유지하는 시스템 구축

## 파일 구조

```
.claude/
├── docs/features/
│   ├── FEATURES.md          # 중앙 기능 레지스트리 (인덱스)
│   ├── auth.md              # 인증 도메인 (11 features)
│   ├── user.md              # 사용자 도메인 (7 features)
│   ├── moment.md            # 모멘트 도메인 (4 features)
│   ├── comment.md           # 댓글/Echo 도메인 (1 feature)
│   ├── group.md             # 그룹 도메인 (29 features)
│   ├── like.md              # 좋아요 도메인 (2 features)
│   ├── notification.md      # 알림 도메인 (6 features)
│   ├── report.md            # 신고 도메인 (2 features)
│   ├── storage.md           # 저장소 도메인 (1 feature)
│   ├── admin.md             # 관리자 도메인 (34 features)
│   └── global.md            # 공유 인프라 (7 features)
├── rules/
│   └── feature-tracking.md  # Claude Code 유지 규칙 (신규)
```

## 구현 단계

---

### Phase 1: FEATURES.md (중앙 인덱스) 생성

**파일**: `.claude/docs/features/FEATURES.md`

내용:
- Quick Reference 테이블: 도메인 | 기능 수 | 상태 | 상세 링크
- Cross-Domain Dependencies: 이벤트 발행/구독 관계 (7개 도메인 이벤트)
  - CommentCreateEvent: comment → notification
  - GroupCommentCreateEvent: comment(group) → notification
  - GroupJoinRequestEvent: group → notification
  - GroupJoinApprovedEvent: group → notification
  - GroupKickedEvent: group → notification
  - MomentLikeEvent: like → notification
  - CommentLikeEvent: like → notification
- Recent Changes: 변경 이력 (향후 관리용, 최근 20건 유지)

---

### Phase 2: 도메인별 상세 문서 생성 (11개 파일)

**상세 수준**: 기능별 비즈니스 규칙, 에러 코드, 테스트 클래스 목록, DB 마이그레이션 번호까지 모두 기록 (도메인당 100-200줄)

각 `{domain}.md` 기능 항목 포맷:

```markdown
### {FEATURE-ID}: {Feature Name}
- **Status**: DONE | IN_PROGRESS | PLANNED | DEPRECATED
- **API**: `METHOD /api/v2/path`
- **Key Classes**:
  - Controller: `{Class}Controller`
  - Service: `{Class}Service`, `{Class}ApplicationService`
  - Entity: `{Entity}.java`
  - DTO: `{Request}.java`, `{Response}.java`
- **Business Rules**: 주요 비즈니스 규칙 설명
- **Dependencies**: 타 도메인 의존성
- **Tests**: 관련 테스트 클래스 목록
- **DB Migration**: V{N}__{description}.sql
- **Error Codes**: 관련 에러 코드 (코드, 메시지, HTTP 상태)
- **Notes**: 구현 참고사항
```

**도메인별 기능 내역**:

#### auth.md (PREFIX: AUTH, 11 features)
| ID | 기능 | API |
|----|------|-----|
| AUTH-001 | 이메일 로그인 | POST /api/v2/auth/login |
| AUTH-002 | 로그아웃 | POST /api/v2/auth/logout |
| AUTH-003 | Google OAuth | GET /api/v2/auth/login/google, GET /api/v2/auth/callback/google |
| AUTH-004 | Apple Sign-in | POST /api/v2/auth/apple |
| AUTH-005 | 로그인 상태 확인 | GET /api/v2/auth/login/check |
| AUTH-006 | 토큰 갱신 | POST /api/v2/auth/refresh |
| AUTH-007 | 이메일 인증 요청 | POST /api/v2/auth/email |
| AUTH-008 | 이메일 인증 확인 | POST /api/v2/auth/email/verify |
| AUTH-009 | 비밀번호 재설정 요청 | POST /api/v2/auth/email/password |
| AUTH-010 | 비밀번호 재설정 실행 | POST /api/v2/auth/email/password/reset |
| AUTH-011 | JWT Cookie 인증 체계 | LoginUserArgumentResolver, @AuthenticationPrincipal |

#### user.md (PREFIX: USER, 7 features)
| ID | 기능 | API |
|----|------|-----|
| USER-001 | 이메일 회원가입 | POST /api/v2/users/signup |
| USER-002 | 현재 사용자 조회 | GET /api/v2/users/me |
| USER-003 | 닉네임 중복 확인 | POST /api/v2/users/signup/nickname/check |
| USER-004 | 랜덤 닉네임 생성 | GET /api/v2/users/signup/nickname |
| USER-005 | 마이페이지 프로필 조회 | GET /api/v2/me/profile |
| USER-006 | 닉네임 변경 | POST /api/v2/me/nickname |
| USER-007 | 비밀번호 변경 | POST /api/v2/me/password |

#### moment.md (PREFIX: MOM, 4 features)
| ID | 기능 | API |
|----|------|-----|
| MOM-001 | 기본 모멘트 생성 | POST /api/v2/moments |
| MOM-002 | 추가 모멘트 생성 | POST /api/v2/moments/extra |
| MOM-003 | 기본 작성 가능 여부 | GET /api/v2/moments/writable/basic |
| MOM-004 | 추가 작성 가능 여부 | GET /api/v2/moments/writable/extra |

#### comment.md (PREFIX: CMT, 1 feature)
| ID | 기능 | API |
|----|------|-----|
| CMT-001 | 댓글/Echo 생성 | POST /api/v2/comments |

#### group.md (PREFIX: GRP, 29 features)
| ID | 기능 | API |
|----|------|-----|
| GRP-001 | 그룹 생성 | POST /api/v2/groups |
| GRP-002 | 내 그룹 목록 | GET /api/v2/groups |
| GRP-003 | 그룹 상세 조회 | GET /api/v2/groups/{groupId} |
| GRP-004 | 그룹 수정 | PATCH /api/v2/groups/{groupId} |
| GRP-005 | 그룹 삭제 | DELETE /api/v2/groups/{groupId} |
| GRP-006 | 승인된 멤버 목록 | GET /api/v2/groups/{groupId}/members |
| GRP-007 | 대기 멤버 목록 | GET /api/v2/groups/{groupId}/pending |
| GRP-008 | 그룹 프로필 수정 | PATCH /api/v2/groups/{groupId}/profile |
| GRP-009 | 그룹 탈퇴 | DELETE /api/v2/groups/{groupId}/leave |
| GRP-010 | 멤버 강퇴 | DELETE /api/v2/groups/{groupId}/members/{memberId} |
| GRP-011 | 멤버 승인 | POST /api/v2/groups/{groupId}/members/{memberId}/approve |
| GRP-012 | 멤버 거절 | POST /api/v2/groups/{groupId}/members/{memberId}/reject |
| GRP-013 | 소유권 이전 | POST /api/v2/groups/{groupId}/transfer/{memberId} |
| GRP-014 | 초대 링크 생성 | POST /api/v2/groups/{groupId}/invite |
| GRP-015 | 초대 링크 조회 | GET /api/v2/invite/{code} |
| GRP-016 | 초대 코드로 가입 | POST /api/v2/groups/join |
| GRP-017 | 그룹 모멘트 생성 | POST /api/v2/groups/{groupId}/moments |
| GRP-018 | 그룹 모멘트 목록 | GET /api/v2/groups/{groupId}/moments |
| GRP-019 | 내 그룹 모멘트 | GET /api/v2/groups/{groupId}/my-moments |
| GRP-020 | 안읽은 내 모멘트 | GET /api/v2/groups/{groupId}/my-moments/unread |
| GRP-021 | 그룹 모멘트 삭제 | DELETE /api/v2/groups/{groupId}/moments/{momentId} |
| GRP-022 | 모멘트 좋아요 토글 | POST /api/v2/groups/{groupId}/moments/{momentId}/like |
| GRP-023 | 댓글 가능 모멘트 | GET /api/v2/groups/{groupId}/moments/commentable |
| GRP-024 | 그룹 댓글 생성 | POST /api/v2/groups/{groupId}/moments/{momentId}/comments |
| GRP-025 | 그룹 댓글 목록 | GET /api/v2/groups/{groupId}/moments/{momentId}/comments |
| GRP-026 | 그룹 댓글 삭제 | DELETE /api/v2/groups/{groupId}/comments/{commentId} |
| GRP-027 | 댓글 좋아요 토글 | POST /api/v2/groups/{groupId}/comments/{commentId}/like |
| GRP-028 | 내 그룹 댓글 | GET /api/v2/groups/{groupId}/my-comments |
| GRP-029 | 안읽은 내 댓글 | GET /api/v2/groups/{groupId}/my-comments/unread |

#### like.md (PREFIX: LIK, 2 features)
| ID | 기능 | API |
|----|------|-----|
| LIK-001 | 모멘트 좋아요 토글 | MomentLikeService (GroupMomentController 경유) |
| LIK-002 | 댓글 좋아요 토글 | CommentLikeService (GroupCommentController 경유) |

#### notification.md (PREFIX: NTF, 6 features)
| ID | 기능 | API |
|----|------|-----|
| NTF-001 | SSE 구독 | GET /api/v2/notifications/subscribe |
| NTF-002 | 알림 목록 조회 | GET /api/v2/notifications |
| NTF-003 | 단건 읽음 처리 | PATCH /api/v2/notifications/{id}/read |
| NTF-004 | 전체 읽음 처리 | PATCH /api/v2/notifications/read-all |
| NTF-005 | 디바이스 등록 | POST /api/v2/push-notifications |
| NTF-006 | 디바이스 해제 | DELETE /api/v2/push-notifications |

#### report.md (PREFIX: RPT, 2 features)
| ID | 기능 | API |
|----|------|-----|
| RPT-001 | 모멘트 신고 | POST /api/v2/moments/{id}/reports |
| RPT-002 | 댓글 신고 | POST /api/v2/comments/{id}/reports |

#### storage.md (PREFIX: STG, 1 feature)
| ID | 기능 | API |
|----|------|-----|
| STG-001 | 업로드 URL 발급 | POST /api/v2/storage/upload-url |

#### admin.md (PREFIX: ADM, 34 features)
| ID | 기능 | API |
|----|------|-----|
| ADM-001 | 관리자 로그인 | POST /api/admin/auth/login |
| ADM-002 | 관리자 로그아웃 | POST /api/admin/auth/logout |
| ADM-003 | 현재 관리자 조회 | GET /api/admin/auth/me |
| ADM-004 | 사용자 목록 | GET /api/admin/users |
| ADM-005 | 사용자 상세 | GET /api/admin/users/{id} |
| ADM-006 | 사용자 수정 | PUT /api/admin/users/{id} |
| ADM-007 | 사용자 삭제 | DELETE /api/admin/users/{id} |
| ADM-008 | 그룹 통계 | GET /api/admin/groups/stats |
| ADM-009 | 그룹 목록 | GET /api/admin/groups |
| ADM-010 | 그룹 상세 | GET /api/admin/groups/{groupId} |
| ADM-011 | 승인 멤버 목록 | GET /api/admin/groups/{groupId}/members |
| ADM-012 | 대기 멤버 목록 | GET /api/admin/groups/{groupId}/pending-members |
| ADM-013 | 그룹 수정 | PUT /api/admin/groups/{groupId} |
| ADM-014 | 그룹 삭제 | DELETE /api/admin/groups/{groupId} |
| ADM-015 | 그룹 복원 | POST /api/admin/groups/{groupId}/restore |
| ADM-016 | 멤버 승인 | POST /api/admin/groups/{groupId}/members/{memberId}/approve |
| ADM-017 | 멤버 거절 | POST /api/admin/groups/{groupId}/members/{memberId}/reject |
| ADM-018 | 멤버 강퇴 | DELETE /api/admin/groups/{groupId}/members/{memberId} |
| ADM-019 | 소유권 이전 | POST /api/admin/groups/{groupId}/transfer-ownership/{id} |
| ADM-020 | 초대 링크 조회 | GET /api/admin/groups/{groupId}/invite-link |
| ADM-021 | 그룹 활동 로그 | GET /api/admin/groups/logs |
| ADM-022 | 그룹 모멘트 목록 | GET /api/admin/groups/{groupId}/moments |
| ADM-023 | 모멘트 삭제 | DELETE /api/admin/groups/{groupId}/moments/{momentId} |
| ADM-024 | 댓글 목록 | GET /api/admin/groups/{groupId}/moments/{momentId}/comments |
| ADM-025 | 댓글 삭제 | DELETE /api/admin/groups/{groupId}/comments/{commentId} |
| ADM-026 | 관리자 계정 목록 | GET /api/admin/accounts |
| ADM-027 | 관리자 계정 생성 | POST /api/admin/accounts |
| ADM-028 | 관리자 차단 | POST /api/admin/accounts/{id}/block |
| ADM-029 | 관리자 차단 해제 | POST /api/admin/accounts/{id}/unblock |
| ADM-030 | 활성 세션 목록 | GET /api/admin/sessions |
| ADM-031 | 세션 상세 | GET /api/admin/sessions/{id} |
| ADM-032 | 세션 무효화 | DELETE /api/admin/sessions/{sessionId} |
| ADM-033 | 관리자 전체 세션 무효화 | DELETE /api/admin/sessions/admin/{adminId} |
| ADM-034 | 세션 이력 | GET /api/admin/sessions/history |

#### global.md (PREFIX: GLB, 7 features)
| ID | 기능 |
|----|------|
| GLB-001 | BaseEntity (감사 필드: createdAt) |
| GLB-002 | ErrorCode + MomentException 에러 처리 |
| GLB-003 | Cursor 기반 페이지네이션 (Cursor, PageSize, Cursorable) |
| GLB-004 | Soft Delete 패턴 (@SQLDelete, @SQLRestriction) |
| GLB-005 | Logstash 구조화 로깅 (ControllerLogAspect 등) |
| GLB-006 | SuccessResponse/ErrorResponse 래퍼 |
| GLB-007 | Health Check (GET /health) |

---

### Phase 3: Claude Code 규칙 파일 생성

**파일**: `.claude/rules/feature-tracking.md`

내용:

```
# Feature Tracking

## 읽기 규칙
- 새 기능 시작 전: FEATURES.md → 관련 {domain}.md 순서로 읽기
- 크로스 도메인 기능: FEATURES.md Cross-Domain Dependencies 확인
- 버그 수정: 관련 도메인 feature doc에서 기존 동작 파악

## 업데이트 규칙

### 새 기능 추가
1. {domain}.md에 다음 ID로 IN_PROGRESS 항목 추가
2. FEATURES.md Recent Changes에 기록
3. 완료 시: DONE 으로 변경, 테스트/마이그레이션 정보 기입, 기능 수 업데이트

### 기존 기능 수정
1. {domain}.md 해당 항목 업데이트
2. FEATURES.md Recent Changes에 변경 내역 기록

### 새 도메인 모듈 추가
1. {domain}.md 신규 생성
2. FEATURES.md Quick Reference에 행 추가

### 새 도메인 이벤트 추가
1. 발행 도메인의 {domain}.md "Domain Events Published" 업데이트
2. FEATURES.md Cross-Domain Dependencies에 행 추가

## Status 값
- DONE: 테스트 포함 완전 구현
- IN_PROGRESS: 현재 구현 중
- PLANNED: 설계만 완료
- DEPRECATED: 제거 예정

## Feature ID 규칙
형식: {PREFIX}-{NNN} (3자리 숫자)
접두사: AUTH, USER, MOM, CMT, GRP, LIK, NTF, RPT, STG, ADM, GLB

## Last Updated 타임스탬프
파일 수정 시 상단의 "Last Updated" 날짜 갱신

## Recent Changes
최근 20건만 유지, 오래된 항목은 삭제
```

---

### Phase 4: CLAUDE.md 업데이트

**파일**: `.claude/CLAUDE.md`

#### 변경 1: `## 참고` 섹션에 Feature Registry 참조 추가

```markdown
### Feature Registry
- **기능 인덱스**: `.claude/docs/features/FEATURES.md` — 전체 기능 목록 및 상태
- **도메인별 상세**: `.claude/docs/features/{domain}.md` — 도메인별 기능, API, 클래스, 테스트 매핑
- **추적 규칙**: `.claude/rules/feature-tracking.md` — 기능 문서 유지 규칙
```

#### 변경 2: 모듈 구조 업데이트

현재 CLAUDE.md의 모듈 구조에서:
- `reward/` 제거 (코드베이스에 존재하지 않음)
- `group/`, `like/`, `admin/` 추가

수정 후:
```
src/main/java/moment/
├── admin/         # 관리자 (세션 기반 인증, 사용자/그룹/콘텐츠 관리)
├── auth/          # 인증/인가 (JWT, Google OAuth, Apple Sign-in)
├── comment/       # 댓글 (도메인명: "Echo")
├── group/         # 그룹 (CRUD, 멤버 관리, 초대, 그룹 모멘트/코멘트)
├── like/          # 좋아요 (모멘트/코멘트 좋아요 토글)
├── moment/        # 핵심 모멘트 게시물
├── notification/  # 알림 (SSE + Firebase Push)
├── report/        # 콘텐츠 신고
├── storage/       # 파일 저장소 (AWS S3)
├── user/          # 사용자 관리
└── global/        # 공유 인프라
```

#### 변경 3: expStar/레벨 시스템 관련 내용 제거

도메인 규칙에서 사용자 레벨 시스템 설명 제거 또는 현재 상태에 맞게 수정:
- `expStar 기반 15개 레벨` 관련 문단
- `User.addStarAndUpdateLevel()` 참조

---

### Phase 5: 검증

1. 모든 Controller 엔드포인트가 feature docs에 포함되었는지 확인
2. 모든 도메인 이벤트(7개)가 Cross-Domain Dependencies에 기록되었는지 확인
3. FEATURES.md Quick Reference의 기능 수와 각 {domain}.md 항목 수 일치 확인
4. CLAUDE.md 모듈 구조가 `src/main/java/moment/` 실제 디렉토리와 일치 확인
5. `./gradlew fastTest` 실행하여 기존 코드에 영향 없음 확인 (문서만 추가/수정이므로 형식적 검증)

---

## 수정 대상 파일 목록

| 파일 | 작업 | 예상 크기 |
|------|------|-----------|
| `.claude/docs/features/FEATURES.md` | 신규 | ~60줄 |
| `.claude/docs/features/auth.md` | 신규 | ~150줄 |
| `.claude/docs/features/user.md` | 신규 | ~100줄 |
| `.claude/docs/features/moment.md` | 신규 | ~80줄 |
| `.claude/docs/features/comment.md` | 신규 | ~50줄 |
| `.claude/docs/features/group.md` | 신규 | ~350줄 |
| `.claude/docs/features/like.md` | 신규 | ~50줄 |
| `.claude/docs/features/notification.md` | 신규 | ~100줄 |
| `.claude/docs/features/report.md` | 신규 | ~60줄 |
| `.claude/docs/features/storage.md` | 신규 | ~40줄 |
| `.claude/docs/features/admin.md` | 신규 | ~400줄 |
| `.claude/docs/features/global.md` | 신규 | ~80줄 |
| `.claude/rules/feature-tracking.md` | 신규 | ~50줄 |
| `.claude/CLAUDE.md` | 수정 | 3곳 수정 |

**총 14개 파일** (13개 신규 생성 + 1개 수정)