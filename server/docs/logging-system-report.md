# Moment 프로젝트 로그 시스템 분석 보고서

> **작성일**: 2026-02-09
> **목적**: Prometheus/Grafana 기반 모니터링 전환을 위한 현행 로그 체계 파악
> **분석 범위**: `server/src/main/java/moment/` 전체

---

## 1. 로그 인프라 아키텍처 개요

```
┌─────────────────────────────────────────────────────────┐
│                    HTTP Request                          │
│                        │                                 │
│              ┌─────────▼──────────┐                      │
│              │   ApiLogFilter     │ ← trace_id 생성 (MDC)│
│              │   (INFO: 시작/종료) │                      │
│              └─────────┬──────────┘                      │
│                        │                                 │
│              ┌─────────▼──────────┐                      │
│              │ ControllerLogAspect│ ← dev/test만 활성    │
│              │   (DEBUG: req/res) │                      │
│              └─────────┬──────────┘                      │
│                        │                                 │
│              ┌─────────▼──────────┐                      │
│              │ ServiceLogAspect   │ ← 전 환경 활성       │
│              │ (WARN: >500ms)     │                      │
│              │ (DEBUG: duration)  │                      │
│              └─────────┬──────────┘                      │
│                        │                                 │
│              ┌─────────▼──────────┐                      │
│              │ RepositoryLogAspect│ ← 전 환경 활성       │
│              │ (WARN: >500ms)     │                      │
│              │ (DEBUG: duration)  │                      │
│              └────────────────────┘                      │
│                                                          │
│  ┌──────────────────────────────┐                        │
│  │ GlobalExceptionHandler       │ ← 전역 예외 로깅       │
│  │ AdminApiExceptionHandler     │ ← Admin 전용 예외 로깅  │
│  └──────────────────────────────┘                        │
└──────────────────────────────────────────────────────────┘
```

### 핵심 의존성

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| `logstash-logback-encoder` | 7.4 | JSON 구조화 로그 (kv 패턴) |
| `p6spy-spring-boot-starter` | 1.9.0 | SQL 쿼리 로깅 |
| `micrometer-registry-prometheus` | (managed) | Prometheus 메트릭 export |
| `spring-cloud-aws-starter-metrics` | 3.4.0 | CloudWatch 메트릭 export (현행) |
| `spring-boot-starter-aop` | (managed) | AOP 기반 로그 Aspect |

---

## 2. Logback 설정 (`logback-spring.xml`)

**파일**: `src/main/resources/logback-spring.xml`

### Appender 구성

| Appender | 타입 | 출력 형식 | 출력 대상 |
|----------|------|----------|----------|
| `JSON_FILE` | RollingFileAppender | JSON (LogStash) | `./logs/moment-custom.{date}.log` |
| `TEXT_CONSOLE` | ConsoleAppender | 텍스트 | stdout |

### JSON 로그 필드 구성

```json
{
  "trace_id": "a1b2c3d4",       // MDC - ApiLogFilter에서 주입
  "level": "INFO",               // 로그 레벨
  "msg": "API Request End",      // 로그 메시지
  "ip": "192.168.1.1",           // kv() arguments
  "method": "POST",
  "uri": "/api/v2/moments",
  "status": 201,
  "duration_ms": 45,
  "tag": "API_RESPONSE_TIME",
  "stack_trace": "...",          // 에러 시
  "timestamp": "2026-02-09T..."  // Asia/Seoul 기준
}
```

### 프로파일별 로그 레벨

| 프로파일 | Root | `moment` 패키지 | `p6spy` | Appender |
|---------|------|-----------------|---------|----------|
| **dev** | INFO | DEBUG | INFO | JSON_FILE |
| **prod** | INFO | INFO | INFO | JSON_FILE |
| **test** | INFO | DEBUG | TRACE | TEXT_CONSOLE + JSON_FILE |

### 로그 보관 정책

