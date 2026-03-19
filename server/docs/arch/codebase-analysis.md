# Moment 프로젝트 아키텍처 분석 보고서

> ArchUnit 도입을 위한 코드베이스 패턴 분석
> 작성일: 2026-02-19
> 최종 수정: 2026-02-20 (패키지 구조 표준화 및 DIP 리팩토링 반영)

## 1. 참고 자료 요약

### 무신사 기술 블로그: ArchUnit으로 아키텍처 규칙 자동 검증

무신사 물류플랫폼팀의 ArchUnit 도입 사례에서 핵심 인사이트:

- **교과서(컨텍스트) vs 시험지(테스트)**: AI에게 규칙을 "알려주는" 것보다 "어기면 빌드가 실패하게 만드는" 것이 효과적
- **적용한 규칙 카테고리**: 레이어 의존성 규칙, 패키지 구조 규칙, 순환 의존성 감지, 도메인 순수성 보호, 어노테이션-네이밍 일관성, API URL 형식 검증, DIP(의존성 역전 원칙)
- **AI 연동 효과**: ArchUnit 테스트 실패 메시지를 AI가 이해하고 스스로 수정하는 자동 피드백 루프 형성
- **도입 결과**: 코드 리뷰에서 아키텍처 위반 발견 시간 감소, 비즈니스 로직에 집중 가능

---

## 2. 프로젝트 구조 개요

### 2.1 멀티 모듈 구성

```
server/
├── common/     # 공유 도메인 엔티티, 리포지토리, 인프라
├── api/        # User-facing REST API (Spring Boot Application)
└── admin/      # Admin REST API (별도 Spring Boot Application)
```

| 모듈 | 역할 | 주요 패키지 |
|------|------|------------|
| `common` | 엔티티, 리포지토리, BaseEntity, ErrorCode, 페이지네이션 VO | `moment.{domain}.domain`, `moment.{domain}.infrastructure` |
| `api` | 서비스 레이어, 컨트롤러, DTO, 이벤트 핸들러 | `moment.{domain}.service`, `moment.{domain}.presentation`, `moment.{domain}.dto` |
| `admin` | 어드민 서비스, 컨트롤러, DTO, 세션/인터셉터 | `moment.admin.service`, `moment.admin.presentation.api`, `moment.admin.dto` |

### 2.2 도메인 모듈 목록

| 도메인 | 설명 | 엔티티 수 |
|--------|------|-----------|
| auth | 인증/인가 (JWT, OAuth, Email) | RefreshToken, EmailVerification |
| user | 사용자 관리 | User |
| moment | 핵심 모멘트 게시물 | Moment, MomentImage |
| comment | 댓글 (Echo) | Comment, CommentImage |
| group | 그룹 관리 | Group, GroupMember, GroupInviteLink |
| like | 좋아요 | MomentLike, CommentLike |
| notification | 알림 (SSE + Push) | Notification, PushNotification |
| report | 신고 | Report |
| block | 사용자 차단 | UserBlock |
| storage | 파일 저장소 (S3) | (엔티티 없음) |
| admin | 관리자 | Admin, AdminSession, AdminGroupLog |
| global | 공유 인프라 | BaseEntity, ErrorCode 등 |

---

## 3. 계층 패턴 분석

### 3.1 표준 계층 구조 (대부분의 모듈)

```
{domain}/ (common 모듈)
├── domain/           # 엔티티, VO, 정책, Enum
└── infrastructure/   # JpaRepository 인터페이스

{domain}/ (api 모듈)
├── service/
│   ├── {domain}/     # 도메인 서비스 (핵심 비즈니스 로직)
│   ├── application/  # 애플리케이션 서비스 (조율)
│   ├── facade/       # 퍼사드 서비스 (이벤트 발행 + 여러 앱서비스 조율)
│   └── eventHandler/ # 이벤트 리스너 (notification 모듈만)
├── presentation/     # @RestController
└── dto/
    ├── request/      # 요청 DTO (record)
    ├── response/     # 응답 DTO (record + from()/of())
    └── {Event}.java  # 도메인 이벤트 (record)
```

### 3.2 모듈별 계층 구조 현황

