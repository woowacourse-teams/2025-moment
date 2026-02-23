# Phase 2: MEDIUM 우선순위 규칙 (10개)

> 기반 문서: `docs/arch/archunit-implementation-plan.md`
> 선행 조건: Phase 1 완료 (22개 규칙 통과)
> 목표: 기존 테스트 파일 확장 + 10개 규칙 추가

---

## 개요

Phase 2는 Phase 1에서 생성한 테스트 파일을 확장하여 MEDIUM 우선순위 규칙을 추가합니다.

| Step | 확장 대상 파일 | 추가 규칙 수 | 규칙 ID |
|------|-------------|------------|---------|
| 2-1 | `NamingConventionRuleTest.java` | 3 | N-004 ~ N-006 |
| 2-2 | `AnnotationConsistencyRuleTest.java` | 2 | A-006 ~ A-007 |
| 2-3 | `PackageStructureRuleTest.java` | 2 | P-005 ~ P-006 |
| 2-4 | `DtoRuleTest.java` | 1 | D-001 |
| 2-5 | `LayerDependencyRuleTest.java` | 2 | L-005 ~ L-006 |

---

## Step 2-1: NamingConventionRuleTest 확장 (N-004 ~ N-006)

### 파일

**`api/src/test/java/moment/arch/NamingConventionRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

```java
// === Phase 2 추가 규칙 ===

@ArchTest
static final ArchRule N_004_EventHandler_클래스는_EventHandler로_끝나야_한다 =
    classes()
        .that().resideInAPackage("..service.eventHandler..")
        .should().haveSimpleNameEndingWith("EventHandler")
        .because("[N-004] eventHandler 패키지의 클래스는 'EventHandler'로 끝나야 합니다. " +
                 "수정 가이드: 클래스명을 {도메인}EventHandler로 변경하세요.");

@ArchTest
static final ArchRule N_005_ApplicationService는_ApplicationService로_끝나야_한다 =
    classes()
        .that().resideInAPackage("..service.application..")
        .and().areAnnotatedWith(Service.class)
        .should().haveSimpleNameEndingWith("ApplicationService")
        .because("[N-005] application 패키지의 @Service 클래스는 'ApplicationService'로 끝나야 합니다. " +
                 "수정 가이드: 클래스명을 {도메인}ApplicationService로 변경하세요.");

@ArchTest
static final ArchRule N_006_FacadeService는_FacadeService로_끝나야_한다 =
    classes()
        .that().resideInAPackage("..service.facade..")
        .and().areAnnotatedWith(Service.class)
        .should().haveSimpleNameEndingWith("FacadeService")
        .because("[N-006] facade 패키지의 @Service 클래스는 'FacadeService'로 끝나야 합니다. " +
                 "수정 가이드: 클래스명을 {도메인}{액션}FacadeService로 변경하세요.");
```

### import 추가

```java
import org.springframework.stereotype.Service;  // 이미 존재할 수 있음
```

### 현재 코드베이스 확인 사항

| 패키지 | 현재 클래스명 | 네이밍 규칙 준수 |
|--------|-------------|---------------|
| `..service.eventHandler..` | `NotificationEventHandler` | EventHandler (통과) |
| `..service.application..` | `MomentApplicationService` | ApplicationService (통과) |
| `..service.application..` | `CommentApplicationService` | ApplicationService (통과) |
| `..service.application..` | `GroupApplicationService` | ApplicationService (통과) |
| `..service.application..` | `GroupMemberApplicationService` | ApplicationService (통과) |
| `..service.facade..` | `MomentCreateFacadeService` | FacadeService (통과) |
| `..service.facade..` | `CommentCreateFacadeService` | FacadeService (통과) |
| `..service.facade..` | `MyGroupMomentPageFacadeService` | FacadeService (통과) |
| `..service.facade..` | `MyGroupCommentPageFacadeService` | FacadeService (통과) |

---

## Step 2-2: AnnotationConsistencyRuleTest 확장 (A-006 ~ A-007)

### 파일

**`api/src/test/java/moment/arch/AnnotationConsistencyRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