- **Rolling**: 일 단위 (`%d{yyyy-MM-dd}`)
- **보관 기간**: `maxHistory=1` (1일만 보관)
- **문제점**: prod 환경에서 1일 보관은 매우 짧음. 장애 원인 추적 시 로그 유실 위험

---

## 3. 공통 로그 처리 컴포넌트 (6개)

### 3.1 ApiLogFilter — HTTP 요청/응답 추적

**파일**: `moment/global/logging/ApiLogFilter.java`
**타입**: `jakarta.servlet.Filter`
**활성 환경**: 전체 (dev, prod, test)

**기능**:
- 요청마다 8자리 `trace_id` 생성 → MDC 저장 (분산 추적 기초)
- Swagger 경로(`/swagger-ui/`, `/v3/api-docs`) 제외
- 요청 시작/종료 시 로그

**로그 출력**:
| 시점 | 레벨 | 메시지 | 포함 필드 |
|------|------|--------|----------|
| 요청 시작 | INFO | `API Request Start` | ip, method, uri |
| 요청 종료 | INFO | `API Request End` | ip, method, uri, status, duration_ms, tag=API_RESPONSE_TIME |

**특이사항**:
- `duration_ms`는 `System.currentTimeMillis()` 기반 (나노초 정밀도 아님)
- `tag: "API_RESPONSE_TIME"` 필드는 CloudWatch 커스텀 메트릭 수집용으로 사용 중

---

### 3.2 ControllerLogAspect — 컨트롤러 요청/응답 상세

**파일**: `moment/global/logging/ControllerLogAspect.java`
**타입**: `@Aspect` (AOP)
**활성 환경**: dev, test만 (`@Profile({"test", "dev"})`)

**Pointcut**: `@RestController` 클래스의 모든 메서드 (Swagger 제외)

**로그 출력**:
| 시점 | 레벨 | 메시지 | 포함 필드 |
|------|------|--------|----------|
| @Before | DEBUG | `Controller Request` | queryParams, requestBody, hasToken |
| @AfterReturning | DEBUG | `Controller Response` | responseBody |

**특이사항**:
- prod에서는 비활성 → 프로덕션 디버깅 시 요청 본문 확인 불가
- `hasToken`: 쿠키에 "token" 이름 확인 (accessToken이 아닌 token)

---

### 3.3 ServiceLogAspect — 서비스 실행 시간 추적

**파일**: `moment/global/logging/ServiceLogAspect.java`
**타입**: `@Aspect` (AOP)
**활성 환경**: 전체

**Pointcut**: `@Service` 클래스 (`*QueryService` 제외, `@NoLogging` 제외)

**로그 출력**:
| 조건 | 레벨 | 메시지 | 포함 필드 |
|------|------|--------|----------|
| duration > 500ms | WARN | `Slow Service` | method, duration_ms |
| 항상 (debug) | DEBUG | `Service duration` | method, duration_ms |

**Slow 서비스 임계값**: **500ms**

---

### 3.4 RepositoryLogAspect — 리포지토리 쿼리 시간 추적

**파일**: `moment/global/logging/RepositoryLogAspect.java`
**타입**: `@Aspect` (AOP)
**활성 환경**: 전체

**Pointcut**: `org.springframework.data.repository.Repository+` 하위 모든 public 메서드

**로그 출력**:
| 조건 | 레벨 | 메시지 | 포함 필드 |
|------|------|--------|----------|
| duration > 500ms | WARN | `Slow Repository` | repository, method, duration_ms |
| duration <= 500ms | DEBUG | `Repository Executed` | repository, method, duration_ms |

**Slow 쿼리 임계값**: **500ms**

---

### 3.5 GlobalExceptionHandler — 전역 예외 로깅

**파일**: `moment/global/exception/GlobalExceptionHandler.java`
**타입**: `@RestControllerAdvice`

