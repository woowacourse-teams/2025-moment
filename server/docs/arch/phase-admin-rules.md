# Phase Admin: Admin 모듈 최소 규칙

> 기반 문서: `docs/arch/archunit-implementation-plan.md`
> 선행 조건: Phase 1 완료 (api 모듈 규칙 안정화 후)
> 목표: Admin 모듈 전용 베이스 클래스 + 4개 테스트 파일 + ~8개 규칙

---

## 개요

Admin 모듈은 API 모듈과 구조가 다르므로 **핵심 규칙만** 최소 적용합니다.

| 파일 | 규칙 수 | 검증 내용 |
|------|--------|---------|
| `AdminBaseArchTest.java` | - | 공유 베이스 (캐시 + 태그) |
| `AdminLayerDependencyRuleTest.java` | 2 | 레이어 의존 방향 |
| `AdminNamingConventionRuleTest.java` | 3 | Admin 네이밍 패턴 |
| `AdminCyclicDependencyRuleTest.java` | 1 | 순환 의존 금지 |
| `AdminAnnotationRuleTest.java` | 2 | 필드 주입 금지, RequiredArgsConstructor |

**모든 파일 위치**: `admin/src/test/java/moment/arch/`

### Admin 모듈에 적용하지 않는 규칙

| 규칙 | 미적용 사유 |
|------|-----------|
| DIP | Admin은 외부 클라이언트 없음 (세션 기반) |
| 이벤트 핸들러 | Admin은 이벤트 기반 통신 미사용 |
| 도메인 순수성 | Admin 서비스가 도메인 로직과 혼합 |
| DTO record | Admin DTO 중 일부가 record가 아닐 수 있음 |
| Entity 반환 금지 | Admin은 단순 CRUD 위주 |

---

## Admin 모듈 현재 구조

```
admin/src/main/java/moment/admin/
├── config/                            # AdminInitializer
├── dto/
│   ├── request/                       # AdminCreateRequest, AdminLoginRequest 등
│   └── response/                      # AdminSuccessResponse, AdminErrorResponse 등
├── global/
│   ├── config/                        # AdminSwaggerConfig, AdminWebConfig, SessionConfig
│   ├── exception/                     # AdminErrorCode, AdminException
│   ├── interceptor/                   # AdminAuthInterceptor
│   ├── listener/                      # AdminSessionListener
│   └── util/                          # AdminSessionManager, ClientIpExtractor, UserAgentParser
├── presentation/
│   └── api/                           # Admin*ApiController, AdminApiExceptionHandler
└── service/
    ├── admin/                         # AdminService
    ├── application/                   # AdminManagementApplicationService
    ├── content/                       # AdminContentService
    ├── group/                         # AdminGroupService, AdminGroupMemberService, AdminGroupLogService, AdminGroupQueryService
    ├── session/                       # AdminSessionService
    └── user/                          # AdminUserService
```

---

## Step A-1: AdminBaseArchTest (베이스 클래스)

### 파일

**`admin/src/test/java/moment/arch/AdminBaseArchTest.java`**

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
public abstract class AdminBaseArchTest {
    // Admin 모듈의 ArchUnit 테스트 베이스 클래스
    // api 모듈의 BaseArchTest와 동일한 설정이지만 독립 배치
}
```

### 주의사항

- Admin 모듈 빌드 시 `common` 모듈의 클래스도 함께 스캔됨 (`packages = "moment"`)
- Admin 전용 규칙은 `..admin..` 패키지로 범위 제한 필요

---

## Step A-2: AdminLayerDependencyRuleTest (레이어 의존성)

### 파일

**`admin/src/test/java/moment/arch/AdminLayerDependencyRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class AdminLayerDependencyRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AL_001_Admin_service는_presentation에_의존할_수_없다 =
        noClasses()
            .that().resideInAPackage("..admin.service..")
            .should().dependOnClassesThat()
            .resideInAPackage("..admin.presentation..")
            .because("[AL-001] Admin service 레이어는 presentation 레이어에 의존할 수 없습니다. " +
                     "수정 가이드: service → presentation 방향의 의존성을 제거하세요.");

    @ArchTest
    static final ArchRule AL_002_Admin_presentation은_service와_dto만_의존할_수_있다 =
        noClasses()
            .that().resideInAPackage("..admin.presentation..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "..admin.config..",
                "..admin.global.listener.."
            )
            .because("[AL-002] Admin presentation 레이어는 config/listener에 직접 의존할 수 없습니다. " +
                     "수정 가이드: presentation에서 불필요한 의존성을 제거하세요.");
}
```

### Admin 레이어 구조

```
presentation/api/   → service/   (허용)
presentation/api/   → dto/       (허용)
presentation/api/   → global/    (허용: 예외 처리, 인증)
service/            → domain/    (허용: common 모듈의 엔티티 참조)
service/            → dto/       (허용)
service/            ↛ presentation/ (금지)
```

### 주의사항

- Admin 모듈의 `presentation/api/` 패키지에는 Controller와 ExceptionHandler가 있음
- `AdminApiExceptionHandler`가 `AdminException`을 참조하는 것은 `..global.exception..`이므로 허용
- `AdminAuthInterceptor`가 `..global.interceptor..`에 있으므로 레이어 검증 대상 외

---

## Step A-3: AdminNamingConventionRuleTest (네이밍 규칙)

### 파일

**`admin/src/test/java/moment/arch/AdminNamingConventionRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

class AdminNamingConventionRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AN_001_Admin_Controller는_ApiController로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .and().resideInAPackage("..admin.presentation..")
            .should().haveSimpleNameEndingWith("ApiController")
            .because("[AN-001] Admin @RestController 클래스는 'ApiController'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 Admin{리소스명}ApiController로 변경하세요. " +
                     "예: AdminUserApiController, AdminGroupApiController");

    @ArchTest
    static final ArchRule AN_002_Admin_Controller는_Admin으로_시작해야_한다 =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .and().resideInAPackage("..admin.presentation..")
            .should().haveSimpleNameStartingWith("Admin")
            .because("[AN-002] Admin @RestController 클래스는 'Admin'으로 시작해야 합니다. " +
                     "수정 가이드: 클래스명 앞에 Admin 접두사를 추가하세요.");

    @ArchTest
    static final ArchRule AN_003_Admin_Service는_Admin으로_시작하거나_Service로_끝나야_한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..admin.service..")
            .should().haveSimpleNameEndingWith("Service")
            .because("[AN-003] Admin @Service 클래스는 'Service'로 끝나야 합니다. " +
                     "수정 가이드: 클래스명을 Admin{도메인}Service로 변경하세요.");
}
```

### 현재 Admin 클래스 네이밍 현황

| 클래스 | AN-001 | AN-002 | 통과 |
|--------|--------|--------|------|
| `AdminAccountApiController` | ApiController (통과) | Admin (통과) | O |
| `AdminAuthApiController` | ApiController (통과) | Admin (통과) | O |
| `AdminGroupApiController` | ApiController (통과) | Admin (통과) | O |
| `AdminSessionApiController` | ApiController (통과) | Admin (통과) | O |
| `AdminUserApiController` | ApiController (통과) | Admin (통과) | O |
| `AdminApiExceptionHandler` | ExceptionHandler (AN-001 대상?) | Admin (통과) | 확인 필요 |

### 주의사항

- `AdminApiExceptionHandler`는 `@RestControllerAdvice`이므로 `@RestController`가 아님 → AN-001 대상 외
- `AdminSessionManager`는 `@Component`이므로 `@Service`가 아님 → AN-003 대상 외

---

## Step A-4: AdminCyclicDependencyRuleTest (순환 의존성)

### 파일

**`admin/src/test/java/moment/arch/AdminCyclicDependencyRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class AdminCyclicDependencyRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AC_001_Admin_패키지_간_순환_의존이_없어야_한다 =
        slices()
            .matching("moment.admin.(**)")
            .should().beFreeOfCycles()
            .because("[AC-001] Admin 모듈 내 패키지 간 순환 의존이 감지되었습니다. " +
                     "수정 가이드: 의존성 방향을 단방향으로 정리하세요. " +
                     "presentation → service → domain/dto 방향을 유지하세요.");
}
```

### 주의사항

- Admin 모듈은 `moment.admin.*` 패키지만 대상
- `moment.admin.(**)`로 Admin 패키지 내부만 슬라이싱
- Common 모듈의 엔티티/리포지토리와의 의존성은 순환 검사 대상 외

---

## Step A-5: AdminAnnotationRuleTest (어노테이션 규칙)

### 파일

**`admin/src/test/java/moment/arch/AdminAnnotationRuleTest.java`**

### 구현

```java
package moment.arch;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

class AdminAnnotationRuleTest extends AdminBaseArchTest {

    @ArchTest
    static final ArchRule AA_001_Admin에서_Autowired_필드_주입은_금지한다 =
        noFields()
            .that().areDeclaredInClassesThat()
            .resideInAPackage("..admin..")
            .should().beAnnotatedWith(Autowired.class)
            .because("[AA-001] Admin 모듈에서 @Autowired 필드 주입은 금지됩니다. " +
                     "수정 가이드: @RequiredArgsConstructor + private final 필드로 " +
                     "생성자 주입을 사용하세요.");