```java
// === Phase 2 추가 규칙 ===

@ArchTest
static final ArchRule A_006_Controller의_RequestBody에_Valid를_사용한다 =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .should(useValidWithRequestBody())
        .because("[A-006] Controller에서 @RequestBody를 사용할 때 @Valid를 함께 사용해야 합니다. " +
                 "수정 가이드: @RequestBody 파라미터 앞에 @Valid를 추가하세요.");

@ArchTest
static final ArchRule A_007_Service_클래스는_클래스_레벨_Transactional_readOnly_true를_적용한다 =
    classes()
        .that().areAnnotatedWith(Service.class)
        .should(haveTransactionalReadOnlyTrue())
        .because("[A-007] @Service 클래스는 클래스 레벨에 @Transactional(readOnly = true)를 적용해야 합니다. " +
                 "수정 가이드: 클래스에 @Transactional(readOnly = true)를 추가하세요. " +
                 "쓰기 작업 메서드에는 별도로 @Transactional을 오버라이드하세요.");
```

### 커스텀 ArchCondition 구현

```java
// === A-006 커스텀 조건 ===

/**
 * Controller 메서드에서 @RequestBody가 있으면 @Valid도 함께 있는지 검증
 */
private static ArchCondition<JavaClass> useValidWithRequestBody() {
    return new ArchCondition<>("use @Valid with @RequestBody") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            javaClass.getMethods().stream()
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .forEach(method -> {
                    method.getParameterAnnotations().forEach(annotations -> {
                        boolean hasRequestBody = annotations.stream()
                            .anyMatch(a -> a.getRawType().getName().equals(
                                "org.springframework.web.bind.annotation.RequestBody"));
                        boolean hasValid = annotations.stream()
                            .anyMatch(a -> a.getRawType().getName().equals(
                                "jakarta.validation.Valid"));

                        if (hasRequestBody && !hasValid) {
                            events.add(SimpleConditionEvent.violated(javaClass,
                                String.format("[A-006] %s.%s()에서 @RequestBody에 @Valid가 누락되었습니다",
                                    javaClass.getSimpleName(), method.getName())));
                        }
                    });
                });
        }
    };
}

// === A-007 커스텀 조건 ===

/**
 * @Transactional(readOnly = true)가 클래스 레벨에 있는지 검증
 */
private static ArchCondition<JavaClass> haveTransactionalReadOnlyTrue() {
    return new ArchCondition<>("have @Transactional(readOnly = true) at class level") {
        @Override
        public void check(JavaClass javaClass, ConditionEvents events) {
            boolean hasReadOnlyTrue = javaClass.getAnnotations().stream()
                .filter(a -> a.getRawType().getName().equals(
                    "org.springframework.transaction.annotation.Transactional"))
                .anyMatch(a -> {
                    Object readOnly = a.getProperties().get("readOnly");
                    return Boolean.TRUE.equals(readOnly);
                });

            if (!hasReadOnlyTrue) {
                events.add(SimpleConditionEvent.violated(javaClass,
                    String.format("[A-007] %s에 @Transactional(readOnly = true)가 " +
                        "클래스 레벨에 없습니다. " +
                        "수정 가이드: 클래스에 @Transactional(readOnly = true)를 추가하세요.",
                        javaClass.getName())));
            }
        }
    };
}
```

### 추가 import

```java
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.web.bind.annotation.RestController;
```

### A-006 주의사항

- ArchUnit의 `getParameterAnnotations()` API는 메서드 파라미터별 어노테이션 리스트를 반환
- `@RequestBody`와 `@Valid`가 동일 파라미터에 있는지 확인해야 함
- ArchUnit 버전에 따라 파라미터 어노테이션 접근 API가 다를 수 있음

**대안 구현 (더 단순)**:
```java
// 메서드 바이트코드에서 @Valid와 @RequestBody를 같이 사용하는지만 확인
// 파라미터 레벨까지 추적하지 않고 메서드 레벨에서 검증
```

### A-007 잠재적 위반

현재 코드베이스에서 `@Service` + `@Transactional(readOnly = true)` 패턴:

| 서비스 | 패턴 준수 |
|--------|---------|
| `MomentLikeService` | `@Transactional(readOnly = true)` 클래스 레벨 (통과) |
| `CommentLikeService` | `@Transactional(readOnly = true)` 클래스 레벨 (통과) |
| `MomentApplicationService` | `@Transactional(readOnly = true)` 클래스 레벨 (통과) |
| `MomentCreateFacadeService` | `@Transactional(readOnly = true)` 클래스 레벨 (통과) |

**확인 필요**: 모든 서비스 클래스가 이 패턴을 따르는지 전수 검사

---

## Step 2-3: PackageStructureRuleTest 확장 (P-005 ~ P-006)

### 파일

**`api/src/test/java/moment/arch/PackageStructureRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

```java
// === Phase 2 추가 규칙 ===