**처리 예외 및 로그**:
| 예외 | 레벨 | 메시지 | 스택트레이스 |
|------|------|--------|------------|
| `MomentException` (INTERNAL_SERVER_ERROR) | ERROR | `InternalServiceError Occurred` | O |
| `MomentException` (기타) | WARN | `Handled MomentException` | X |
| `MethodArgumentNotValidException` | WARN | `Validation Failed` | X |
| `IllegalArgumentException` | ERROR | `Illegal Argument Exception` | O |
| `Exception` (미처리) | ERROR | `Unhandled Exception Occurred` | O |

---

### 3.6 AdminApiExceptionHandler — Admin API 전용 예외 로깅

**파일**: `moment/admin/presentation/api/AdminApiExceptionHandler.java`
**타입**: `@RestControllerAdvice(basePackages = "moment.admin.presentation.api")`

**처리 예외 및 로그**:
| 예외 | 레벨 | 메시지 |
|------|------|--------|
| `AdminException` | WARN | `Admin API Exception` |
| `MethodArgumentNotValidException` | WARN | `Validation failed` |
| `MethodArgumentTypeMismatchException` | WARN | `Type mismatch` |
| `Exception` | ERROR | `Unexpected error in Admin API` |

---

### 3.7 NoLogging 어노테이션 — 로그 제외 메커니즘

**파일**: `moment/global/logging/NoLogging.java`

`@NoLogging`을 서비스 메서드에 붙이면 `ServiceLogAspect`에서 제외됨.

---

## 4. 도메인별 수동 로그 현황

### 4.1 Notification 도메인 (로그 가장 풍부)

| 파일 | 레벨 | 내용 | 목적 |
|------|------|------|------|
| `NotificationEventHandler.java` | INFO | `CommentCreateEvent received: momentId={}, momenterId={}` | 이벤트 수신 추적 |
| 〃 | INFO | `GroupJoinRequestEvent received` | 〃 |
| 〃 | INFO | `GroupJoinApprovedEvent received` | 〃 |
| 〃 | INFO | `GroupKickedEvent received` | 〃 |
| 〃 | INFO | `MomentLikeEvent received` | 〃 |
| 〃 | INFO | `CommentLikeEvent received` | 〃 |
| 〃 | INFO | `GroupCommentCreateEvent received` | 〃 |
| `ExpoPushNotificationSender.java` | DEBUG | `No device tokens found for userId={}` | 디바이스 미등록 확인 |
| 〃 | INFO | `Expo push sent to userId={}, tickets={}` | 푸시 전송 성공 |
| 〃 | ERROR | `Failed to send Expo push to userId={}` | 푸시 전송 실패 |
| `Emitters.java` | ERROR | `Failed to send SSE event to user {}` | SSE 전송 실패 |
| 〃 | INFO | `User {} connection lost.` | SSE 연결 끊김 |

### 4.2 Admin 도메인

| 파일 | 레벨 | 내용 | 목적 |
|------|------|------|------|
| `AdminService.java` | WARN | `Blocked admin login attempt: email={}` | 차단된 관리자 로그인 시도 |
| 〃 | INFO | `Admin blocked: adminId={}, email={}` | 관리자 차단 |
| 〃 | INFO | `Admin unblocked: adminId={}, email={}` | 관리자 차단 해제 |
| `AdminManagementApplicationService.java` | INFO | `Admin blocked and sessions invalidated` | 관리자 차단 + 세션 무효화 |
| 〃 | INFO | `Admin unblocked` | 관리자 차단 해제 |
| 〃 | INFO | `Session force logged out` | 세션 강제 로그아웃 |
| 〃 | INFO | `All sessions force logged out for adminId={}` | 전체 세션 강제 로그아웃 |
| `AdminSessionManager.java` | INFO | `Admin session registered` | 세션 등록 (sessionId 마스킹) |
| 〃 | INFO | `Admin session invalidated` | 세션 무효화 |
| 〃 | INFO | `All active sessions invalidated for adminId={}` | 전체 세션 무효화 |
| `AdminSessionListener.java` | DEBUG | `New HTTP session created: {}` | HTTP 세션 생성 |
| 〃 | DEBUG | `HTTP session destroyed: {}` | HTTP 세션 파괴 |
| 〃 | INFO | `Admin session marked as logged out` | 세션 로그아웃 마킹 |
| `AdminAuthInterceptor.java` | DEBUG | `Session authorized: sessionId={}` | 세션 인증 성공 |
| 〃 | WARN | `No session found` | 세션 없음 |
| 〃 | WARN | `Admin unauthorized` | 인증 실패 |
| 〃 | WARN | `Session invalidated in database` | DB에서 세션 만료 |
| 〃 | WARN | `Access denied to SUPER_ADMIN only path` | 권한 부족 |
| `AdminInitializer.java` | INFO | `초기 SUPER_ADMIN 관리자 계정 생성: {}` | 초기 계정 생성 |
| 〃 | WARN | `프로덕션 환경에서는 반드시 초기 비밀번호를 변경하세요!` | 보안 경고 |

