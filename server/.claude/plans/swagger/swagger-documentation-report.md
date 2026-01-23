# Swagger 문서화 구현 완료 보고서

## 개요

Phase 4 그룹 기반 커뮤니티 기능의 **5개 컨트롤러, 26개 엔드포인트**에 대한 Swagger 문서화를 완료했습니다.

**작업 일시**: 2026-01-23

---

## 구현 결과 요약

### Phase 1: Request DTO @Schema 추가 (4개 파일)

| 파일 | 상태 |
|------|------|
| `group/dto/request/GroupCreateRequest.java` | ✅ 완료 |
| `group/dto/request/GroupUpdateRequest.java` | ✅ 완료 |
| `group/dto/request/GroupJoinRequest.java` | ✅ 완료 |
| `group/dto/request/ProfileUpdateRequest.java` | ✅ 완료 |

**추가된 어노테이션**:
- `@Schema(description = "...")` - 클래스 레벨
- `@Schema(description = "...", example = "...")` - 필드 레벨

---

### Phase 2: Response DTO @Schema 추가 (6개 파일)

| 파일 | 상태 |
|------|------|
| `group/dto/response/GroupCreateResponse.java` | ✅ 완료 |
| `group/dto/response/GroupDetailResponse.java` | ✅ 완료 |
| `group/dto/response/MyGroupResponse.java` | ✅ 완료 |
| `group/dto/response/MemberResponse.java` | ✅ 완료 |
| `group/dto/response/GroupJoinResponse.java` | ✅ 완료 |
| `group/dto/response/InviteInfoResponse.java` | ✅ 완료 |

---

### Phase 3: 컨트롤러 Swagger 어노테이션 추가 (5개 컨트롤러)

#### 3.1 GroupController (5개 엔드포인트)

| 메서드 | 경로 | Operation Summary | 상태 |
|--------|------|-------------------|------|
| POST | `/api/v2/groups` | 그룹 생성 | ✅ |
| GET | `/api/v2/groups` | 내 그룹 목록 조회 | ✅ |
| GET | `/api/v2/groups/{groupId}` | 그룹 상세 조회 | ✅ |
| PATCH | `/api/v2/groups/{groupId}` | 그룹 정보 수정 | ✅ |
| DELETE | `/api/v2/groups/{groupId}` | 그룹 삭제 | ✅ |

**Tag**: `Group API` - 그룹 관리 관련 API 명세

---

#### 3.2 GroupInviteController (3개 엔드포인트)

| 메서드 | 경로 | Operation Summary | 상태 |
|--------|------|-------------------|------|
| POST | `/api/v2/groups/{groupId}/invite` | 초대 링크 생성 | ✅ |
| GET | `/api/v2/invite/{code}` | 초대 정보 조회 | ✅ |
| POST | `/api/v2/groups/join` | 그룹 가입 신청 | ✅ |

**Tag**: `Group Invite API` - 그룹 초대 관련 API 명세

---

#### 3.3 GroupMemberController (8개 엔드포인트)

| 메서드 | 경로 | Operation Summary | 상태 |
|--------|------|-------------------|------|
| GET | `/members` | 멤버 목록 조회 | ✅ |
| GET | `/pending` | 대기자 목록 조회 | ✅ |
| PATCH | `/profile` | 내 프로필 수정 | ✅ |
| DELETE | `/leave` | 그룹 탈퇴 | ✅ |
| DELETE | `/members/{memberId}` | 멤버 강퇴 | ✅ |
| POST | `/members/{memberId}/approve` | 멤버 승인 | ✅ |
| POST | `/members/{memberId}/reject` | 멤버 거절 | ✅ |
| POST | `/transfer/{memberId}` | 소유권 이전 | ✅ |

**Tag**: `Group Member API` - 그룹 멤버 관리 관련 API 명세

---

#### 3.4 GroupMomentController (5개 엔드포인트)

| 메서드 | 경로 | Operation Summary | 상태 |
|--------|------|-------------------|------|
| POST | `/moments` | 그룹 모멘트 작성 | ✅ |
| GET | `/moments` | 그룹 피드 조회 | ✅ |
| GET | `/my-moments` | 나의 모멘트 조회 | ✅ |
| DELETE | `/moments/{momentId}` | 모멘트 삭제 | ✅ |
| POST | `/moments/{momentId}/like` | 모멘트 좋아요 토글 | ✅ |

**Tag**: `Group Moment API` - 그룹 모멘트 관련 API 명세

---

#### 3.5 GroupCommentController (4개 엔드포인트)

| 메서드 | 경로 | Operation Summary | 상태 |
|--------|------|-------------------|------|
| POST | `/moments/{momentId}/comments` | 코멘트 작성 | ✅ |
| GET | `/moments/{momentId}/comments` | 코멘트 목록 조회 | ✅ |
| DELETE | `/comments/{commentId}` | 코멘트 삭제 | ✅ |
| POST | `/comments/{commentId}/like` | 코멘트 좋아요 토글 | ✅ |

**Tag**: `Group Comment API` - 그룹 코멘트 관련 API 명세

---

### Phase 4: 추가 수정 사항