@ArchTest
static final ArchRule P_005_Request_DTO는_dto_request_패키지에_위치한다 =
    classes()
        .that().haveSimpleNameEndingWith("Request")
        .and().areAssignableTo(Record.class)
        .and().resideInAPackage("..dto..")
        .should().resideInAPackage("..dto.request..")
        .because("[P-005] Request DTO(record)는 dto/request 패키지에 위치해야 합니다. " +
                 "수정 가이드: 해당 클래스를 {도메인}.dto.request 패키지로 이동하세요.");

@ArchTest
static final ArchRule P_006_Response_DTO는_dto_response_패키지에_위치한다 =
    classes()
        .that().haveSimpleNameEndingWith("Response")
        .and().areAssignableTo(Record.class)
        .and().resideInAPackage("..dto..")
        .should().resideInAPackage("..dto.response..")
        .because("[P-006] Response DTO(record)는 dto/response 패키지에 위치해야 합니다. " +
                 "수정 가이드: 해당 클래스를 {도메인}.dto.response 패키지로 이동하세요.");
```

### 주의사항

- `.and().resideInAPackage("..dto..")` 조건으로 dto 패키지 내의 record만 대상
- `SuccessResponse`, `ErrorResponse`는 `global` 패키지에 있으므로 대상 외
- `AdminSuccessResponse`, `AdminErrorResponse`는 `admin.dto.response`에 있으므로 통과
- `Authentication` record는 `user/dto/request/`에 있으므로 이름이 `Request`로 끝나지 않아 대상 외

### 잠재적 위반

- `LikeToggleResponse`는 `like/dto/response/`에 위치 → 통과
- `MomentLikeEvent`, `CommentLikeEvent`는 `like/dto/event/`에 위치하고 이름이 `Event`로 끝남 → 대상 외

---

## Step 2-4: DtoRuleTest 확장 (D-001)

### 파일

**`api/src/test/java/moment/arch/DtoRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

```java
// === Phase 2 추가 규칙 ===

@ArchTest
static final ArchRule D_001_dto_request와_response_패키지의_클래스는_record여야_한다 =
    classes()
        .that().resideInAnyPackage("..dto.request..", "..dto.response..")
        .should().beAssignableTo(Record.class)
        .because("[D-001] dto/request, dto/response 패키지의 클래스는 Java record여야 합니다. " +
                 "수정 가이드: class를 record로 변경하세요. " +
                 "예: public record UserCreateRequest(String email, String password) {}");
```

### 구현 원리

- Java `record`는 `java.lang.Record`를 자동으로 상속
- `beAssignableTo(Record.class)`로 record 여부 확인 가능
- ArchUnit은 record 여부를 직접 검사하는 API가 없으므로 상속 관계 활용

### 현재 코드베이스 확인

- 모든 Request/Response DTO가 이미 record로 선언됨 (CLAUDE.md 규칙 준수)
- `MomentCreateRequest`, `UserCreateRequest`, `LikeToggleResponse` 등 — 모두 record (통과)

### 잠재적 위반

- `Authentication` record는 `user/dto/request/`에 위치하며 record임 (통과)
- Admin 모듈의 DTO는 admin 패키지에 있으므로 api 모듈 스캔 범위에서 제외 가능 (확인 필요)
  - `@AnalyzeClasses(packages = "moment")`이므로 admin DTO도 포함될 수 있음
  - Admin DTO(`AdminLoginRequest` 등)가 `admin/src/main/java/moment/admin/dto/request/`에 있고 record인지 확인 필요

---

## Step 2-5: LayerDependencyRuleTest 확장 (L-005 ~ L-006)

### 파일

**`api/src/test/java/moment/arch/LayerDependencyRuleTest.java`** (기존 파일에 추가)

### 추가할 규칙

Phase 1의 `layeredArchitecture()` 규칙과는 **별도의** 독립 규칙으로 추가:

