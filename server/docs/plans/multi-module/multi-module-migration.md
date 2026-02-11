# Moment 프로젝트 멀티모듈 분리 계획

> Created: 2026-02-11
> Updated: 2026-02-11 (리뷰 반영)
> Branch: refactor/#1087
> Status: PLANNED

## Context

현재 단일 모듈 모놀리스(`server/src/`)를 **common, api, admin** 3개 Gradle 서브모듈로 분리한다.

**목표**: admin과 api의 독립 배포 + 추후 api 추가 분리를 위한 확장 가능한 구조
**원칙**: 동일 Java 패키지(`moment.*`) 유지로 import 변경 최소화

## 실행 전략

### "복사 후 삭제" 전략

Phase 2~4에서 코드를 이동할 때 **먼저 대상 모듈에 복사 → 빌드 확인 → 원본 삭제** 순서로 진행한다.
엔티티/레포지토리를 common으로 이동하면 `src/main/java`에 남아있는 서비스들이 컴파일 에러를 내기 때문에,
Phase 2~4는 하나의 atomic 작업으로 처리한다.

### Phase별 롤백 전략

각 Phase 완료 시 git tag를 생성하여 롤백 가능하게 한다:

```bash
git tag phase-0-complete   # Phase 0 완료 후
git tag phase-1-complete   # Phase 1 완료 후
git tag phase-2-4-complete # Phase 2~4 완료 후 (atomic 작업)
git tag phase-5-complete   # Phase 5 완료 후
git tag phase-6-complete   # Phase 6 완료 후
```

**Phase 5-1(`src/` 삭제)은 모든 모듈의 빌드+테스트+bootJar 통과 후에만 실행한다.**

## 목표 구조

```
server/                (root project - 소스 코드 없음)
├── common/            (java-library: 엔티티 + 레포지토리 + 공유 인프라)
│   ├── src/main/java/moment/
│   ├── src/main/resources/db/migration/
│   └── src/testFixtures/java/moment/    ← 공유 테스트 픽스쳐
├── api/               (spring-boot app: 사용자 API)
│   ├── src/main/java/moment/
│   └── src/test/java/moment/
└── admin/             (spring-boot app: 관리자 API)
    ├── src/main/java/moment/
    └── src/test/java/moment/
```

---

## Phase 0: 사전 작업 (모놀리스 내 결합도 해소)

> 모듈 분리 전에 단일 모듈 상태에서 결합을 끊는다. 각 단계 후 `./gradlew test`로 검증.

### 0-1. V35 H2 테스트 마이그레이션 파일 생성

MySQL에 `V35__create_admin_group_logs.sql`이 존재하지만 H2 대응 파일이 없다. 모듈 분리 후 admin의 `@SpringBootTest`가 Flyway V35에서 실패한다.

**생성**: `src/test/resources/db/migration/h2/V35__create_admin_group_logs__h2.sql`

### 0-2. AdminFixture dead code 정리

`AdminFixture`가 테스트 코드 어디에서도 사용되지 않는다(grep 결과 0건). testFixtures 이동 전에 삭제 또는 유지 여부를 확인한다.

```bash
grep -r "AdminFixture" src/test/ --include="*.java" -l
```

사용처가 없으면 `AdminFixture.java`를 삭제한다.

### 0-3. AdminGroupApiController: `SuccessResponse` → `AdminSuccessResponse` 교체

**파일**: `src/main/java/moment/admin/presentation/api/AdminGroupApiController.java`

- import `moment.global.dto.response.SuccessResponse` → `moment.admin.dto.response.AdminSuccessResponse`
- 모든 반환 타입 `SuccessResponse<...>` → `AdminSuccessResponse<...>` (18개 메서드)
- 모든 `SuccessResponse.of(...)` → `AdminSuccessResponse.of(...)` (18개 호출)

**이유**: admin이 api 전용 응답 래퍼에 의존하는 것을 제거

### 0-4. WebConfig 분리 (CORS 정책 차별화 포함)

**삭제**: `src/main/java/moment/global/config/WebConfig.java`

**생성 1**: `src/main/java/moment/global/config/ApiWebConfig.java`
```java
@Configuration
@RequiredArgsConstructor
public class ApiWebConfig implements WebMvcConfigurer {
    private final AuthService authService;
    // addArgumentResolvers: LoginUserArgumentResolver
    // addCorsMappings: 프론트엔드 도메인만 허용 (allowedOriginPatterns("*") 제거)
    //   → allowedOrigins("https://moment-app.com") 등 명시적 도메인
}
```

