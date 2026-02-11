# Phase 3: admin 모듈 코드 이동

> Created: 2026-02-11
> Status: PLANNED
> Phase 2~4 atomic 작업의 일부. 원본 삭제는 Phase 5에서.

## 목적

admin 전용 서비스, 컨트롤러, DTO, 설정을 admin 모듈로 이동한다.
admin의 `domain/`과 `infrastructure/`는 이미 Phase 2에서 common으로 이동됨.

---

## 3-1. admin 패키지 이동

### 대상: `src/main/java/moment/admin/` → `admin/src/main/java/moment/admin/`

`domain/`과 `infrastructure/`를 **제외한** 나머지 전체 이동.

```bash
# 디렉토리 구조 생성
mkdir -p admin/src/main/java/moment/admin/{config,dto/request,dto/response}
mkdir -p admin/src/main/java/moment/admin/global/{config,exception,interceptor,listener,util}
mkdir -p admin/src/main/java/moment/admin/presentation/api
mkdir -p admin/src/main/java/moment/admin/service/{admin,application,content,group,session,user}
```

### 파일별 이동 목록

#### config/

```bash
cp src/main/java/moment/admin/config/AdminInitializer.java admin/src/main/java/moment/admin/config/
```

#### dto/request/ (4개)

```bash
cp src/main/java/moment/admin/dto/request/AdminCreateRequest.java admin/src/main/java/moment/admin/dto/request/
cp src/main/java/moment/admin/dto/request/AdminGroupUpdateRequest.java admin/src/main/java/moment/admin/dto/request/
cp src/main/java/moment/admin/dto/request/AdminLoginRequest.java admin/src/main/java/moment/admin/dto/request/
cp src/main/java/moment/admin/dto/request/AdminUserUpdateRequest.java admin/src/main/java/moment/admin/dto/request/
```

#### dto/response/ (24개)

```bash
cp src/main/java/moment/admin/dto/response/*.java admin/src/main/java/moment/admin/dto/response/
```

전체 목록:
- AdminAccountListResponse, AdminAccountResponse
- AdminCommentAuthorInfo, AdminCommentListResponse, AdminCommentResponse
- AdminErrorResponse, AdminSuccessResponse
- AdminGroupDetailResponse, AdminGroupInviteLinkResponse, AdminGroupListResponse
- AdminGroupLogListResponse, AdminGroupLogResponse
- AdminGroupMemberListResponse, AdminGroupMemberResponse
- AdminGroupOwnerInfo, AdminGroupStatsResponse, AdminGroupSummary
- AdminInviteLinkInfo, AdminLoginResponse
- AdminMemberUserInfo, AdminMeResponse
- AdminMomentAuthorInfo, AdminMomentListResponse, AdminMomentResponse
- AdminResponse, AdminSessionDetailResponse, AdminSessionHistoryResponse
- AdminSessionResponse, AdminUserDetailResponse, AdminUserListResponse, AdminUserResponse

#### global/config/ (SessionConfig + Phase 0에서 생성한 AdminWebConfig, AdminSwaggerConfig)

```bash
cp src/main/java/moment/admin/global/config/SessionConfig.java admin/src/main/java/moment/admin/global/config/
cp src/main/java/moment/admin/global/config/AdminWebConfig.java admin/src/main/java/moment/admin/global/config/
cp src/main/java/moment/admin/global/config/AdminSwaggerConfig.java admin/src/main/java/moment/admin/global/config/
```

#### global/exception/ (2개)

```bash
cp src/main/java/moment/admin/global/exception/AdminErrorCode.java admin/src/main/java/moment/admin/global/exception/
cp src/main/java/moment/admin/global/exception/AdminException.java admin/src/main/java/moment/admin/global/exception/
```

#### global/interceptor/ (1개)

```bash
cp src/main/java/moment/admin/global/interceptor/AdminAuthInterceptor.java admin/src/main/java/moment/admin/global/interceptor/
```

#### global/listener/ (1개)

```bash
cp src/main/java/moment/admin/global/listener/AdminSessionListener.java admin/src/main/java/moment/admin/global/listener/
```

#### global/util/ (3개)

```bash
cp src/main/java/moment/admin/global/util/AdminSessionManager.java admin/src/main/java/moment/admin/global/util/
cp src/main/java/moment/admin/global/util/ClientIpExtractor.java admin/src/main/java/moment/admin/global/util/
cp src/main/java/moment/admin/global/util/UserAgentParser.java admin/src/main/java/moment/admin/global/util/
```

#### presentation/api/ (6개)

