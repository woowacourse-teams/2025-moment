# ArchUnit 도입 세부 구현 계획

> 작성일: 2026-02-20
> 기반 문서: `docs/arch/codebase-analysis.md`
> 상태: 계획 수립 완료

---

## 1. 의사결정 요약

인터뷰를 통해 확정된 주요 의사결정:

| 항목 | 결정 | 근거 |
|------|------|------|
| CI 실행 전략 | `fastTest`에 포함 | 매 빌드마다 아키텍처 위반 즉시 감지 |
| 비표준 모듈 처리 | ArchUnit 도입 **전** 표준화 | 예외 없는 깔끔한 규칙 유지 |
| 클래스 스캔 범위 | api + admin 각각 독립 배치 | 모듈별 독립적 규칙 관리 |
| 위반 정책 | 초기부터 전부 통과 필수 | 선행 작업으로 기존 위반 모두 수정 후 도입 |
| AdminGroupLog Soft Delete | 의도적 미적용 (예외 허용) | 감사 로그 성격상 영구 보존 |
| 구현 범위 | Phase 1~3 전체 (35개 규칙) | 한 번에 완전한 규칙 세트 구축 |
| 클래스 캐시 | SharedClassCache + 공유 베이스 클래스 | 스캔 1회로 성능 최적화 |
| 테스트 태그 | 새 태그 `ARCHITECTURE` 추가 | 기존 UNIT/INTEGRATION/E2E와 분리 |
| DIP 범위 | 현재 5개 + 새 외부 클라이언트 강제 | 점진적 확장 가능한 규칙 |
| 도메인 순수성 | Spring 어노테이션만 금지 (JPA 허용) | 현실적 타협, JPA 분리는 과잉 |
| 테스트 파일 구성 | 보고서 제안대로 9개 + 베이스 클래스 | 관심사별 명확한 분리 |
| 이벤트 규칙 엄격도 | 느슨 적용 (권장사항) | 동기 이벤트 리스너 허용 |
| Admin 규칙 범위 | 최소 규칙만 적용 | admin은 구조가 다르므로 핵심만 검증 |
| DTO record 범위 | dto/request + dto/response만 | 이벤트 record는 별도 관리 |
| Transactional 검증 | readOnly=true 속성까지 검증 | 커스텀 ArchCondition으로 구현 |
| 실패 메시지 | 규칙 ID + 수정 가이드 포함 | AI 자동 수정 피드백 루프 지원 |
| 표준화 작업 | 계획에 포함 (미완료) | like/group 모듈 선행 표준화 필요 |

---

## 2. 작업 순서 및 Phase 구성

### Phase 0: 선행 작업 (ArchUnit 도입 전 필수)

#### Step 0-1: like 모듈 패키지 구조 표준화

**현재 구조:**
```
api/src/main/java/moment/like/
└── service/
    ├── MomentLikeService.java
    └── CommentLikeService.java
```

**변경 후:**
```
api/src/main/java/moment/like/
└── service/
    └── like/
        ├── MomentLikeService.java
        └── CommentLikeService.java
```

**작업 내용:**
1. `like/service/like/` 패키지 생성
2. `MomentLikeService.java`, `CommentLikeService.java`를 `service/like/`로 이동
3. import 경로 변경 (의존하는 클래스 모두 수정)
4. 테스트 파일도 동일하게 이동
5. 전체 테스트 통과 확인

#### Step 0-2: group/invite 서비스 위치 표준화

**현재 구조:**
```
api/src/main/java/moment/group/
└── service/
    ├── group/
    │   ├── GroupService.java
    │   └── GroupMemberService.java
    ├── invite/
    │   └── InviteLinkService.java
    └── application/
        └── ...
```

**변경 후:**
```
api/src/main/java/moment/group/
└── service/
    ├── group/
    │   ├── GroupService.java
    │   ├── GroupMemberService.java
    │   └── InviteLinkService.java      ← 이동
    └── application/
        └── ...
```

