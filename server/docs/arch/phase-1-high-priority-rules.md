# Phase 1: 인프라 구축 + HIGH 우선순위 규칙 (22개)

> 기반 문서: `docs/arch/archunit-implementation-plan.md`
> 선행 조건: Phase 0 완료
> 목표: 공유 베이스 클래스 + 9개 테스트 파일 + 22개 규칙

---

## 개요

| Step | 테스트 파일 | 규칙 수 | 규칙 ID |
|------|-----------|--------|---------|
| 1-1 | `BaseArchTest.java` | - | 인프라 |
| 1-2 | `LayerDependencyRuleTest.java` | 4 | L-001 ~ L-004 |
| 1-3 | `PackageStructureRuleTest.java` | 4 | P-001 ~ P-004 |
| 1-4 | `NamingConventionRuleTest.java` | 3 | N-001 ~ N-003 |
| 1-5 | `AnnotationConsistencyRuleTest.java` | 5 | A-001 ~ A-005 |
| 1-6 | `CyclicDependencyRuleTest.java` | 2 | C-001 ~ C-002 |
| 1-7 | `DtoRuleTest.java` | 3 | D-002 ~ D-004 |
| 1-8 | `DomainPurityRuleTest.java` | 2 | DS-001 ~ DS-002 |
| 1-9 | `DipRuleTest.java` | 2 | DIP-001 ~ DIP-002 |

**모든 파일 위치**: `api/src/test/java/moment/arch/`

---

## Step 1-1: 공유 베이스 클래스 생성

### 파일

**`api/src/test/java/moment/arch/BaseArchTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import moment.config.TestTags;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;

@Tag(TestTags.ARCHITECTURE)
@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseArchTest {
    // ArchUnit JUnit5에서 동일한 @AnalyzeClasses 설정을 가진 테스트 클래스들은
    // ClassFileImporter의 캐시를 자동으로 공유함. 별도 설정 불필요.
}
```

### 설계 의도

- `@Tag(ARCHITECTURE)`: fastTest에 자동 포함, e2eTest에서 제외
- `packages = "moment"`: api 모듈의 모든 moment 패키지 스캔
- `DoNotIncludeTests`: 테스트 클래스는 분석 대상에서 제외
- `ReplaceUnderscores`: 한글 테스트 메서드명의 `_`를 공백으로 치환

### 검증

```bash
./gradlew compileTestJava -p api    # 컴파일 확인
```

---

## Step 1-2: LayerDependencyRuleTest (L-001 ~ L-004)

### 파일

**`api/src/test/java/moment/arch/LayerDependencyRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

class LayerDependencyRuleTest extends BaseArchTest {

    // === 레이어 정의 ===
    // presentation: REST 컨트롤러 (moment.*.presentation..)
    // service: 모든 서비스 계층 (facade, application, domain, eventHandler)
    // domain: 엔티티, 값 객체, 정책
    // infrastructure: 리포지토리, 외부 어댑터
    // dto: 요청/응답 DTO, 이벤트

    @ArchTest
    static final ArchRule 레이어_의존성_규칙 = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Presentation").definedBy("..presentation..")
        .layer("Service").definedBy("..service..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")
        .layer("Dto").definedBy("..dto..")
        .layer("Global").definedBy("..global..")

        // L-001: presentation은 service와 dto만 의존 가능
        .whereLayer("Presentation").mayOnlyAccessLayers(
            "Service", "Dto", "Global")

        // L-002: service는 presentation에 의존 불가
        .whereLayer("Service").mayNotAccessLayer("Presentation")

        // L-003: domain은 다른 레이어에 의존하지 않음
        .whereLayer("Domain").mayOnlyAccessLayers("Domain", "Global")

        // L-004: infrastructure는 domain만 의존 가능 (+ Spring/JPA는 외부 의존성이므로 자동 허용)
        .whereLayer("Infrastructure").mayOnlyAccessLayers(
            "Domain", "Infrastructure", "Global")

        .because("[L-001~L-004] 레이어 의존성은 presentation → service → domain 방향이어야 합니다. " +
                 "수정 가이드: 역방향 의존이 발견되면 service 레이어를 통해 접근하도록 수정하세요.");
}
```