```bash
cp src/main/java/moment/admin/presentation/api/AdminApiExceptionHandler.java admin/src/main/java/moment/admin/presentation/api/
cp src/main/java/moment/admin/presentation/api/AdminAccountApiController.java admin/src/main/java/moment/admin/presentation/api/
cp src/main/java/moment/admin/presentation/api/AdminAuthApiController.java admin/src/main/java/moment/admin/presentation/api/
cp src/main/java/moment/admin/presentation/api/AdminGroupApiController.java admin/src/main/java/moment/admin/presentation/api/
cp src/main/java/moment/admin/presentation/api/AdminSessionApiController.java admin/src/main/java/moment/admin/presentation/api/
cp src/main/java/moment/admin/presentation/api/AdminUserApiController.java admin/src/main/java/moment/admin/presentation/api/
```

#### service/ (9개)

```bash
cp src/main/java/moment/admin/service/admin/AdminService.java admin/src/main/java/moment/admin/service/admin/
cp src/main/java/moment/admin/service/application/AdminManagementApplicationService.java admin/src/main/java/moment/admin/service/application/
cp src/main/java/moment/admin/service/content/AdminContentService.java admin/src/main/java/moment/admin/service/content/
cp src/main/java/moment/admin/service/group/AdminGroupLogService.java admin/src/main/java/moment/admin/service/group/
cp src/main/java/moment/admin/service/group/AdminGroupMemberService.java admin/src/main/java/moment/admin/service/group/
cp src/main/java/moment/admin/service/group/AdminGroupQueryService.java admin/src/main/java/moment/admin/service/group/
cp src/main/java/moment/admin/service/group/AdminGroupService.java admin/src/main/java/moment/admin/service/group/
cp src/main/java/moment/admin/service/session/AdminSessionService.java admin/src/main/java/moment/admin/service/session/
cp src/main/java/moment/admin/service/user/AdminUserService.java admin/src/main/java/moment/admin/service/user/
```

---

## 3-2. SessionConfig 세션 쿠키 보안 강화

### 현재 상태

```java
@Bean
public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName(sessionCookieName);
    return serializer;
}
```

### 변경 후 (admin/src/main/java/moment/admin/global/config/SessionConfig.java)

```java
@Bean
public CookieSerializer cookieSerializer() {
    DefaultCookieSerializer serializer = new DefaultCookieSerializer();
    serializer.setCookieName(sessionCookieName);
    serializer.setUseHttpOnlyCookie(true);
    serializer.setUseSecureCookie(true);
    serializer.setSameSite("Strict");
    return serializer;
}
```

### 보안 설명

| 속성 | 값 | 효과 |
|------|-----|------|
| `HttpOnly` | `true` | JavaScript에서 쿠키 접근 불가 (XSS 방어) |
| `Secure` | `true` | HTTPS에서만 쿠키 전송 |
| `SameSite` | `Strict` | 크로스 사이트 요청 시 쿠키 미전송 (CSRF 방어) |

---

## 3-3. AdminApplication 생성

### 파일: `admin/src/main/java/moment/AdminApplication.java`

```java
package moment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

### 설명

- `@EnableJpaAuditing`: common의 `JpaAuditingConfig`에서 처리 (중복 선언 불필요)
- `@EnableAsync`: admin은 비동기 이벤트 핸들러가 없으므로 불필요
- `@EnableScheduling`: AdminSessionService에서 만료 세션 정리 스케줄러 사용 시 필요
- 패키지 `moment`: common + admin 패키지 모두 스캔

---

## 3-4. admin 설정 파일 생성

### `admin/src/main/resources/application-dev.yml`

```yaml
spring:
  application:
    name: moment-admin

  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        show_sql: false
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
    open-in-view: false

  flyway:
    enabled: false  # admin은 마이그레이션 미실행 (api가 담당)

  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never
      table-name: SPRING_SESSION
    timeout: ${ADMIN_SESSION_TIMEOUT:3600s}

server:
  port: 8081
  forward-headers-strategy: native

admin:
  initial:
    email: ${ADMIN_INITIAL_EMAIL}
    password: ${ADMIN_INITIAL_PASSWORD}
    name: ${ADMIN_INITIAL_NAME}
  session:
    timeout: ${ADMIN_SESSION_TIMEOUT}
    cookie-name: SESSION
```

### `admin/src/main/resources/application-prod.yml`

```yaml
spring:
  application:
    name: moment-admin

  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        show_sql: false
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
    open-in-view: false

  flyway:
    enabled: false

  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never
      table-name: SPRING_SESSION
    timeout: ${ADMIN_SESSION_TIMEOUT:3600s}

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false

server:
  port: 8081
  forward-headers-strategy: native

admin:
  initial:
    email: ${ADMIN_INITIAL_EMAIL}
    password: ${ADMIN_INITIAL_PASSWORD}
    name: ${ADMIN_INITIAL_NAME}
  session:
    timeout: ${ADMIN_SESSION_TIMEOUT}
    cookie-name: SESSION