**작업 내용:**
1. `InviteLinkService.java`를 `service/invite/` → `service/group/`으로 이동
2. import 경로 변경
3. 테스트 파일도 `service/invite/` → `service/group/`으로 이동
4. `service/invite/` 패키지 삭제
5. 전체 테스트 통과 확인

#### Step 0-3: Gradle 의존성 추가

**`build.gradle` (루트, subprojects 블록):**
```groovy
subprojects {
    dependencies {
        // 기존 의존성...
        testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
    }
}
```

#### Step 0-4: TestTags에 ARCHITECTURE 태그 추가

**파일**: `common/src/testFixtures/java/moment/config/TestTags.java`

```java
public class TestTags {
    public static final String UNIT = "unit";
    public static final String INTEGRATION = "integration";
    public static final String E2E = "e2e";
    public static final String ARCHITECTURE = "architecture";  // 추가
}
```

> `fastTest`는 `excludeTags 'e2e'`이므로 ARCHITECTURE 태그가 붙은 테스트는 fastTest에 자동 포함됨.

---

### Phase 1: 인프라 구축 + HIGH 우선순위 규칙 (21개)

#### Step 1-1: 공유 베이스 클래스 생성

**파일**: `api/src/test/java/moment/arch/BaseArchTest.java`

```java
@Tag(TestTags.ARCHITECTURE)
@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class BaseArchTest {
    // SharedClassCache가 자동으로 활성화됨
    // @AnalyzeClasses가 동일한 packages/importOptions를 사용하면
    // ArchUnit이 내부적으로 캐시를 공유
}
```

**파일**: `admin/src/test/java/moment/arch/AdminBaseArchTest.java`

```java
@Tag(TestTags.ARCHITECTURE)
@AnalyzeClasses(
    packages = "moment",
    importOptions = ImportOption.DoNotIncludeTests.class
)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class AdminBaseArchTest {
}
```

> **캐시 메커니즘**: ArchUnit JUnit5에서 동일한 `@AnalyzeClasses` 설정을 가진 테스트 클래스들은 `ClassFileImporter`의 캐시를 자동으로 공유합니다. 별도 설정 불필요.

#### Step 1-2: LayerDependencyRuleTest.java (L-001 ~ L-004)

**위치**: `api/src/test/java/moment/arch/LayerDependencyRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| L-001 | `presentation_레이어는_service_레이어만_의존할_수_있다()` | `..presentation..` → `..service..` only |
| L-002 | `service_레이어는_presentation_레이어에_의존할_수_없다()` | `..service..` ↛ `..presentation..` |
| L-003 | `domain_레이어는_다른_레이어에_의존하지_않는다()` | `..domain..` → `..domain..`, JPA, Lombok만 |
| L-004 | `infrastructure_레이어는_domain만_의존할_수_있다()` | `..infrastructure..` → `..domain..` only (+ Spring/JPA 허용) |

**실패 메시지 형식:**
```
[L-001] presentation 레이어는 service 레이어만 의존 가능합니다.
위반: {클래스명}이(가) {의존 대상}에 의존합니다.
수정 가이드: presentation → service → domain 방향으로 의존성을 수정하세요.
```

**구현 핵심 포인트:**
- `layeredArchitecture()` API 사용
- 각 레이어 정의: `presentation`, `service`(facade/application/domain/eventHandler 포함), `domain`, `infrastructure`, `dto`
- `.because("[L-001] ...")` 으로 규칙 ID + 설명 포함

#### Step 1-3: PackageStructureRuleTest.java (P-001 ~ P-004)

**위치**: `api/src/test/java/moment/arch/PackageStructureRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| P-001 | `RestController_클래스는_presentation_패키지에_위치해야_한다()` | `@RestController` → `..presentation..` |
| P-002 | `Service_클래스는_service_패키지에_위치해야_한다()` | `@Service` → `..service..` |
| P-003 | `Repository_인터페이스는_infrastructure_패키지에_위치해야_한다()` | `extends JpaRepository` → `..infrastructure..` |
| P-004 | `Entity_클래스는_domain_패키지에_위치해야_한다()` | `@Entity` → `..domain..` |