#### 표준 패턴을 따르는 모듈 (service/ 기반)

| 모듈 | Domain Service | Application Service | Facade Service |
|------|---------------|--------------------|--------------------|
| moment | MomentService, MomentImageService | MomentApplicationService | MomentCreateFacadeService, CommentableMomentFacadeService, MyGroupMomentPageFacadeService |
| comment | CommentService, CommentImageService | CommentApplicationService | CommentCreateFacadeService, GroupCommentCreateFacadeService, MyGroupCommentPageFacadeService |
| group | GroupService, GroupMemberService | GroupApplicationService, GroupMemberApplicationService | - |
| user | UserService, UserWithdrawService | NicknameGenerateApplicationService | MyPageFacadeService |
| notification | NotificationService, SseNotificationService, PushNotificationService | NotificationApplicationService, PushNotificationApplicationService | NotificationFacadeService |
| block | UserBlockService | UserBlockApplicationService | - |
| auth | AuthService, AuthEmailService | GoogleAuthService, AppleAuthService | - |
| storage | FileStorageService | - | - |
| report | ReportService | ReportApplicationService | ReportCreateFacadeService |

#### 비표준 구조를 가진 모듈

| 모듈 | 현재 구조 | 표준과의 차이 |
|------|-----------|--------------|
| **like** | `like/service/MomentLikeService.java` (직접) | `service/like/` 하위 패키지 없이 `service/` 바로 아래에 위치. presentation 레이어 없음 (group 컨트롤러에 통합) |
| **group** | `group/service/invite/InviteLinkService.java` | `InviteLinkService`가 `service/group/`이 아닌 `service/invite/` 별도 패키지에 위치 |

> ~~auth, storage, report 모듈은 2026-02-19 기준 비표준이었으나~~, 2026-02-20 패키지 구조 표준화 리팩토링으로 모두 `service/` 기반으로 통일됨

### 3.3 계층 의존성 방향 (현재 준수 상태)

```
presentation → service/facade → service/application → service/{domain} → domain
                    ↓                    ↓                     ↓
              (이벤트 발행)         (다른 도메인 서비스)    (infrastructure/repository)
```

**검증 결과**: 역방향 의존성 위반 없음

---

## 4. 의존성 패턴 분석

### 4.1 크로스 도메인 의존성

```
user ← (거의 모든 도메인에서 참조)
  ├── moment (momenter)
  ├── comment (commenter)
  ├── group (member)
  ├── like (liker)
  ├── notification (recipient)
  ├── block (blocker/blocked)
  └── report (reporter)

storage ← moment, comment (이미지 업로드)

notification ← (이벤트 수신)
  ├── comment (CommentCreateEvent, GroupCommentCreateEvent)
  ├── like (MomentLikeEvent, CommentLikeEvent)
  └── group (GroupJoinRequestEvent, GroupJoinApprovedEvent, GroupKickedEvent)
```

### 4.2 이벤트 기반 의존성

| 이벤트 | 발행 도메인 | 구독 도메인 |
|--------|------------|------------|
| `CommentCreateEvent` | comment (Facade) | notification (EventHandler) |
| `GroupCommentCreateEvent` | comment (Facade) | notification (EventHandler) |
| `MomentLikeEvent` | like | notification (EventHandler) |
| `CommentLikeEvent` | like | notification (EventHandler) |
| `GroupJoinRequestEvent` | group | notification (EventHandler) |
| `GroupJoinApprovedEvent` | group | notification (EventHandler) |
| `GroupKickedEvent` | group | notification (EventHandler) |

### 4.3 DIP (의존성 역전 원칙) 적용 현황

2026-02-20 리팩토링으로 외부 인프라 의존성에 DIP가 적용됨:

| 인터페이스 | 위치 | 구현체 | 위치 |
|-----------|------|--------|------|
| `GoogleOAuthClient` | `auth/service/application/` | `GoogleAuthClient` | `auth/infrastructure/` |
| `AppleOAuthClient` | `auth/service/application/` | `AppleAuthClient` | `auth/infrastructure/` |
| `FileUploadClient` | `storage/service/storage/` | `AwsS3Client` | `storage/infrastructure/` |
| `TokenManager` | `auth/service/auth/` | `JwtTokenManager` | `auth/infrastructure/` |
| `EmailService` | `auth/service/auth/` | (구현체) | `auth/infrastructure/` |