**생성 2**: `src/main/java/moment/admin/global/config/AdminWebConfig.java`
```java
@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {
    private final AdminAuthInterceptor adminAuthInterceptor;
    // addInterceptors: /api/admin/** (exclude login)
    // addCorsMappings: admin 도메인만 허용
    //   → allowedOrigins("https://admin.moment-app.com") 등 명시적 도메인
}
```

**보안 참고**: admin은 세션 쿠키 기반이므로 `allowedOriginPatterns("*")` + `allowCredentials(true)` 조합은 CSRF 공격에 취약하다. 반드시 모듈별로 허용 도메인을 제한한다.

### 0-5. SwaggerConfig 분리

**삭제**: `src/main/java/moment/global/config/SwaggerConfig.java`

**생성 1**: `src/main/java/moment/global/config/ApiSwaggerConfig.java` (API 태그만)
**생성 2**: `src/main/java/moment/admin/global/config/AdminSwaggerConfig.java` (Admin 태그만)

### 0-6. 테스트 픽스쳐에서 DTO 의존 메서드 분리

**UserFixture** (`src/test/java/moment/fixture/UserFixture.java`):
- `createUserCreateRequest()` 등 4개 메서드 → 별도 `UserRequestFixture.java`로 분리 (추후 api/test로 이동)
- UserFixture에는 엔티티 생성 메서드만 유지

**AdminFixture** (`src/test/java/moment/fixture/AdminFixture.java`):
- 0-2에서 dead code로 확인되어 삭제했으면 이 단계 skip
- 살아있다면: `createAdminCreateRequest()` 등 3개 메서드 → 별도 `AdminRequestFixture.java`로 분리 (추후 admin/test로 이동)
- AdminFixture에는 엔티티 생성 메서드만 유지

### 0-7. 검증
```bash
./gradlew test  # 모든 테스트 통과 확인
git tag phase-0-complete
```

---

## Phase 1: Gradle 멀티모듈 구조 설정

### 1-1. `settings.gradle` 수정

```groovy
rootProject.name = 'moment'
include 'common', 'api', 'admin'
```

### 1-2. 루트 `build.gradle` 재구성

기존 의존성/플러그인을 `subprojects` 블록으로 이동:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.moment'
    version = '0.0.1-SNAPSHOT'

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(21) }
    }

    configurations {
        compileOnly { extendsFrom annotationProcessor }
    }

    repositories { mavenCentral() }

    dependencyManagement {
        imports {
            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
        }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    tasks.named('test') { useJUnitPlatform() }
}
```

### 1-3. `common/build.gradle`

```groovy
plugins {
    id 'java-library'
    id 'java-test-fixtures'
}

// bootJar 태스크 없음 (java-library). plain jar 생성 비활성화
jar { enabled = true }

dependencies {
    api 'org.springframework.boot:spring-boot-starter-data-jpa'
    api 'org.springframework.boot:spring-boot-starter-validation'
    api 'org.springframework.boot:spring-boot-starter-aop'
    api 'org.springframework.security:spring-security-crypto'
    api 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8'
    api 'net.logstash.logback:logstash-logback-encoder:7.4'
    api 'org.flywaydb:flyway-mysql'

    // NOTE: ErrorCode가 HttpStatus에 의존하여 starter-web이 필요.
    // 장기적으로 ErrorCode에서 HttpStatus를 int status code로 대체하여 제거 예정.
    api 'org.springframework.boot:spring-boot-starter-web'

    runtimeOnly 'com.mysql:mysql-connector-j'

    testFixturesImplementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    testFixturesImplementation 'org.springframework.boot:spring-boot-starter-test'
    testFixturesCompileOnly 'org.projectlombok:lombok'
    testFixturesAnnotationProcessor 'org.projectlombok:lombok'
}
```

### 1-4. `api/build.gradle`

```groovy
plugins {
    id 'org.springframework.boot'
}

// plain jar 생성 비활성화 (Dockerfile *.jar 와일드카드 대응)
jar { enabled = false }

dependencies {
    implementation project(':common')
    testImplementation testFixtures(project(':common'))

    // API 전용 의존성
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    implementation 'org.springframework.boot:spring-boot-starter-mail'
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-s3:3.4.0'
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-metrics'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
    implementation 'io.micrometer:micrometer-registry-prometheus'

    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    testRuntimeOnly 'com.h2database:h2'

    testImplementation 'io.rest-assured:rest-assured'
    testImplementation 'com.launchdarkly:okhttp-eventsource:2.7.1'
}

