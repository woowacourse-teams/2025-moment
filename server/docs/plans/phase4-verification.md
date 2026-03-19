# Phase 4: 전체 테스트 실행 및 검증

> Parent: [plan.md](./plan.md)
> Status: 구현 대기
> 선행 조건: Phase 1, 2, 3 모두 완료

## 목표

모든 Phase 수정이 완료된 후 전체 테스트 스위트를 실행하여 회귀 없음을 검증한다.

---

## Step 4-1: 수정된 테스트 개별 실행

각 Phase에서 수정한 테스트를 개별 실행하여 정상 통과 확인:

```bash
# Phase 1: DeepLinkGenerator
./gradlew test --tests "moment.notification.domain.DeepLinkGeneratorTest"

# Phase 2: Like Service (수정 없지만 컴파일 정상 확인)
./gradlew test --tests "moment.like.service.MomentLikeServiceTest"
./gradlew test --tests "moment.like.service.CommentLikeServiceTest"

# Phase 3: NotificationEventHandler
./gradlew test --tests "moment.notification.service.eventHandler.NotificationEventHandlerTest"
```

---

## Step 4-2: 전체 빠른 테스트 실행

```bash
./gradlew fastTest
```

e2e 제외 전체 단위/통합 테스트 통과 확인.

---

## Step 4-3: 컴파일 경고 확인

```bash
./gradlew build
```

- 미사용 import 없는지
- 경고 없이 빌드 성공하는지

---

## 검증 체크리스트

### 기능 검증
- [ ] DeepLinkGenerator 테스트 6개 전부 PASS
- [ ] MomentLikeServiceTest 7개 전부 PASS
- [ ] CommentLikeServiceTest 7개 전부 PASS
- [ ] NotificationEventHandlerTest 7개 전부 PASS
- [ ] `./gradlew fastTest` 전체 PASS
- [ ] `./gradlew build` 성공

### 코드 품질 검증
- [ ] 미사용 import 없음
- [ ] 개인 모멘트 관련 레거시 테스트 삭제됨
- [ ] DeepLinkGenerator의 switch가 모든 NotificationType을 커버
- [ ] record 필드 추가로 인한 컴파일 에러 전부 해결

### 경로 매핑 검증

| NotificationType | 생성되는 딥링크 | 클라이언트 라우트 매칭 |
|---|---|---|
| NEW_COMMENT_ON_MOMENT | `/groups/{gid}/collection/my-moment` | `/groups/:groupId/collection/my-moment` |
| GROUP_JOIN_REQUEST | `/groups/{gid}/today-moment` | `/groups/:groupId/today-moment` |
| GROUP_JOIN_APPROVED | `/groups/{gid}/today-moment` | `/groups/:groupId/today-moment` |
| GROUP_KICKED | `null` | - |
| MOMENT_LIKED | `/groups/{gid}/collection/my-moment` | `/groups/:groupId/collection/my-moment` |
| COMMENT_LIKED | `/groups/{gid}/collection/my-comment` | `/groups/:groupId/collection/my-comment` |

---

## 전체 수정 파일 요약

### 프로덕션 코드 (6개)

| # | 파일 | Phase | 변경 |
|---|------|-------|------|
| 1 | `notification/domain/DeepLinkGenerator.java` | 1 | 딥링크 경로 전면 수정 |
| 2 | `like/dto/event/MomentLikeEvent.java` | 2 | `Long groupId` 필드 추가 |
| 3 | `like/dto/event/CommentLikeEvent.java` | 2 | `Long groupId` 필드 추가 |
| 4 | `like/service/MomentLikeService.java` | 2 | `moment.getGroup().getId()` 추가 |
| 5 | `like/service/CommentLikeService.java` | 2 | `member.getGroup().getId()` 추가 |
| 6 | `notification/service/eventHandler/NotificationEventHandler.java` | 3 | SourceData에 groupId 포함 |

### 테스트 코드 (2개)

| # | 파일 | Phase | 변경 |
|---|------|-------|------|
| 1 | `notification/domain/DeepLinkGeneratorTest.java` | 1 | 기대값 수정 + 레거시 테스트 삭제 |
| 2 | `notification/service/eventHandler/NotificationEventHandlerTest.java` | 3 | 이벤트 생성자 + SourceData 기대값 수정 |

### 수정 불필요 테스트 (2개)

| # | 파일 | 이유 |
|---|------|------|
| 1 | `like/service/MomentLikeServiceTest.java` | `any(MomentLikeEvent.class)` 로 검증 |
| 2 | `like/service/CommentLikeServiceTest.java` | `any(CommentLikeEvent.class)` 로 검증 |
