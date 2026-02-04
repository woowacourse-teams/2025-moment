# 회원 탈퇴 (User Withdrawal) 구현 계획

## 요구사항
- 사용자 soft delete + PII(email, nickname, password) 익명화
- **콘텐츠(모멘트, 댓글, 좋아요) 유지 + "탈퇴한 사용자" 표시**
- 본인 확인 없이 바로 탈퇴 (인증된 상태)
- 그룹 소유자는 탈퇴 차단
- **nickname 유니크 제약조건 유지** (V36 마이그레이션 불필요)

---

## 설계 결정사항

### 익명화 전략
| 필드 | 익명화 값 | 이유 |
|------|----------|------|
| email | `withdrawn_{userId}@moment.invalid` | userId로 고유성 보장, 원본 이메일 해제 |
| nickname | `탈퇴한 사용자_{userId}` | **유니크 제약조건 유지**, DB에서 고유 |
| password | `WITHDRAWN` | BCrypt 아니므로 로그인 불가 |

### 핵심 이슈: 순서 버그 수정
`authService.logout()` 내부에서 `userRepository.findById(userId)`를 호출하므로, **User soft delete 이전에** 호출해야 함. Facade에서 순서를 조율.

### 콘텐츠 표시 전략
- `MomentRepository`의 `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter`로 변경
- `@SQLRestriction("deleted_at IS NULL")`에 의해 soft-deleted User는 LEFT JOIN 시 null
- DTO에서 momenter/commenter가 null이면 `"탈퇴한 사용자"` 표시
- `comment.getCommenter().getId()`는 JPA lazy proxy에서 FK ID를 반환하므로 동작함

---

## 탈퇴 플로우

```
MyPageController.withdraw()
  ↓
MyPageFacadeService.withdraw(userId)  [@Transactional]
  ├─ userWithdrawService.validateWithdrawable(userId)  // 1. 그룹 소유 검증
  ├─ authService.logout(userId)                        // 2. RefreshToken 삭제 (유저 아직 활성)
  └─ userWithdrawService.withdraw(userId)              // 3. 정리 + 익명화 + soft delete
      ├─ pushNotificationRepository.deleteAllByUserId(userId)
      ├─ emitters.remove(userId)
      ├─ user.anonymize()
      └─ userRepository.delete(user)
  ↓
Clear cookies (accessToken, refreshToken maxAge=0)
```

---

## Phase A: 회원 탈퇴 API

### A-1: ErrorCode 추가
**파일:** `src/main/java/moment/global/exception/ErrorCode.java`
```java
USER_HAS_OWNED_GROUP("U-014", "소유한 그룹이 있어 탈퇴할 수 없습니다. 그룹을 삭제하거나 소유자를 변경해 주세요.", HttpStatus.BAD_REQUEST),
```

### A-2: User.anonymize() + 단위 테스트
**파일:** `src/main/java/moment/user/domain/User.java`
```java
public void anonymize() {
    this.email = "withdrawn_" + this.id + "@moment.invalid";
    this.password = "WITHDRAWN";
    this.nickname = "탈퇴한 사용자_" + this.id;
}
```

**테스트:** `src/test/java/moment/user/domain/UserTest.java`
| 테스트명 | 검증 |
|---------|------|
| `anonymize_호출_시_이메일이_익명화된다` | `withdrawn_{id}@moment.invalid` |
| `anonymize_호출_시_비밀번호가_무효화된다` | `"WITHDRAWN"` |
| `anonymize_호출_시_닉네임이_익명화된다` | `"탈퇴한 사용자_{id}"` |

### A-3: PushNotificationRepository 메서드 추가
**파일:** `src/main/java/moment/notification/infrastructure/PushNotificationRepository.java`
```java
void deleteAllByUserId(Long userId);
```

