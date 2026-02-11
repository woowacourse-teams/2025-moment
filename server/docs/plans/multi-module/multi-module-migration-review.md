# 멀티모듈 분리 계획 종합 리뷰 보고서

> **대상**: `docs/plans/multi-module/multi-module-migration.md`
> **리뷰어**: architecture-reviewer, database-reviewer, security-reviewer, domain-reviewer, devils-advocate
> **날짜**: 2026-02-11
> **상태**: REVIEW_COMPLETE

---

## CRITICAL (반드시 수정) — 5건

### 1. [계획 정확성] Event DTO 위치가 명시되지 않음

**발견자**: architecture-reviewer (CRITICAL), domain-reviewer (HIGH)

계획에서 각 도메인의 `dto/`를 일괄적으로 api로 이동한다고 명시하지만, 이벤트 레코드(`CommentCreateEvent`, `MomentLikeEvent` 등 7개)도 `dto/` 하위에 위치한다. 이벤트와 request/response DTO를 구분 없이 이동하면 혼란이 발생한다.

현재 모든 이벤트 발행자(facade)와 소비자(NotificationEventHandler)가 api 모듈에 위치하므로 api에 두는 것이 맞지만, **계획에 이벤트 레코드의 이동 위치를 명시적으로 구분**해야 한다.

**수정 제안**: Phase 4-1 테이블에 "이벤트 DTO는 api 모듈로 이동" 행을 추가하고, 향후 admin에서 이벤트가 필요할 경우 common으로 이동하는 확장 전략을 기술한다.

---

### 2. [계획 정확성] Report 도메인 이동 범위가 과소 기술됨

**발견자**: domain-reviewer (CRITICAL), architecture-reviewer (HIGH)

Phase 4-1에서 report를 `service/ (application 1파일)`이라고만 기술했지만, 실제 이동 대상은 6개 파일이다:
- `ReportApplicationService.java`, `ReportService.java`, `ReportCreateFacadeService.java`
- `ReportController.java`, `ReportCreateRequest.java`, `ReportCreateResponse.java`

`ReportCreateFacadeService`는 `MomentApplicationService`와 `CommentApplicationService`에 크로스 도메인 의존성이 있어 반드시 api 모듈로 이동해야 한다.

**수정 제안**: report 모듈의 파일별 이동 목록을 정확하게 작성한다. like 모듈도 동일하게 세부 기술 필요.

---

### 3. [DB] V35 H2 테스트 마이그레이션 파일 누락

**발견자**: database-reviewer (CRITICAL)

MySQL에 `V35__create_admin_group_logs.sql`이 존재하지만 H2 대응 파일이 없다. 모듈 분리 후 admin의 `@SpringBootTest`가 Flyway V35에서 실패한다.

**수정 제안**: Phase 0 이전에 `V35__create_admin_group_logs__h2.sql`을 생성한다.

---

### 4. [계획 정확성] auth infrastructure 분리 기준 불명확

**발견자**: architecture-reviewer (HIGH)

auth의 `domain/` + `infrastructure/`를 common에 배치한다고 했지만, `JwtTokenManager`는 `auth/infrastructure/`에 있으면서 JWT 라이브러리(jjwt)에 의존한다. jjwt는 api에만 의존성이 있으므로 `JwtTokenManager`가 common에 들어가면 컴파일 에러가 발생한다.

**수정 제안**: auth infrastructure 중 이동 위치를 명확히 구분한다:
- common: `RefreshTokenRepository`, `EmailVerificationRepository`
- api: `JwtTokenManager`, `AppleAuthClient`, `GoogleAuthClient`

---

### 5. [계획 정확성] Phase 2→3→4 진행 중 빌드 불가 상태 지속

**발견자**: devils-advocate (HIGH)

Phase 2에서 엔티티/레포지토리를 common으로 이동하면, `src/main/java`에 남아있는 서비스들이 컴파일 에러를 낸다. Phase 3(admin)과 Phase 4(api)가 완료되기 전까지 빌드가 불가능하다. TDD 원칙("모든 테스트 통과 후에만 커밋")과 충돌한다.