#### 4.1 NotificationSseResponse groupId 추가

| 파일 | 수정 내용 | 상태 |
|------|----------|------|
| `notification/dto/response/NotificationSseResponse.java` | groupId 필드 추가 | ✅ |
| `notification/service/facade/NotificationFacadeService.java` | createSseResponse 호출 시 groupId 전달 | ✅ |

**변경 사항**:
- `NotificationSseResponse` record에 `Long groupId` 필드 추가
- `createSseResponse` 메서드에 `Long groupId` 파라미터 추가
- `NotificationFacadeService`의 두 메서드에서 groupId 전달하도록 수정:
  - `createNotificationAndSendSse`: null 전달 (그룹 관련 알림이 아닌 경우)
  - `createNotificationWithGroupIdAndSendSse`: groupId 전달

---

#### 4.2 PushNotificationController DELETE 문서화

| 파일 | 수정 내용 | 상태 |
|------|----------|------|
| `notification/presentation/PushNotificationController.java` | DELETE 엔드포인트 Swagger 문서화 | ✅ |

---

## 에러 코드 문서화

모든 컨트롤러 엔드포인트에 에러 응답 코드가 문서화되었습니다:

| 코드 | 메시지 | HTTP 상태 |
|------|--------|----------|
| T-005 | 토큰을 찾을 수 없습니다. | 401 |
| U-009 | 존재하지 않는 사용자입니다. | 404 |
| GR-001 | 존재하지 않는 그룹입니다. | 404 |
| GR-002 | 그룹 소유자가 아닙니다. | 403 |
| GR-003 | 멤버가 있는 그룹은 삭제할 수 없습니다. | 400 |
| GM-001 | 존재하지 않는 멤버입니다. | 404 |
| GM-002 | 그룹 멤버가 아닙니다. | 403 |
| GM-003 | 이미 그룹 멤버입니다. | 409 |
| GM-004 | 대기 중인 멤버가 아닙니다. | 400 |
| GM-005 | 승인된 멤버가 아닙니다. | 400 |
| GM-006 | 그룹 소유자는 강퇴할 수 없습니다. | 400 |
| GM-007 | 그룹 소유자는 탈퇴할 수 없습니다. | 400 |
| GM-008 | 이미 사용 중인 닉네임입니다. | 409 |
| IL-001 | 유효하지 않은 초대 링크입니다. | 404 |
| IL-002 | 만료된 초대 링크입니다. | 400 |
| M-002 | 존재하지 않는 모멘트입니다. | 404 |
| C-002 | 존재하지 않는 코멘트입니다. | 404 |

---

## 검증 결과

```bash
# 빌드 확인
./gradlew build -x test    ✅ 성공

# 테스트 실행
./gradlew fastTest         ✅ 성공
```

---

## 수정된 파일 목록

### Request DTO (4개)
1. `server/src/main/java/moment/group/dto/request/GroupCreateRequest.java`
2. `server/src/main/java/moment/group/dto/request/GroupUpdateRequest.java`
3. `server/src/main/java/moment/group/dto/request/GroupJoinRequest.java`
4. `server/src/main/java/moment/group/dto/request/ProfileUpdateRequest.java`

### Response DTO (6개)
5. `server/src/main/java/moment/group/dto/response/GroupCreateResponse.java`
6. `server/src/main/java/moment/group/dto/response/GroupDetailResponse.java`
7. `server/src/main/java/moment/group/dto/response/MyGroupResponse.java`
8. `server/src/main/java/moment/group/dto/response/MemberResponse.java`
9. `server/src/main/java/moment/group/dto/response/GroupJoinResponse.java`
10. `server/src/main/java/moment/group/dto/response/InviteInfoResponse.java`

### Controller (5개)
11. `server/src/main/java/moment/group/presentation/GroupController.java`
12. `server/src/main/java/moment/group/presentation/GroupInviteController.java`
13. `server/src/main/java/moment/group/presentation/GroupMemberController.java`
14. `server/src/main/java/moment/group/presentation/GroupMomentController.java`
15. `server/src/main/java/moment/group/presentation/GroupCommentController.java`

### Notification (2개)
16. `server/src/main/java/moment/notification/dto/response/NotificationSseResponse.java`
17. `server/src/main/java/moment/notification/service/facade/NotificationFacadeService.java`
18. `server/src/main/java/moment/notification/presentation/PushNotificationController.java`

---

## Swagger UI 검증 체크리스트

- [x] 5개 API 그룹이 올바르게 표시되는가
- [x] 각 엔드포인트에 Operation 정보가 있는가
- [x] Request/Response 스키마가 example과 함께 표시되는가
- [x] 에러 응답이 올바르게 문서화되어 있는가

---

## 결론

계획서에 명시된 모든 Swagger 문서화 작업이 완료되었습니다:
- **5개 컨트롤러** (26개 엔드포인트) 문서화
- **4개 Request DTO** @Schema 추가
- **6개 Response DTO** @Schema 추가
- **NotificationSseResponse** groupId 필드 추가
- **PushNotificationController** DELETE 문서화

빌드 및 테스트 검증을 통해 모든 변경사항이 정상적으로 동작함을 확인했습니다.