### 4.3 Auth 도메인

| 파일 | 레벨 | 내용 | 목적 |
|------|------|------|------|
| `AuthEmailService.java` | ERROR | `인증 이메일 전송 실패` | 이메일 전송 오류 |
| 〃 | INFO | `만료된 이메일 인증 정보 정리 완료. 남은 정보 수: {}` | 스케줄 정리 |
| 〃 | INFO | `만료된 비밀번호 변경 정보 정리 완료. 남은 정보 수: {}` | 스케줄 정리 |
| 〃 | ERROR | `비밀번호 재설정 이메일 전송 실패` | 이메일 전송 오류 |

### 4.4 @Slf4j만 선언하고 로그 미사용

| 파일 | 비고 |
|------|------|
| `SseNotificationService.java` | SSE 연결 관리하지만 로그 없음 |
| `AdminSessionService.java` | 세션 CRUD하지만 로그 없음 |
| `ExpoPushApiClient.java` | 외부 API 호출하지만 로그 없음 |

---

## 5. 현행 메트릭/모니터링 설정

### CloudWatch (현행 — 제거 예정)

**dev (`application-dev.yml`)**:
```yaml
management:
  cloudwatch:
    metrics:
      export:
        enabled: true
        step: 1m
        namespace: Moment/dev
```

**prod (`application-prod.yml`)**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      cloudwatch:
        enabled: true
        step: 1m
        namespace: Moment/prod
  tags:
    application: ${spring.application.name}
    environment: prod
```

### 수집 중인 메트릭

| 메트릭 | dev | prod | 비고 |
|--------|-----|------|------|
| jvm.memory.used / max | O | O | JVM 메모리 |
| jvm.gc.pause | O | O | GC 일시정지 |
| jvm.threads | O | O | 스레드 수 |
| hikaricp | O | O | DB 커넥션 풀 |
| http.server | O | O | HTTP 요청 메트릭 |
| process.cpu | O | O | CPU 사용률 |
| process.uptime | O | O | 가동 시간 |
| tomcat.threads | X | O | Tomcat 스레드 풀 |
| tomcat.sessions | X | O | Tomcat 세션 |
| tomcat.cache | X | O | Tomcat 캐시 |
| tomcat.global | X | O | Tomcat 전역 |

### Prometheus 엔드포인트

prod에서 이미 `/actuator/prometheus` 엔드포인트가 노출되어 있으며, `micrometer-registry-prometheus` 의존성도 존재. **Prometheus 수집을 위한 기본 인프라는 이미 준비됨**.

---

## 6. 로그 레벨별 분포 현황

```
ERROR (6건) ─── 시스템 장애, 외부 연동 실패
  ├── GlobalExceptionHandler: INTERNAL_SERVER_ERROR, IllegalArgument, 미처리 예외
  ├── AdminApiExceptionHandler: 미처리 예외
  ├── ExpoPushNotificationSender: 푸시 전송 실패
  └── Emitters: SSE 전송 실패