**수정 제안**: "복사 후 삭제" 전략을 사용한다. 먼저 common에 복사 → 빌드 확인 → 원본 삭제. 또는 Phase 2~4를 하나의 atomic 작업으로 처리하고, Phase별 git tag를 만들어 롤백 가능하게 한다.

---

## HIGH (강력 권고) — 8건

### 6. [보안] CORS `allowedOriginPatterns("*")` + `allowCredentials(true)`

**발견자**: security-reviewer (CRITICAL) → **devils-advocate 반론 후 HIGH로 조정** (기존 문제이며 계획 자체의 결함은 아님, 그러나 분리 시 반드시 수정해야 함)

admin은 세션 쿠키 기반이므로 모든 오리진 허용 시 CSRF 공격에 취약하다. Phase 0-2에서 WebConfig를 분리할 때 각 모듈별 CORS 정책을 차별화해야 한다.

**수정 제안**: `ApiWebConfig`는 프론트엔드 도메인만, `AdminWebConfig`는 admin 도메인만 허용한다.

---

### 7. [보안] 세션 쿠키 보안 속성 미설정

**발견자**: security-reviewer (HIGH)

`SessionConfig`의 `CookieSerializer`에 `HttpOnly`, `Secure`, `SameSite` 속성이 없다. admin 독립 배포 시 세션 쿠키 보안이 기본값에 의존한다.

**수정 제안**: Phase 3에서 admin 모듈의 `SessionConfig`에 `setUseHttpOnlyCookie(true)`, `setUseSecureCookie(true)`, `setSameSite("Strict")`를 명시한다.

---

### 8. [DB] testFixtures 리소스 classpath 검증 필요

**발견자**: database-reviewer (HIGH)

H2 마이그레이션을 `common/src/testFixtures/resources/db/migration/h2/`에 두는 전략이 실제로 Gradle `testFixtures` jar에 올바르게 포함되는지 파일럿 검증이 필요하다.

**수정 제안**: Phase 1 완료 후 common 모듈 단독 빌드 → testFixtures jar 내부에 `db/migration/h2/` 경로가 포함되는지 확인한다. 문제 시 `common/src/test/resources/`로 대안을 사용한다.

---

### 9. [보안] 환경 변수(.env) 모듈 간 공유

**발견자**: security-reviewer (HIGH)

docker-compose에서 api와 admin이 같은 `.env` 파일을 사용한다. admin 컨테이너에 JWT 시크릿, S3 키 등 불필요한 변수가 노출된다.

**수정 제안**: `.env.common`(DB), `.env.api`(JWT, S3, OAuth), `.env.admin`(ADMIN_INITIAL_*)으로 분리한다.

---

### 10. [롤백] Phase별 롤백 전략 부재

**발견자**: devils-advocate (HIGH)

6개 Phase 중간에 실패 시 되돌리는 전략이 없다. 특히 Phase 5-1의 `src/` 삭제는 비가역적이다.

**수정 제안**: 각 Phase 완료 시 git tag를 생성하고, Phase 5-1은 모든 모듈의 빌드+테스트+bootJar 통과 후에만 실행한다.

---

### 11. [아키텍처] common에 `spring-boot-starter-web` 포함

**발견자**: architecture-reviewer (HIGH), devils-advocate (HIGH)

common은 domain + infrastructure 계층인데 starter-web은 presentation 의존성이다. `ErrorCode`가 `HttpStatus`에 의존하기 때문으로 보인다.

**수정 제안**: 장기적으로 `ErrorCode`에서 `HttpStatus`를 int status code로 대체하여 web 의존성을 제거한다. 단기적으로는 계획에 이 의존 이유를 명시한다.

---

### 12. [계획 누락] 리소스 파일 이동 대상 미기술

**발견자**: devils-advocate (HIGH), devils-advocate (MEDIUM)

다음 리소스 파일의 이동이 계획에 언급되지 않았다:
- `src/main/resources/email/reminder.html` → `api/src/main/resources/email/`
- `src/main/resources/static/admin/css/` → `admin/src/main/resources/static/admin/css/`

**수정 제안**: Phase 4에 리소스 파일 이동 목록을 추가한다.

---

### 13. [배포] 기존 GitHub Actions 워크플로우 이름 충돌

**발견자**: devils-advocate (HIGH)

