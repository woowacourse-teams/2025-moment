# 멀티모듈 분리 코드 리뷰 보고서

> **대상**: `refactor/#1087` 브랜치 (구현 완료된 코드)
> **Base**: `main`
> **리뷰어**: architecture-reviewer, code-quality-reviewer, security-reviewer, database-reviewer, test-reviewer, devils-advocate
> **날짜**: 2026-02-11
> **상태**: REQUEST_CHANGES

---

## 개요

| 항목 | 내용 |
|------|------|
| 커밋 수 | 5개 |
| 변경 파일 | 553개 (+5,423/-633) |
| 변경 도메인 | common, admin, api (전체 모듈 구조 개편) |
| 팀 규모 | 6명 |
| 주요 변경 | Gradle 멀티모듈 마이그레이션 (monolith → common/admin/api) + UserBlockRepository 버그 수정 |

### 커밋 이력

| 커밋 | 설명 |
|------|------|
| `5b687ee3` | [FIX] existsBidirectionalBlock ClassCastException 해결 및 테스트 정책 강화 |
| `9784ba8a` | refactor: Phase 0 - 모듈 분리 사전 작업 (결합도 해소) |
| `4feb4695` | refactor: Phase 1 - Gradle 멀티모듈 구조 설정 |
| `84bba1b8` | refactor: Phase 2~4 - 코드 모듈 분리 (common/admin/api) |
| `d1d3baaf` | refactor: Phase 5 - 원본 src/ 삭제 및 루트 프로젝트 정리 |

---

## Summary

| 등급 | 건수 | 발견자 |
|------|------|--------|
| CRITICAL | 4 | architecture, code-quality, test |
| HIGH | 2 | database, test |
| MEDIUM | 10 | architecture, code-quality, security, database, test |

## Verdict: REQUEST CHANGES

> CRITICAL 4건, HIGH 2건 존재. 핵심 이슈 해결 후 머지 권고.

---

## CRITICAL (반드시 수정) — 4건

### 1. @EnableJpaAuditing 중복 선언

**발견자**: architecture-reviewer, code-quality-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/main/java/moment/ApiApplication.java:9`, `common/src/main/java/moment/global/config/JpaAuditingConfig.java:7`
- **문제**: `@EnableJpaAuditing`이 두 곳에 동시 선언. 계획 문서에서 "ApiApplication에서 제거"라고 명시했으나 미수행
- **영향**: `BeanDefinitionOverrideException` 가능성, 모듈 간 설정 방식 불일치
- **수정**: `ApiApplication.java`에서 `@EnableJpaAuditing` 제거

```java
// ApiApplication.java - @EnableJpaAuditing 제거
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class ApiApplication { ... }
```

---

### 2. API 모듈에 Admin 전용 설정 잔존

**발견자**: architecture-reviewer, security-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/main/resources/application-dev.yml:31-37,122-129`, `api/src/main/resources/application-prod.yml:39-44,151-158`
- **문제**: `spring.session.*`, `admin.initial.*`, `admin.session.*` 등 admin 전용 설정이 API 모듈에 잔존
- **영향**: API 모듈에 불필요한 admin 환경변수 결합, 최소 권한 원칙 위반
- **수정**: api 모듈의 `application-dev.yml`과 `application-prod.yml`에서 해당 섹션 완전 제거

---

### 3. Mock 정책 위반 — UserBlockServiceTest

**발견자**: test-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/test/java/moment/block/service/block/UserBlockServiceTest.java:32`
- **문제**: `UserBlockRepository`를 `@Mock`으로 처리. JPQL 문법 오류 검증 불가
- **영향**: `existsBidirectionalBlock` 쿼리가 native SQL에서 JPQL로 변경되었는데, Mock 사용 시 새 JPQL의 정확성 검증 불가
- **수정**: `@SpringBootTest(webEnvironment = NONE)` + `@Transactional` + 실제 Repository 사용으로 전환

---

### 4. Mock 정책 위반 — UserBlockApplicationServiceTest

**발견자**: test-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/test/java/moment/block/service/application/UserBlockApplicationServiceTest.java:29-31`
- **문제**: `UserBlockService`, `UserService`를 `@Mock`으로 처리. 오케스트레이션 로직 검증 불가
- **수정**: 위와 동일하게 통합 테스트로 전환