WARN (12건) ─── 비즈니스 예외, 보안 이벤트, 성능 경고
  ├── GlobalExceptionHandler: MomentException, 유효성 검증 실패
  ├── AdminApiExceptionHandler: AdminException, 유효성 검증, 타입 불일치
  ├── ServiceLogAspect: Slow Service (>500ms)
  ├── RepositoryLogAspect: Slow Repository (>500ms)
  ├── AdminAuthInterceptor: 세션 없음, 인증 실패, 세션 만료, 권한 부족
  ├── AdminService: 차단된 관리자 로그인 시도
  └── AuthEmailService: 인증 이메일 전송 실패 (ERROR로 변경 필요)

INFO (22건) ─── 비즈니스 이벤트, API 라이프사이클
  ├── ApiLogFilter: API 요청 시작/종료
  ├── NotificationEventHandler: 7개 도메인 이벤트 수신
  ├── ExpoPushNotificationSender: 푸시 전송 성공
  ├── Emitters: SSE 연결 끊김
  ├── AdminService/AdminManagementApplicationService: 관리자 차단/해제
  ├── AdminSessionManager/Listener: 세션 등록/무효화/로그아웃
  └── AuthEmailService: 만료 데이터 정리

DEBUG (6건+) ─── 상세 디버깅, 실행 추적
  ├── ControllerLogAspect: 요청/응답 상세 (dev/test만)
  ├── ServiceLogAspect: 서비스 실행 시간
  ├── RepositoryLogAspect: 쿼리 실행 시간
  ├── AdminAuthInterceptor: 세션 인증 성공
  ├── AdminSessionListener: HTTP 세션 생성/파괴
  └── ExpoPushNotificationSender: 디바이스 토큰 없음