`admin-ci.yml`과 `admin-cd.yml`이 이미 존재한다(프론트엔드 admin 앱용). 새로 생성하는 `admin-server-ci.yml`과 혼동될 수 있다.

**수정 제안**: 기존 워크플로우 파일 존재를 확인하고, 명확한 네이밍(`backend-admin-ci.yml` 등)을 사용한다.

---

## MEDIUM (개선 권고) — 9건

### 14. [아키텍처] `@EnableJpaAuditing` 중복 선언

**발견자**: architecture-reviewer (HIGH), domain-reviewer (MEDIUM), devils-advocate (SUGGESTION)

api와 admin 양쪽에 선언하지 말고 common에 `@Configuration` + `@EnableJpaAuditing` 클래스를 하나 만들면 누락을 방지할 수 있다.

---

### 15. [아키텍처] common 테스트 전략 미명시

**발견자**: architecture-reviewer (MEDIUM), devils-advocate (MEDIUM)

`@DataJpaTest` Repository 테스트는 common의 코드를 검증하므로 `common/src/test/`에 배치하는 것이 논리적이다. common 변경 시 Repository 쿼리 검증이 common 빌드에서 바로 수행된다.

---

### 16. [DB] admin 전용 Repository 메서드가 common에 포함

**발견자**: database-reviewer (MEDIUM), domain-reviewer (HIGH)

`MomentRepository`, `GroupRepository`에 `// ===== Admin API용 =====`으로 표시된 admin 전용 쿼리가 common에 남는다. 현 단계에서는 유지하되, 향후 admin 전용 Repository 인터페이스 도입을 검토한다.

---

### 17. [보안] Swagger UI 운영 환경 노출

**발견자**: security-reviewer (MEDIUM)

prod 프로필에서 Swagger UI를 비활성화하는 계획이 없다. `springdoc.api-docs.enabled: false`와 `springdoc.swagger-ui.enabled: false`를 prod에 설정하거나, `@Profile("!prod")` 조건을 추가한다.

---

### 18. [보안] Dockerfile non-root 사용자 미설정

**발견자**: security-reviewer (MEDIUM)

컨테이너가 root로 실행된다. Dockerfile에 non-root 사용자 추가 권장:
```dockerfile
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser
```

---

### 19. [DB] Native Query MySQL/H2 호환성

**발견자**: database-reviewer (MEDIUM)

`GroupRepository`의 `DATE()`, `CONCAT` 등 MySQL-specific 표현이 H2 호환 모드에서 다르게 동작할 수 있다. admin 모듈 분리 후 admin 관련 native query를 사용하는 Repository 테스트가 모두 통과하는지 확인 필요.

---

### 20. [아키텍처] 누락된 Gradle 의존성/플러그인

**발견자**: architecture-reviewer (MEDIUM)

- `spring-cloud-aws-starter-metrics`가 api/build.gradle에 누락
- `SonarQube` 플러그인이 계획의 새 build.gradle에 미반영
- `configurations { compileOnly { extendsFrom annotationProcessor } }` 블록 미반영

---

### 21. [도메인] AdminInitializer 테스트 환경 변수

**발견자**: domain-reviewer (MEDIUM)

`AdminInitializer`가 `CommandLineRunner`로 실행되므로 admin `application-test.yml`에 `admin.initial.*` 테스트 값을 반드시 포함해야 한다.

---

### 22. [도메인] AdminFixture가 dead code

**발견자**: domain-reviewer (HIGH)

`AdminFixture`가 테스트 코드 어디에서도 사용되지 않는다(grep 결과 0건). testFixtures 이동 전에 정리 대상으로 검토 필요.

---

## SUGGESTION (참고) — 7건

| # | 영역 | 제안 | 발견자 |
|---|------|------|--------|
| 23 | DB | `flyway_schema_history` 테이블 분리 전략 (향후 admin에 마이그레이션 추가 시) | database |
| 24 | DB | HikariCP 커넥션 풀 총합이 MySQL `max_connections` 이내인지 확인 | database |
| 25 | DB | Dockerfile의 `*.jar` 와일드카드 → plain jar 생성 비활성화(`jar { enabled = false }`) 추가 | database |
| 26 | 아키텍처 | `logback-spring.xml`을 common에 공유하여 중복 방지 | architecture, devils-advocate |
| 27 | 아키텍처 | H2를 `runtimeOnly` → `testRuntimeOnly`로 변경 | architecture |
| 28 | 보안 | GitHub Actions에 Environment-level secrets 사용으로 모듈별 시크릿 스코핑 | security |
| 29 | 전략 | Phase 0만 먼저 수행 후 1개월 운영, 멀티모듈 필요성 재평가 (점진적 접근) | devils-advocate |