    @ArchTest
    static final ArchRule AA_002_Admin_Service_클래스는_RequiredArgsConstructor를_사용한다 =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..admin.service..")
            .should().beAnnotatedWith(RequiredArgsConstructor.class)
            .because("[AA-002] Admin @Service 클래스는 @RequiredArgsConstructor를 사용해야 합니다. " +
                     "수정 가이드: 클래스에 @RequiredArgsConstructor를 추가하고, " +
                     "의존성을 private final 필드로 선언하세요.");
}
```

### 현재 Admin 서비스 패턴 확인

| 서비스 | @RequiredArgsConstructor | 통과 |
|--------|------------------------|------|
| `AdminService` | 확인 필요 | ? |
| `AdminManagementApplicationService` | 확인 필요 | ? |
| `AdminContentService` | 확인 필요 | ? |
| `AdminGroupService` | 확인 필요 | ? |
| `AdminGroupMemberService` | 확인 필요 | ? |
| `AdminGroupLogService` | 확인 필요 | ? |
| `AdminGroupQueryService` | 확인 필요 | ? |
| `AdminSessionService` | 확인 필요 | ? |
| `AdminUserService` | 확인 필요 | ? |

**구현 전 확인**: 모든 Admin 서비스가 `@RequiredArgsConstructor`를 사용하는지 grep으로 확인

```bash
grep -l "RequiredArgsConstructor" admin/src/main/java/moment/admin/service/**/*.java
```

---

## Admin 모듈 파일 구조 요약

```
admin/src/test/java/moment/arch/
├── AdminBaseArchTest.java               # Admin 공유 베이스
├── AdminLayerDependencyRuleTest.java    # AL-001 ~ AL-002
├── AdminNamingConventionRuleTest.java   # AN-001 ~ AN-003
├── AdminCyclicDependencyRuleTest.java   # AC-001
└── AdminAnnotationRuleTest.java         # AA-001 ~ AA-002
```

---

## Phase Admin 전체 검증

### 실행 명령

```bash
cd server

# 1. Admin ArchUnit 테스트만 실행
./gradlew test -p admin --tests "moment.arch.*"

# 2. Admin fastTest에 포함 확인
./gradlew fastTest -p admin

# 3. 전체 프로젝트 테스트
./gradlew test
```

### 완료 기준

- [ ] `AdminBaseArchTest` 정상 동작
- [ ] AL-001, AL-002: Admin 레이어 의존성 규칙 통과
- [ ] AN-001 ~ AN-003: Admin 네이밍 규칙 통과
- [ ] AC-001: Admin 순환 의존성 규칙 통과
- [ ] AA-001 ~ AA-002: Admin 어노테이션 규칙 통과
- [ ] `./gradlew fastTest -p admin` 실행 시간 허용 범위 내

### 커밋 전략

```
커밋 1: test: Admin 모듈 ArchUnit 베이스 클래스 및 전체 규칙 추가 (AL, AN, AC, AA)
```

Admin 모듈은 규칙 수가 적으므로 단일 커밋으로 처리 가능.

### 실패 시 대응 전략

| 위반 유형 | 대응 방법 |
|---------|---------|
| Admin 레이어 역방향 의존 | service에서 presentation import 제거 |
| Admin Controller 이름 불일치 | Admin{리소스}ApiController로 리네이밍 |
| Admin 순환 의존 | 의존 방향 단일화 |
| @Autowired 필드 주입 | 생성자 주입으로 변경 |
| @RequiredArgsConstructor 누락 | 어노테이션 추가 |

---

## 전체 프로젝트 최종 검증 (Phase 0~3 + Admin 완료 후)

### 최종 검증 명령

```bash
cd server

# 1. 전체 테스트 실행
./gradlew test

# 2. fastTest 실행 및 시간 측정
time ./gradlew fastTest

# 3. ArchUnit 테스트만 실행 (api + admin)
./gradlew test -p api --tests "moment.arch.*"
./gradlew test -p admin --tests "moment.arch.*"
```

### 최종 체크리스트

- [ ] `./gradlew test` 전체 통과
- [ ] `./gradlew fastTest` 실행 시간 허용 범위 내 (기존 대비 +5초 이내)
- [ ] 실패 메시지에 규칙 ID + 수정 가이드 포함 확인
- [ ] API 모듈: 35개 규칙 통과
- [ ] Admin 모듈: 8개 규칙 통과

### 규칙 총 현황표

| Phase | 모듈 | 규칙 수 | 규칙 ID |
|-------|------|--------|---------|
| Phase 1 | api | 22 | L-001~004, P-001~004, N-001~003, A-001~005, C-001~002, D-002~004, DS-001~002, DIP-001~002 |
| Phase 2 | api | 10 | N-004~006, A-006~007, P-005~006, D-001, L-005~006 |
| Phase 3 | api | 3 | AU-001~002, M-003 |
| Admin | admin | 8 | AL-001~002, AN-001~003, AC-001, AA-001~002 |
| **합계** | | **43** | |