### 규칙 상세

| 규칙 ID | 검증 내용 | ArchUnit API |
|---------|----------|-------------|
| L-001 | presentation → service, dto, global만 | `whereLayer("Presentation").mayOnlyAccessLayers(...)` |
| L-002 | service ↛ presentation | `whereLayer("Service").mayNotAccessLayer(...)` |
| L-003 | domain → domain, global만 | `whereLayer("Domain").mayOnlyAccessLayers(...)` |
| L-004 | infrastructure → domain, infrastructure, global만 | `whereLayer("Infrastructure").mayOnlyAccessLayers(...)` |

### 주의사항

- `layeredArchitecture()`는 `consideringOnlyDependenciesInLayers()`와 함께 사용해야 외부 라이브러리 의존성(JPA, Spring 등)을 무시
- `..global..` 패키지는 공유 인프라이므로 모든 레이어에서 접근 허용
- `..dto..` 패키지는 presentation과 service에서 접근 가능

### 검증 포인트

- Controller에서 Repository 직접 접근 시 L-001 위반
- Service에서 Controller import 시 L-002 위반
- Entity에서 @Service 클래스 참조 시 L-003 위반

---

## Step 1-3: PackageStructureRuleTest (P-001 ~ P-004)

### 파일

**`api/src/test/java/moment/arch/PackageStructureRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class PackageStructureRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule P_001_RestController_클래스는_presentation_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("..presentation..")
            .because("[P-001] @RestController 클래스는 presentation 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.presentation 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_002_Service_클래스는_service_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAPackage("..service..")
            .because("[P-002] @Service 클래스는 service 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.service.{하위패키지} 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_003_Repository_인터페이스는_infrastructure_패키지에_위치해야_한다 =
        classes()
            .that().areAssignableTo(JpaRepository.class)
            .should().resideInAPackage("..infrastructure..")
            .because("[P-003] JpaRepository 확장 인터페이스는 infrastructure 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 인터페이스를 {도메인}.infrastructure 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule P_004_Entity_클래스는_domain_패키지에_위치해야_한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..domain..")
            .because("[P-004] @Entity 클래스는 domain 패키지에 위치해야 합니다. " +
                     "수정 가이드: 해당 클래스를 {도메인}.domain 패키지로 이동하세요.");
}
```

### 현재 코드베이스 확인 사항

- 엔티티: `common/src/main/java/moment/{domain}/domain/` — 모두 `..domain..` 패키지에 위치 (통과)
- 리포지토리: `common/src/main/java/moment/{domain}/infrastructure/` — 모두 `..infrastructure..` 패키지 (통과)
- 서비스: `api/src/main/java/moment/{domain}/service/` — Phase 0 표준화 후 모두 `..service..` (통과)
- 컨트롤러: `api/src/main/java/moment/{domain}/presentation/` — 모두 `..presentation..` (통과)

### 잠재적 위반 확인 필요

- `common` 모듈의 엔티티/리포지토리도 스캔 범위에 포함 (`packages = "moment"`)
- Admin 엔티티 `AdminGroupLog`도 `..domain..`에 위치하므로 통과

---

## Step 1-4: NamingConventionRuleTest (N-001 ~ N-003)

### 파일

**`api/src/test/java/moment/arch/NamingConventionRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class NamingConventionRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule N_001_Service_어노테이션_클래스는_Service로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should().haveSimpleNameEndingWith("Service")
            .because("[N-001] @Service 어노테이션이 붙은 클래스는 'Service'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {도메인명}Service로 변경하세요.");

    @ArchTest
    static final ArchRule N_002_RestController_어노테이션_클래스는_Controller로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .because("[N-002] @RestController 어노테이션이 붙은 클래스는 'Controller'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 {리소스명}Controller로 변경하세요.");

    @ArchTest
    static final ArchRule N_003_JpaRepository_확장_인터페이스는_Repository로_끝나야_한다 =
        classes()
            .that().areAssignableTo(JpaRepository.class)
            .should().haveSimpleNameEndingWith("Repository")
            .because("[N-003] JpaRepository를 확장하는 인터페이스는 'Repository'로 끝나야 합니다. " +
                     "수정 가이드: 인터페이스명을 {엔티티명}Repository로 변경하세요.");
}
```

