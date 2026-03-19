# Phase 4: api 모듈 코드 이동

> Created: 2026-02-11
> Status: PLANNED
> Phase 2~4 atomic 작업의 마지막 단계. 원본 삭제는 Phase 5에서.

## 목적

사용자 API의 서비스, 컨트롤러, DTO, 이벤트, 설정을 api 모듈로 이동한다.
각 도메인의 `domain/`과 `infrastructure/`는 이미 Phase 2에서 common으로 이동됨.

---

## 4-1. 각 도메인의 서비스/프레젠테이션/DTO 이동

### auth (서비스 + 프레젠테이션 + DTO + api 전용 infrastructure)

```bash
mkdir -p api/src/main/java/moment/auth/{application,dto/request,dto/response,dto/apple,dto/google,infrastructure,presentation}

# application/ (7개)
cp src/main/java/moment/auth/application/AppleAuthService.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/AuthEmailService.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/AuthService.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/EmailService.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/GoogleAuthService.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/TokenManager.java api/src/main/java/moment/auth/application/
cp src/main/java/moment/auth/application/TokensIssuer.java api/src/main/java/moment/auth/application/

# dto/ (apple, google, request, response)
cp src/main/java/moment/auth/dto/apple/*.java api/src/main/java/moment/auth/dto/apple/
cp src/main/java/moment/auth/dto/google/*.java api/src/main/java/moment/auth/dto/google/
cp src/main/java/moment/auth/dto/request/*.java api/src/main/java/moment/auth/dto/request/
cp src/main/java/moment/auth/dto/response/*.java api/src/main/java/moment/auth/dto/response/

# api 전용 infrastructure (jjwt/OAuth 의존)
cp src/main/java/moment/auth/infrastructure/JwtTokenManager.java api/src/main/java/moment/auth/infrastructure/
cp src/main/java/moment/auth/infrastructure/AppleAuthClient.java api/src/main/java/moment/auth/infrastructure/
cp src/main/java/moment/auth/infrastructure/GoogleAuthClient.java api/src/main/java/moment/auth/infrastructure/

# presentation/ (3개)
cp src/main/java/moment/auth/presentation/AuthController.java api/src/main/java/moment/auth/presentation/
cp src/main/java/moment/auth/presentation/AuthenticationPrincipal.java api/src/main/java/moment/auth/presentation/
cp src/main/java/moment/auth/presentation/LoginUserArgumentResolver.java api/src/main/java/moment/auth/presentation/
```

### user

```bash
mkdir -p api/src/main/java/moment/user/{service/user,service/application,service/facade,presentation,dto/request,dto/response}

cp src/main/java/moment/user/service/user/*.java api/src/main/java/moment/user/service/user/
cp src/main/java/moment/user/service/application/*.java api/src/main/java/moment/user/service/application/
cp src/main/java/moment/user/service/facade/*.java api/src/main/java/moment/user/service/facade/
cp src/main/java/moment/user/presentation/*.java api/src/main/java/moment/user/presentation/
cp src/main/java/moment/user/dto/request/*.java api/src/main/java/moment/user/dto/request/
cp src/main/java/moment/user/dto/response/*.java api/src/main/java/moment/user/dto/response/
```

### moment

```bash
mkdir -p api/src/main/java/moment/moment/{service/moment,service/application,service/facade,presentation,dto/request,dto/response,dto/response/tobe}

cp src/main/java/moment/moment/service/moment/*.java api/src/main/java/moment/moment/service/moment/
cp src/main/java/moment/moment/service/application/*.java api/src/main/java/moment/moment/service/application/
cp src/main/java/moment/moment/service/facade/*.java api/src/main/java/moment/moment/service/facade/
cp src/main/java/moment/moment/presentation/*.java api/src/main/java/moment/moment/presentation/
cp src/main/java/moment/moment/dto/request/*.java api/src/main/java/moment/moment/dto/request/
cp src/main/java/moment/moment/dto/response/*.java api/src/main/java/moment/moment/dto/response/
cp src/main/java/moment/moment/dto/response/tobe/*.java api/src/main/java/moment/moment/dto/response/tobe/
```

### comment

