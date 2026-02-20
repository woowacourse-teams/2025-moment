# Phase 3: 확장 규칙 (3개)

> 기반 문서: `docs/arch/archunit-implementation-plan.md`
> 선행 조건: Phase 2 완료 (32개 규칙 통과)
> 목표: 기존 테스트 파일 확장 + ModuleBoundaryRuleTest 신규 + 3개 규칙 추가

---

## 개요

Phase 3는 인증 패턴 검증, 모듈 경계 규칙, API URL 형식 검증 등 고급 규칙입니다.

| Step | 확장/신규 파일 | 추가 규칙 수 | 규칙 ID |
|------|-------------|------------|---------|
| 3-1 | `AnnotationConsistencyRuleTest.java` (확장) | 2 | AU-001 ~ AU-002 |
| 3-2 | `ModuleBoundaryRuleTest.java` (신규) | 1 | M-003 |
| 3-3 | `AnnotationConsistencyRuleTest.java` (확장) | - | API URL 형식 (선택) |

---

## Step 3-1: 인증 패턴 규칙 (AU-001 ~ AU-002)

### 파일

**`api/src/test/java/moment/arch/AnnotationConsistencyRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

```java
// === Phase 3 인증 패턴 규칙 ===

@ArchTest
static final ArchRule AU_001_User_API_Controller는_AuthenticationPrincipal을_사용한다 =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .and().resideOutsideOfPackage("..admin..")   // Admin 모듈 제외
        .should(useAuthenticationPrincipal())
        .because("[AU-001] User API Controller는 @AuthenticationPrincipal을 사용해야 합니다. " +
                 "수정 가이드: 인증이 필요한 엔드포인트에 " +
                 "@AuthenticationPrincipal Authentication authentication 파라미터를 추가하세요.");
```

### AU-001 커스텀 ArchCondition

```java
/**
 * User API Controller의 핸들러 메서드 중 하나 이상이
 * @AuthenticationPrincipal을 파라미터로 사용하는지 검증.
 *
 * 예외: 인증 불필요 엔드포인트 (회원가입, 로그인 등)가 있는 Controller는
 * 모든 메서드에 @AuthenticationPrincipal이 있을 필요 없음.
 * → 최소 1개 메서드에 있으면 통과.
 */