**패턴**: 서비스 레이어에 인터페이스 정의 → infrastructure에 구현체 배치
- 서비스는 인터페이스에만 의존하여 테스트 용이성과 교체 유연성 확보
- 외부 API(Google, Apple, AWS S3) 클라이언트에 적용

### 4.4 모듈 간 경계 규칙

- **common 모듈**: domain, infrastructure만 포함 (서비스 로직 없음)
- **api 모듈**: common의 엔티티/리포지토리를 가져와서 서비스, 컨트롤러, DTO 구현
- **admin 모듈**: common의 엔티티/리포지토리를 가져와서 별도 서비스, 컨트롤러, DTO 구현
- **api ↔ admin 직접 의존 없음** (common을 통해서만 공유)

---

## 5. 네이밍 패턴 분석

### 5.1 클래스 네이밍 규칙

| 유형 | 패턴 | 예시 | 준수율 |
|------|------|------|--------|
| Entity | `{Name}` (단수) | `User`, `Moment`, `Comment` | 100% |
| Repository | `{Entity}Repository` | `UserRepository`, `MomentRepository` | 100% |
| Domain Service | `{Domain}Service` | `UserService`, `MomentService` | 100% |
| Application Service | `{Domain}ApplicationService` | `MomentApplicationService` | 100% |
| Facade Service | `{Domain}{Action}FacadeService` 또는 `{Domain}FacadeService` | `MomentCreateFacadeService`, `NotificationFacadeService` | 100% |
| Event Handler | `{Domain}EventHandler` | `NotificationEventHandler` | 100% |
| User API Controller | `{Resource}Controller` | `MomentController`, `UserController` | 100% |
| Admin API Controller | `Admin{Resource}ApiController` | `AdminUserApiController` | 100% |
| Request DTO | `{Resource}{Action}Request` | `MomentCreateRequest` | 100% |
| Response DTO | `{Resource}{Purpose}Response` | `MomentCreateResponse` | 100% |
| Domain Event | `{Domain}{Action}Event` | `CommentCreateEvent` | 100% |
| Exception | `MomentException` / `AdminException` | 도메인별 단일 예외 | 100% |
| Test | `{Class}Test` | `MomentServiceTest` | 100% |
| Fixture | `{Entity}Fixture` | `UserFixture` | 100% |

### 5.2 메서드 네이밍 규칙

| 목적 | 패턴 | 예시 |
|------|------|------|
| 조회 (없으면 예외) | `get{Entity}By()` | `getUserBy(Long id)` |
| 조회 (Optional) | `findBy{Property}()` | `findByEmail(String email)` |
| 존재 확인 | `existsBy{Property}()` | `existsByNickname(String nickname)` |
| 생성 | `create{Entity}()` | `createBasicMoment()` |
| 수정 | `update{Property}()` | `updateNickname()` |
| 삭제 | `delete{Entity}()` | `deleteMoment()` |

### 5.3 테스트 메서드 네이밍

```java
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserServiceTest {
    @Test
    void 일반_회원가입_유저를_추가한다() { ... }
    @Test
    void 중복된_닉네임이_존재하는_경우_유저를_추가할_수_없다() { ... }
}
```

### 5.4 API 엔드포인트 패턴

| API | Base Path | 예시 |
|-----|-----------|------|
| User API | `/api/v2/{resources}` | `/api/v2/moments`, `/api/v2/users/signup` |
| Admin API | `/api/admin/{resources}` | `/api/admin/users`, `/api/admin/groups` |

---

## 6. 어노테이션 패턴 분석

### 6.1 레이어별 어노테이션