### A-4: UserWithdrawService 생성 + 단위 테스트
**파일 생성:** `src/main/java/moment/user/service/user/UserWithdrawService.java`
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserWithdrawService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final Emitters emitters;

    public void validateWithdrawable(Long userId) {
        if (!groupRepository.findByOwnerId(userId).isEmpty()) {
            throw new MomentException(ErrorCode.USER_HAS_OWNED_GROUP);
        }
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));
        pushNotificationRepository.deleteAllByUserId(userId);
        emitters.remove(userId);
        user.anonymize();
        userRepository.delete(user);
    }
}
```

**테스트:** `src/test/java/moment/user/service/user/UserWithdrawServiceTest.java`
| 테스트명 | 검증 |
|---------|------|
| `그룹을_소유하지_않은_유저는_탈퇴_검증에_성공한다` | 예외 없음 |
| `그룹을_소유한_유저는_탈퇴_검증에_실패한다` | USER_HAS_OWNED_GROUP 예외 |
| `유저_탈퇴_시_PII가_익명화되고_소프트_삭제된다` | anonymize + delete 호출 |
| `존재하지_않는_유저_탈퇴_시_예외가_발생한다` | USER_NOT_FOUND 예외 |
| `탈퇴_시_푸시알림_구독이_삭제된다` | deleteAllByUserId 호출 |
| `탈퇴_시_SSE_이미터가_제거된다` | emitters.remove 호출 |

### A-5: MyPageFacadeService.withdraw()
**파일 수정:** `src/main/java/moment/user/service/facade/MyPageFacadeService.java`

추가 의존성: `UserWithdrawService`, `AuthService`
```java
@Transactional
public void withdraw(Long userId) {
    userWithdrawService.validateWithdrawable(userId);
    authService.logout(userId);
    userWithdrawService.withdraw(userId);
}
```

**테스트:** `src/test/java/moment/user/service/facade/MyPageFacadeServiceTest.java`
| 테스트명 | 검증 |
|---------|------|
| `회원탈퇴_시_검증_로그아웃_탈퇴_순서로_호출된다` | InOrder 검증 |
| `그룹_소유자가_회원탈퇴를_시도하면_예외가_발생한다` | logout/withdraw 호출 안 됨 |

### A-6: MyPageController - DELETE /api/v2/me
**파일 수정:** `src/main/java/moment/user/presentation/MyPageController.java`

비밀번호 변경과 동일한 쿠키 초기화 패턴 사용.

**테스트:** `src/test/java/moment/user/presentation/MyPageControllerTest.java`
| 테스트명 | 타입 | 검증 |
|---------|------|------|
| `회원탈퇴에_성공하고_쿠키가_초기화된다` | E2E | 200, 쿠키 비어있음, 프로필 조회 실패 |
| `그룹_소유자가_회원탈퇴를_시도하면_400_에러를_반환한다` | E2E | 400, U-014 |
| `인증되지_않은_사용자가_회원탈퇴를_시도하면_401_에러를_반환한다` | E2E | 401 |

---

## Phase B: 콘텐츠 표시 수정 (탈퇴 유저 콘텐츠 유지)

### B-1: MomentRepository - INNER JOIN → LEFT JOIN

**파일:** `src/main/java/moment/moment/infrastructure/MomentRepository.java`

**변경 대상 쿼리** (7개): 모두 `JOIN FETCH m.momenter` → `LEFT JOIN FETCH m.momenter`

| 메서드 | 용도 |
|--------|------|
| `findAllWithMomenterByIds` | 탐색 피드 (ID로 모멘트 조회) |
| `findByGroupIdOrderByIdDesc` | 그룹 피드 첫 페이지 |
| `findByGroupIdAndIdLessThanOrderByIdDesc` | 그룹 피드 다음 페이지 |
| `findByGroupIdAndMemberIdOrderByIdDesc` | 그룹 멤버 모멘트 첫 페이지 |
| `findByGroupIdAndMemberIdAndIdLessThanOrderByIdDesc` | 그룹 멤버 모멘트 다음 페이지 |
| `findByGroupIdAndMemberIdAndIdIn` | 그룹 멤버 읽지 않은 모멘트 |
| `findByGroupIdAndMemberIdAndIdInAndIdLessThan` | 같은 쿼리 커서 |

> 개인 피드 쿼리(`findMyMomentFirstPage` 등)는 변경 불필요 — 탈퇴 유저는 인증 불가.

### B-2: MomentComposition - null 안전 처리

**파일:** `src/main/java/moment/moment/dto/response/tobe/MomentComposition.java`

```java
public static MomentComposition of(Moment moment, String imageUrl) {
    User momenter = moment.getMomenter();
    return new MomentComposition(
            moment.getId(),
            momenter != null ? momenter.getId() : null,
            moment.getContent(),
            momenter != null ? momenter.getNickname() : "탈퇴한 사용자",
            imageUrl,
            moment.getCreatedAt()
    );
}
```

### B-3: CommentComposition - null 안전 처리

**파일:** `src/main/java/moment/comment/dto/tobe/CommentComposition.java`

```java
public static CommentComposition of(Comment comment, User commenter, String imageUrl) {
    return new CommentComposition(
            comment.getId(),
            comment.getContent(),
            commenter != null ? commenter.getNickname() : "탈퇴한 사용자",
            imageUrl,
            comment.getCreatedAt(),
            comment.getMomentId()
    );
}
```

### B-4: CommentApplicationService - null User 처리

**파일:** `src/main/java/moment/comment/service/application/CommentApplicationService.java`

`mapCommentersByComments()`: `Collectors.toMap`은 null value를 허용하지 않으므로 `HashMap` 직접 사용으로 변경

```java
private Map<Comment, User> mapCommentersByComments(List<User> commenters, List<Comment> comments) {
    Map<Long, User> userById = commenters.stream()
            .collect(Collectors.toMap(User::getId, user -> user));

    Map<Comment, User> result = new HashMap<>();
    for (Comment comment : comments) {
        result.put(comment, userById.get(comment.getCommenter().getId()));
    }
    return result;
}
```

### B-5: CommentableMomentResponse - null 안전 처리

**파일:** `src/main/java/moment/moment/dto/response/CommentableMomentResponse.java`

`moment.getMomenter().getNickname()` → null 체크 추가

### B-6: Like 서비스 - null 안전 처리

**파일:** `src/main/java/moment/like/service/MomentLikeService.java`
- `moment.getMomenter().getId()` → lazy proxy에서 FK ID 반환으로 동작하지만, 안전을 위해 null 체크 추가
- 탈퇴 유저에게는 좋아요 알림 발송 skip

**파일:** `src/main/java/moment/like/service/CommentLikeService.java`
- 동일하게 null 체크 추가

---

## 수정 파일 목록

### Phase A: 신규 생성 (3개)
| 파일 | 목적 |
|------|------|
| `moment/user/service/user/UserWithdrawService.java` | 탈퇴 도메인 서비스 |
| `test/.../user/service/user/UserWithdrawServiceTest.java` | 탈퇴 서비스 단위 테스트 |
| `test/.../user/service/facade/MyPageFacadeServiceTest.java` | 퍼사드 단위 테스트 |

### Phase A: 수정 (5개)
| 파일 | 변경 |
|------|------|
| `moment/global/exception/ErrorCode.java` | U-014 추가 |
| `moment/user/domain/User.java` | `anonymize()` 추가 |
| `moment/notification/infrastructure/PushNotificationRepository.java` | `deleteAllByUserId()` 추가 |
| `moment/user/service/facade/MyPageFacadeService.java` | `withdraw()` + 의존성 추가 |
| `moment/user/presentation/MyPageController.java` | `DELETE /api/v2/me` + Swagger |

### Phase A: 테스트 수정 (2개)
| 파일 | 변경 |
|------|------|
| `test/.../user/domain/UserTest.java` | anonymize 테스트 3개 추가 |
| `test/.../user/presentation/MyPageControllerTest.java` | E2E 테스트 3개 추가 |

### Phase B: 수정 (6개)
| 파일 | 변경 |
|------|------|
| `moment/moment/infrastructure/MomentRepository.java` | 7개 쿼리 LEFT JOIN 변경 |
| `moment/moment/dto/response/tobe/MomentComposition.java` | null 안전 처리 |
| `moment/comment/dto/tobe/CommentComposition.java` | null 안전 처리 |
| `moment/comment/service/application/CommentApplicationService.java` | mapCommentersByComments null 처리 |
| `moment/moment/dto/response/CommentableMomentResponse.java` | null 안전 처리 |
| `moment/like/service/MomentLikeService.java` + `CommentLikeService.java` | null 체크 추가 |

---

## 검증

```bash
cd /Users/kwonkeonhyeong/Desktop/2025-moment-feat-1059/server

# Phase A 완료 후
./gradlew fastTest

# Phase B 완료 후
./gradlew test

# 전체 빌드
./gradlew build
```