private static ArchCondition<JavaClass> useAuthenticationPrincipal() {
    return new ArchCondition<>("use @AuthenticationPrincipal in at least one method") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            // 인증이 전혀 불필요한 Controller 제외 (AuthController 등)
            if (javaClass.getSimpleName().contains("Auth")) {
                return;
            }

            boolean hasAuthPrincipal = javaClass.getMethods().stream()
                .flatMap(method -> method.getParameterAnnotations().stream())
                .flatMap(java.util.Collection::stream)
                .anyMatch(annotation ->
                    annotation.getRawType().getSimpleName()
                        .equals("AuthenticationPrincipal"));

            if (!hasAuthPrincipal) {
                events.add(SimpleConditionEvent.violated(javaClass,
                    String.format("[AU-001] %s에 @AuthenticationPrincipal을 사용하는 메서드가 없습니다",
                        javaClass.getSimpleName())));
            }
        }
    };
}
```

### AU-002 구현 (Admin 세션 기반 인증)

```java
// AU-002는 Admin 모듈 규칙이므로 admin 모듈 테스트에서 별도 검증.
// Admin Controller는 HttpSession 파라미터 또는 Interceptor 기반 인증을 사용.
// Admin 모듈은 Phase Admin에서 처리.
//
// 참고: AdminAuthInterceptor가 모든 /api/admin/** 경로를 인터셉트하므로
// 개별 Controller에서 세션 파라미터가 없어도 인증은 보장됨.
// → AU-002는 admin 모듈에서만 의미 있으며, 규칙 강도를 "확인" 수준으로 제한.
```

### AU-001 주의사항

- `AuthController`는 인증이 불필요한 엔드포인트만 가지므로 제외
- `@AuthenticationPrincipal`은 프로젝트 커스텀 어노테이션 (`moment.user.dto.request.AuthenticationPrincipal`)
- User API의 대부분 Controller는 이미 `@AuthenticationPrincipal` 사용 중

### 잠재적 위반

- 공개 API만 가진 Controller가 있을 수 있음 (예: 건강 체크 엔드포인트)
- 필요시 `@AuthenticationPrincipal(required = false)` 사용하는 경우도 있음

---

## Step 3-2: ModuleBoundaryRuleTest (M-003) — 신규 파일

### 파일

**`api/src/test/java/moment/arch/ModuleBoundaryRuleTest.java`** (신규 생성)

### 구현

```java
package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class ModuleBoundaryRuleTest extends BaseArchTest {

    /**
     * M-003: 도메인 간 서비스 의존은 Application 또는 Facade를 통해서만 가능
     *
     * Domain Service (service/{domain}/) 간 직접 의존을 금지.
     * 다른 도메인의 서비스가 필요하면 Application 또는 Facade Service를 통해 접근.
     *
     * 예외:
     * - 같은 도메인 내 Domain Service 간 의존은 허용
     * - Domain Service → Application/Facade Service 의존은 허용
     */
    @ArchTest
    static final ArchRule M_003_도메인_간_서비스_의존은_Application_또는_Facade를_통해서만_가능하다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..service.(*)..")
            .and().resideOutsideOfPackage("..service.application..")
            .and().resideOutsideOfPackage("..service.facade..")
            .and().resideOutsideOfPackage("..service.eventHandler..")
            .should(notDependOnOtherDomainServiceDirectly())
            .because("[M-003] 도메인 간 서비스 의존은 Application 또는 Facade를 통해서만 가능합니다. " +
                     "수정 가이드: 다른 도메인의 Domain Service를 직접 호출하지 말고, " +
                     "해당 도메인의 Application Service 또는 Facade Service를 통해 접근하세요.");

    // === 커스텀 ArchCondition ===

    /**
     * Domain Service가 다른 도메인의 Domain Service를 직접 의존하는지 검증.
     *
     * 허용: moment.moment.service.moment.MomentService → moment.moment.service.moment.MomentImageService (같은 도메인)
     * 금지: moment.comment.service.comment.CommentService → moment.moment.service.moment.MomentService (다른 도메인)
     */
    private static ArchCondition<JavaClass> notDependOnOtherDomainServiceDirectly() {
        return new ArchCondition<>("not depend on other domain's Domain Service directly") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                // 현재 클래스의 도메인 추출 (moment.{domain}.service.{subpackage})
                String currentDomain = extractDomain(javaClass.getPackageName());
                if (currentDomain == null) return;

                javaClass.getDirectDependenciesFromSelf().stream()
                    .map(dep -> dep.getTargetClass())
                    .filter(target -> target.isAnnotatedWith(Service.class))
                    .filter(target -> isDomainService(target))
                    .filter(target -> {
                        String targetDomain = extractDomain(target.getPackageName());
                        return targetDomain != null && !targetDomain.equals(currentDomain);
                    })
                    .forEach(target -> {
                        events.add(SimpleConditionEvent.violated(javaClass,
                            String.format("%s이(가) 다른 도메인의 Domain Service(%s)에 직접 의존합니다. " +
                                "Application 또는 Facade Service를 통해 접근하세요.",
                                javaClass.getSimpleName(),
                                target.getSimpleName())));
                    });
            }

            /**
             * 패키지명에서 도메인 추출
             * 예: "moment.moment.service.moment" → "moment"
             *     "moment.comment.service.comment" → "comment"
             *     "moment.user.service.user" → "user"
             */
            private String extractDomain(String packageName) {
                // moment.{domain}.service.{subpackage}
                String[] parts = packageName.split("\\.");
                if (parts.length >= 2 && "moment".equals(parts[0])) {
                    return parts[1]; // 도메인명
                }
                return null;
            }

            /**
             * Domain Service인지 확인
             * - service.application.., service.facade.., service.eventHandler..는 Domain Service가 아님
             * - service.{domain}..만 Domain Service
             */
            private boolean isDomainService(JavaClass javaClass) {
                String pkg = javaClass.getPackageName();
                return pkg.contains(".service.")
                    && !pkg.contains(".service.application")
                    && !pkg.contains(".service.facade")
                    && !pkg.contains(".service.eventHandler");
            }
        };
    }
}
```

### M-003 도메인 서비스 의존 매트릭스

현재 코드베이스에서 확인이 필요한 도메인 간 의존:

| Domain Service | 의존 대상 | 허용 여부 |
|---------------|---------|---------|
| `MomentService` | `UserService` | 금지 → `MomentApplicationService`를 통해 |
| `CommentService` | `MomentService` | 금지 → `CommentApplicationService`를 통해 |
| `GroupMemberService` | `UserService` | 금지 → `GroupMemberApplicationService`를 통해 |
| `MomentLikeService` | `MomentService` (같은 like 도메인?) | 확인 필요 |

### 잠재적 위반 시 대응

1. Domain Service 간 직접 의존이 발견되면:
   - Application Service를 통해 조율하도록 리팩토링
   - 또는 이벤트 기반 통신으로 전환
2. 같은 도메인 내 서비스 간 의존은 허용 (예: `MomentService` → `MomentImageService`)

---

## Step 3-3: API URL 형식 검증 (선택적 규칙)

### 파일

**`api/src/test/java/moment/arch/AnnotationConsistencyRuleTest.java`** 또는 별도 파일

### 구현

```java
// === Phase 3 API URL 형식 검증 (선택적) ===