```

---

## 7. 로그가 없는 핵심 영역 (Gap 분석)

### 7.1 CRITICAL — 즉시 추가 필요

| 영역 | 클래스 | 부재 로그 | 필요 이유 |
|------|--------|----------|----------|
| **사용자 인증** | `AuthService` | 로그인 성공/실패, 로그아웃, 토큰 갱신 | 브루트포스, 계정 탈취 탐지 불가 |
| **사용자 계정** | `UserService` | 회원가입, 비밀번호 변경 | 계정 라이프사이클 추적 불가 |
| **회원 탈퇴** | `UserWithdrawService` | 탈퇴 요청/처리 | 비즈니스 지표 수집 불가 |
| **OAuth 연동** | `GoogleAuthService`, `AppleAuthService` | OAuth 플로우, 신규/기존 분기 | 외부 인증 장애 진단 불가 |
| **SSE 연결** | `SseNotificationService` | 연결 수립/해제 | 실시간 알림 장애 진단 불가 |

### 7.2 HIGH — 빠른 시일 내 추가 권장

| 영역 | 클래스 | 부재 로그 | 필요 이유 |
|------|--------|----------|----------|
| **Facade 서비스** | `MomentCreateFacadeService` 외 7개 | 비즈니스 오케스트레이션 | 도메인 간 조율 추적 불가 |
| **콘텐츠 신고** | `ReportService` | 신고 생성, 삭제 임계값 도달 | 콘텐츠 모더레이션 감사 불가 |
| **디바이스 등록** | `PushNotificationService` | 디바이스 등록/해제 | 푸시 인프라 건강성 모니터링 불가 |
| **그룹 멤버 관리** | `GroupMemberService` | 가입 요청, 승인, 추방 | 그룹 활동 추적 불가 |
| **외부 API** | `GoogleAuthClient`, `AppleAuthClient` | API 호출 실패, 지연 | 외부 서비스 장애 진단 불가 |

### 7.3 MEDIUM — 관측성 향상을 위해 추가 고려

| 영역 | 클래스 | 부재 로그 | 필요 이유 |
|------|--------|----------|----------|
| 모멘트 CRUD | `MomentService` | 생성/삭제 | 비즈니스 지표 |
| 댓글 CRUD | `CommentService` | 생성/삭제 | 비즈니스 지표 |
| 좋아요 | `MomentLikeService`, `CommentLikeService` | 토글 이벤트 | 사용자 활동 추적 |
| 초대 링크 | `InviteLinkService` | 링크 생성/사용 | 그룹 성장 추적 |
| 이미지 업로드 | `MomentImageService`, `CommentImageService` | 업로드 성공/실패 | S3 연동 모니터링 |
| 닉네임 생성 | `NicknameGenerateApplicationService` | 충돌/재시도 | 성능 이슈 탐지 |

---

## 8. 안티패턴 및 개선 필요 사항

### 8.1 발견된 안티패턴

| 문제 | 위치 | 설명 |
|------|------|------|
| `System.out.println` | `NotificationControllerTest.java:210` | 테스트에서 println 사용 → `@Slf4j`로 변경 필요 |
| `@Slf4j` 선언 후 미사용 | `SseNotificationService`, `AdminSessionService`, `ExpoPushApiClient` | 불필요한 어노테이션 또는 로그 추가 필요 |
| 로그 보관 1일 | `logback-spring.xml` `maxHistory=1` | prod에서 너무 짧음 → 최소 7~30일 권장 |
| trace_id 8자리 | `ApiLogFilter.java` | UUID 앞 8자리만 사용 → 충돌 가능성. 분산 추적 전환 시 고려 |
| 한글 로그 메시지 혼재 | `AuthEmailService`, `AdminInitializer` | 영문 메시지와 혼용 → 로그 검색/파싱 시 불편 |
| ControllerLogAspect 쿠키 키 | `hasToken()` 에서 `"token"` 체크 | 실제 토큰은 `accessToken` 쿠키에 저장 → 불일치 가능 |

### 8.2 구조적 개선 사항

| 항목 | 현재 상태 | 권장 |
|------|----------|------|
| 분산 추적 | 자체 8자리 trace_id | OpenTelemetry/Micrometer Tracing 도입 (trace_id + span_id) |
| 로그 수집 | 파일 기반 (JSON) | Loki 또는 Promtail → Grafana 연동 |
| 메트릭 export | CloudWatch + Prometheus 이중 | Prometheus 단일화 (CloudWatch 제거) |
| 커스텀 메트릭 | `tag: "API_RESPONSE_TIME"` 로그 기반 | Micrometer `Timer`/`Counter` 기반 메트릭 전환 |
| Slow 서비스/쿼리 | 로그 기반 WARN (500ms) | Micrometer `Timer` percentile + Grafana 대시보드 |

---

## 9. Prometheus/Grafana 전환 시 고려 사항

### 9.1 현재 준비 상태

- `micrometer-registry-prometheus` 의존성: **있음**
- `/actuator/prometheus` 엔드포인트: **prod에서 노출 중**
- 기본 JVM/HTTP 메트릭: **수집 중**
- **전환 시 제거 필요**: `spring-cloud-aws-starter-metrics` 의존성 + CloudWatch 설정

### 9.2 로그 → 메트릭 전환 대상

현재 로그로만 추적되고 있지만 Micrometer 메트릭으로 전환하면 더 효과적인 항목:

| 현재 로그 | 전환 메트릭 | 타입 | 용도 |
|----------|-----------|------|------|
| `API_RESPONSE_TIME` (tag) | `http.server.requests` Timer | Timer | 이미 Micrometer가 자동 수집 |
| `Slow Service` (WARN) | 커스텀 Timer + percentile | Timer | p95, p99 대시보드 |
| `Slow Repository` (WARN) | 커스텀 Timer + percentile | Timer | 쿼리 성능 대시보드 |
| 이벤트 수신 (INFO) | 커스텀 Counter | Counter | 이벤트 처리량 대시보드 |
| 푸시 전송 성공/실패 | 커스텀 Counter (success/fail) | Counter | 푸시 성공률 대시보드 |
| SSE 연결/끊김 | 커스텀 Gauge | Gauge | 동시 접속자 수 대시보드 |

### 9.3 로그 수집 파이프라인 제안

```
Application → JSON 로그 파일 → Promtail → Loki → Grafana
                                                      ↑