---

## GOOD (잘된 점)

| 영역 | 평가 | 합의 |
|------|------|------|
| **동일 패키지 유지** | `moment.*` 패키지 유지로 import 변경 최소화 | 전원 동의 |
| **Phase 0 사전 작업** | 모듈 분리 전 결합도 해소가 위험을 줄임 | 전원 동의 |
| **testFixtures 활용** | `java-test-fixtures` 플러그인으로 공유 픽스쳐 관리 | 전원 동의 |
| **이벤트 격리** | 모든 이벤트 발행/소비가 api 모듈 내에서 완결 | domain, architecture |
| **Flyway 단일 실행** | api만 Flyway 실행, 마이그레이션 충돌 방지 | database, domain |
| **인증 분리** | JWT(api) vs Session(admin) 이미 독립적 | security |
| **의존성 분리** | JWT/S3(api), Session/Thymeleaf(admin) 명확 분리 | architecture, devils-advocate |
| **배포 파이프라인** | Phase 6에서 Docker/CI/CD까지 포괄적 계획 | architecture |

---

## Devils-Advocate 반론에 따른 심각도 조정

| 원래 심각도 | 조정 후 | 근거 |
|------------|---------|------|
| devils-advocate: "멀티모듈 필요성" CRITICAL | → SUGGESTION (#29) | 전략적 결정은 이미 내려졌으며, Profile 기반 대안은 계획의 목표("추후 api 추가 분리를 위한 확장 가능한 구조")를 달성하지 못함 |
| devils-advocate: "Fat Common" CRITICAL | → HIGH (#11에 통합) | common이 비대한 것은 사실이나, 10개 도메인의 엔티티/레포지토리를 공유하는 현 구조에서 불가피. starter-web 제거로 일부 완화 가능 |
| devils-advocate: "Admin DTO → Entity 의존성" CRITICAL | → 해당 없음 | admin response DTO가 common 엔티티를 import하는 것은 `implementation project(':common')` 관계에서 정상적이며, 모듈 분리의 의도된 설계 |
| security-reviewer: "CORS" CRITICAL | → HIGH (#6) | 기존 코드의 문제이며 계획이 도입한 결함이 아님. 단, 분리 시 반드시 수정 필요 |
| security-reviewer: "GlobalExceptionHandler 범위" CRITICAL | → HIGH | 모듈 분리 후 classpath 분리로 자연 해소. Phase 0 체크리스트에 추가 권고 |

---

## 요약

| 등급 | 건수 | 핵심 조치 |
|------|------|----------|
| **CRITICAL** | 5 | Event DTO 위치 명시, Report 이동 범위 정정, V35 H2 마이그레이션 생성, auth infra 분리 기준 명확화, Phase 2~4 빌드 불가 상태 해소 |
| **HIGH** | 8 | CORS 정책 분리, 세션 쿠키 보안, testFixtures 검증, 환경변수 분리, 롤백 전략, starter-web 제거, 리소스 파일 이동, CI 이름 충돌 |
| **MEDIUM** | 9 | JpaAuditing 중복, common 테스트 전략, admin 전용 Repository, Swagger prod, Dockerfile, Native Query, Gradle 누락, AdminInitializer, AdminFixture dead code |
| **SUGGESTION** | 7 | flyway 테이블 분리, HikariCP, plain jar, logback 공유, H2 scope, GitHub secrets, 점진적 접근 |

**전체 평가**: 기술적으로 실행 가능한 잘 설계된 계획이다. CRITICAL 5건은 모두 **계획 문서의 정확성/완전성** 문제이며, 수정이 용이하다. 가장 큰 실행 리스크는 Phase 2~4 진행 중 빌드 불가 상태를 어떻게 관리할지이다. "복사 후 삭제" 전략과 Phase별 git tag 생성을 권고한다.