| 레이어 | 클래스 레벨 | 메서드 레벨 |
|--------|------------|------------|
| Entity | `@Entity`, `@Getter`, `@NoArgsConstructor(access=PROTECTED)`, `@SQLDelete`, `@SQLRestriction`, `@EqualsAndHashCode` | - |
| Repository | (인터페이스, 어노테이션 없음) | `@Query`, `@Param`, `@Modifying` |
| Domain Service | `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly=true)` | `@Transactional` (쓰기) |
| Application Service | `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly=true)` | `@Transactional` (쓰기) |
| Facade Service | `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly=true)` | `@Transactional` (쓰기) |
| Event Handler | `@Component`, `@RequiredArgsConstructor`, `@Slf4j` | `@Async`, `@TransactionalEventListener(phase=AFTER_COMMIT)` |
| Controller (User) | `@RestController`, `@RequiredArgsConstructor`, `@RequestMapping("/api/v2/...")`, `@Tag` | `@Operation`, `@ApiResponses`, `@PostMapping` 등 |
| Controller (Admin) | `@RestController`, `@RequiredArgsConstructor`, `@RequestMapping("/api/admin/...")`, `@Tag` | `@Operation`, `@ApiResponses` |
| Exception Handler | `@RestControllerAdvice`, `@Slf4j` | `@ExceptionHandler` |
| Configuration | `@Configuration` | `@Bean` |

### 6.2 DTO 어노테이션

| 유형 | 어노테이션 |
|------|-----------|
| Request DTO | `@Schema` + Jakarta Validation (`@NotBlank`, `@Pattern`, `@Size` 등) |
| Response DTO | `@Schema` + 정적 팩토리 메서드 (`from()`, `of()`) |
| Domain Event | 없음 (순수 record) |

---

## 7. 발견된 비일관성

### 7.1 ~~패키지 구조 비일관성~~ (RESOLVED)

> ✅ **해결됨** (2026-02-20): auth, storage, report 3개 모듈이 `service/` 기반으로 표준화됨
>
> | 커밋 | 내용 |
> |------|------|
> | `5a5d8415` | report 모듈 패키지 구조를 service/로 표준화 |
> | `881b02e7` | auth 모듈 패키지 구조를 service/로 표준화 |
> | `1a6ac61f` | storage 모듈 패키지 구조를 service/로 표준화 |

### 7.2 like 모듈 서비스 패키지 구조 (LOW)

`like` 모듈의 서비스가 `service/like/` 하위 패키지 없이 `service/` 바로 아래에 위치:
- `like/service/MomentLikeService.java` (표준: `like/service/like/MomentLikeService.java`)
- `like/service/CommentLikeService.java` (표준: `like/service/like/CommentLikeService.java`)
- presentation 레이어가 없어 group 도메인의 컨트롤러에서 직접 호출
- 규모가 작은 모듈이라 현재 구조도 합리적이나, ArchUnit 규칙에서 예외 처리 필요할 수 있음

### 7.3 group 모듈 invite 서비스 위치 (LOW)

`InviteLinkService`가 `service/group/`이 아닌 `service/invite/` 별도 패키지에 위치:
- `group/service/invite/InviteLinkService.java`
- 초대 기능의 독립적 성격을 고려하면 의도적 분리일 수 있음

### 7.4 Soft Delete 미적용 엔티티 (MEDIUM)

`AdminGroupLog` 엔티티에 `@SQLDelete`와 `@SQLRestriction`이 없음:
- 경로: `common/src/main/java/moment/admin/domain/AdminGroupLog.java`
- 감사 로그 성격이라 의도적일 수 있으나 일관성 측면에서 확인 필요

---

## 8. ArchUnit 적용 대상 규칙 도출

블로그 사례와 프로젝트 분석을 종합하여, 다음 카테고리의 ArchUnit 테스트를 제안합니다.

### 8.1 레이어 의존성 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| L-001 | presentation 레이어는 service 레이어만 의존 가능 | HIGH |
| L-002 | service 레이어는 domain, infrastructure 의존 가능, presentation 의존 불가 | HIGH |
| L-003 | domain 레이어는 다른 레이어에 의존하지 않음 (순수성) | HIGH |
| L-004 | infrastructure 레이어는 domain만 의존 가능 | HIGH |
| L-005 | dto 패키지는 domain에만 의존 가능 (변환 목적) | MEDIUM |
| L-006 | eventHandler는 facade/application 서비스만 의존 가능 | MEDIUM |

