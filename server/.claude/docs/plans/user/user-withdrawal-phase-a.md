# Phase A: 회원 탈퇴 API 세부 구현 계획

> **상위 계획**: `user-withdrawal.md`
> **목표**: 회원 탈퇴 API 엔드포인트 구현 (DELETE /api/v2/me)

---

## TDD 테스트 순서

각 스텝은 Red-Green-Refactor 사이클을 따릅니다.

---

### A-1: ErrorCode 추가

**파일**: `src/main/java/moment/global/exception/ErrorCode.java`

**변경 내용**:
- 기존 User 에러코드 U-013 다음에 U-014 추가

```java
// U-013 뒤에 추가
USER_HAS_OWNED_GROUP("U-014", "소유한 그룹이 있어 탈퇴할 수 없습니다. 그룹을 삭제하거나 소유자를 변경해 주세요.", HttpStatus.BAD_REQUEST),
```

**위치**: `ErrorCode.java:24` (PASSWORD_CHANGE_UNSUPPORTED_PROVIDER 뒤)

**테스트**: 별도 테스트 불필요 (enum 값 추가)

---

### A-2: User.anonymize() + 단위 테스트

**파일**: `src/main/java/moment/user/domain/User.java`

**변경 내용**:
- `anonymize()` 메서드 추가 (검증 우회하여 직접 필드 설정)

```java
public void anonymize() {
    this.email = "withdrawn_" + this.id + "@moment.invalid";
    this.password = "WITHDRAWN";
    this.nickname = "탈퇴한 사용자_" + this.id;
}
```

**위치**: `User.java:112` (`checkProviderType` 메서드 뒤)

**주의사항**:
- `validateEmail()`, `validateNickname()` 호출하지 않음 (익명화 값은 기존 검증 규칙과 다름)
- `this.id`는 이미 영속화된 엔티티에서만 호출하므로 null이 아님
- 닉네임 `"탈퇴한 사용자_{id}"` 형식은 기존 `NICKNAME_REGEX(^.{1,15}$)`를 초과할 수 있으므로 검증 우회 필수
- email `withdrawn_{id}@moment.invalid`는 유니크 제약조건에 의해 userId마다 고유

#### 테스트 (Red 먼저)

**파일**: `src/test/java/moment/user/domain/UserTest.java`

기존 `UserTest.java` 에 테스트 3개 추가. `UserFixture.createUserWithId()`를 사용하여 id가 설정된 User로 테스트.

```
테스트 1: anonymize_호출_시_이메일이_익명화된다
  - Given: UserFixture.createUserWithId(1L)
  - When: user.anonymize()
  - Then: user.getEmail() == "withdrawn_1@moment.invalid"

테스트 2: anonymize_호출_시_비밀번호가_무효화된다
  - Given: UserFixture.createUserWithId(1L)
  - When: user.anonymize()
  - Then: user.getPassword() == "WITHDRAWN"

테스트 3: anonymize_호출_시_닉네임이_익명화된다
  - Given: UserFixture.createUserWithId(1L)
  - When: user.anonymize()
  - Then: user.getNickname() == "탈퇴한 사용자_1"
```

---

### A-3: PushNotificationRepository 메서드 추가

**파일**: `src/main/java/moment/notification/infrastructure/PushNotificationRepository.java`

**변경 내용**:
```java
void deleteAllByUserId(Long userId);
```

**위치**: `PushNotificationRepository.java:18` (마지막 메서드 뒤)

**설명**:
- Spring Data JPA 메서드 네이밍 컨벤션으로 자동 쿼리 생성
- `PushNotification` 엔티티의 `user` 필드 (`@ManyToOne`, `user_id` FK)를 기반으로 동작
- `@SQLDelete` soft delete가 적용되어 실제로는 `deleted_at = NOW()` 실행

**테스트**: Repository 메서드는 E2E에서 간접 검증

---

### A-4: UserWithdrawService 생성 + 단위 테스트

**파일 생성**: `src/main/java/moment/user/service/user/UserWithdrawService.java`