```

### `admin/src/test/resources/application-test.yml`

```yaml
spring:
  application:
    name: moment-admin

  main:
    allow-bean-definition-overriding: true

  datasource:
    url: jdbc:h2:mem:admin-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE

  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        show_sql: false
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
    open-in-view: false

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration/h2

  session:
    store-type: jdbc
    jdbc:
      initialize-schema: never
      table-name: SPRING_SESSION
    timeout: ${admin.session.timeout:3600s}

  h2:
    console:
      enabled: true

admin:
  initial:
    email: test-admin@moment.com
    password: test-password-123!
    name: test-admin
  session:
    timeout: 3600
    cookie-name: SESSION
```

---

## 3-5. admin 리소스 파일 이동

현재 `src/main/resources/static/admin/css/` 디렉토리가 비어있음 (파일 없음).
향후 admin 정적 파일이 추가되면 `admin/src/main/resources/static/admin/css/`로 배치.

### logback-spring.xml 복사

```bash
cp src/main/resources/logback-spring.xml admin/src/main/resources/logback-spring.xml
```

---

## 3-6. admin 테스트 이동

### 테스트 코드 이동

```bash
# 디렉토리 구조 생성 (기존 테스트 구조 미러링)
mkdir -p admin/src/test/java/moment/admin/{domain,global/util,presentation/api}
mkdir -p admin/src/test/java/moment/admin/service/{admin,session,user,group}
mkdir -p admin/src/test/java/moment/admin/service  # AdminContentServiceTest
mkdir -p admin/src/test/java/moment/fixture

# 도메인 단위 테스트
cp src/test/java/moment/admin/domain/AdminTest.java admin/src/test/java/moment/admin/domain/
cp src/test/java/moment/admin/domain/AdminGroupLogTest.java admin/src/test/java/moment/admin/domain/

# 유틸 테스트
cp src/test/java/moment/admin/global/util/AdminSessionManagerTest.java admin/src/test/java/moment/admin/global/util/
cp src/test/java/moment/admin/global/util/ClientIpExtractorTest.java admin/src/test/java/moment/admin/global/util/
cp src/test/java/moment/admin/global/util/UserAgentParserTest.java admin/src/test/java/moment/admin/global/util/

# E2E 테스트 (presentation/api/)
cp src/test/java/moment/admin/presentation/api/AdminCommentDeleteApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminCommentListApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupDeleteApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupDetailApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupListApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupLogApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupMemberApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupRestoreApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupStatsApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminGroupUpdateApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminInviteLinkApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminMemberApproveApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminMemberKickApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminMemberRejectApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminMomentDeleteApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminMomentListApiTest.java admin/src/test/java/moment/admin/presentation/api/
cp src/test/java/moment/admin/presentation/api/AdminOwnershipTransferApiTest.java admin/src/test/java/moment/admin/presentation/api/

# 서비스 테스트
cp src/test/java/moment/admin/service/admin/AdminServiceTest.java admin/src/test/java/moment/admin/service/admin/
cp src/test/java/moment/admin/service/AdminContentServiceTest.java admin/src/test/java/moment/admin/service/
cp src/test/java/moment/admin/service/AdminGroupLogServiceTest.java admin/src/test/java/moment/admin/service/
cp src/test/java/moment/admin/service/AdminGroupMemberServiceTest.java admin/src/test/java/moment/admin/service/
cp src/test/java/moment/admin/service/AdminGroupServiceTest.java admin/src/test/java/moment/admin/service/
cp src/test/java/moment/admin/service/session/AdminSessionServiceTest.java admin/src/test/java/moment/admin/service/session/
cp src/test/java/moment/admin/service/user/AdminUserServiceTest.java admin/src/test/java/moment/admin/service/user/

# DTO 의존 픽스쳐 (Phase 0에서 분리)
cp src/test/java/moment/fixture/AdminRequestFixture.java admin/src/test/java/moment/fixture/
```

---

## 3-7. Phase 3 이동 파일 요약

### admin/src/main/java/moment/admin/ (약 51개 파일)

| 카테고리 | 파일 수 |
|----------|---------|
| config/ | 1 |
| dto/request/ | 4 |
| dto/response/ | ~24 |
| global/config/ | 3 |
| global/exception/ | 2 |
| global/interceptor/ | 1 |
| global/listener/ | 1 |
| global/util/ | 3 |
| presentation/api/ | 6 |
| service/ | 9 |

### admin/src/main/java/moment/ (1개)

- `AdminApplication.java` (신규)

### admin/src/main/resources/ (3개)

- `application-dev.yml` (신규)
- `application-prod.yml` (신규)
- `logback-spring.xml` (복사)

### admin/src/test/java/moment/admin/ (약 24개 파일)

### admin/src/test/java/moment/fixture/ (1개)

- `AdminRequestFixture.java`

### admin/src/test/resources/ (1개)

- `application-test.yml` (신규)