### 현재 코드베이스 확인 사항

- 서비스: `UserService`, `MomentService`, `CommentLikeService`, `MomentApplicationService` 등 — 모두 `Service` suffix (통과)
- 컨트롤러: `MomentController`, `UserController`, `GroupMomentController` 등 — 모두 `Controller` suffix (통과)
- 리포지토리: `UserRepository`, `MomentRepository`, `MomentLikeRepository` 등 — 모두 `Repository` suffix (통과)

### 잠재적 위반 확인 필요

- `NotificationEventHandler`: `@Component`이므로 N-001 대상 아님 (통과)
- `AuthEmailService`: `@Service`이고 `Service`로 끝나므로 통과

---

## Step 1-5: AnnotationConsistencyRuleTest (A-001 ~ A-005)

### 파일

**`api/src/test/java/moment/arch/AnnotationConsistencyRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import lombok.RequiredArgsConstructor;
import moment.global.domain.BaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AnnotationConsistencyRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule A_001_Transactional은_service_패키지에서만_사용한다 =
        noClasses()
            .that().resideOutsideOfPackage("..service..")
            .should().beAnnotatedWith(Transactional.class)
            .because("[A-001] @Transactional은 service 패키지에서만 사용해야 합니다. " +
                     "수정 가이드: @Transactional을 Controller/Repository에서 제거하고, " +
                     "트랜잭션 경계를 Service 레이어로 이동하세요.");

    // A-002: @TransactionalEventListener + @Async 권장 (느슨 적용)
    // 빌드 실패가 아닌 로그 경고만 출력하는 방식으로 구현
    // ArchUnit 정적 규칙으로는 "경고만" 출력이 불가하므로,
    // @Disabled 처리하고 주석으로 권장사항 기록
    // 향후 프로젝트에서 비동기 전환이 완료되면 활성화
    //
    // @ArchTest
    // @Disabled("권장사항: @TransactionalEventListener에 @Async를 함께 사용하세요")
    // static final ArchRule A_002 = ...;

    @ArchTest
    static final ArchRule A_003_Entity는_SQLDelete와_SQLRestriction을_사용한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .and().doNotHaveSimpleName("AdminGroupLog")  // 감사 로그 예외
            .should().beAnnotatedWith(SQLDelete.class)
            .andShould().beAnnotatedWith(SQLRestriction.class)
            .because("[A-003] Entity는 Soft Delete 패턴(@SQLDelete, @SQLRestriction)을 적용해야 합니다. " +
                     "예외: AdminGroupLog (감사 로그 성격상 영구 보존). " +
                     "수정 가이드: @SQLDelete(sql = \"UPDATE {table} SET deleted_at = NOW() WHERE id = ?\")와 " +
                     "@SQLRestriction(\"deleted_at IS NULL\")을 추가하세요.");

    @ArchTest
    static final ArchRule A_004_Entity는_BaseEntity를_확장한다 =
        classes()
            .that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(BaseEntity.class)
            .because("[A-004] @Entity 클래스는 BaseEntity를 확장해야 합니다. " +
                     "수정 가이드: 클래스 선언에 'extends BaseEntity'를 추가하세요. " +
                     "BaseEntity는 @CreatedDate를 제공합니다.");

    @ArchTest
    static final ArchRule A_005_Service_클래스는_RequiredArgsConstructor를_사용한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .should().beAnnotatedWith(RequiredArgsConstructor.class)
            .because("[A-005] @Service 클래스는 @RequiredArgsConstructor를 사용해야 합니다. " +
                     "수정 가이드: 클래스에 @RequiredArgsConstructor를 추가하고, " +
                     "의존성을 private final 필드로 선언하세요.");
}
```

### 주의사항

#### A-001 잠재적 위반