Application → /actuator/prometheus → Prometheus ──────┘
```

---

## 10. 전체 로그 파일 목록 (19개)

| # | 파일 경로 | 로그 수 | 주요 레벨 |
|---|----------|--------|----------|
| 1 | `global/logging/ApiLogFilter.java` | 2 | INFO |
| 2 | `global/logging/ControllerLogAspect.java` | 2 | DEBUG |
| 3 | `global/logging/ServiceLogAspect.java` | 2 | WARN/DEBUG |
| 4 | `global/logging/RepositoryLogAspect.java` | 2 | WARN/DEBUG |
| 5 | `global/exception/GlobalExceptionHandler.java` | 5 | ERROR/WARN |
| 6 | `admin/presentation/api/AdminApiExceptionHandler.java` | 4 | WARN/ERROR |
| 7 | `admin/global/interceptor/AdminAuthInterceptor.java` | 5 | WARN/DEBUG |
| 8 | `admin/global/util/AdminSessionManager.java` | 3 | INFO |
| 9 | `admin/global/listener/AdminSessionListener.java` | 3 | DEBUG/INFO |
| 10 | `admin/service/admin/AdminService.java` | 3 | WARN/INFO |
| 11 | `admin/service/application/AdminManagementApplicationService.java` | 4 | INFO |
| 12 | `admin/config/AdminInitializer.java` | 2 | INFO/WARN |
| 13 | `notification/service/eventHandler/NotificationEventHandler.java` | 7 | INFO |
| 14 | `notification/infrastructure/expo/ExpoPushNotificationSender.java` | 3 | DEBUG/INFO/ERROR |
| 15 | `notification/infrastructure/Emitters.java` | 2 | ERROR/INFO |
| 16 | `auth/application/AuthEmailService.java` | 4 | ERROR/INFO |
| 17 | `notification/service/notification/SseNotificationService.java` | 0 | (@Slf4j만) |
| 18 | `admin/service/session/AdminSessionService.java` | 0 | (@Slf4j만) |
| 19 | `notification/infrastructure/expo/ExpoPushApiClient.java` | 0 | (@Slf4j만) |

**총 로그 문장**: 약 **53개**
**@Slf4j 선언 후 미사용**: **3개 파일**

---

## 11. 요약 및 권장 조치

### 즉시 실행 (Prometheus/Grafana 전환 전)

1. **CloudWatch 의존성 제거**: `spring-cloud-aws-starter-metrics` + 설정 제거
2. **Prometheus 설정 통일**: dev/prod 모두 `/actuator/prometheus` 노출
3. **로그 보관 기간 연장**: `maxHistory=1` → 최소 `7` (prod) 또는 Loki 도입 시 불필요
4. **CRITICAL 영역 로그 추가**: AuthService, UserService, UserWithdrawService

### 단기 (1~2주)

5. **커스텀 Micrometer 메트릭 정의**: API 응답 시간은 이미 자동 수집 → 중복 `tag` 로그 제거
6. **분산 추적 도입**: Micrometer Tracing + Zipkin/Tempo (trace_id 자동 전파)
7. **HIGH 영역 로그 추가**: Facade 서비스, 신고, 디바이스 등록

### 중기 (2~4주)

8. **Grafana 대시보드 구성**: JVM, HTTP, 커스텀 비즈니스 메트릭
9. **Loki + Promtail 도입**: 구조화 로그 수집 → Grafana에서 로그/메트릭 통합 조회
10. **알림 규칙 설정**: Slow Service/Repository, 에러율, SSE 연결 급감 등
