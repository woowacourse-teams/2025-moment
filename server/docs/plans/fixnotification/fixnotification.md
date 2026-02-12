# 디바이스 토큰 Lifecycle 관리 수정 계획

## Context

푸시 알림 시스템에서 디바이스 토큰의 lifecycle이 관리되지 않아 두 가지 문제가 발생:

1. **로그아웃 시 토큰 미삭제**: `AuthService.logout()`이 refreshToken만 삭제하고 디바이스 토큰(`PushNotification`)을 남겨둠 → 로그아웃 후에도 푸시 알림 수신
2. **크로스유저 토큰 중복**: 같은 디바이스에서 계정 전환 시 `existsByUserAndDeviceEndpoint(user, deviceEndpoint)` 가 유저별로만 체크하여 동일 토큰이 여러 유저에 등록됨 → 다른 유저의 알림이 잘못 전달

---

## 수정 1: 크로스유저 토큰 중복 해결 (PushNotificationService.save)

**파일**: `api/src/main/java/moment/notification/service/notification/PushNotificationService.java`

디바이스 토큰 등록 시, 같은 토큰이 다른 유저에 등록되어 있으면 먼저 삭제 후 저장:

```java
@Transactional
public void save(User user, String deviceEndpoint) {
    if (pushNotificationRepository.existsByUserAndDeviceEndpoint(user, deviceEndpoint)) {
        return;  // 이미 이 유저에 등록됨 → 스킵
    }
    pushNotificationRepository.deleteByDeviceEndpoint(deviceEndpoint);  // 다른 유저 소유 토큰 제거
    pushNotificationRepository.save(new PushNotification(user, deviceEndpoint));
}
```

- `deleteByDeviceEndpoint()` 메서드는 이미 Repository에 존재하나 미사용 상태
- `@SQLDelete` soft delete가 적용되므로 실제로는 `deleted_at = NOW()` UPDATE 수행

---

## 수정 2: 로그아웃 시 디바이스 토큰 삭제

### 2a. LogoutRequest DTO 생성 (신규)

**파일**: `api/src/main/java/moment/auth/dto/request/LogoutRequest.java`

```java
@Schema(description = "로그아웃 요청")
public record LogoutRequest(
    @Schema(description = "디바이스 푸시 토큰 (선택)",
            example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]", nullable = true)
    String deviceEndpoint
) {}
```

### 2b. AuthService.logout() 수정

**파일**: `api/src/main/java/moment/auth/application/AuthService.java`

- `PushNotificationService` 의존성 추가
- `logout(Long userId)` → `logout(Long userId, String deviceEndpoint)` 시그니처 변경
- `deviceEndpoint`가 non-null/non-blank이면 해당 디바이스 토큰 삭제

```java
private final PushNotificationService pushNotificationService;  // 추가

@Transactional
public void logout(Long userId, String deviceEndpoint) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new MomentException(ErrorCode.USER_NOT_FOUND));

    if (refreshTokenRepository.existsByUser(user)) {
        refreshTokenRepository.deleteByUser(user);
    }

    if (deviceEndpoint != null && !deviceEndpoint.isBlank()) {
        pushNotificationService.deleteBy(user, deviceEndpoint);
    }
}
```

### 2c. AuthController.logout() 수정

**파일**: `api/src/main/java/moment/auth/presentation/AuthController.java`

- `@RequestBody(required = false) LogoutRequest request` 파라미터 추가
- 하위 호환성 보장: body 없이 요청해도 기존 동작 유지

```java
@PostMapping("/logout")
public ResponseEntity<SuccessResponse<Void>> logout(
        @AuthenticationPrincipal Authentication authentication,
        @RequestBody(required = false) LogoutRequest request
) {
    String deviceEndpoint = (request != null) ? request.deviceEndpoint() : null;
    authService.logout(authentication.id(), deviceEndpoint);
    // ... 쿠키 클리어 (기존 로직 유지)
}
```

---

## 테스트 계획 (TDD 순서)

### Phase 1: Repository 테스트 — deleteByDeviceEndpoint 검증

**파일**: `api/src/test/java/moment/notification/infrastructure/PushNotificationRepositoryTest.java`

