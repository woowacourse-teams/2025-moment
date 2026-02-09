# Phase 7: 통합 테스트 및 엣지 케이스

- **Status**: DRAFT
- **Created**: 2026-02-09
- **Parent Plan**: [user-block-plan.md](../user-block-plan.md)
- **Depends On**: Phase 1~6 전체

---

## 목표

전체 차단 기능의 통합 테스트와 엣지 케이스를 검증한다.

---

## 현재 테스트 패턴

### 단위 테스트 패턴 (MomentLikeServiceTest)

**파일**: `src/test/java/moment/like/service/MomentLikeServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentLikeServiceTest {
    @Mock private MomentLikeRepository likeRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private MomentLikeService likeService;

    @Test
    void 좋아요_토글_새로_생성() {
        // Given / When / Then
    }
}
```

### Fixture 패턴 (UserFixture)

**파일**: `src/test/java/moment/fixture/UserFixture.java`

```java
public static User createUserWithId(Long id) {
    User user = new User(getEmail(), "password123!@#", getNickname(), ProviderType.EMAIL);
    setId(user, id);
    return user;
}
```

---

## 단위 테스트 계획

### 7-1. UserBlockTest (Phase 1에서 일부 작성)

**파일**: `src/test/java/moment/block/domain/UserBlockTest.java`

추가 테스트:
| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `isDeleted_deletedAt이_존재하면_true를_반환한다` | deletedAt 설정 후 확인 |

### 7-2. UserBlockServiceTest (Phase 2에서 작성, 엣지 케이스 추가)

**파일**: `src/test/java/moment/block/service/block/UserBlockServiceTest.java`

추가 엣지 케이스 테스트:
| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `A가_B를_차단하고_B도_A를_차단한_경우_두_관계가_독립적으로_존재한다` | 양쪽 모두 별도 row |
| T2 | `차단_해제_후_재차단하면_restore가_호출된다` | soft delete -> restore 사이클 |
| T3 | `getBlockedUserIds가_양방향_모두_반환한다` | A->B 차단 시 A와 B 모두 서로를 차단 목록에 포함 |

### 7-3. MomentApplicationServiceTest

**파일**: `src/test/java/moment/moment/service/application/MomentApplicationServiceTest.java`

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `그룹_모멘트_조회_시_차단된_사용자_ID가_전달된다` | verify 호출 |
| T2 | `좋아요_토글_시_차단된_사용자면_예외가_발생한다` | BL-004 |
| T3 | `좋아요_토글_시_차단되지_않은_사용자면_정상_동작한다` | 정상 케이스 |

### 7-4. CommentApplicationServiceTest

**파일**: `src/test/java/moment/comment/service/application/CommentApplicationServiceTest.java`

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `그룹_댓글_조회_시_차단된_사용자의_댓글이_제외된다` | Java 필터링 |
| T2 | `댓글_좋아요_토글_시_차단된_사용자면_예외가_발생한다` | BL-004 |

### 7-5. NotificationEventHandlerTest

**파일**: `src/test/java/moment/notification/service/eventHandler/NotificationEventHandlerTest.java`

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `댓글_이벤트_차단_시_알림을_보내지_않는다` | verify never |
| T2 | `그룹_댓글_이벤트_차단_시_알림을_보내지_않는다` | verify never |
| T3 | `모멘트_좋아요_이벤트_차단_시_알림을_보내지_않는다` | verify never |
| T4 | `댓글_좋아요_이벤트_차단_시_알림을_보내지_않는다` | verify never |
| T5 | `차단되지_않은_경우_정상_알림을_보낸다` | verify called |

### 7-6. CommentCreateFacadeServiceTest

**파일**: `src/test/java/moment/comment/service/facade/CommentCreateFacadeServiceTest.java`

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `차단된_사용자의_모멘트에_댓글_작성_시_예외가_발생한다` | BL-004 |
| T2 | `차단되지_않은_경우_정상_댓글_생성` | 정상 케이스 |

### 7-7. GroupCommentCreateFacadeServiceTest

**파일**: `src/test/java/moment/comment/service/facade/GroupCommentCreateFacadeServiceTest.java`

| # | 테스트명 | 설명 |
|---|---------|------|
| T1 | `차단된_사용자의_그룹_모멘트에_댓글_작성_시_예외가_발생한다` | BL-004 |

---

## E2E 테스트 계획

### 7-8. UserBlockControllerTest

**파일 생성**: `src/test/java/moment/block/presentation/UserBlockControllerTest.java`