tasks.register('fastTest', Test) {
    useJUnitPlatform { excludeTags 'e2e' }
}
tasks.register('e2eTest', Test) {
    useJUnitPlatform { includeTags 'e2e' }
}
```

### 1-5. `admin/build.gradle`

```groovy
plugins {
    id 'org.springframework.boot'
}

// plain jar 생성 비활성화 (Dockerfile *.jar 와일드카드 대응)
jar { enabled = false }

dependencies {
    implementation project(':common')
    testImplementation testFixtures(project(':common'))

    // Admin 전용 의존성
    implementation 'org.springframework.session:spring-session-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'

    testRuntimeOnly 'com.h2database:h2'

    testImplementation 'io.rest-assured:rest-assured'
}

tasks.register('fastTest', Test) {
    useJUnitPlatform { excludeTags 'e2e' }
}
tasks.register('e2eTest', Test) {
    useJUnitPlatform { includeTags 'e2e' }
}
```

### 1-6. 디렉토리 생성

```
mkdir -p common/src/{main/java/moment,main/resources,testFixtures/java/moment,testFixtures/resources,test}
mkdir -p api/src/{main/java/moment,main/resources,test/java/moment,test/resources}
mkdir -p admin/src/{main/java/moment,main/resources,test/java/moment,test/resources}
```

### 1-7. testFixtures classpath 검증

Phase 1 완료 후 common 모듈 단독 빌드를 수행하여, testFixtures jar 내부에 `db/migration/h2/` 경로가 올바르게 포함되는지 확인한다.

```bash
./gradlew :common:testFixturesJar
jar tf common/build/libs/common-*-test-fixtures.jar | grep "db/migration/h2"
```

문제 발생 시 대안: `common/src/test/resources/db/migration/h2/`로 이동.

```bash
git tag phase-1-complete
```

---

## Phase 2: common 모듈 코드 이동

> **전략**: "복사 후 삭제" — 먼저 common에 복사 → Phase 3~4 완료 후 원본 삭제.
> Phase 2~4는 하나의 atomic 작업으로 진행한다.

### 2-1. 공유 글로벌 인프라 이동 → `common/src/main/java/moment/global/`

| 원본 경로 | 내용 |
|-----------|------|
| `global/domain/BaseEntity.java` | 모든 엔티티의 부모 클래스 |
| `global/domain/TargetType.java` | 알림 타겟 타입 Enum |
| `global/page/Cursor.java, Cursorable.java, PageSize.java` | 커서 페이지네이션 |
| `global/exception/ErrorCode.java, MomentException.java` | 공통 예외 처리 |
| `global/config/AppConfig.java` | PasswordEncoder, Clock 빈 |
| `global/logging/*.java` (5개 파일) | AOP 로깅 |

### 2-2. `@EnableJpaAuditing` 공통 설정 생성

**생성**: `common/src/main/java/moment/global/config/JpaAuditingConfig.java`

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
```

api와 admin Application 클래스에서 `@EnableJpaAuditing`을 제거하고 common의 설정을 사용한다.

### 2-3. 도메인 엔티티 + 레포지토리 이동 → `common/src/main/java/moment/{domain}/`

각 도메인의 `domain/`과 `infrastructure/` 패키지만 이동:

| 도메인 | domain/ (엔티티, 정책, 값 객체) | infrastructure/ (레포지토리) |
|--------|------|------|
| user | User, ProviderType, NicknameGenerator 등 | UserRepository |
| moment | Moment, MomentImage, WriteType, 정책 등 | MomentRepository, MomentImageRepository |
| comment | Comment, CommentImage 등 | CommentRepository, CommentImageRepository |
| group | Group, GroupMember, GroupInviteLink, MemberRole, MemberStatus | GroupRepository, GroupMemberRepository, GroupInviteLinkRepository |
| like | MomentLike, CommentLike | MomentLikeRepository, CommentLikeRepository |
| notification | Notification 엔티티 등 | NotificationRepository 등 |
| report | Report, ReportReason | ReportRepository |
| block | UserBlock | UserBlockRepository |
| auth | EmailVerification, RefreshToken, Tokens (domain/) | RefreshTokenRepository, EmailVerificationRepository (infrastructure/) |
| admin | Admin, AdminSession, AdminGroupLog, AdminRole 등 | AdminRepository, AdminSessionRepository, AdminGroupLogRepository |

**주의 — auth infrastructure 분리**:

auth `infrastructure/`에는 레포지토리와 외부 클라이언트가 혼재한다. JWT 라이브러리(jjwt)에 의존하는 클래스는 common에 넣으면 컴파일 에러가 발생하므로 다음과 같이 분리한다:

| 클래스 | 이동 대상 | 이유 |
|--------|-----------|------|
| `RefreshTokenRepository` | **common** | JPA 레포지토리 (공유 인프라) |
| `EmailVerificationRepository` | **common** | JPA 레포지토리 (공유 인프라) |
| `JwtTokenManager` | **api** (Phase 4에서 이동) | jjwt 라이브러리 의존 (api 전용) |
| `AppleAuthClient` | **api** (Phase 4에서 이동) | 외부 OAuth 클라이언트 (api 전용) |
| `GoogleAuthClient` | **api** (Phase 4에서 이동) | 외부 OAuth 클라이언트 (api 전용) |

### 2-4. Flyway 마이그레이션 이동

- `src/main/resources/db/migration/mysql/V1~V38__*.sql` → `common/src/main/resources/db/migration/mysql/`

### 2-5. 테스트 픽스쳐 이동 → `common/src/testFixtures/java/moment/`

| 원본 | 대상 |
|------|------|
| `fixture/UserFixture.java` (엔티티 메서드만) | `moment/fixture/UserFixture.java` |
| `fixture/AdminFixture.java` (엔티티 메서드만, Phase 0에서 삭제 안 했을 경우) | `moment/fixture/AdminFixture.java` |
| `fixture/MomentFixture.java` | `moment/fixture/MomentFixture.java` |
| `fixture/CommentFixture.java` | `moment/fixture/CommentFixture.java` |
| `fixture/GroupFixture.java` | `moment/fixture/GroupFixture.java` |
| `fixture/GroupMemberFixture.java` | `moment/fixture/GroupMemberFixture.java` |
| `fixture/GroupInviteLinkFixture.java` | `moment/fixture/GroupInviteLinkFixture.java` |
| `fixture/MomentLikeFixture.java` | `moment/fixture/MomentLikeFixture.java` |
| `fixture/CommentLikeFixture.java` | `moment/fixture/CommentLikeFixture.java` |
| `fixture/UserBlockFixture.java` | `moment/fixture/UserBlockFixture.java` |
| `common/DatabaseCleaner.java` | `moment/common/DatabaseCleaner.java` |
| `config/TestTags.java` | `moment/config/TestTags.java` |
| `support/MomentCreatedAtHelper.java` | `moment/support/MomentCreatedAtHelper.java` |
| `support/CommentCreatedAtHelper.java` | `moment/support/CommentCreatedAtHelper.java` |

H2 테스트 마이그레이션:
- `src/test/resources/db/migration/h2/V*__*_h2.sql` → `common/src/testFixtures/resources/db/migration/h2/`

---

## Phase 3: admin 모듈 코드 이동

### 3-1. admin 패키지 이동 → `admin/src/main/java/moment/admin/`

`domain/`과 `infrastructure/`를 제외한 나머지 전체:

```
admin/
├── config/           → AdminInitializer
├── dto/request/      → AdminCreateRequest, AdminLoginRequest, AdminUpdateRequest 등
├── dto/response/     → AdminSuccessResponse, AdminErrorResponse, Admin*Response 등
├── global/config/    → SessionConfig, AdminWebConfig, AdminSwaggerConfig
├── global/exception/ → AdminErrorCode, AdminException
├── global/interceptor/ → AdminAuthInterceptor
├── global/listener/  → AdminSessionListener
├── global/util/      → AdminSessionManager, ClientIpExtractor, UserAgentParser
├── presentation/api/ → AdminApiExceptionHandler, Admin*ApiController (6개)
└── service/          → AdminService, AdminSessionService, AdminUserService,
                        AdminGroupService, AdminGroupQueryService,
                        AdminGroupMemberService, AdminContentService,
                        AdminManagementApplicationService, AdminGroupLogService
```

### 3-2. SessionConfig 세션 쿠키 보안 강화

admin 모듈의 `SessionConfig`에서 `CookieSerializer`에 보안 속성을 명시한다:

```java
@Bean
public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setUseHttpOnlyCookie(true);
    serializer.setUseSecureCookie(true);
    serializer.setSameSite("Strict");
    return serializer;
}
```

### 3-3. AdminApplication 생성

**파일**: `admin/src/main/java/moment/AdminApplication.java`

```java
@EnableScheduling
@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

(`@EnableJpaAuditing`은 common의 `JpaAuditingConfig`에서 처리, `@EnableAsync` 불필요 - admin은 비동기 이벤트 핸들러 없음)

### 3-4. admin 설정 파일 생성

**`admin/src/main/resources/application-dev.yml`**: DB 연결 + Spring Session JDBC + admin.session.timeout + admin.initial.* 설정. Flyway `enabled: false`.
**`admin/src/main/resources/application-prod.yml`**: 운영 설정. 서버 포트 api와 다르게 (예: 8081). Swagger UI 비활성화:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**`admin/src/test/resources/application-test.yml`**: H2 + Spring Session JDBC 테스트 설정. Flyway `enabled: true` (H2 스키마 생성용). `admin.initial.*` 테스트 값 반드시 포함:

```yaml
admin:
  initial:
    email: test-admin@moment.com
    password: test-password-123!
    nickname: test-admin
```

### 3-5. admin 리소스 파일 이동

- `src/main/resources/static/admin/css/` → `admin/src/main/resources/static/admin/css/`

### 3-6. admin 테스트 이동

`src/test/java/moment/admin/` 전체 → `admin/src/test/java/moment/admin/`

DTO 의존 픽스쳐:
- Phase 0에서 분리한 `AdminRequestFixture.java` → `admin/src/test/java/moment/fixture/AdminRequestFixture.java`

---

## Phase 4: api 모듈 코드 이동

### 4-1. 각 도메인의 서비스/프레젠테이션/DTO 이동 → `api/src/main/java/moment/`

`domain/`과 `infrastructure/`를 제외한 나머지:

| 도메인 | 이동 대상 패키지 |
|--------|-----------------|
| auth | application/, dto/, presentation/ (JWT, OAuth, LoginUserArgumentResolver), **infrastructure/ 중 api 전용** (`JwtTokenManager`, `AppleAuthClient`, `GoogleAuthClient`) |
| user | service/, presentation/, dto/ |
| moment | service/, presentation/, dto/ |
| comment | service/, presentation/, dto/ |
| group | service/, presentation/, dto/ |
| like | service/ (`MomentLikeService`, `CommentLikeService`, `MomentLikeApplicationService`, `CommentLikeApplicationService`, `MomentLikeToggleFacadeService`, `CommentLikeToggleFacadeService`), presentation/ (`MomentLikeController`, `CommentLikeController`), dto/ |
| notification | service/, presentation/, dto/ |
| report | service/ (`ReportApplicationService`, `ReportService`, `ReportCreateFacadeService`), presentation/ (`ReportController`), dto/ (`ReportCreateRequest`, `ReportCreateResponse`) |
| storage | **전체** (JPA 엔티티 없음 - application/, infrastructure/, presentation/, dto/) |
| block | service/, presentation/, dto/ |

### 4-2. 이벤트 DTO 이동 → `api/src/main/java/moment/{domain}/dto/`

각 도메인의 `dto/` 하위에 위치한 이벤트 레코드는 **api 모듈로 이동**한다. 현재 모든 이벤트 발행자(Facade)와 소비자(NotificationEventHandler)가 api 모듈에 위치하므로 api에 두는 것이 적합하다.

| 이벤트 레코드 | 원본 위치 | 이동 대상 |
|---------------|-----------|-----------|
| `CommentCreateEvent` | `comment/dto/` | `api: comment/dto/` |
| `MomentLikeEvent` | `like/dto/` | `api: like/dto/` |
| `CommentLikeEvent` | `like/dto/` | `api: like/dto/` |
| `GroupJoinRequestEvent` | `group/dto/` | `api: group/dto/` |
| `GroupJoinApproveEvent` | `group/dto/` | `api: group/dto/` |
| `GroupMomentCreateEvent` | `moment/dto/` | `api: moment/dto/` |
| `GroupCommentCreateEvent` | `comment/dto/` | `api: comment/dto/` |

**향후 확장**: admin에서 이벤트가 필요해지면 해당 이벤트 레코드를 common으로 이동한다.

### 4-3. api 전용 글로벌 파일 이동 → `api/src/main/java/moment/global/`

| 파일 | 비고 |
|------|------|
| `global/dto/response/SuccessResponse.java` | api 전용 응답 래퍼 |
| `global/dto/response/ErrorResponse.java` | api 전용 에러 응답 |
| `global/exception/GlobalExceptionHandler.java` | api 전용 예외 핸들러 |
| `global/config/ApiWebConfig.java` | Phase 0에서 생성 |
| `global/config/ApiSwaggerConfig.java` | Phase 0에서 생성 |
| `global/config/CacheConfig.java` | Apple 키 캐시 |
| `global/config/MailConfig.java` | 이메일 |
| `global/config/RestTemplateConfig.java` | REST 클라이언트 |
| `global/presentation/HealthCheckController.java` | 헬스 체크 |

### 4-4. ApiApplication 생성

**파일**: `api/src/main/java/moment/ApiApplication.java`

```java
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

(`@EnableJpaAuditing`은 common의 `JpaAuditingConfig`에서 처리)

### 4-5. api 설정 파일 이동

기존 `application-dev.yml`, `application-prod.yml` → `api/src/main/resources/`
- admin 관련 설정 제거 (admin.initial.*, admin.session.*, spring.session.jdbc)
- Flyway `enabled: true` 유지 (api가 마이그레이션 실행 담당)

`application-prod.yml`에 Swagger UI 비활성화 추가:
```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

`logback-spring.xml` → `api/src/main/resources/` (admin에도 별도 복사)

### 4-6. api 리소스 파일 이동

- `src/main/resources/email/reminder.html` → `api/src/main/resources/email/reminder.html`

### 4-7. api 테스트 이동

`src/test/java/moment/` 에서 admin 제외 전체 → `api/src/test/java/moment/`
- auth/, block/, comment/, group/, like/, moment/, notification/, report/, storage/, user/

DTO 의존 픽스쳐:
- Phase 0에서 분리한 `UserRequestFixture.java` → `api/src/test/java/moment/fixture/UserRequestFixture.java`

`api/src/test/resources/application-test.yml`: 기존 test 설정에서 admin 관련 제거.

---

## Phase 5: 정리 및 검증

### 5-1. 기존 `src/` 디렉토리 삭제

**전제 조건**: 모든 모듈의 빌드+테스트+bootJar 통과 확인 후에만 실행.

```bash
./gradlew clean build                    # 전체 빌드+테스트
./gradlew :api:bootJar :admin:bootJar    # 실행 JAR 생성 확인
# 위 모두 성공한 경우에만 삭제
```

모든 코드 이동 완료 후 `server/src/` 삭제. `MomentApplication.java` 포함.

### 5-2. 컴포넌트 스캔 검증

- `ApiApplication` (패키지 `moment`) → `moment.*` 스캔 → common + api 클래스만 classpath에 존재
- `AdminApplication` (패키지 `moment`) → `moment.*` 스캔 → common + admin 클래스만 classpath에 존재
- Gradle 모듈 경계가 classpath를 자동으로 분리하므로 추가 설정 불필요

### 5-3. Flyway 설정 검증

| 모듈 | 운영 (MySQL) | 테스트 (H2) |
|------|-------------|------------|
| api | `flyway.enabled: true`, `locations: classpath:db/migration/mysql` | `flyway.enabled: true`, `locations: classpath:db/migration/h2` |
| admin | `flyway.enabled: false` | `flyway.enabled: true`, `locations: classpath:db/migration/h2` |

마이그레이션 파일은 common에 있으므로 양쪽 classpath에서 접근 가능.

### 5-4. 빌드 및 테스트 검증

```bash
./gradlew clean build           # 전체 빌드
./gradlew :common:build         # common 모듈 빌드
./gradlew :api:test             # api 테스트
./gradlew :admin:test           # admin 테스트
./gradlew :api:bootJar          # api 실행 JAR 생성
./gradlew :admin:bootJar        # admin 실행 JAR 생성
git tag phase-5-complete
```

---

## Phase 6: 배포 파이프라인 수정

> 모듈 분리 후 api와 admin이 각각 독립적으로 빌드/배포되어야 함.

### 6-1. Dockerfile 분리

**현재**: `server/Dockerfile` → `COPY build/libs/*.jar app.jar` (단일 JAR)

모듈 분리 후 JAR 경로가 변경됨:
- api: `api/build/libs/api-0.0.1-SNAPSHOT.jar`
- admin: `admin/build/libs/admin-0.0.1-SNAPSHOT.jar`

**생성 1**: `server/api/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY build/libs/*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
```

**생성 2**: `server/admin/Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY build/libs/*.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
```

**삭제**: 기존 `server/Dockerfile`

### 6-2. docker-compose.yml 수정 (환경 변수 분리)

**현재**: 단일 `app` 서비스 (port 8080), 단일 `.env`
**변경**: api + admin 2개 서비스, 환경 변수 분리

환경 변수 파일 분리:
- `.env.common` — DB 접속 정보 (MYSQL_HOST, MYSQL_PORT 등)
- `.env.api` — JWT 시크릿, S3 키, OAuth 클라이언트 등 api 전용
- `.env.admin` — ADMIN_INITIAL_* 등 admin 전용

```yaml
services:
  mysql:
    # ... (기존과 동일)

  api:
    container_name: moment-api-server
    build:
      context: ./api
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8080:8080"
    env_file:
      - .env.common
      - .env.api
    environment:
      SPRING_PROFILES_ACTIVE: dev

  admin-server:
    container_name: moment-admin-server
    build:
      context: ./admin
    depends_on:
      mysql:
        condition: service_healthy
    ports:
      - "8081:8081"
    env_file:
      - .env.common
      - .env.admin
    environment:
      SPRING_PROFILES_ACTIVE: dev
```

### 6-3. GitHub Actions 워크플로우 수정

**수정**: `.github/workflows/prod-server-ci.yml`

주요 변경점:
- `paths` 필터: `['server/**']` → `['server/api/**', 'server/common/**']` (common 변경도 api 빌드 트리거)
- 테스트: `./gradlew fastTest e2eTest` → `./gradlew :api:fastTest :api:e2eTest`
- 빌드: `./gradlew clean build -x test` → `./gradlew :api:clean :api:build -x test`
- Docker context: `./server` → `./server/api`
- Dockerfile: `./server/Dockerfile` → `./server/api/Dockerfile`
- Docker Hub 이미지명: `moment-prod-images` → `moment-api` (또는 기존 유지)

**수정**: `.github/workflows/prod-server-cd.yml`
- 컨테이너명: `moment-app-server` → `moment-api-server`
- docker compose: `--no-deps -d app` → `--no-deps -d api`

**신규**: `.github/workflows/backend-admin-ci.yml` (기존 `admin-ci.yml`은 프론트엔드용이므로 `backend-` 접두사로 구분)
```yaml
name: Backend Admin CI
on:
  pull_request:
    paths: ['server/admin/**', 'server/common/**']
  push:
    branches: [main]
    paths: ['server/admin/**', 'server/common/**']
jobs:
  test:
    # ./gradlew :admin:fastTest :admin:e2eTest
  build-and-push:
    # ./gradlew :admin:clean :admin:build -x test
    # Docker build: context ./server/admin, file ./server/admin/Dockerfile
    # 별도 Docker Hub 이미지 (moment-admin-server)
```

**신규**: `.github/workflows/backend-admin-cd.yml`
```yaml
name: Backend Admin CD
on:
  workflow_run:
    workflows: ["Backend Admin CI"]
jobs:
  deploy:
    # SSH로 EC2에 배포
    # 컨테이너명: moment-admin-server
    # docker compose: --no-deps -d admin-server
```

### 6-4. 배포 스크립트 수정

**수정**: `server/deploy.sh` - docker compose로 두 서비스 모두 시작
**수정**: `server/scripts/start_server.sh` - api/admin 컨테이너 별도 관리

### 6-5. 배포 전략

| 항목 | 결정 |
|------|------|
| Docker Hub | 같은 계정, 이미지명 분리 (`moment-api`, `moment-admin-server`) |
| EC2 배치 | 같은 EC2에 2개 컨테이너 (추후 분리 가능) |
| CI 트리거 | common 변경 시 양쪽 CI 모두 트리거 |
| 개별 배포 | `docker compose up --no-deps -d {service}` |

### 6-6. 배포 검증

```bash
# 로컬 Docker 빌드 테스트
./gradlew :api:build -x test && docker build -t moment-api ./api
./gradlew :admin:build -x test && docker build -t moment-admin ./admin
docker compose up -d
# api: http://localhost:8080/health
# admin: http://localhost:8081/api/admin/auth/login
git tag phase-6-complete
```

---

## 핵심 파일 목록

### 수정 대상 (Phase 0)
- `src/test/resources/db/migration/h2/` - V35 H2 마이그레이션 추가
- `src/test/java/moment/fixture/AdminFixture.java` - dead code 정리 확인
- `src/main/java/moment/admin/presentation/api/AdminGroupApiController.java` - SuccessResponse → AdminSuccessResponse
- `src/main/java/moment/global/config/WebConfig.java` - 삭제 후 2개로 분리 (CORS 정책 차별화 포함)
- `src/main/java/moment/global/config/SwaggerConfig.java` - 삭제 후 2개로 분리
- `src/test/java/moment/fixture/UserFixture.java` - DTO 메서드 분리

### 신규 생성
- `settings.gradle` - 서브모듈 선언
- `common/build.gradle`, `api/build.gradle`, `admin/build.gradle`
- `common/src/main/java/moment/global/config/JpaAuditingConfig.java` - 공통 JPA Auditing 설정
- `api/src/main/java/moment/ApiApplication.java`
- `admin/src/main/java/moment/AdminApplication.java`
- 각 모듈의 `application-*.yml` 설정 파일 (Swagger prod 비활성화 포함)
- `.env.common`, `.env.api`, `.env.admin` - 환경 변수 분리

### 배포 관련 수정 (Phase 6)
- `server/Dockerfile` → 삭제, `server/api/Dockerfile` + `server/admin/Dockerfile`로 분리 (non-root 사용자 포함)
- `server/docker-compose.yml` → api + admin-server 2개 서비스로 수정 (env 분리)
- `.github/workflows/prod-server-ci.yml` → api 모듈 빌드로 변경, paths 필터 수정
- `.github/workflows/prod-server-cd.yml` → 컨테이너명 변경
- `.github/workflows/backend-admin-ci.yml` → 신규 생성 (admin 백엔드 CI, 기존 프론트엔드 `admin-ci.yml`과 구분)
- `.github/workflows/backend-admin-cd.yml` → 신규 생성 (admin 백엔드 CD)
- `server/deploy.sh`, `server/scripts/start_server.sh` → 2개 서비스 대응

---

## 리뷰 반영 추적

> 아래는 `multi-module-migration-review.md`의 리뷰 항목 반영 현황이다.

### CRITICAL (5/5건 반영)

| # | 제목 | 반영 위치 |
|---|------|-----------|
| C1 | Event DTO 위치 명시 | Phase 4-2 (이벤트 DTO 이동 섹션 신설) |
| C2 | Report 이동 범위 정정 | Phase 4-1 (report/like 파일별 상세 기술) |
| C3 | V35 H2 마이그레이션 | Phase 0-1 (새 단계 추가) |
| C4 | auth infra 분리 기준 | Phase 2-3 auth 행 분리 + Phase 4-1 auth 행 수정 |
| C5 | Phase 2~4 빌드 불가 | 실행 전략 섹션 + Phase 2 설명 + Phase별 git tag |

### HIGH (8/8건 반영)

| # | 제목 | 반영 위치 |
|---|------|-----------|
| H6 | CORS 정책 분리 | Phase 0-4 (도메인별 CORS 설정) |
| H7 | 세션 쿠키 보안 | Phase 3-2 (SessionConfig 보안 속성) |
| H8 | testFixtures classpath 검증 | Phase 1-7 (검증 단계 추가) |
| H9 | 환경 변수 분리 | Phase 6-2 (.env.common/.api/.admin 분리) |
| H10 | 롤백 전략 | 실행 전략 섹션 (Phase별 git tag) |
| H11 | common starter-web 이유 | Phase 1-3 (주석으로 이유 명시) |
| H12 | 리소스 파일 이동 | Phase 3-5, Phase 4-6 (리소스 이동 단계 추가) |
| H13 | CI 이름 충돌 | Phase 6-3 (`backend-admin-ci/cd.yml`로 변경) |

### MEDIUM (선별 반영)

| # | 제목 | 반영 여부 | 위치/비고 |
|---|------|-----------|-----------|
| M14 | @EnableJpaAuditing 중복 | 반영 | Phase 2-2, Phase 3-3, Phase 4-4 |
| M17 | Swagger prod 비활성화 | 반영 | Phase 3-4, Phase 4-5 |
| M18 | Dockerfile non-root | 반영 | Phase 6-1 |
| M20 | Gradle 누락 의존성 | 반영 | Phase 1-2 (configurations 블록), Phase 1-4 (metrics) |
| M21 | AdminInitializer 테스트 변수 | 반영 | Phase 3-4 (test yml 예시) |
| M22 | AdminFixture dead code | 반영 | Phase 0-2 (정리 단계 추가) |
| S25 | plain jar 비활성화 | 반영 | Phase 1-4, Phase 1-5 (`jar { enabled = false }`) |
| M15 | common 테스트 전략 | 미반영 | 현 단계에서는 api/admin에 유지 |
| M16 | admin 전용 Repository | 미반영 | 현 단계 유지, 향후 검토 |
| M19 | Native Query 호환성 | 미반영 | Phase 5-4 테스트 검증에서 커버 |
