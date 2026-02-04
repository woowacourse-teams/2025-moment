# Phase 5: 검증

> Status: PENDING
> Parent Plan: feature-documentation-system.md

## 목표

생성/수정된 모든 문서의 정확성과 일관성을 검증

## 검증 항목

### 5-1. Controller 엔드포인트 완전성 검증

**방법**: 모든 `@*Mapping` 어노테이션을 grep하여 feature docs와 대조

**검증 대상 컨트롤러** (21개):

| 모듈 | Controller | 엔드포인트 수 |
|------|------------|-------------|
| auth | AuthController | 11 |
| user | UserController | 4 |
| user | MyPageController | 3 |
| moment | MomentController | 4 |
| comment | CommentController | 1 |
| group | GroupController | 5 |
| group | GroupMemberController | 4 |
| group | GroupMemberApprovalController | 4 |
| group | GroupInviteController | 3 |
| group | GroupMomentController | 7 |
| group | GroupCommentController | 6 |
| notification | NotificationController | 4 |
| notification | PushNotificationController | 2 |
| report | ReportController | 2 |
| storage | FileStorageController | 1 |
| admin | AdminAuthApiController | 3 |
| admin | AdminUserApiController | 4 |
| admin | AdminGroupApiController | 19 |
| admin | AdminAccountApiController | 4 |
| admin | AdminSessionApiController | 5 |
| global | HealthCheckController | 1 |

**검증 스크립트**:

```bash
# 모든 @*Mapping 어노테이션 추출
grep -rn '@\(Get\|Post\|Put\|Patch\|Delete\)Mapping' \
  src/main/java/moment/*/presentation/*.java \
  src/main/java/moment/admin/presentation/api/*.java \
  src/main/java/moment/global/presentation/*.java \
  | wc -l
# 예상 결과: feature docs 총 기능 수와 근사
```

---

### 5-2. 도메인 이벤트 완전성 검증

**방법**: `*Event.java` 파일 목록과 FEATURES.md Cross-Domain Dependencies 대조

**코드베이스에 존재하는 이벤트 (8개)**:

| Event | 파일 위치 |
|-------|-----------|
| `CommentCreateEvent` | `comment/dto/CommentCreateEvent.java` |
| `EchoCreateEvent` | `comment/dto/EchoCreateEvent.java` |
| `GroupCommentCreateEvent` | `comment/dto/event/GroupCommentCreateEvent.java` |
| `GroupJoinRequestEvent` | `group/dto/event/GroupJoinRequestEvent.java` |
| `GroupJoinApprovedEvent` | `group/dto/event/GroupJoinApprovedEvent.java` |
| `GroupKickedEvent` | `group/dto/event/GroupKickedEvent.java` |
| `MomentLikeEvent` | `like/dto/event/MomentLikeEvent.java` |
| `CommentLikeEvent` | `like/dto/event/CommentLikeEvent.java` |

**검증 명령**:

```bash
# 모든 이벤트 record 검색
grep -rn 'public record.*Event' src/main/java/moment/ | wc -l
# 예상: 8
```

---

### 5-3. FEATURES.md 기능 수 일치 검증

**방법**: FEATURES.md Quick Reference의 기능 수와 각 `{domain}.md` 항목 수 대조

| 도메인 | Quick Reference 기능 수 | {domain}.md 항목 수 | 일치 |
|--------|------------------------|---------------------|------|
| auth | 11 | AUTH-001 ~ AUTH-011 | |
| user | 7 | USER-001 ~ USER-007 | |
| moment | 4 | MOM-001 ~ MOM-004 | |
| comment | 1 | CMT-001 | |
| group | 29 | GRP-001 ~ GRP-029 | |
| like | 2 | LIK-001 ~ LIK-002 | |
| notification | 6 | NTF-001 ~ NTF-006 | |
| report | 2 | RPT-001 ~ RPT-002 | |
| storage | 1 | STG-001 | |
| admin | 34 | ADM-001 ~ ADM-034 | |
| global | 7 | GLB-001 ~ GLB-007 | |
| **합계** | **104** | | |

---

### 5-4. CLAUDE.md 모듈 구조 검증

**방법**: 실제 디렉토리 목록과 CLAUDE.md 기술 내용 대조

```bash
# 실제 모듈 디렉토리
ls -d src/main/java/moment/*/
# 예상: admin, auth, comment, global, group, like, moment, notification, report, storage, user
# reward가 없어야 함
```

**확인 사항**:
- [ ] `reward/` 디렉토리가 존재하지 않는지
- [ ] `admin/`, `group/`, `like/` 디렉토리가 존재하는지
- [ ] CLAUDE.md에 기술된 모듈과 실제 디렉토리가 1:1 대응

---

### 5-5. 빌드 영향 검증

**방법**: `./gradlew fastTest` 실행

```bash
./gradlew fastTest
```

**목적**: 문서만 추가/수정했으므로 기존 코드에 영향이 없음을 형식적으로 확인

---

### 5-6. 문서 간 링크 검증

**방법**: FEATURES.md의 상대 경로 링크가 실제 파일을 가리키는지 확인

```bash
# feature docs 디렉토리 내 모든 .md 파일 목록
ls .claude/docs/features/*.md
# 예상 파일: FEATURES.md, auth.md, user.md, moment.md, comment.md,
#            group.md, like.md, notification.md, report.md, storage.md,
#            admin.md, global.md (총 12개)
```

---

### 5-7. 에러 코드 완전성 검증

**방법**: ErrorCode/AdminErrorCode enum의 모든 항목이 도메인별 문서에 기록되었는지

```bash
# User API ErrorCode 항목 수
grep -c '^\s*[A-Z_-]*(.*HttpStatus' src/main/java/moment/global/exception/ErrorCode.java

# Admin API ErrorCode 항목 수
grep -c '^\s*[A-Z_-]*(.*HttpStatus' src/main/java/moment/admin/global/exception/AdminErrorCode.java
```

---

## 작업 순서

1. 5-4 먼저 수행 (모듈 구조 확인 - Phase 4에 필요한 사전 정보)
2. Phase 1~4 구현 완료 후 5-1 ~ 5-3, 5-6, 5-7 수행
3. 5-5 마지막으로 수행 (빌드 검증)

## 선행 조건

- Phase 1, 2, 3, 4 모두 완료

## 검증 결과 기록

검증 완료 시 아래 체크리스트 업데이트:

- [ ] 5-1: 모든 Controller 엔드포인트 feature docs에 포함
- [ ] 5-2: 8개 도메인 이벤트 Cross-Domain Dependencies 기록 완료
- [ ] 5-3: FEATURES.md 기능 수 = 각 {domain}.md 항목 수
- [ ] 5-4: CLAUDE.md 모듈 구조 = 실제 디렉토리
- [ ] 5-5: `./gradlew fastTest` 통과
- [ ] 5-6: 문서 간 링크 유효
- [ ] 5-7: 에러 코드 완전