**실패 메시지 형식:**
```
[P-001] @RestController 클래스는 presentation 패키지에 위치해야 합니다.
위반: {클래스명}이(가) {현재 패키지}에 위치합니다.
수정 가이드: {클래스명}을(를) {도메인}.presentation 패키지로 이동하세요.
```

#### Step 1-4: NamingConventionRuleTest.java (N-001 ~ N-003)

**위치**: `api/src/test/java/moment/arch/NamingConventionRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| N-001 | `Service_어노테이션_클래스는_Service로_끝나야_한다()` | `@Service` → `*Service` |
| N-002 | `RestController_어노테이션_클래스는_Controller로_끝나야_한다()` | `@RestController` → `*Controller` |
| N-003 | `JpaRepository_확장_인터페이스는_Repository로_끝나야_한다()` | `extends JpaRepository` → `*Repository` |

**실패 메시지 형식:**
```
[N-001] @Service 어노테이션이 붙은 클래스는 'Service'로 끝나야 합니다.
위반: {클래스명}
수정 가이드: 클래스명을 {추천명}Service로 변경하세요.
```

#### Step 1-5: AnnotationConsistencyRuleTest.java (A-001 ~ A-005)

**위치**: `api/src/test/java/moment/arch/AnnotationConsistencyRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| A-001 | `Transactional은_service_패키지에서만_사용한다()` | `@Transactional` → `..service..` only |
| A-002 | `TransactionalEventListener는_Async와_함께_사용을_권장한다()` | `@TransactionalEventListener` 있으면 `@Async` 권장 (warning 레벨, 느슨 적용) |
| A-003 | `Entity는_SQLDelete와_SQLRestriction을_사용한다()` | `@Entity` → `@SQLDelete` + `@SQLRestriction` (예외: `AdminGroupLog`) |
| A-004 | `Entity는_BaseEntity를_확장한다()` | `@Entity` → `extends BaseEntity` |
| A-005 | `Service_클래스는_RequiredArgsConstructor를_사용한다()` | `@Service` → `@RequiredArgsConstructor` |

**A-002 느슨 적용 구현:**
```java
// @TransactionalEventListener가 있는 메서드에 @Async가 없으면
// 빌드 실패가 아닌 로그 경고만 출력 (ArchRule이 아닌 별도 검증 메서드)
// 또는 @Disabled로 두고 주석으로 권장사항 기록
```

**A-003 예외 처리:**
```java
classes().that().areAnnotatedWith(Entity.class)
    .and().doNotHaveSimpleName("AdminGroupLog")  // 감사 로그 예외
    .should().beAnnotatedWith(SQLDelete.class)
    .andShould().beAnnotatedWith(SQLRestriction.class)
    .because("[A-003] Entity는 Soft Delete 패턴(@SQLDelete, @SQLRestriction)을 적용해야 합니다. " +
             "예외: AdminGroupLog (감사 로그). " +
             "수정 가이드: @SQLDelete(sql = \"UPDATE {table} SET deleted_at = NOW() WHERE id = ?\")와 " +
             "@SQLRestriction(\"deleted_at IS NULL\")을 추가하세요.");
```

#### Step 1-6: CyclicDependencyRuleTest.java (C-001 ~ C-002)