---

## HIGH (강력 권고) — 2건

### 5. 데드 참조 — test-users.sql

**발견자**: database-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/main/resources/application-dev.yml:41`
- **문제**: `classpath:sql/test-users.sql` 참조하나 해당 파일 미존재
- **영향**: dev 환경 기동 시 SQL init 에러로 **애플리케이션 시작 실패**
- **수정**: `sql.init` 블록 제거 또는 `test-users.sql` 파일 복원

```yaml
# application-dev.yml 에서 아래 블록 제거 또는 파일 복원 필요
  sql:
    init:
      mode: always
      data-locations: classpath:sql/test-users.sql
```

---

### 6. findBlockedUserIds soft delete 테스트 누락

**발견자**: test-reviewer | **반론 판정**: AGREE

- **파일**: `api/src/test/java/moment/block/infrastructure/UserBlockRepositoryTest.java:117-131`
- **문제**: `findBlockedUserIds`는 native SQL에서 `deleted_at IS NULL` 수동 필터링하는데, soft delete 시나리오 미검증
- **수정**: soft delete된 차단이 결과에서 제외되는지 검증하는 테스트 추가

```java
@Test
void soft_delete된_차단은_차단_목록에서_제외된다() {
    // given
    UserBlock block = userBlockRepository.save(new UserBlock(userA, userB));
    userBlockRepository.delete(block);
    entityManager.flush();
    entityManager.clear();

    // when
    List<Long> blockedUserIds = userBlockRepository.findBlockedUserIds(userA.getId());

    // then
    assertThat(blockedUserIds).isEmpty();
}
```

---

## MEDIUM (개선 권고) — 10건

| # | 이슈 | 발견자 | 원본 등급 | 조정 사유 |
|---|------|--------|----------|----------|
| 7 | ErrorCode에 Admin 에러 코드(A-001~A-009) 포함 | architecture | HIGH→MEDIUM | 기존 구조, 후속 정리 대상 |
| 8 | Expo Push 외부 API 클라이언트가 common 위치 | architecture | HIGH→MEDIUM | 의도적 트레이드오프, 후속 리팩토링 대상 |
| 9 | common의 spring-boot-starter-web 의존성 | architecture | HIGH→MEDIUM | 이미 인지/문서화된 기술 부채 |
| 10 | Tidy First 위반 (커밋 5b687ee3 혼합) | code-quality | HIGH→MEDIUM | 이미 커밋됨, 향후 참고 |
| 11 | allow-bean-definition-overriding: true | code-quality | MEDIUM | Bean 충돌 문제 은폐 가능 |
| 12 | Admin 세션 쿠키 secure/sameSite 미설정 | security | HIGH→MEDIUM | 기존 이슈, PR 범위 밖 |
| 13 | @Valid 누락 6건 | security | HIGH→MEDIUM | 기존 이슈, 별도 수정 권장 |
| 14 | Flyway 경로 공유 (admin testFixtures 확인) | database | MEDIUM | 실제 테스트 실행으로 검증 필요 |
| 15 | findByBlockerAndBlockedUserIncludeDeleted 활성 차단 테스트 | test | HIGH→MEDIUM | 핵심 기능은 이미 커버 |
| 16 | 테스트 태그(@Tag) 누락 (2개 테스트 클래스) | test | MEDIUM | fastTest/e2eTest 분류 누락 가능 |

### 상세 설명

**#7. ErrorCode에 Admin 에러 코드 포함**
- `common/src/main/java/moment/global/exception/ErrorCode.java:55-63`에 A-001~A-009 admin 전용 에러 코드
- admin 모듈에 별도 `AdminErrorCode` 존재하므로 common에서 제거 가능
- 후속 작업으로 처리 권장

**#8. Expo Push 외부 API 클라이언트가 common 위치**
- `common/src/main/java/moment/notification/infrastructure/expo/` 하위
- notification 도메인 전체가 common에 위치하여 자연스러운 배치이나, 외부 API는 api 모듈이 적절
- admin이 Push를 사용하지 않으므로 api로 이동 가능

**#9. common의 spring-boot-starter-web 의존성**
- `common/build.gradle:19` — ErrorCode가 HttpStatus에 의존하여 필요
- 코드 주석에 장기적 제거 계획 명시됨

**#10. Tidy First 위반**
- 커밋 `5b687ee3`에서 버그 수정 + 문서/테스트 정책 변경 혼합
- PR #1086에서 이미 머지된 내용이므로 현 PR에서 재분리 불필요

**#11. allow-bean-definition-overriding: true**
- `admin/src/test/resources/application-test.yml:6`, `api/src/test/resources/application-test.yml:8`
- 모듈 분리 완료 후 이 설정 제거 가능 여부 확인 필요

**#12. Admin 세션 쿠키 보안 속성**
- `admin/src/main/java/moment/admin/global/config/SessionConfig.java`
- `DefaultCookieSerializer` 기본값으로 httpOnly=true이나, secure/sameSite는 별도 설정 필요
- 이 PR 범위 밖 기존 이슈

**#13. @Valid 누락**
- `AuthController` (PasswordUpdateRequest), `NotificationController`, `PushNotificationController`, `ReportController` 등 6건
- 모든 `@RequestBody` 앞에 `@Valid` 추가 필요
- 이 PR 범위 밖 기존 이슈

**#14. Flyway 경로 공유**
- admin 테스트에서 `flyway.locations: classpath:db/migration/h2` 사용
- H2 마이그레이션이 common testFixtures에 위치 → admin build.gradle의 `testFixtures(project(':common'))` 의존으로 동작해야 하나 실제 검증 필요

**#15. findByBlockerAndBlockedUserIncludeDeleted 활성 차단 테스트**
- 삭제된 차단 조회 테스트는 존재하나, 활성(미삭제) 차단 조회 테스트 누락
- 핵심 기능(삭제 포함 조회)은 이미 커버되어 있으므로 MEDIUM

**#16. 테스트 태그 누락**
- `UserBlockServiceTest`, `UserBlockApplicationServiceTest`에 `@Tag(TestTags.UNIT)` 또는 `@Tag(TestTags.INTEGRATION)` 미부여
- `fastTest`/`e2eTest` Gradle task 분류에서 누락 가능

---

## SUGGESTION (참고)

- Logback 설정이 api에만 존재 (admin은 기본 Spring Boot 로깅)
- CORS `allowedOriginPatterns("*")` — 프로덕션에서 구체적 origin 지정 권장
- `UserBlockRepository.findAllByBlocker` 미사용 메서드 제거 검토
- `UserBlockRepositoryTest` Nested 클래스명 한글화 가능
- Actuator `show-details: always` 프로덕션 설정 주의

---

## GOOD (잘된 점)

| 영역 | 평가 |
|------|------|
| **모듈 간 의존성 방향** | `common ← admin`, `common ← api` 단방향. 순환 의존 없음 |
| **testFixtures 활용** | `java-test-fixtures` 플러그인으로 Fixture 중앙 관리 |
| **Config 분리** | 각 모듈에 적절하게 SwaggerConfig/WebConfig 배치 |
| **커밋 구조** | Phase 0→1→2~4→5 체계적 단계 분리 |
| **Dead Code 완전 제거** | 원본 `src/` 디렉토리 잔존 파일 없음 |
| **버그 수정 정확성** | `existsBidirectionalBlock` JPQL 전환으로 ClassCastException 올바르게 해결 |
| **Repository 테스트 품질** | `@DataJpaTest` + 다양한 시나리오 커버 |
| **인증/인가 분리** | JWT(api) / Session(admin) 올바르게 모듈별 분리 |
| **환경변수 보안** | 모든 민감 정보 `${ENV_VAR}` 형태, 하드코딩 없음 |
| **Flyway 무결성** | 마이그레이션 파일 내용 변경 없이 경로만 이동 (체크섬 안전) |
| **Gradle 구조** | `subprojects` 공통 관리 + `java-test-fixtures` 공유 깔끔 |
| **Fixture 분리** | UserFixture(common) / UserRequestFixture(api) / AdminRequestFixture(admin) 적절 분리 |

---

## 반론 검증 결과 (devils-advocate)

| 원본 이슈 | 원본 등급 | 판정 | 조정 등급 | 사유 요약 |
|-----------|----------|------|----------|----------|
| @EnableJpaAuditing 중복 | CRITICAL | AGREE | CRITICAL | 계획 문서 미수행 |
| Admin 설정 잔존 | CRITICAL | AGREE | CRITICAL | 불필요한 결합 |
| Mock 위반 (2건) | CRITICAL | AGREE | CRITICAL | 정책 일관성 |
| test-users.sql 데드 참조 | HIGH | AGREE | HIGH | 런타임 실패 가능 |
| findBlockedUserIds soft delete | HIGH | AGREE | HIGH | native 쿼리 검증 필요 |
| ErrorCode Admin 코드 | HIGH | DOWNGRADE | MEDIUM | 후속 정리 대상 |
| Expo Push common 위치 | HIGH | DOWNGRADE | MEDIUM | 의도적 배치 |
| GlobalExceptionHandler 스코프 | HIGH | DISMISS | — | 별도 Application |
| starter-web 의존 | HIGH | DOWNGRADE | MEDIUM | 문서화된 부채 |
| Tidy First (5b687ee3) | HIGH | DOWNGRADE | MEDIUM | 이미 커밋됨 |
| 세션 쿠키 보안 | HIGH | DOWNGRADE | MEDIUM | 기존 이슈 |
| @Valid 누락 | HIGH | DOWNGRADE | MEDIUM | 기존 이슈 |
| findAllByBlocker 커버리지 | HIGH | DOWNGRADE | LOW | 미사용 메서드 |
| 인덱스 부재 | HIGH | DOWNGRADE | LOW | 현재 데이터량 충분 |
| findByBlockerAndBlockedUser edge | HIGH | DOWNGRADE | MEDIUM | 핵심 이미 커버 |

### 기각된 이슈 (7건, 참고용)

- **GlobalExceptionHandler 스코프** (architecture, HIGH) — 별도 Application으로 실행되어 basePackages 지정 불필요
- **Flyway 마이그레이션 단일 소스 문서화** (architecture, MEDIUM) — 의도적 전략
- **Phase 0 Tidy First 커밋 혼합** (code-quality, MEDIUM) — SuccessResponse→AdminSuccessResponse는 모듈 분리 사전 작업
- **Bean 이름 비대칭** (code-quality, MEDIUM) — 별도 Application이므로 충돌 없음
- **CORS 와일드카드** (security, MEDIUM) — 기존 이슈, PR 범위 밖
- **Google OAuth state 파라미터** (security, MEDIUM) — 기존 이슈, PR 범위 밖
- **네이티브 쿼리 @SQLRestriction bypass 주석** (database, MEDIUM) — 컨벤션 수준 제안
- **admin 테스트 session.initialize-schema 불일치** (test, MEDIUM) — 의도적 차이 (admin은 Session JDBC 사용)

---

## 권장 조치 순서

| 순서 | 작업 | 예상 시간 |
|------|------|----------|
| 1 | `ApiApplication.java`에서 `@EnableJpaAuditing` 제거 | 1분 |
| 2 | API 모듈 yml에서 admin 전용 설정 제거 | 5분 |
| 3 | `application-dev.yml`에서 `sql.init` 블록 제거 또는 파일 복원 | 2분 |
| 4 | `findBlockedUserIds` soft delete 테스트 추가 | 10분 |
| 5 | `UserBlockServiceTest` / `UserBlockApplicationServiceTest` 통합 테스트 전환 | 30분 (별도 커밋 가능) |