**패키지**: `moment.user.service.user` (기존 UserService와 동일 패키지)

**의존성**:
- `UserRepository` - 유저 조회 및 삭제
- `GroupRepository` - 그룹 소유 여부 검증
- `PushNotificationRepository` - 푸시 알림 구독 삭제
- `Emitters` - SSE 이미터 제거

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

**import 목록**:
```java
import lombok.RequiredArgsConstructor;
import moment.global.exception.ErrorCode;
import moment.global.exception.MomentException;
import moment.group.infrastructure.GroupRepository;
import moment.notification.infrastructure.Emitters;
import moment.notification.infrastructure.PushNotificationRepository;
import moment.user.domain.User;
import moment.user.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

#### 테스트 (Red 먼저)

**파일 생성**: `src/test/java/moment/user/service/user/UserWithdrawServiceTest.java`

- Mock 기반 단위 테스트 (`@ExtendWith(MockitoExtension.class)`)
- `@InjectMocks UserWithdrawService`
- `@Mock UserRepository`, `@Mock GroupRepository`, `@Mock PushNotificationRepository`, `@Mock Emitters`

```
테스트 1: 그룹을_소유하지_않은_유저는_탈퇴_검증에_성공한다
  - Given: groupRepository.findByOwnerId(userId) returns emptyList
  - When: validateWithdrawable(userId)
  - Then: 예외 없음

테스트 2: 그룹을_소유한_유저는_탈퇴_검증에_실패한다
  - Given: groupRepository.findByOwnerId(userId) returns [group]
  - When: validateWithdrawable(userId)
  - Then: MomentException(USER_HAS_OWNED_GROUP) 발생

테스트 3: 유저_탈퇴_시_PII가_익명화되고_소프트_삭제된다
  - Given: userRepository.findById(userId) returns user (id=1L)
  - When: withdraw(userId)
  - Then:
    - user.getEmail() == "withdrawn_1@moment.invalid"
    - user.getNickname() == "탈퇴한 사용자_1"
    - user.getPassword() == "WITHDRAWN"
    - verify(userRepository).delete(user)

테스트 4: 존재하지_않는_유저_탈퇴_시_예외가_발생한다
  - Given: userRepository.findById(userId) returns empty
  - When: withdraw(userId)
  - Then: MomentException(USER_NOT_FOUND) 발생

테스트 5: 탈퇴_시_푸시알림_구독이_삭제된다
  - Given: userRepository.findById(userId) returns user
  - When: withdraw(userId)
  - Then: verify(pushNotificationRepository).deleteAllByUserId(userId)

테스트 6: 탈퇴_시_SSE_이미터가_제거된다
  - Given: userRepository.findById(userId) returns user
  - When: withdraw(userId)
  - Then: verify(emitters).remove(userId)
```

**Fixture 사용**: `UserFixture.createUserWithId(1L)` - id가 설정된 유저 필요 (anonymize 시 id 사용)

**Group Mock**: Group 엔티티 또는 `mock(Group.class)` 사용

---

### A-5: MyPageFacadeService.withdraw() 추가

**파일 수정**: `src/main/java/moment/user/service/facade/MyPageFacadeService.java`

**변경 내용**:

1. 의존성 추가:
```java
private final UserWithdrawService userWithdrawService;
private final AuthService authService;
```

2. import 추가:
```java
import moment.auth.application.AuthService;
import moment.user.service.user.UserWithdrawService;
```

3. 메서드 추가:
```java
@Transactional
public void withdraw(Long userId) {
    userWithdrawService.validateWithdrawable(userId);
    authService.logout(userId);
    userWithdrawService.withdraw(userId);
}
```

**호출 순서 중요**:
1. `validateWithdrawable` - 그룹 소유 검증 (실패 시 빠른 반환)
2. `authService.logout` - RefreshToken 삭제 (**User soft delete 전에 호출해야 함** - `logout()`이 내부에서 `userRepository.findById()`를 호출하므로 soft delete 후에는 유저를 찾지 못함)
3. `userWithdrawService.withdraw` - 익명화 + soft delete

#### 테스트 (Red 먼저)

**파일 생성**: `src/test/java/moment/user/service/facade/MyPageFacadeServiceTest.java`

- Mock 기반 단위 테스트 (`@ExtendWith(MockitoExtension.class)`)
- `@InjectMocks MyPageFacadeService`
- `@Mock UserService`, `@Mock UserWithdrawService`, `@Mock AuthService`

```
테스트 1: 회원탈퇴_시_검증_로그아웃_탈퇴_순서로_호출된다
  - Given: userId = 1L
  - When: withdraw(userId)
  - Then:
    - InOrder inOrder = inOrder(userWithdrawService, authService)
    - inOrder.verify(userWithdrawService).validateWithdrawable(userId)
    - inOrder.verify(authService).logout(userId)
    - inOrder.verify(userWithdrawService).withdraw(userId)