**위치**: `api/src/test/java/moment/arch/CyclicDependencyRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| C-001 | `도메인_모듈_간_순환_의존이_없어야_한다()` | `slices().matching("moment.(*)..").should().beFreeOfCycles()` |
| C-002 | `패키지_간_순환_의존이_없어야_한다()` | `slices().matching("moment.(**)").should().beFreeOfCycles()` |

**실패 메시지 형식:**
```
[C-001] 도메인 모듈 간 순환 의존이 감지되었습니다.
순환 경로: moment.user → moment.moment → moment.user
수정 가이드: 이벤트 기반 통신(@TransactionalEventListener)으로 전환하거나,
공통 인터페이스를 common 모듈로 추출하세요.
```

#### Step 1-7: DtoRuleTest.java (D-002 ~ D-004)

**위치**: `api/src/test/java/moment/arch/DtoRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| D-002 | `Controller는_Entity를_직접_반환하지_않는다()` | Controller의 메서드 반환타입에 `@Entity` 클래스 미포함 |
| D-003 | `Autowired_필드_주입은_프로덕션_코드에서_금지한다()` | `@Autowired` 필드 없음 (프로덕션 코드) |
| D-004 | `Application_Service와_Facade_Service는_Entity를_직접_반환하지_않는다()` | `..service.application..`, `..service.facade..`의 public 메서드 반환타입에 `@Entity` 클래스 미포함 |

**D-002 / D-004 Entity 반환 금지 계층 규칙:**

```
계층별 Entity 반환 허용 여부:

  Domain Service (service/{domain}/)     → ✅ Entity 직접 반환 허용
  Application Service (service/application/) → ❌ Entity 반환 금지 (DTO 변환 필수)
  Facade Service (service/facade/)       → ❌ Entity 반환 금지 (DTO 변환 필수)
  Controller (presentation/)             → ❌ Entity 반환 금지 (DTO 변환 필수)
```

**D-004 구현 핵심:**
```java
// 커스텀 ArchCondition: Application/Facade Service의 public 메서드가
// @Entity 클래스를 반환타입으로 사용하지 않는지 검증
// - 직접 반환 (Moment, User 등) 금지
// - Collection<Entity> (List<Moment> 등)도 감지
// - Domain Service(service/{domain}/)는 허용하므로 패키지 범위를 정확히 지정

classes().that().resideInAnyPackage("..service.application..", "..service.facade..")
    .and().areAnnotatedWith(Service.class)
    .should(notReturnEntityFromPublicMethods())
    .because("[D-004] Application Service와 Facade Service는 Entity를 직접 반환할 수 없습니다. " +
             "Domain Service에서 Entity를 받아 DTO로 변환한 후 반환하세요. " +
             "수정 가이드: 반환타입을 Response DTO(record)로 변경하고, " +
             "DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.");
```

**D-002 구현 핵심:**
```java
// 커스텀 ArchCondition: Controller 메서드의 반환 타입이 Entity가 아닌지 검증
// SuccessResponse<T>의 T가 Entity인 경우도 포착 (제네릭 분석 한계로 메서드 시그니처 검사)
```

**D-003 실패 메시지:**
```
[D-003] 프로덕션 코드에서 @Autowired 필드 주입은 금지됩니다.
위반: {클래스명}.{필드명}
수정 가이드: @RequiredArgsConstructor + private final 필드로 생성자 주입을 사용하세요.
```

#### Step 1-8: DomainPurityRuleTest.java (DS-001 ~ DS-002)