### 8.2 모듈 경계 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| M-001 | api 모듈은 admin 모듈에 직접 의존 불가 | HIGH |
| M-002 | admin 모듈은 api 모듈에 직접 의존 불가 | HIGH |
| M-003 | 도메인 모듈 간 직접 서비스 의존은 Application/Facade 서비스를 통해서만 | MEDIUM |

### 8.3 패키지 구조 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| P-001 | `@RestController` 클래스는 `presentation` 패키지에 위치 | HIGH |
| P-002 | `@Service` 클래스는 `service` 패키지(또는 하위)에 위치 | HIGH |
| P-003 | Repository 인터페이스는 `infrastructure` 패키지에 위치 | HIGH |
| P-004 | Entity 클래스는 `domain` 패키지에 위치 | HIGH |
| P-005 | Request DTO는 `dto/request` 패키지에 위치 | MEDIUM |
| P-006 | Response DTO는 `dto/response` 패키지에 위치 | MEDIUM |

### 8.4 네이밍 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| N-001 | `@Service` 어노테이션 클래스는 `Service`로 끝나야 함 | HIGH |
| N-002 | `@RestController` 어노테이션 클래스는 `Controller`로 끝나야 함 | HIGH |
| N-003 | JpaRepository 확장 인터페이스는 `Repository`로 끝나야 함 | HIGH |
| N-004 | `@Component` + `@TransactionalEventListener` 메서드가 있는 클래스는 `EventHandler`로 끝나야 함 | MEDIUM |
| N-005 | ApplicationService는 `ApplicationService`로 끝나야 함 | MEDIUM |
| N-006 | FacadeService는 `FacadeService`로 끝나야 함 | MEDIUM |

### 8.5 어노테이션 일관성 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| A-001 | `@Transactional`은 service 패키지 클래스에서만 사용 (controller, repository 금지) | HIGH |
| A-002 | `@TransactionalEventListener`는 반드시 `@Async`와 함께 사용 | HIGH |
| A-003 | Entity는 반드시 `@SQLDelete`와 `@SQLRestriction` 사용 (soft delete). 예외: `AdminGroupLog` (감사 로그) | HIGH |
| A-004 | Entity는 반드시 `BaseEntity` 확장 | HIGH |
| A-005 | Service 클래스는 `@RequiredArgsConstructor` 사용 (필드 주입 금지) | HIGH |
| A-006 | Controller의 요청 파라미터에 `@Valid` 사용 | MEDIUM |
| A-007 | Service 클래스는 클래스 레벨 `@Transactional(readOnly = true)` 기본 적용 | MEDIUM |

### 8.6 순환 의존성 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| C-001 | 도메인 모듈 간 순환 의존 금지 | HIGH |
| C-002 | 패키지 간 순환 의존 금지 | HIGH |

### 8.7 DTO 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| D-001 | dto 패키지의 클래스는 record여야 함 | MEDIUM |
| D-002 | Controller는 Entity를 직접 반환하지 않음 (DTO를 통해서만) | HIGH |
| D-003 | `@Autowired` 필드 주입은 프로덕션 코드에서 금지 | HIGH |

### 8.8 도메인 순수성 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| DS-001 | domain 패키지의 클래스는 Spring 프레임워크에 의존하지 않음 (@Service, @Component 등 금지) | HIGH |
| DS-002 | domain 패키지는 infrastructure, service, presentation, dto 패키지에 의존 불가 | HIGH |

### 8.9 DIP (의존성 역전) 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| DIP-001 | infrastructure 패키지의 외부 클라이언트 구현체는 service 패키지의 인터페이스를 구현해야 함 | HIGH |
| DIP-002 | service 패키지는 infrastructure의 구체 클래스에 직접 의존하지 않음 (인터페이스를 통해서만) | HIGH |

### 8.10 인증 패턴 규칙

| 규칙 ID | 규칙 설명 | 우선순위 |
|---------|-----------|---------|
| AU-001 | User API Controller는 `@AuthenticationPrincipal` 사용 | MEDIUM |
| AU-002 | Admin API Controller는 `HttpSession` 또는 Interceptor 기반 인증 | MEDIUM |

---

## 9. ArchUnit 테스트 구조 제안