| 테스트 | 설명 |
|--------|------|
| `디바이스_엔드포인트로_푸시_알림을_삭제한다` | userA, userB 동일 토큰 등록 → `deleteByDeviceEndpoint()` → 둘 다 삭제 확인 |

### Phase 2: Service 테스트 — 크로스유저 중복 제거 검증

**파일**: `api/src/test/java/moment/notification/service/notification/PushNotificationServiceTest.java`

| 테스트 | 설명 |
|--------|------|
| `다른_사용자가_동일한_디바이스_토큰을_등록하면_기존_등록이_삭제된다` | userA 토큰 등록 → userB 같은 토큰 등록 → userA 토큰 삭제됨, userB만 존재 |

### Phase 3: AuthService 통합 테스트 (신규 파일)

**파일**: `api/src/test/java/moment/auth/application/AuthServiceTest.java`

| 테스트 | 설명 |
|--------|------|
| `로그아웃_시_디바이스_엔드포인트가_제공되면_해당_토큰이_삭제된다` | 유저+토큰 등록 → `logout(userId, endpoint)` → RefreshToken 삭제 + PushNotification 삭제 |
| `로그아웃_시_디바이스_엔드포인트가_null이면_토큰은_유지된다` | `logout(userId, null)` → RefreshToken만 삭제, PushNotification 유지 |

### Phase 4: E2E 테스트

**파일**: `api/src/test/java/moment/auth/presentation/AuthControllerTest.java`

| 테스트 | 설명 |
|--------|------|
| `로그아웃_시_디바이스_엔드포인트를_전달하면_해당_토큰이_삭제된다` | 로그인 → 토큰 등록 → POST logout with body → DB에서 토큰 삭제 확인 |
| `로그아웃_시_body_없이_요청해도_성공한다` | 하위 호환성 확인 (기존 클라이언트 동작) |

---

## 수정 파일 목록

| 파일 | 작업 |
|------|------|
| `api/src/main/java/moment/auth/dto/request/LogoutRequest.java` | **신규** — 로그아웃 요청 DTO |
| `api/src/main/java/moment/notification/service/notification/PushNotificationService.java` | **수정** — `save()`에 크로스유저 중복 제거 추가 |
| `api/src/main/java/moment/auth/application/AuthService.java` | **수정** — `PushNotificationService` 의존성 추가, `logout()` 시그니처 변경 |
| `api/src/main/java/moment/auth/presentation/AuthController.java` | **수정** — optional `LogoutRequest` body 파라미터 추가 |
| `api/src/test/java/moment/notification/infrastructure/PushNotificationRepositoryTest.java` | **수정** — `deleteByDeviceEndpoint` 테스트 추가 |
| `api/src/test/java/moment/notification/service/notification/PushNotificationServiceTest.java` | **수정** — 크로스유저 중복 테스트 추가 |
| `api/src/test/java/moment/auth/application/AuthServiceTest.java` | **신규** — AuthService 통합 테스트 |
| `api/src/test/java/moment/auth/presentation/AuthControllerTest.java` | **수정** — 로그아웃 E2E 테스트 추가 |

---

## 구현 순서

1. **Phase 1 (Red)**: `PushNotificationRepositoryTest`에 `deleteByDeviceEndpoint` 테스트 추가 → 실행 확인 (이미 메서드 존재하므로 Pass 예상)
2. **Phase 2 (Red → Green)**: `PushNotificationServiceTest`에 크로스유저 테스트 추가 → Fail → `PushNotificationService.save()` 수정 → Pass
3. **Phase 3 (Red → Green)**: `AuthServiceTest` 신규 작성 → Fail → `LogoutRequest` DTO 생성 + `AuthService.logout()` 수정 → Pass
4. **Phase 4 (Red → Green)**: `AuthControllerTest` E2E 테스트 추가 → Fail → `AuthController.logout()` 수정 → Pass
5. **검증**: `./gradlew fastTest` 전체 통과 확인

---

## 검증 방법

```bash
./gradlew fastTest   # 전체 테스트 (E2E 제외)
./gradlew e2eTest    # E2E 테스트
```

기존 `로그아웃에_성공한다` E2E 테스트가 body 없이 요청하므로, `@RequestBody(required = false)`로 하위 호환성 자동 검증됨.