```bash
mkdir -p api/src/main/java/moment/comment/{service/comment,service/application,service/facade,presentation,dto/request,dto/response,dto/response/tobe,dto/event}

cp src/main/java/moment/comment/service/comment/*.java api/src/main/java/moment/comment/service/comment/
cp src/main/java/moment/comment/service/application/*.java api/src/main/java/moment/comment/service/application/
cp src/main/java/moment/comment/service/facade/*.java api/src/main/java/moment/comment/service/facade/
cp src/main/java/moment/comment/presentation/*.java api/src/main/java/moment/comment/presentation/
cp src/main/java/moment/comment/dto/request/*.java api/src/main/java/moment/comment/dto/request/
cp src/main/java/moment/comment/dto/response/*.java api/src/main/java/moment/comment/dto/response/
# dto 직속 파일 (이벤트)
cp src/main/java/moment/comment/dto/CommentCreateEvent.java api/src/main/java/moment/comment/dto/
# dto/event/ (그룹 관련 이벤트)
cp src/main/java/moment/comment/dto/event/*.java api/src/main/java/moment/comment/dto/event/
# dto/tobe (있는 경우)
find src/main/java/moment/comment/dto -name "*.java" -not -path "*/request/*" -not -path "*/response/*" -not -path "*/event/*" | head
```

### group

```bash
mkdir -p api/src/main/java/moment/group/{service/group,service/application,service/invite,presentation,dto/request,dto/response,dto/event}

cp src/main/java/moment/group/service/group/*.java api/src/main/java/moment/group/service/group/
cp src/main/java/moment/group/service/application/*.java api/src/main/java/moment/group/service/application/
cp src/main/java/moment/group/service/invite/*.java api/src/main/java/moment/group/service/invite/
cp src/main/java/moment/group/presentation/*.java api/src/main/java/moment/group/presentation/
cp src/main/java/moment/group/dto/request/*.java api/src/main/java/moment/group/dto/request/
cp src/main/java/moment/group/dto/response/*.java api/src/main/java/moment/group/dto/response/
cp src/main/java/moment/group/dto/event/*.java api/src/main/java/moment/group/dto/event/
```

### like

```bash
mkdir -p api/src/main/java/moment/like/{service,presentation,dto/event}

cp src/main/java/moment/like/service/*.java api/src/main/java/moment/like/service/
cp src/main/java/moment/like/presentation/*.java api/src/main/java/moment/like/presentation/ 2>/dev/null
cp src/main/java/moment/like/dto/event/*.java api/src/main/java/moment/like/dto/event/
```

> like 도메인에는 presentation이 없을 수 있음 (moment/comment 컨트롤러에 통합).
> 파일 존재 여부 확인 필요.

### notification

```bash
mkdir -p api/src/main/java/moment/notification/{service/notification,service/application,service/facade,service/eventHandler,presentation,dto/request,dto/response}

cp src/main/java/moment/notification/service/notification/*.java api/src/main/java/moment/notification/service/notification/
cp src/main/java/moment/notification/service/application/*.java api/src/main/java/moment/notification/service/application/
cp src/main/java/moment/notification/service/facade/*.java api/src/main/java/moment/notification/service/facade/
cp src/main/java/moment/notification/service/eventHandler/*.java api/src/main/java/moment/notification/service/eventHandler/
cp src/main/java/moment/notification/presentation/*.java api/src/main/java/moment/notification/presentation/
cp src/main/java/moment/notification/dto/request/*.java api/src/main/java/moment/notification/dto/request/ 2>/dev/null
cp src/main/java/moment/notification/dto/response/*.java api/src/main/java/moment/notification/dto/response/
```

### report

```bash
mkdir -p api/src/main/java/moment/report/{application,presentation,dto/request,dto/response}

cp src/main/java/moment/report/application/*.java api/src/main/java/moment/report/application/
cp src/main/java/moment/report/presentation/*.java api/src/main/java/moment/report/presentation/
cp src/main/java/moment/report/dto/request/*.java api/src/main/java/moment/report/dto/request/ 2>/dev/null
cp src/main/java/moment/report/dto/response/*.java api/src/main/java/moment/report/dto/response/ 2>/dev/null
```

### storage (전체 — JPA 엔티티 없음)