**위치**: `api/src/test/java/moment/arch/DomainPurityRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| DS-001 | `domain_패키지는_Spring_프레임워크에_의존하지_않는다()` | `..domain..` ↛ `org.springframework..` (JPA, Lombok은 허용) |
| DS-002 | `domain_패키지는_상위_레이어에_의존하지_않는다()` | `..domain..` ↛ `..infrastructure..`, `..service..`, `..presentation..`, `..dto..` |

**DS-001 허용 패키지 목록:**
- `jakarta.persistence..` (JPA 어노테이션)
- `jakarta.validation..` (Bean Validation)
- `lombok..` (Lombok)
- `java..`, `javax..` (Java 표준)
- `org.hibernate..` (Hibernate 어노테이션: `@SQLDelete`, `@SQLRestriction`)

**금지 패키지:**
- `org.springframework..` (모든 Spring 어노테이션)

#### Step 1-9: DipRuleTest.java (DIP-001 ~ DIP-002)

**위치**: `api/src/test/java/moment/arch/DipRuleTest.java`

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| DIP-001 | `infrastructure의_외부_클라이언트는_service의_인터페이스를_구현해야_한다()` | 특정 infrastructure 클래스가 service 인터페이스 구현 |
| DIP-002 | `service는_infrastructure_구체_클래스에_직접_의존하지_않는다()` | `..service..` ↛ `..infrastructure..` 구체 클래스 (Repository 인터페이스는 허용) |

**DIP-001 검증 대상 (현재 5개 + 향후 확장):**

| 인터페이스 (service) | 구현체 (infrastructure) |
|---------------------|----------------------|
| `GoogleOAuthClient` | `GoogleAuthClient` |
| `AppleOAuthClient` | `AppleAuthClient` |
| `FileUploadClient` | `AwsS3Client` |
| `TokenManager` | `JwtTokenManager` |
| `EmailService` | (구현체) |

**향후 확장 규칙:**
```java
// infrastructure 패키지에서 외부 API를 호출하는 새로운 클래스가 추가될 때,
// Repository가 아닌 클래스는 반드시 service 패키지의 인터페이스를 구현해야 함
classes().that().resideInAPackage("..infrastructure..")
    .and().areNotInterfaces()
    .and().doNotHaveSimpleNameEndingWith("Repository")
    .and().areNotAnnotatedWith(Configuration.class)
    .and(areNotJpaRelated())  // Converter, Listener 등 제외
    .should().implement(interfacesInServicePackage())
    .because("[DIP-001] infrastructure의 외부 클라이언트 구현체는 " +
             "service 패키지의 인터페이스를 구현해야 합니다. " +
             "수정 가이드: service 패키지에 인터페이스를 정의하고, " +
             "infrastructure 클래스가 이를 구현하도록 변경하세요.");
```

---

### Phase 2: MEDIUM 우선순위 규칙 (10개)

#### Step 2-1: NamingConventionRuleTest.java 확장 (N-004 ~ N-006)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| N-004 | `EventHandler_클래스는_EventHandler로_끝나야_한다()` | `@Component` + `@TransactionalEventListener` 메서드 존재 → `*EventHandler` |
| N-005 | `ApplicationService는_ApplicationService로_끝나야_한다()` | `..service.application..`의 `@Service` → `*ApplicationService` |
| N-006 | `FacadeService는_FacadeService로_끝나야_한다()` | `..service.facade..`의 `@Service` → `*FacadeService` |

#### Step 2-2: AnnotationConsistencyRuleTest.java 확장 (A-006 ~ A-007)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| A-006 | `Controller의_RequestBody에_Valid를_사용한다()` | `@RequestBody` 파라미터에 `@Valid` 동반 |
| A-007 | `Service_클래스는_클래스_레벨_Transactional_readOnly_true를_적용한다()` | `@Service` → 클래스 레벨 `@Transactional(readOnly = true)` |

**A-007 커스텀 ArchCondition 구현:**
```java
// 커스텀 조건: @Transactional 어노테이션의 readOnly 속성이 true인지 검증
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

#### Step 2-3: PackageStructureRuleTest.java 확장 (P-005 ~ P-006)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| P-005 | `Request_DTO는_dto_request_패키지에_위치한다()` | `*Request` record → `..dto.request..` |
| P-006 | `Response_DTO는_dto_response_패키지에_위치한다()` | `*Response` record → `..dto.response..` |

#### Step 2-4: DtoRuleTest.java 확장 (D-001)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| D-001 | `dto_request와_response_패키지의_클래스는_record여야_한다()` | `..dto.request..` + `..dto.response..`의 클래스 → Java record |