### 9.1 테스트 파일 구성

```
api/src/test/java/moment/arch/
├── LayerDependencyRuleTest.java       # 레이어 의존성 (L-*)
├── ModuleBoundaryRuleTest.java        # 모듈 경계 (M-*)
├── PackageStructureRuleTest.java      # 패키지 구조 (P-*)
├── NamingConventionRuleTest.java      # 네이밍 규칙 (N-*)
├── AnnotationConsistencyRuleTest.java # 어노테이션 일관성 (A-*)
├── CyclicDependencyRuleTest.java      # 순환 의존성 (C-*)
├── DtoRuleTest.java                   # DTO 규칙 (D-*)
├── DomainPurityRuleTest.java          # 도메인 순수성 (DS-*)
└── DipRuleTest.java                   # 의존성 역전 (DIP-*)
```

> **위치 근거**: api 모듈이 common을 의존하므로 `api/src/test/`에 두면 common+api 전체 클래스를 한 번에 스캔 가능. admin 모듈 전용 규칙은 `admin/src/test/java/moment/arch/`에 별도 배치.

### 9.2 의존성 추가

```groovy
// common/build.gradle 또는 root build.gradle의 subprojects
testImplementation 'com.tngtech.archunit:archunit-junit5:1.3.0'
```

### 9.3 우선순위별 구현 순서

**Phase 1 (즉시)**: HIGH 우선순위 규칙 (21개)
- L-001~L-004: 레이어 의존성
- P-001~P-004: 패키지 구조 (컨트롤러, 서비스, 리포지토리, 엔티티)
- N-001~N-003: 핵심 네이밍 (Service, Controller, Repository)
- A-001~A-005: 핵심 어노테이션 (Transactional 위치, Soft Delete, BaseEntity)
- C-001~C-002: 순환 의존성
- D-002~D-003: 엔티티 직접 반환 금지, 필드 주입 금지
- DIP-001~DIP-002: 의존성 역전 원칙 (인터페이스 기반 의존)
- DS-001~DS-002: 도메인 순수성

**Phase 2 (단기)**: MEDIUM 우선순위 규칙 (10개)
- N-004~N-006: 세부 네이밍 (EventHandler, ApplicationService, FacadeService)
- A-006~A-007: @Valid, @Transactional(readOnly)
- P-005~P-006: DTO 패키지 위치
- D-001: DTO record 강제
- L-005~L-006: dto/eventHandler 의존성

**Phase 3 (중기)**: 확장 규칙 (3개)
- AU-001~AU-002: 인증 패턴
- M-003: 도메인 간 서비스 의존 규칙
- API URL 형식 검증

---

## 10. 선행 조건: 비일관성 해결

### 10.1 ~~패키지 구조 통일~~ (완료)

> ✅ **해결됨**: auth, storage, report 모듈 모두 `service/` 기반으로 표준화 완료.
> ArchUnit 도입에 더 이상 선행 작업 불필요.

### 10.2 Soft Delete 확인 (권장)

- `AdminGroupLog` 엔티티의 soft delete 미적용이 의도적인지 확인
- 감사 로그 성격이므로 ArchUnit 규칙(A-003)에서 예외로 허용하는 것이 적절할 수 있음

### 10.3 like 모듈 구조 결정 (선택)

- 현재 `like/service/` 아래에 하위 패키지 없이 서비스 배치
- ArchUnit 패키지 구조 규칙(P-002)에서 예외 허용 또는 `service/like/`로 이동 필요
- presentation 레이어 부재도 의도적인지 확인 필요

---

## 11. 기대 효과

1. **AI 코드 생성 품질 향상**: Claude가 생성한 코드가 ArchUnit 테스트를 통과해야 하므로, 아키텍처 위반이 즉시 감지되고 자동 수정됨
2. **코드 리뷰 효율화**: 아키텍처 규칙 검증을 자동화하여, 리뷰어가 비즈니스 로직에 집중 가능
3. **신규 팀원 온보딩**: 아키텍처 규칙이 테스트 코드로 문서화되어 명시적으로 학습 가능
4. **일관성 유지**: 프로젝트 성장에 따른 아키텍처 드리프트 방지