- `@Configuration` 클래스에서 `@Transactional` 사용하는 경우 → 현재 코드베이스에서 확인 필요
- 필요시 `and().areNotAnnotatedWith(Configuration.class)` 조건 추가

#### A-002 느슨 적용

- 빌드를 실패시키지 않으므로 `@Disabled` 또는 주석 처리
- 현재 `NotificationEventHandler`의 이벤트 리스너에 `@Async`가 있으므로 대부분 통과
- 동기 이벤트 리스너가 있을 수 있으므로 강제하지 않음

#### A-003 AdminGroupLog 예외

- `AdminGroupLog`는 감사 로그 성격상 영구 보존이 의도됨
- `.doNotHaveSimpleName("AdminGroupLog")`로 명시적 예외 처리

#### A-004 BaseEntity 위치

- `BaseEntity`는 `common/src/main/java/moment/global/domain/BaseEntity.java`
- api 모듈의 테스트에서 common 모듈의 클래스를 참조 가능 (의존성 존재)

---

## Step 1-6: CyclicDependencyRuleTest (C-001 ~ C-002)

### 파일

**`api/src/test/java/moment/arch/CyclicDependencyRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class CyclicDependencyRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule C_001_도메인_모듈_간_순환_의존이_없어야_한다 =
        slices()
            .matching("moment.(*)..")
            .should().beFreeOfCycles()
            .because("[C-001] 도메인 모듈 간 순환 의존이 감지되었습니다. " +
                     "수정 가이드: 이벤트 기반 통신(@TransactionalEventListener)으로 전환하거나, " +
                     "공통 인터페이스를 common 모듈로 추출하세요.");

    @ArchTest
    static final ArchRule C_002_패키지_간_순환_의존이_없어야_한다 =
        slices()
            .matching("moment.(**)")
            .should().beFreeOfCycles()
            .because("[C-002] 패키지 간 순환 의존이 감지되었습니다. " +
                     "수정 가이드: 의존성 방향을 단방향으로 정리하거나, " +
                     "공통 인터페이스를 추출하여 의존성 역전을 적용하세요.");
}
```

### 위험도 분석

- C-001: 도메인 모듈 수준 (auth, user, moment, comment, group, like, notification, report, storage, block)
  - 현재 이벤트 기반 비동기 통신을 사용하므로 순환 가능성 낮음
  - 주의: `like` 서비스가 `moment`/`comment` 도메인 엔티티를 직접 참조할 수 있음
- C-002: 세부 패키지 수준
  - 더 세분화된 검사로, 같은 도메인 내에서도 순환 감지
  - `service.application` ↔ `service.group` 간 순환 가능성 확인 필요

### 잠재적 위반 시 대응

C-001/C-002 위반 시:
1. 순환 경로 확인 (ArchUnit 오류 메시지에 경로 표시)
2. 이벤트 기반 통신으로 전환 또는 인터페이스 추출
3. 불가피한 경우 `ignoreDependency()` 로 특정 의존성 제외 가능 (최후의 수단)

---

## Step 1-7: DtoRuleTest (D-002 ~ D-004)

### 파일