| # | 테스트명 | HTTP | 검증 |
|---|---------|------|------|
| T1 | `사용자_차단_성공` | POST /api/v2/users/{id}/blocks | 201 |
| T2 | `자기_자신_차단_실패` | POST /api/v2/users/{id}/blocks | 400, BL-001 |
| T3 | `이미_차단된_사용자_차단_실패` | POST /api/v2/users/{id}/blocks | 409, BL-002 |
| T4 | `차단_해제_성공` | DELETE /api/v2/users/{id}/blocks | 204 |
| T5 | `존재하지_않는_차단_해제_실패` | DELETE /api/v2/users/{id}/blocks | 404, BL-003 |
| T6 | `차단_목록_조회_성공` | GET /api/v2/users/blocks | 200 |
| T7 | `차단_후_그룹_피드에서_모멘트_미노출` | GET 그룹 모멘트 조회 | 차단된 사용자 모멘트 없음 |
| T8 | `차단_후_댓글_미노출` | GET 그룹 댓글 조회 | 차단된 사용자 댓글 없음 |
| T9 | `차단된_사용자_모멘트에_댓글_작성_실패` | POST 그룹 댓글 생성 | 403, BL-004 |
| T10 | `차단된_사용자_모멘트에_좋아요_실패` | POST 모멘트 좋아요 토글 | 403, BL-004 |
| T11 | `차단_해제_후_콘텐츠_정상_노출` | 차단해제 -> 피드조회 | 모멘트 노출 |

---

## 엣지 케이스 테스트

| # | 시나리오 | 기대 결과 |
|---|---------|----------|
| E1 | A가 B를 차단하고, B도 A를 차단 (이중 차단) | 두 차단 모두 독립 존재, 서로 콘텐츠 미노출 |
| E2 | 차단된 사용자가 동시에 신고(report)된 경우 | 두 필터 모두 적용, 중복 제거 없음 |
| E3 | 댓글 가능한 모멘트가 모두 차단 사용자의 것인 경우 | 빈 결과 반환 |
| E4 | 같은 그룹 내 차단된 사용자의 모멘트/댓글 미노출 | 그룹 피드에서 해당 사용자 콘텐츠 제외 |
| E5 | 차단 해제 후 콘텐츠 즉시 복원 | 해제 직후 피드에서 다시 노출 |
| E6 | `EMPTY_BLOCK_LIST` (List.of(-1L)) 더미값으로 쿼리 정상 동작 | 결과에 영향 없음 |
| E7 | 댓글 count가 차단 필터 적용 후 정확 | "댓글 N개" 표시와 실제 노출 수 일치 |
| E8 | 내가 받은 댓글 페이지에서 차단된 사용자 댓글 미노출 | MyGroupCommentPage 필터링 |
| E9 | 탈퇴한 사용자의 댓글 (commenterUserId == null) | 필터링하지 않음 (노출 유지) |
| E10 | 차단된 사용자의 좋아요 count는 유지 | 좋아요 수에 차단 영향 없음 |

---

## 테스트 실행 명령어

```bash
# 빠른 테스트 (e2e 제외)
./gradlew fastTest

# 전체 테스트 (e2e 포함)
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "moment.block.domain.UserBlockTest"
./gradlew test --tests "moment.block.service.block.UserBlockServiceTest"
./gradlew test --tests "moment.block.presentation.UserBlockControllerTest"
```

---

## 테스트 파일 목록

| 파일 경로 | Phase에서 생성 |
|----------|-------------|
| `src/test/java/moment/block/domain/UserBlockTest.java` | Phase 1 |
| `src/test/java/moment/fixture/UserBlockFixture.java` | Phase 1 |
| `src/test/java/moment/block/service/block/UserBlockServiceTest.java` | Phase 2 |
| `src/test/java/moment/block/service/application/UserBlockApplicationServiceTest.java` | Phase 2 (또는 7) |
| `src/test/java/moment/moment/service/application/MomentApplicationServiceTest.java` | Phase 7 (기존 파일에 추가) |
| `src/test/java/moment/comment/service/application/CommentApplicationServiceTest.java` | Phase 7 (기존 파일에 추가) |
| `src/test/java/moment/notification/service/eventHandler/NotificationEventHandlerTest.java` | Phase 7 (기존 파일에 추가) |
| `src/test/java/moment/comment/service/facade/CommentCreateFacadeServiceTest.java` | Phase 7 (기존 파일에 추가) |
| `src/test/java/moment/comment/service/facade/GroupCommentCreateFacadeServiceTest.java` | Phase 7 (기존 파일에 추가) |
| `src/test/java/moment/block/presentation/UserBlockControllerTest.java` | Phase 7 (E2E) |

## 검증 체크리스트

- [ ] `./gradlew fastTest` 전체 통과
- [ ] 차단 API 정상 동작 (POST/DELETE/GET)
- [ ] 그룹 피드에서 차단된 사용자 모멘트 미노출
- [ ] 댓글 조회에서 차단된 사용자 댓글 미노출
- [ ] 차단된 사용자의 댓글 작성 시 403 에러
- [ ] 차단된 사용자의 좋아요 시 403 에러
- [ ] 차단된 사용자로부터 알림 미수신
- [ ] 모멘트 목록의 댓글 count가 필터링 후 실제 수와 일치
- [ ] 차단 해제 후 콘텐츠 정상 복원
- [ ] 내가 받은 댓글 페이지에서 차단된 사용자 댓글 미노출