```bash
mkdir -p api/src/main/java/moment/storage/{application,infrastructure,presentation,dto/request,dto/response}

cp src/main/java/moment/storage/application/*.java api/src/main/java/moment/storage/application/
cp src/main/java/moment/storage/infrastructure/*.java api/src/main/java/moment/storage/infrastructure/
cp src/main/java/moment/storage/presentation/*.java api/src/main/java/moment/storage/presentation/
cp src/main/java/moment/storage/dto/request/*.java api/src/main/java/moment/storage/dto/request/
cp src/main/java/moment/storage/dto/response/*.java api/src/main/java/moment/storage/dto/response/
```

### block

```bash
mkdir -p api/src/main/java/moment/block/{service/block,service/application,presentation,dto/request,dto/response}

cp src/main/java/moment/block/service/block/*.java api/src/main/java/moment/block/service/block/ 2>/dev/null
cp src/main/java/moment/block/service/application/*.java api/src/main/java/moment/block/service/application/ 2>/dev/null
cp src/main/java/moment/block/service/*.java api/src/main/java/moment/block/service/ 2>/dev/null
cp src/main/java/moment/block/presentation/*.java api/src/main/java/moment/block/presentation/
cp src/main/java/moment/block/dto/request/*.java api/src/main/java/moment/block/dto/request/ 2>/dev/null
cp src/main/java/moment/block/dto/response/*.java api/src/main/java/moment/block/dto/response/ 2>/dev/null
```

---

## 4-2. 이벤트 DTO 이동 확인

Phase 4-1에서 각 도메인의 dto/ 이동 시 이벤트 레코드도 함께 이동됨. 확인 목록:

| 이벤트 | 원본 위치 | api 위치 |
|--------|-----------|----------|
| `CommentCreateEvent` | `comment/dto/CommentCreateEvent.java` | `api: comment/dto/CommentCreateEvent.java` |
| `GroupCommentCreateEvent` | `comment/dto/event/GroupCommentCreateEvent.java` | `api: comment/dto/event/GroupCommentCreateEvent.java` |
| `GroupJoinRequestEvent` | `group/dto/event/GroupJoinRequestEvent.java` | `api: group/dto/event/GroupJoinRequestEvent.java` |
| `GroupJoinApprovedEvent` | `group/dto/event/GroupJoinApprovedEvent.java` | `api: group/dto/event/GroupJoinApprovedEvent.java` |
| `GroupKickedEvent` | `group/dto/event/GroupKickedEvent.java` | `api: group/dto/event/GroupKickedEvent.java` |
| `MomentLikeEvent` | `like/dto/event/MomentLikeEvent.java` | `api: like/dto/event/MomentLikeEvent.java` |
| `CommentLikeEvent` | `like/dto/event/CommentLikeEvent.java` | `api: like/dto/event/CommentLikeEvent.java` |

---

## 4-3. api 전용 글로벌 파일 이동

```bash
mkdir -p api/src/main/java/moment/global/{dto/response,exception,config,presentation}

# DTO
cp src/main/java/moment/global/dto/response/SuccessResponse.java api/src/main/java/moment/global/dto/response/
cp src/main/java/moment/global/dto/response/ErrorResponse.java api/src/main/java/moment/global/dto/response/

# Exception Handler
cp src/main/java/moment/global/exception/GlobalExceptionHandler.java api/src/main/java/moment/global/exception/

# Config (Phase 0에서 생성한 것들)
cp src/main/java/moment/global/config/ApiWebConfig.java api/src/main/java/moment/global/config/
cp src/main/java/moment/global/config/ApiSwaggerConfig.java api/src/main/java/moment/global/config/
cp src/main/java/moment/global/config/CacheConfig.java api/src/main/java/moment/global/config/
cp src/main/java/moment/global/config/MailConfig.java api/src/main/java/moment/global/config/
cp src/main/java/moment/global/config/RestTemplateConfig.java api/src/main/java/moment/global/config/

# Health Check
cp src/main/java/moment/global/presentation/HealthCheckController.java api/src/main/java/moment/global/presentation/
```

---

## 4-4. ApiApplication 생성

### 파일: `api/src/main/java/moment/ApiApplication.java`