**`api/src/test/java/moment/arch/DtoRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

class DtoRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule D_002_Controller는_Entity를_직접_반환하지_않는다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should(notReturnEntityFromPublicMethods())
            .because("[D-002] Controller는 Entity를 직접 반환할 수 없습니다. " +
                     "수정 가이드: 반환타입을 Response DTO(record)로 변경하고, " +
                     "DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.");

    @ArchTest
    static final ArchRule D_003_Autowired_필드_주입은_프로덕션_코드에서_금지한다 =
        noFields()
            .should().beAnnotatedWith(Autowired.class)
            .because("[D-003] 프로덕션 코드에서 @Autowired 필드 주입은 금지됩니다. " +
                     "수정 가이드: @RequiredArgsConstructor + private final 필드로 생성자 주입을 사용하세요.");

    @ArchTest
    static final ArchRule D_004_Application_Service와_Facade_Service는_Entity를_직접_반환하지_않는다 =
        classes()
            .that().resideInAnyPackage("..service.application..", "..service.facade..")
            .and().areAnnotatedWith(Service.class)
            .should(notReturnEntityFromPublicMethods())
            .because("[D-004] Application Service와 Facade Service는 Entity를 직접 반환할 수 없습니다. " +
                     "Domain Service에서 Entity를 받아 DTO로 변환한 후 반환하세요. " +
                     "수정 가이드: 반환타입을 Response DTO(record)로 변경하고, " +
                     "DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.");

    // === 커스텀 ArchCondition ===

    private static ArchCondition<JavaClass> notReturnEntityFromPublicMethods() {
        return new ArchCondition<>("not return @Entity classes from public methods") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaMethod method : javaClass.getMethods()) {
                    if (!method.getModifiers().contains(JavaModifier.PUBLIC)) {
                        continue;
                    }

                    JavaClass returnType = method.getRawReturnType();

                    // 직접 Entity 반환 체크
                    if (isEntityClass(returnType)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                            String.format("%s.%s()이(가) Entity(%s)를 직접 반환합니다",
                                javaClass.getSimpleName(),
                                method.getName(),
                                returnType.getSimpleName())));
                    }

                    // 제네릭 타입 파라미터 체크 (List<Entity>, Optional<Entity> 등)
                    method.getReturnType().getAllInvolvedRawTypes().stream()
                        .filter(type -> !type.equals(returnType))
                        .filter(this::isEntityClass)
                        .forEach(entityType ->
                            events.add(SimpleConditionEvent.violated(javaClass,
                                String.format("%s.%s()이(가) 컬렉션으로 Entity(%s)를 반환합니다",
                                    javaClass.getSimpleName(),
                                    method.getName(),
                                    entityType.getSimpleName()))));
                }
            }

            private boolean isEntityClass(JavaClass javaClass) {
                return javaClass.isAnnotatedWith(Entity.class);
            }
        };
    }
}
```

### 주의사항

#### D-002 & D-004 구현 복잡도

- 커스텀 `ArchCondition`이 필요한 이유: ArchUnit 기본 API로는 "메서드 반환타입이 @Entity인지" 직접 검사 불가
- `SuccessResponse<T>`의 제네릭 T까지 완벽하게 추적하는 것은 ArchUnit 한계 (타입 소거)
- `getAllInvolvedRawTypes()`로 제네릭 파라미터의 raw type까지는 감지 가능

#### D-003 범위

- `noFields().should().beAnnotatedWith(Autowired.class)`는 전체 프로덕션 코드에 적용
- 테스트 코드는 `@AnalyzeClasses`의 `DoNotIncludeTests`로 제외되므로 안전
- 현재 코드베이스에서 `@Autowired` 필드 주입이 없으므로 통과 예상

#### D-004 패키지 범위

- `..service.application..`: `MomentApplicationService`, `CommentApplicationService` 등
- `..service.facade..`: `MomentCreateFacadeService`, `CommentCreateFacadeService` 등
- Domain Service (`..service.{domain}..`)는 Entity 직접 반환 **허용**

---

## Step 1-8: DomainPurityRuleTest (DS-001 ~ DS-002)

### 파일

**`api/src/test/java/moment/arch/DomainPurityRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DomainPurityRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule DS_001_domain_패키지는_Spring_프레임워크에_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("org.springframework..")
            .because("[DS-001] domain 패키지는 Spring 프레임워크에 의존하지 않아야 합니다. " +
                     "허용: JPA(jakarta.persistence), Hibernate(@SQLDelete, @SQLRestriction), " +
                     "Lombok, Java 표준 라이브러리. " +
                     "수정 가이드: Spring 어노테이션(@Service, @Component, @Transactional 등)을 " +
                     "domain 패키지에서 제거하세요.");

    @ArchTest
    static final ArchRule DS_002_domain_패키지는_상위_레이어에_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..infrastructure..",
                "..service..",
                "..presentation..",
                "..dto.."
            )
            .because("[DS-002] domain 패키지는 상위 레이어에 의존하지 않아야 합니다. " +
                     "수정 가이드: domain → infrastructure/service/presentation/dto 방향의 " +
                     "의존성을 제거하세요. 필요한 경우 인터페이스를 domain에 정의하고 " +
                     "infrastructure에서 구현하세요.");
}
```