**구현:** ArchUnit은 record 여부를 직접 검사하는 API가 없으므로, `java.lang.Record`를 상속하는지 확인:
```java
classes().that().resideInAnyPackage("..dto.request..", "..dto.response..")
    .should().beAssignableTo(Record.class)
    .because("[D-001] dto/request, dto/response 패키지의 클래스는 record여야 합니다. " +
             "수정 가이드: class를 record로 변경하세요.");
```

#### Step 2-5: LayerDependencyRuleTest.java 확장 (L-005 ~ L-006)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| L-005 | `dto_패키지는_domain에만_의존할_수_있다()` | `..dto..` → `..domain..` only (변환 목적) |
| L-006 | `eventHandler는_facade_또는_application_서비스만_의존할_수_있다()` | `..eventHandler..` → `..facade..` or `..application..` only |

---

### Phase 3: 확장 규칙 (3개)

#### Step 3-1: AnnotationConsistencyRuleTest.java 확장 (AU-001 ~ AU-002)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| AU-001 | `User_API_Controller는_AuthenticationPrincipal을_사용한다()` | User API `@RestController` → 메서드에 `@AuthenticationPrincipal` 사용 |
| AU-002 | `Admin_API_Controller는_세션_기반_인증을_사용한다()` | Admin `@RestController` → `HttpSession` 파라미터 또는 Interceptor 기반 |

#### Step 3-2: ModuleBoundaryRuleTest.java 확장 (M-003)

| 규칙 ID | 테스트 메서드 | 검증 내용 |
|---------|-------------|----------|
| M-003 | `도메인_간_서비스_의존은_Application_또는_Facade를_통해서만_가능하다()` | Domain Service가 다른 도메인의 Domain Service를 직접 의존 금지 |

#### Step 3-3: API URL 형식 검증 (추가 규칙)

```java
// User API: /api/v2/{resources} 패턴
// Admin API: /api/admin/{resources} 패턴
// @RequestMapping 값의 형식 검증
```

---

## 3. Admin 모듈 최소 규칙

`admin/src/test/java/moment/arch/`에 배치할 최소 규칙:

| 규칙 | 파일 | 내용 |
|------|------|------|
| 레이어 의존성 | `AdminLayerDependencyRuleTest.java` | presentation → service → domain 방향 |
| 네이밍 규칙 | `AdminNamingConventionRuleTest.java` | `Admin*ApiController`, `Admin*Service` |
| 순환 의존성 | `AdminCyclicDependencyRuleTest.java` | 패키지 간 순환 금지 |
| 필드 주입 금지 | `AdminAnnotationRuleTest.java` | `@Autowired` 필드 금지, `@RequiredArgsConstructor` 필수 |

> Admin 모듈은 4개 테스트 파일만 배치. DIP, 이벤트 핸들러, 도메인 순수성 등은 제외.

---

## 4. 파일 구조 요약

### api 모듈

```
api/src/test/java/moment/arch/
├── BaseArchTest.java                    # 공유 베이스 (캐시 + 태그)
├── LayerDependencyRuleTest.java         # L-001 ~ L-006
├── ModuleBoundaryRuleTest.java          # M-001 ~ M-003
├── PackageStructureRuleTest.java        # P-001 ~ P-006
├── NamingConventionRuleTest.java        # N-001 ~ N-006
├── AnnotationConsistencyRuleTest.java   # A-001 ~ A-007, AU-001 ~ AU-002
├── CyclicDependencyRuleTest.java        # C-001 ~ C-002
├── DtoRuleTest.java                     # D-001 ~ D-004
├── DomainPurityRuleTest.java            # DS-001 ~ DS-002
└── DipRuleTest.java                     # DIP-001 ~ DIP-002
```

### admin 모듈

```
admin/src/test/java/moment/arch/
├── AdminBaseArchTest.java               # Admin 공유 베이스
├── AdminLayerDependencyRuleTest.java    # 레이어 의존성
├── AdminNamingConventionRuleTest.java   # 네이밍 규칙
├── AdminCyclicDependencyRuleTest.java   # 순환 의존성
└── AdminAnnotationRuleTest.java         # 필드 주입 금지
```