```java
package moment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

### 설명

- `@EnableJpaAuditing`: common의 `JpaAuditingConfig`에서 처리
- `@EnableAsync`: 비동기 이벤트 핸들러(NotificationEventHandler) 필요
- `@EnableScheduling`: 리마인더 스케줄러 등
- 패키지 `moment`: common + api 패키지 모두 스캔

---

## 4-5. api 설정 파일 이동/생성

### `api/src/main/resources/application-dev.yml`

기존 `src/main/resources/application-dev.yml`을 복사 후 **admin 관련 설정 제거**:

```bash
cp src/main/resources/application-dev.yml api/src/main/resources/application-dev.yml
```

**제거할 설정**:
- `admin.initial.*` (email, password, name)
- `admin.session.*` (timeout, cookie-name)
- `spring.session.jdbc.*` 전체 (api는 JWT 기반, 세션 불필요)
- `server.servlet.session.*`

### `api/src/main/resources/application-prod.yml`

기존 `src/main/resources/application-prod.yml`을 복사 후 admin 설정 제거 + Swagger 비활성화 추가:

```bash
cp src/main/resources/application-prod.yml api/src/main/resources/application-prod.yml
```

**추가 설정**:
```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**제거할 설정**: admin 관련 (위와 동일)

### `api/src/main/resources/logback-spring.xml`

```bash
cp src/main/resources/logback-spring.xml api/src/main/resources/logback-spring.xml
```

### `api/src/test/resources/application-test.yml`

기존 `src/test/resources/application-test.yml`을 복사 후 admin 설정 제거:

```bash
cp src/test/resources/application-test.yml api/src/test/resources/application-test.yml
```

**제거할 설정**:
- `admin.*` 전체
- `spring.session.*` 전체

---

## 4-6. api 리소스 파일 이동

```bash
mkdir -p api/src/main/resources/email
cp src/main/resources/email/reminder.html api/src/main/resources/email/
```

---

## 4-7. api 테스트 이동

### admin 제외 전체 테스트 복사

```bash
# 도메인별 테스트 디렉토리 생성 및 복사 (admin 제외)
for domain in auth block comment group like moment notification report storage user; do
    mkdir -p api/src/test/java/moment/$domain
    cp -r src/test/java/moment/$domain/* api/src/test/java/moment/$domain/
done

# global 테스트
mkdir -p api/src/test/java/moment/global/domain
cp src/test/java/moment/global/domain/TargetTypeTest.java api/src/test/java/moment/global/domain/

# DTO 의존 픽스쳐 (Phase 0에서 분리)
mkdir -p api/src/test/java/moment/fixture
cp src/test/java/moment/fixture/UserRequestFixture.java api/src/test/java/moment/fixture/
```

### 테스트 파일 수 (admin 제외)

| 도메인 | 대략 파일 수 |
|--------|-------------|
| auth | ~10 |
| block | ~5 |
| comment | ~10 |
| group | ~12 |
| like | ~4 |
| moment | ~12 |
| notification | ~15 |
| report | ~2 |
| storage | ~3 |
| user | ~8 |
| global | ~1 |
| fixture | ~1 |
| **합계** | **~83** |

---

## 4-8. Phase 4 이동 파일 요약

### api/src/main/java/moment/ (약 100+ 파일)

| 카테고리 | 설명 |
|----------|------|
| `ApiApplication.java` | 신규 (Application 진입점) |
| auth | application(7) + dto(~15) + infrastructure(3) + presentation(3) |
| user | service + presentation + dto |
| moment | service + presentation + dto |
| comment | service + presentation + dto + events |
| group | service + presentation + dto + events |
| like | service + dto/event |
| notification | service + presentation + dto |
| report | application + presentation + dto |
| storage | 전체 (application + infrastructure + presentation + dto) |
| block | service + presentation + dto |
| global | dto/response(2) + exception(1) + config(5) + presentation(1) |

### api/src/main/resources/ (4+ 파일)

- `application-dev.yml` (수정된 복사)
- `application-prod.yml` (수정된 복사)
- `logback-spring.xml` (복사)
- `email/reminder.html` (이동)

### api/src/test/ (약 84 파일)

- 도메인별 테스트 전체 (admin 제외)
- `application-test.yml` (수정된 복사)
- `UserRequestFixture.java`