테스트 2: 그룹_소유자가_회원탈퇴를_시도하면_예외가_발생한다
  - Given: doThrow(new MomentException(USER_HAS_OWNED_GROUP))
           .when(userWithdrawService).validateWithdrawable(userId)
  - When: withdraw(userId)
  - Then:
    - MomentException(USER_HAS_OWNED_GROUP) 발생
    - verify(authService, never()).logout(any())
    - verify(userWithdrawService, never()).withdraw(any())
```

---

### A-6: MyPageController - DELETE /api/v2/me

**파일 수정**: `src/main/java/moment/user/presentation/MyPageController.java`

**변경 내용**:

1. import 추가:
```java
import org.springframework.web.bind.annotation.DeleteMapping;
```

2. 메서드 추가 (기존 `changePassword` 메서드의 쿠키 초기화 패턴 재사용):

```java
@Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. 소유한 그룹이 있는 경우 탈퇴할 수 없습니다.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = """
                - [U-014] 소유한 그룹이 있어 탈퇴할 수 없습니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = """
                - [T-005] 토큰을 찾을 수 없습니다.
                """,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
})
@DeleteMapping
public ResponseEntity<SuccessResponse<Void>> withdraw(
        @AuthenticationPrincipal Authentication authentication
) {
    myPageFacadeService.withdraw(authentication.id());

    ResponseCookie accessCookie = ResponseCookie.from("accessToken", null)
            .sameSite("none")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", null)
            .sameSite("none")
            .secure(true)
            .httpOnly(true)
            .path("/")
            .maxAge(0)
            .build();

    HttpStatus status = HttpStatus.OK;

    return ResponseEntity.status(status)
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(SuccessResponse.of(status, null));
}
```

**패턴 참고**: 기존 `changePassword()` 메서드 (MyPageController.java:106-137)의 쿠키 초기화 코드와 동일한 패턴

**주의**: `@DeleteMapping` 사용 (경로 없음 - `@RequestMapping("/api/v2/me")`에 의해 `DELETE /api/v2/me`로 매핑)

**컨트롤러에서 logout 호출하지 않는 이유**: logout은 이미 `MyPageFacadeService.withdraw()` 내부에서 호출됨. 컨트롤러는 쿠키 초기화만 담당.

#### E2E 테스트 (Red 먼저)

**파일 수정**: `src/test/java/moment/user/presentation/MyPageControllerTest.java`

기존 E2E 테스트 파일에 테스트 3개 추가. 추가 필요 의존성: `GroupRepository`, `GroupFixture` (또는 Group 직접 생성).

**추가 의존성 주입** (테스트 클래스 필드):
```java
@Autowired
private GroupRepository groupRepository;
```

```
테스트 1: 회원탈퇴에_성공하고_쿠키가_초기화된다
  - Given:
    - User user 저장
    - accessToken 생성
  - When: DELETE /api/v2/me (cookie: accessToken)
  - Then:
    - statusCode == 200
    - response.getCookie("accessToken") 비어있음
    - response.getCookie("refreshToken") 비어있음
    - GET /api/v2/me/profile (같은 토큰) → 404 또는 401
      (유저가 soft delete 되었으므로 프로필 조회 실패)

테스트 2: 그룹_소유자가_회원탈퇴를_시도하면_400_에러를_반환한다
  - Given:
    - User user 저장
    - Group group(owner=user) 저장
    - accessToken 생성
  - When: DELETE /api/v2/me (cookie: accessToken)
  - Then:
    - statusCode == 400
    - error code == "U-014"

테스트 3: 인증되지_않은_사용자가_회원탈퇴를_시도하면_401_에러를_반환한다
  - Given: 토큰 없음
  - When: DELETE /api/v2/me (no cookie)
  - Then:
    - statusCode == 401
```

---

## 구현 순서 요약

| 순서 | 작업 | 타입 | TDD 사이클 |
|------|------|------|-----------|
| 1 | A-1: ErrorCode U-014 추가 | 구조적 변경 | - (enum 추가) |
| 2 | A-2: User.anonymize() 테스트 작성 | Red | 테스트 3개 실패 |
| 3 | A-2: User.anonymize() 구현 | Green | 테스트 3개 통과 |
| 4 | A-3: PushNotificationRepository 메서드 추가 | 구조적 변경 | - |
| 5 | A-4: UserWithdrawServiceTest 작성 | Red | 테스트 6개 실패 |
| 6 | A-4: UserWithdrawService 구현 | Green | 테스트 6개 통과 |
| 7 | A-5: MyPageFacadeServiceTest 작성 | Red | 테스트 2개 실패 |
| 8 | A-5: MyPageFacadeService.withdraw() 구현 | Green | 테스트 2개 통과 |
| 9 | A-6: MyPageControllerTest E2E 작성 | Red | 테스트 3개 실패 |
| 10 | A-6: MyPageController endpoint 구현 | Green | 테스트 3개 통과 |

---

## 검증 명령어

```bash
cd /Users/kwonkeonhyeong/Desktop/2025-moment-feat-1059/server

# 단위 테스트만 실행
./gradlew fastTest

# 특정 테스트 클래스 실행
./gradlew test --tests "moment.user.domain.UserTest"
./gradlew test --tests "moment.user.service.user.UserWithdrawServiceTest"
./gradlew test --tests "moment.user.service.facade.MyPageFacadeServiceTest"

# E2E 포함 전체 테스트
./gradlew test --tests "moment.user.presentation.MyPageControllerTest"

# 전체 빌드
./gradlew build
```

---

## 수정/생성 파일 목록

### 신규 생성 (3개)
| 파일 | 목적 |
|------|------|
| `src/main/java/moment/user/service/user/UserWithdrawService.java` | 탈퇴 도메인 서비스 |
| `src/test/java/moment/user/service/user/UserWithdrawServiceTest.java` | 탈퇴 서비스 단위 테스트 |
| `src/test/java/moment/user/service/facade/MyPageFacadeServiceTest.java` | 퍼사드 단위 테스트 |

### 수정 (5개)
| 파일 | 변경 |
|------|------|
| `src/main/java/moment/global/exception/ErrorCode.java` (line 24) | U-014 추가 |
| `src/main/java/moment/user/domain/User.java` (line 112) | `anonymize()` 메서드 추가 |
| `src/main/java/moment/notification/infrastructure/PushNotificationRepository.java` (line 18) | `deleteAllByUserId()` 추가 |
| `src/main/java/moment/user/service/facade/MyPageFacadeService.java` (전체) | 의존성 + `withdraw()` 추가 |
| `src/main/java/moment/user/presentation/MyPageController.java` (끝) | `DELETE /api/v2/me` 엔드포인트 추가 |

### 테스트 수정 (2개)
| 파일 | 변경 |
|------|------|
| `src/test/java/moment/user/domain/UserTest.java` | anonymize 테스트 3개 추가 |
| `src/test/java/moment/user/presentation/MyPageControllerTest.java` | E2E 테스트 3개 + GroupRepository 주입 추가 |