/**
 * User API: /api/v2/{resources} 패턴
 * Admin API: /api/admin/{resources} 패턴
 *
 * @RequestMapping의 value가 위 패턴을 따르는지 검증.
 * 구현 난이도가 높고 예외가 많아 선택적 적용.
 */
@ArchTest
static final ArchRule URL_형식_User_API는_api_v2로_시작해야_한다 =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .and().resideOutsideOfPackage("..admin..")
        .should(haveRequestMappingStartingWith("/api/v2"))
        .because("[URL-001] User API Controller는 /api/v2/ 경로를 사용해야 합니다. " +
                 "수정 가이드: @RequestMapping 값을 /api/v2/{리소스명}으로 변경하세요.");
```

### 커스텀 ArchCondition

```java
private static ArchCondition<JavaClass> haveRequestMappingStartingWith(String prefix) {
    return new ArchCondition<>("have @RequestMapping starting with " + prefix) {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            javaClass.getAnnotations().stream()
                .filter(a -> a.getRawType().getName().equals(
                    "org.springframework.web.bind.annotation.RequestMapping"))
                .forEach(a -> {
                    Object value = a.getProperties().get("value");
                    if (value instanceof String[] paths) {
                        for (String path : paths) {
                            if (!path.startsWith(prefix)) {
                                events.add(SimpleConditionEvent.violated(javaClass,
                                    String.format("[URL-001] %s의 @RequestMapping('%s')이 " +
                                        "'%s'로 시작하지 않습니다",
                                        javaClass.getSimpleName(), path, prefix)));
                            }
                        }
                    }
                });
        }
    };
}
```

### 주의사항

- 일부 Controller는 `@RequestMapping` 없이 메서드 레벨 `@GetMapping`/`@PostMapping`만 사용할 수 있음
- 레거시 API (`/api/v1/`)가 있을 수 있으므로 기존 코드 확인 후 적용
- 이 규칙은 **선택적**이며, 팀 합의 후 활성화

---

## Phase 3 전체 검증

### 실행 명령

```bash
cd server

# 1. Phase 3 포함 전체 ArchUnit 테스트
./gradlew test -p api --tests "moment.arch.*"

# 2. 새로 추가된 ModuleBoundaryRuleTest만 실행
./gradlew test -p api --tests "moment.arch.ModuleBoundaryRuleTest"

# 3. fastTest 전체 실행
./gradlew fastTest -p api
```

### 완료 기준

- [ ] AU-001 User API 인증 패턴 검증 통과
- [ ] M-003 도메인 간 서비스 의존성 규칙 통과
- [ ] (선택) API URL 형식 규칙 통과
- [ ] `./gradlew fastTest` 실행 시간 허용 범위 내

### 커밋 전략

```
커밋 1: test: ArchUnit 인증 패턴 규칙 추가 (AU-001)
커밋 2: test: ArchUnit 모듈 경계 규칙 추가 (M-003) — ModuleBoundaryRuleTest 신규
커밋 3: test: ArchUnit API URL 형식 규칙 추가 (선택적)
```

### Phase 3 위험도 분석

| 규칙 | 위험도 | 사유 |
|------|--------|------|
| AU-001 | 중 | 인증 불필요 Controller 예외 처리 필요 |
| M-003 | 높음 | 도메인 간 직접 의존이 있을 수 있어 리팩토링 필요 가능 |
| URL 형식 | 낮음 | 선택적, 레거시 API 유무에 따라 조정 |

### M-003 위반 발생 시 리팩토링 가이드

```
AS-IS (위반):
  CommentService → MomentService (다른 도메인 Domain Service 직접 호출)

TO-BE (준수):
  CommentApplicationService → MomentService (Application Service에서 조율)
  CommentService → MomentService (X, 금지)
```

리팩토링 단계:
1. Domain Service 간 직접 의존 발견
2. 호출하는 쪽에서 Application Service로 로직 이동
3. 테스트 수정 및 통과 확인