---

## 5. 실패 메시지 표준 형식

모든 ArchUnit 규칙의 `.because()` 메시지는 다음 형식을 따름:

```
[{규칙 ID}] {규칙 설명}.
위반: {위반 내용}.
수정 가이드: {구체적 수정 방법}.
```

**예시:**
```
[L-001] presentation 레이어는 service 레이어만 의존할 수 있습니다.
수정 가이드: domain/infrastructure에 직접 접근하지 말고, service 레이어를 통해 접근하세요.

[A-003] Entity는 Soft Delete 패턴을 적용해야 합니다. 예외: AdminGroupLog.
수정 가이드: @SQLDelete(sql = "UPDATE {table} SET deleted_at = NOW() WHERE id = ?")와
@SQLRestriction("deleted_at IS NULL")을 엔티티 클래스에 추가하세요.

[D-004] Application Service와 Facade Service는 Entity를 직접 반환할 수 없습니다.
수정 가이드: 반환타입을 Response DTO(record)로 변경하고, DTO.from(entity) 또는 DTO.of(entity)로 변환하세요.

[DIP-001] infrastructure의 외부 클라이언트 구현체는 service 패키지의 인터페이스를 구현해야 합니다.
수정 가이드: service 패키지에 인터페이스를 정의하고, infrastructure 클래스가 이를 구현하도록 변경하세요.
```

---

## 6. Gradle 설정 변경 요약

### 루트 `build.gradle`

```groovy
subprojects {
    dependencies {
        // 기존...
        testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
    }
}
```

### `api/build.gradle` (변경 없음)

기존 `fastTest`가 `excludeTags 'e2e'`이므로 ARCHITECTURE 태그 테스트는 자동 포함.

### `admin/build.gradle` (변경 없음)

동일하게 `fastTest`에 자동 포함.

---

## 7. 구현 작업 목록 (총 추정)

| 단계 | 작업 | 파일 수 | 규칙 수 |
|------|------|---------|---------|
| Phase 0 | 선행 작업 (표준화 + 의존성 + 태그) | ~10개 수정 | - |
| Phase 1 | 인프라 + HIGH 규칙 (api) | 10개 신규 | 22개 |
| Phase 2 | MEDIUM 규칙 확장 (api) | 기존 파일 확장 | 10개 |
| Phase 3 | 확장 규칙 (api) | 기존 파일 확장 | 3개 |
| Admin | 최소 규칙 | 5개 신규 | ~8개 |
| **합계** | | **~25개** | **~43개** |

---

## 8. 검증 체크리스트

### 도입 전 확인
- [ ] like 모듈 표준화 완료 (`service/like/`)
- [ ] group/invite 표준화 완료 (`service/group/`)
- [ ] ArchUnit 의존성 추가
- [ ] TestTags.ARCHITECTURE 추가
- [ ] 전체 테스트 통과 (`./gradlew test`)

### Phase 1 완료 후
- [ ] BaseArchTest 캐시 동작 확인
- [ ] 22개 HIGH 규칙 모두 통과
- [ ] `./gradlew fastTest` 실행 시간 증가량 확인 (목표: +5초 이내)

### Phase 2 완료 후
- [ ] 10개 MEDIUM 규칙 모두 통과
- [ ] A-007 커스텀 ArchCondition 정상 동작

### Phase 3 완료 후
- [ ] 3개 확장 규칙 통과
- [ ] Admin 모듈 규칙 통과

### 최종 확인
- [ ] `./gradlew test` 전체 통과
- [ ] `./gradlew fastTest` 실행 시간 허용 범위 내
- [ ] 실패 메시지에 규칙 ID + 수정 가이드 포함 확인
- [ ] docs/arch/codebase-analysis.md 업데이트