### 허용/금지 패키지 정리

| 구분 | 패키지 | 사유 |
|------|--------|------|
| **허용** | `jakarta.persistence..` | JPA 어노테이션 (@Entity, @ManyToOne 등) |
| **허용** | `jakarta.validation..` | Bean Validation |
| **허용** | `lombok..` | 보일러플레이트 제거 |
| **허용** | `java..`, `javax..` | Java 표준 |
| **허용** | `org.hibernate..` | @SQLDelete, @SQLRestriction |
| **금지** | `org.springframework..` | 모든 Spring 어노테이션 |

### 잠재적 위반 확인

- `BaseEntity`는 `moment.global.domain` 패키지에 있고 `@EntityListeners(AuditingEntityListener.class)` 사용
  - `AuditingEntityListener`는 `org.springframework.data.jpa.domain.support` 패키지
  - **위반 가능**: BaseEntity가 Spring에 의존
  - **대응**: BaseEntity는 `..global.domain..`이므로 `..domain..` 패키지에 해당 → 확인 필요
  - 위반이면 DS-001 규칙에서 BaseEntity를 명시적으로 제외하거나, AuditingEntityListener 의존을 허용

---

## Step 1-9: DipRuleTest (DIP-001 ~ DIP-002)

### 파일

**`api/src/test/java/moment/arch/DipRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DipRuleTest extends BaseArchTest {

    @ArchTest
    static final ArchRule DIP_001_infrastructure의_외부_클라이언트는_service의_인터페이스를_구현해야_한다 =
        classes()
            .that().resideInAPackage("..infrastructure..")
            .and().areNotInterfaces()
            .and().doNotHaveSimpleNameEndingWith("Repository")
            .and().areNotAnnotatedWith(Configuration.class)
            .and(areNotJpaRelatedClasses())
            .should(implementInterfaceInServicePackage())
            .because("[DIP-001] infrastructure의 외부 클라이언트 구현체는 " +
                     "service 패키지의 인터페이스를 구현해야 합니다. " +
                     "수정 가이드: service 패키지에 인터페이스를 정의하고, " +
                     "infrastructure 클래스가 이를 구현하도록 변경하세요.");

    @ArchTest
    static final ArchRule DIP_002_service는_infrastructure_구체_클래스에_직접_의존하지_않는다 =
        noClasses()
            .that().resideInAPackage("..service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .andShould().dependOnClassesThat(
                com.tngtech.archunit.base.DescribedPredicate.describe(
                    "are concrete classes (not interfaces)",
                    javaClass -> !javaClass.isInterface()
                        && !javaClass.getSimpleName().endsWith("Repository")
                )
            )
            .because("[DIP-002] service 패키지는 infrastructure의 구체 클래스에 직접 의존할 수 없습니다. " +
                     "Repository 인터페이스는 허용됩니다. " +
                     "수정 가이드: service 패키지의 인터페이스를 통해 의존하세요.");

    // === Helper Methods ===

    /**
     * JPA 관련 클래스 (Converter, EntityListener 등) 제외
     */
    private static com.tngtech.archunit.base.DescribedPredicate<JavaClass> areNotJpaRelatedClasses() {
        return com.tngtech.archunit.base.DescribedPredicate.describe(
            "are not JPA-related infrastructure classes",
            javaClass -> {
                String simpleName = javaClass.getSimpleName();
                return !simpleName.endsWith("Converter")
                    && !simpleName.endsWith("Listener")
                    && !simpleName.endsWith("Initializer");
            }
        );
    }

    /**
     * service 패키지의 인터페이스를 구현하는지 검증
     */
    private static ArchCondition<JavaClass> implementInterfaceInServicePackage() {
        return new ArchCondition<>("implement an interface from service package") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean implementsServiceInterface = javaClass.getRawInterfaces().stream()
                    .anyMatch(iface -> iface.getPackageName().contains(".service."));

                if (!implementsServiceInterface) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                        String.format("%s이(가) service 패키지의 인터페이스를 구현하지 않습니다",
                            javaClass.getName())));
                }
            }
        };
    }
}
```