```java
// === Phase 2 추가 규칙 ===

@ArchTest
static final ArchRule L_005_dto_패키지는_domain에만_의존할_수_있다 =
    classes()
        .that().resideInAPackage("..dto..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..dto..",          // 자기 자신
            "..domain..",       // 엔티티 참조 (DTO 변환)
            "..global..",       // 공유 유틸리티
            "java..",           // Java 표준
            "jakarta..",        // Jakarta Validation, Persistence
            "lombok..",         // Lombok
            "io.swagger..",     // Swagger @Schema
            "com.fasterxml.."   // Jackson
        )
        .because("[L-005] dto 패키지는 domain 패키지에만 의존할 수 있습니다. " +
                 "수정 가이드: DTO에서 service/infrastructure/presentation 패키지의 " +
                 "클래스를 참조하지 마세요. DTO는 domain 엔티티를 변환하는 역할만 합니다.");

@ArchTest
static final ArchRule L_006_eventHandler는_facade_또는_application_서비스만_의존할_수_있다 =
    classes()
        .that().resideInAPackage("..service.eventHandler..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage(
            "..service.eventHandler..",   // 자기 자신
            "..service.facade..",         // Facade 서비스 호출
            "..service.application..",    // Application 서비스 호출
            "..dto..",                    // 이벤트 DTO 참조
            "..domain..",                 // 도메인 엔티티 참조
            "..global..",                 // 공유 유틸리티
            "java..",                     // Java 표준
            "org.springframework..",      // Spring 어노테이션
            "org.slf4j..",               // 로깅
            "lombok.."                    // Lombok
        )
        .because("[L-006] eventHandler는 facade 또는 application 서비스만 의존할 수 있습니다. " +
                 "수정 가이드: eventHandler에서 domain 서비스를 직접 호출하지 말고, " +
                 "facade 또는 application 서비스를 통해 호출하세요.");
```

### L-005 주의사항

- DTO에서 domain 엔티티를 참조하는 것은 허용 (정적 팩토리 `from()`/`of()` 패턴)
- Swagger `@Schema` 어노테이션(`io.swagger..`) 허용
- Jakarta Validation 어노테이션(`@NotBlank`, `@Pattern` 등) 허용
- 이벤트 record도 `..dto..` 패키지에 있으므로 동일 규칙 적용

### L-006 주의사항

- `NotificationEventHandler`가 `NotificationFacadeService`를 호출하는 현재 패턴 (통과)
- EventHandler에서 Domain Service(`..service.{domain}..`)를 직접 호출하면 위반
- `@Async` + `@TransactionalEventListener` 핸들러에서의 의존성만 제한

### 잠재적 위반

L-005:
- DTO에서 서비스 클래스를 import하는 경우 → 현재 없음 (통과 예상)

L-006:
- EventHandler에서 UserService 같은 Domain Service를 직접 호출하는 경우 → 확인 필요
- 필요시 `NotificationFacadeService`를 통해 호출하도록 리팩토링

---

## Phase 2 전체 검증

### 실행 명령

```bash
cd server

# 1. Phase 2 추가 규칙 포함 전체 ArchUnit 테스트
./gradlew test -p api --tests "moment.arch.*"

# 2. fastTest에 포함 확인
./gradlew fastTest -p api

# 3. 특정 테스트 파일만 실행 (디버깅 시)
./gradlew test -p api --tests "moment.arch.NamingConventionRuleTest"
./gradlew test -p api --tests "moment.arch.AnnotationConsistencyRuleTest"
```

### 완료 기준

- [ ] 10개 MEDIUM 규칙 모두 통과
- [ ] A-006 커스텀 ArchCondition (RequestBody + Valid) 정상 동작
- [ ] A-007 커스텀 ArchCondition (Transactional readOnly) 정상 동작
- [ ] D-001 record 검증 정상 동작
- [ ] `./gradlew fastTest` 실행 시간 허용 범위 내

### 커밋 전략

```
커밋 1: test: ArchUnit MEDIUM 우선순위 네이밍/패키지 규칙 추가 (N-004~N-006, P-005~P-006)
커밋 2: test: ArchUnit 어노테이션 일관성 규칙 추가 (A-006~A-007) - 커스텀 ArchCondition 포함
커밋 3: test: ArchUnit DTO/레이어 의존성 규칙 추가 (D-001, L-005~L-006)
```

### 실패 시 대응 전략

| 위반 유형 | 대응 방법 |
|---------|---------|
| EventHandler 이름 불일치 | `EventHandler` suffix로 리네이밍 |
| ApplicationService 이름 불일치 | `ApplicationService` suffix로 리네이밍 |
| @RequestBody에 @Valid 누락 | Controller 메서드에 `@Valid` 추가 |
| @Transactional(readOnly=true) 누락 | 서비스 클래스 레벨에 어노테이션 추가 |
| DTO가 record가 아님 | class → record 변환 |
| dto에서 service 의존 | 해당 import 제거, 필요시 domain으로 이동 |
| eventHandler에서 Domain Service 직접 호출 | Facade/Application Service를 통해 호출하도록 리팩토링 |