### 현재 DIP 적용 현황 (5개)

| 인터페이스 (service 패키지) | 구현체 (infrastructure 패키지) | 위치 |
|---------------------------|------------------------------|------|
| `GoogleOAuthClient` (`auth/service/application/`) | `GoogleAuthClient` (`auth/infrastructure/`) | api 모듈 |
| `AppleOAuthClient` (`auth/service/application/`) | `AppleAuthClient` (`auth/infrastructure/`) | api 모듈 |
| `FileUploadClient` (`storage/service/storage/`) | `AwsS3Client` (`storage/infrastructure/`) | api 모듈 |
| `TokenManager` (`auth/service/auth/`) | `JwtTokenManager` (`auth/infrastructure/`) | api 모듈 |
| `EmailService` (`auth/service/auth/`) | `AuthEmailService` (`auth/service/auth/`) | api 모듈 |

### 주의사항

#### DIP-001 제외 대상

infrastructure 패키지에 있지만 DIP 대상이 아닌 클래스:
- `*Repository`: JPA 리포지토리 인터페이스 (Spring Data가 구현 생성)
- `*Converter`: JPA AttributeConverter (`SourceDataConverter`)
- `*Listener`: JPA EntityListener
- `@Configuration`: Spring 설정 클래스

#### DIP-002 복잡도

- `noClasses().that().resideInAPackage("..service..")` 조건과 구체 클래스 필터링을 결합
- Repository 인터페이스 참조는 허용해야 하므로 인터페이스/구체 클래스 구분 필요
- 대안: 단순히 service에서 infrastructure 패키지 의존을 금지하되, Repository는 허용

**대안 구현 (더 단순)**:
```java
// service 패키지에서 infrastructure 패키지의 클래스 중
// Repository가 아닌 구체 클래스에 대한 의존 금지
noClasses()
    .that().resideInAPackage("..service..")
    .should().dependOnClassesThat(
        resideInAPackage("..infrastructure..")
            .and(areNotAssignableTo(JpaRepository.class))
            .and(are(not(interfaces())))
    );
```

---

## Phase 1 전체 검증

### 실행 명령

```bash
cd server

# 1. ArchUnit 테스트만 실행
./gradlew test -p api --tests "moment.arch.*"

# 2. fastTest에 포함 확인
./gradlew fastTest -p api

# 3. 실행 시간 측정
time ./gradlew fastTest -p api
```

### 완료 기준

- [ ] `BaseArchTest` 클래스 캐시 동작 확인 (테스트 간 클래스 스캔 1회)
- [ ] 22개 HIGH 규칙 모두 통과
- [ ] `./gradlew fastTest` 실행 시간 증가량 +5초 이내
- [ ] 실패 메시지에 규칙 ID + 수정 가이드 포함

### 커밋 전략

```
커밋 1: test: ArchUnit 베이스 클래스 및 레이어 의존성 규칙 추가 (L-001~L-004)
커밋 2: test: 패키지 구조 및 네이밍 규칙 추가 (P-001~P-004, N-001~N-003)
커밋 3: test: 어노테이션 일관성 규칙 추가 (A-001~A-005)
커밋 4: test: 순환 의존성, DTO, 도메인 순수성, DIP 규칙 추가 (C, D, DS, DIP)
```

### 실패 시 대응 전략

| 위반 유형 | 대응 방법 |
|---------|---------|
| 레이어 역방향 의존 | 의존성을 올바른 방향으로 수정 |
| 순환 의존성 | 이벤트 기반 통신으로 전환 또는 인터페이스 추출 |
| Entity 직접 반환 | DTO 변환 로직 추가 |
| @Autowired 필드 주입 | 생성자 주입으로 변경 |
| Spring 어노테이션 in domain | 해당 어노테이션 제거 또는 domain 외부로 이동 |
| DIP 미적용 | service에 인터페이스 정의 후 infrastructure에서 구현 |
