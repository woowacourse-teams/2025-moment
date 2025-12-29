# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고할 가이드를 제공합니다.

<role>
당신은 "Moment" 프로젝트의 시니어 백엔드 엔지니어입니다. Moment는 사용자들이 삶의 순간을 공유하고 긍정적인 상호작용을 통해 정서적 지지를 받는 Spring Boot 애플리케이션입니다. 백엔드는 Domain-Driven Design과 Clean Architecture 원칙을 따릅니다.

주요 책임:
- 도메인 모듈 간 아키텍처 일관성 유지
- 확립된 패턴과 컨벤션 준수
- 코드 품질 및 테스트 커버리지 보장
- 도메인 비즈니스 규칙 및 정책 보존
</role>

<project_overview>
**Moment**는 사용자가:
- 삶의 순간 공유 (기쁜/슬픈/힘든 순간)
- "Echo" (댓글)를 통한 긍정적 피드백 수령
- 보상 획득 및 레벨업
- 실시간 알림 수신 (SSE + Push)

**기술 스택**: Java 21, Spring Boot 3.5.3, MySQL, JWT, AWS S3, Firebase, Flyway
</project_overview>

## 빌드 & 테스트 명령어

<commands>
```bash
# 먼저 server 디렉토리로 이동
cd server

# 빌드
./gradlew build

# 테스트 실행 (전체 스위트, 통합 테스트 포함)
./gradlew test

# 빠른 테스트 (e2e 태그 제외) - 개발 중 사용
./gradlew fastTest

# e2e 테스트만 실행
./gradlew e2eTest

# 로컬에서 애플리케이션 실행
./gradlew bootRun

# SonarQube 분석
./gradlew sonar
```
</commands>

## 아키텍처

<architecture>
### 모듈 구조

코드베이스는 기능 모듈별로 구성된 **도메인 주도 모듈러 모놀리스**를 따릅니다:

```
src/main/java/moment/
├── auth/              # 인증 및 인가 (JWT, 토큰)
├── comment/           # 댓글 (도메인명: "Echo")
├── moment/            # 핵심 모멘트 게시물
├── notification/      # 알림 (SSE + Firebase Push)
├── report/            # 콘텐츠 신고 시스템
├── reward/            # 보상/포인트 시스템
├── storage/           # 파일 저장소 (AWS S3)
├── user/              # 사용자 관리 & 레벨
└── global/            # 공유 인프라 (예외, 베이스 엔티티)
```

### Clean Architecture 레이어 (모듈별)

각 모듈은 다음 레이어 구조를 따릅니다:

```
{domain}/
├── domain/            # 엔티티, 값 객체, 도메인 정책, 이벤트
│                      # 예시: OnceADayPolicy, CommentCreateEvent
├── infrastructure/    # 리포지토리, 외부 어댑터
│                      # 예시: UserRepository, S3Adapter
├── service/
│   ├── facade/        # 여러 애플리케이션 서비스 조율 (최상위 파사드)
│   │                  # 예시: NotificationFacadeService
│   ├── application/   # 애플리케이션 오케스트레이션
│   │                  # 예시: NotificationApplicationService, PushNotificationApplicationService
│   ├── eventHandler/  # 이벤트 리스너 (@TransactionalEventListener, @Async)
│   │                  # 예시: NotificationEventHandler
│   └── {domain}/      # 핵심 도메인 비즈니스 로직
│                      # 예시: UserService, NotificationService
├── presentation/      # REST 컨트롤러 (@RestController)
└── dto/
    ├── request/       # 요청 DTO
    └── response/      # 응답 DTO (static from() 팩토리 사용)
```

### 레이어 책임

| 레이어 | 책임 | 예시 |
|-------|-----|------|
| **Domain** | 순수 비즈니스 로직, 정책, 엔티티, 도메인 이벤트 | `OnceADayPolicy`, `User`, `CommentCreateEvent` |
| **Service/Facade** | 여러 애플리케이션 서비스 조율 (최상위 파사드) | `NotificationFacadeService` |
| **Service/Application** | 도메인 서비스와 외부 시스템 조율 | `NotificationApplicationService` |
| **Service/EventHandler** | 비동기 이벤트 처리 | `NotificationEventHandler` |
| **Service/{domain}** | 핵심 도메인 작업 | `UserService.getUserBy()`, `NotificationService` |
| **Infrastructure** | 데이터 접근, 외부 통합 | `UserRepository`, `S3FileStorage` |
| **Presentation** | HTTP 레이어, 요청/응답 매핑 | `UserController` |
</architecture>

<critical_rules>
## 필수 구현 규칙

### 1. Soft Delete 패턴 (절대 Hard Delete 금지)
모든 엔티티는 Hibernate의 `@SQLDelete`와 `@SQLRestriction`을 사용합니다:

```java
@SQLDelete(sql = "UPDATE table_name SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

**해야 할 것**: Soft Delete를 트리거하는 `repository.delete()` 사용
**하지 말아야 할 것**: 데이터베이스에서 직접 삭제하거나 네이티브 쿼리로 Hard Delete

### 2. 이벤트 기반 비동기 처리
알림은 도메인 이벤트와 `@TransactionalEventListener`를 통해 비동기로 처리됩니다:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentCreateEvent(CommentCreateEvent event) {
    notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
        event.momenterId(),
        event.momentId(),
        NotificationType.NEW_COMMENT_ON_MOMENT,
        TargetType.MOMENT,
        PushNotificationMessage.REPLY_TO_MOMENT
    );
}
```

**해야 할 것**:
- 비동기 작업에 `@Async`와 `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` 사용
- 트랜잭션 커밋 후 이벤트 처리로 데이터 일관성 보장
- `@EnableAsync`가 메인 애플리케이션 클래스에 선언되어 있어야 함

**하지 말아야 할 것**:
- 동기적으로 알림 전송 (응답 시간 증가)
- 트랜잭션 커밋 전에 이벤트 처리 (데이터 불일치 위험)

### 3. Base Entity 패턴
모든 엔티티는 자동 감사를 위해 `BaseEntity`를 확장합니다:

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
}
```

**해야 할 것**: 새 엔티티에 `BaseEntity` 확장
**하지 말아야 할 것**: `createdAt` 필드 수동 관리

### 4. DTO 변환 패턴
응답 DTO는 정적 팩토리 메서드를 사용하여 엔티티에서 변환:

```java
public record UserResponse(Long id, String nickname, Level level) {
    // 정적 팩토리 메서드로 엔티티 → DTO 변환
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getNickname(),
            user.getLevel()
        );
    }
}
```

**원칙**:
- DTO 변환 로직은 DTO 내부에 캡슐화
- 컨트롤러는 변환된 DTO만 반환
- 복잡한 변환이 필요한 경우 별도 Mapper 고려

### 5. 에러 처리
도메인별로 사전 정의된 `ErrorCode` enum과 함께 `MomentException` 사용:

```java
// ErrorCode enum에 도메인별 에러 정의
public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    MOMENT_NOT_FOUND("모멘트를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    // ...
}

// 사용법
throw new MomentException(ErrorCode.USER_NOT_FOUND);
```

**원칙**:
- 예외는 ErrorCode enum을 통해 일관되게 관리
- 새로운 예외 타입보다는 ErrorCode 추가로 확장

### 6. 트랜잭션 경계
리포지토리가 아닌 서비스 레이어에 `@Transactional` 배치:

```java
@Service
@Transactional(readOnly = true)
public class UserService {

    @Transactional  // 쓰기 작업
    public User createUser(UserCreateRequest request) {
        // 비즈니스 로직
    }
}
```

**해야 할 것**: 서비스 레이어에서 트랜잭션
**하지 말아야 할 것**: 리포지토리나 컨트롤러 레이어에서 트랜잭션
</critical_rules>

<domain_rules>
## 중요한 도메인 비즈니스 규칙

### 모멘트 생성 정책
- **기본 모멘트**: 하루 1회 (OnceADayPolicy로 강제)
- **추가 모멘트**: 포인트 소모 (PointDeductionPolicy로 강제)
- 작성 유형: `BASIC` 또는 `EXTRA` (enum WriteType)

<example>
```java
// OnceADayPolicy는 사용자가 오늘 이미 기본 모멘트를 생성했는지 확인
public class OnceADayPolicy implements MomentCreatePolicy {
    @Override
    public MomentCreationStatus check(User user, LocalDateTime now) {
        if (user.hasCreatedBasicMomentToday(now)) {
            return MomentCreationStatus.ALREADY_CREATED_TODAY;
        }
        return MomentCreationStatus.ALLOWED;
    }
}
```
</example>

### 사용자 레벨 시스템
사용자는 `expStar` 기반 15개 레벨을 가집니다:
- `ASTEROID_WHITE` (0-4) → `GAS_GIANT_SKY` (32000+)
- `User.addStarAndUpdateLevel()`을 통한 자동 레벨 업데이트

<example>
```java
public void addStarAndUpdateLevel(int pointToAdd) {
    this.availableStar += pointToAdd;
    if (pointToAdd >= 0) {
        this.expStar += pointToAdd;
    }
    this.level = Level.getLevel(this.expStar);  // 자동 업데이트
}
```
</example>

### 알림 시스템 (이벤트 기반 아키텍처)
알림은 도메인 이벤트를 통해 트랜잭션 커밋 후 비동기로 처리됩니다:

1. **도메인 이벤트 발행**: 댓글 생성, Echo 생성 등
2. **이벤트 핸들러**: `@TransactionalEventListener`로 이벤트 수신
3. **비동기 알림 전송**: SSE + Push 알림

**아키텍처 레이어**:
- `NotificationFacadeService`: SSE + Push 알림 조율
- `NotificationApplicationService`: DB 알림 생성
- `PushNotificationApplicationService`: Firebase Push 전송
- `NotificationEventHandler`: 이벤트 리스닝 및 비동기 처리

<example>
```java
// 1. 이벤트 발행 (댓글 서비스)
@Transactional
public Comment createComment(Long momentId, CommentCreateRequest request) {
    Comment comment = commentRepository.save(new Comment(...));

    // 도메인 이벤트 발행
    eventPublisher.publishEvent(new CommentCreateEvent(
        comment.getMoment().getUser().getId(),
        momentId,
        comment.getId()
    ));

    return comment;
}

// 2. 이벤트 핸들러 (비동기 처리)
@Component
public class NotificationEventHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        // 트랜잭션 커밋 후 실행 (데이터 일관성 보장)
        notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(
            event.momenterId(),
            event.momentId(),
            NotificationType.NEW_COMMENT_ON_MOMENT,
            TargetType.MOMENT,
            PushNotificationMessage.REPLY_TO_MOMENT
        );
    }
}

// 3. Facade 서비스 (SSE + Push 조율)
@Service
public class NotificationFacadeService {

    public void createNotificationAndSendSseAndSendToDeviceEndpoint(
        Long userId, Long targetId,
        NotificationType type, TargetType targetType,
        PushNotificationMessage message
    ) {
        // 3-1. DB 알림 생성 및 SSE 전송
        Notification notification = notificationApplicationService.createNotification(
            userId, targetId, type, targetType
        );
        sseNotificationService.sendToClient(userId, "notification",
            NotificationSseResponse.from(notification));

        // 3-2. Push 알림 전송
        pushNotificationApplicationService.sendToDeviceEndpoint(userId, message);
    }
}
```
</example>

### 신고 시스템
사용자는 사전 정의된 사유로 콘텐츠를 신고할 수 있습니다:
- `SPAM_OR_ADVERTISEMENT` (스팸 또는 광고)
- `SEXUAL_CONTENT` (성적인 콘텐츠)
- `HATE_SPEECH_OR_DISCRIMINATION` (혐오 발언 또는 차별)
- `ABUSE_OR_HARASSMENT` (학대 또는 괴롭힘)
- `VIOLENT_OR_DANGEROUS_CONTENT` (폭력적이거나 위험한 콘텐츠)
- `PRIVACY_VIOLATION` (개인정보 침해)
- `ILLEGAL_INFORMATION` (불법 정보)
</domain_rules>

<patterns>
## 일반적인 구현 패턴

### 패턴 1: 리포지토리 쿼리
Spring Data JPA 메서드 네이밍 컨벤션 활용:

<example>
```java
// Spring Data JPA 쿼리 메서드
Optional<User> findByEmailAndProviderType(String email, ProviderType type);
List<User> findAllByIdIn(List<Long> ids);

// 복잡한 조건은 @Query 활용
@Query("SELECT u FROM users u WHERE u.expStar >= :minStar")
List<User> findUsersAboveLevel(@Param("minStar") int minStar);
```

**원칙**:
- Spring Data JPA 메서드 네이밍으로 간단한 쿼리 표현
- 복잡한 조건은 JPQL `@Query` 사용
- 네이티브 쿼리는 최후의 수단으로 사용
</example>

### 패턴 2: 서비스 레이어 구성

<example>
```java
// Application Service (Facade) - 여러 도메인 서비스 조율
@Service
@RequiredArgsConstructor
public class MomentApplicationService {

    private final MomentService momentService;
    private final UserService userService;
    private final RewardService rewardService;
    private final NotificationFacadeService notificationFacadeService;

    @Transactional
    public MomentCreateResponse createBasicMoment(Long userId, MomentCreateRequest request) {
        // 1. 사용자 조회 (UserService)
        User user = userService.getUserBy(userId);

        // 2. 모멘트 생성 (MomentService)
        Moment moment = momentService.createBasicMoment(user, request);

        // 3. 사용자에게 보상 (RewardService)
        rewardService.rewardForMomentCreation(user);

        // 4. 알림 전송 (NotificationFacadeService)
        notificationFacadeService.notifyFollowers(moment);

        return MomentCreateResponse.from(moment);
    }
}

// Domain Service - 순수 비즈니스 로직
@Service
@RequiredArgsConstructor
public class MomentService {

    private final MomentRepository momentRepository;
    private final OnceADayPolicy onceADayPolicy;

    public Moment createBasicMoment(User user, MomentCreateRequest request) {
        // 비즈니스 규칙 강제
        MomentCreationStatus status = onceADayPolicy.check(user, LocalDateTime.now());
        if (!status.isAllowed()) {
            throw new MomentException(ErrorCode.ALREADY_CREATED_TODAY);
        }

        Moment moment = new Moment(user, request.content(), WriteType.BASIC);
        return momentRepository.save(moment);
    }
}
```
</example>

### 패턴 3: 비동기 메서드

<example>
```java
// 해야 할 것: @Async와 적절한 반환 타입을 가진 비동기 메서드
@Async
public CompletableFuture<Void> sendPushNotificationAsync(PushNotificationCommand command) {
    firebaseMessaging.send(message);
    return CompletableFuture.completedFuture(null);
}

// 하지 말아야 할 것: 비동기 컨텍스트에서 동기 메서드
@Async
public void sendPushNotification(PushNotificationCommand command) {
    // 비동기 실행자 스레드를 블록킹함
    Thread.sleep(1000);
}
```
</example>

### 패턴 4: 테스트 전략

<example>
```java
// 단위 테스트 (빠름)
class UserServiceTest {
    @Test
    void getUserBy_존재하는_사용자_반환() {
        // given
        User user = new User("test@example.com", "password", "nickname", ProviderType.EMAIL);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        User result = userService.getUserBy(1L);

        // then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }
}

// 통합 테스트 (E2E - 태그됨)
@Tag("e2e")
class MomentControllerTest extends AcceptanceTest {
    @Test
    void 모멘트_생성_성공() {
        // given
        String token = 로그인("user@example.com");

        // when
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .body(request)
            .post("/api/v1/moments");

        // then
        assertThat(response.statusCode()).isEqualTo(201);
    }
}
```
</example>
</patterns>

<database>
## 데이터베이스 & 마이그레이션

### Flyway 마이그레이션
- **위치**: `src/main/resources/db/migration/mysql/`
- **네이밍**: `V{version}__description.sql`
- **자동 적용**: 애플리케이션 시작 시

<example>
```sql
-- V001__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(15) NOT NULL,
    provider_type VARCHAR(20) NOT NULL,
    available_star INT NOT NULL DEFAULT 0,
    exp_star INT NOT NULL DEFAULT 0,
    level VARCHAR(50),
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_email (email),
    INDEX idx_deleted_at (deleted_at)
);
```
</example>

### 데이터베이스 설정
- **운영**: MySQL 8.0+
- **테스트**: H2 인메모리
- **커넥션 풀**: HikariCP (기본값)
</database>

<workflow>
## 개발 워크플로우

### 단계별 기능 개발

1. **도메인 모듈 식별**
   - auth, user, moment, notification 등 어디에 속하는가?
   - 횡단 관심사라면 `global/` 고려

2. **도메인 엔티티 생성 (필요시)**
   - `BaseEntity` 확장
   - `@SQLDelete`와 `@SQLRestriction` 추가
   - 엔티티 메서드에 비즈니스 로직 구현

3. **리포지토리 생성**
   - `JpaRepository<Entity, Long>` 확장
   - 메서드 네이밍 컨벤션 사용
   - 필요시 커스텀 `@Query` 추가

4. **도메인 서비스 구현**
   - `service/{domain}/`에 배치
   - 클래스 레벨에 `@Transactional(readOnly = true)` 추가
   - 쓰기 작업에 `@Transactional` 표시

5. **애플리케이션 서비스 구현 (오케스트레이션 필요시)**
   - `service/application/`에 배치
   - 여러 도메인 서비스 조율

6. **DTO 생성**
   - 불변성을 위해 `record` 사용
   - 정적 `from()` 팩토리 메서드 추가

7. **컨트롤러 구현**
   - `presentation/`에 배치
   - `@RestController` 사용
   - `@Valid`로 검증

8. **테스트 작성**
   - 서비스를 위한 단위 테스트
   - `@Tag("e2e")`를 사용한 통합 테스트
   - 개발 중 `./gradlew fastTest` 실행

9. **Flyway 마이그레이션 생성 (DB 변경 시)**
   - 새 마이그레이션 파일 추가
   - 깨끗한 데이터베이스로 테스트

10. **Swagger 문서 업데이트**
    - Springdoc OpenAPI가 자동 생성
    - `/swagger-ui.html`에서 확인

### 커밋 전

```bash
# 빠른 테스트 실행
./gradlew fastTest

# 전체 테스트 스위트 실행
./gradlew test

# 코드 커버리지 확인 (선택)
./gradlew jacocoTestReport
```
</workflow>

<technology_notes>
## 주요 기술 노트

### Lombok
광범위하게 사용 - 보일러플레이트 작성 피하기:
- `@Getter`, `@Setter`
- `@NoArgsConstructor`, `@AllArgsConstructor`
- `@RequiredArgsConstructor` (`final` 필드용)
- `@Builder`

### Records (Java 14+)
DTO에 선호 - 기본적으로 불변:
```java
public record UserResponse(Long id, String nickname) {
    // 자동: 생성자, getter, equals, hashCode, toString
}
```

### Jakarta Validation
검증을 위한 애노테이션 사용:
- `@Valid`, `@NotNull`, `@NotBlank`
- `@Email`, `@Size`, `@Min`, `@Max`

### Spring Security
- 비밀번호 인코딩: `BCryptPasswordEncoder`
- JWT: 커스텀 `JwtTokenManager` (Spring Security JWT 아님)

### AWS 통합
- **S3**: 모멘트 이미지를 위한 파일 저장소
- **CloudWatch**: 애플리케이션 메트릭
</technology_notes>

<examples>
## 구현 참고 예시

프로젝트의 기존 도메인을 참고하여 패턴을 학습하세요:

### 참고할 기존 구현
- **User 도메인**: `user/domain/User.java`, `user/service/user/UserService.java`
  - BaseEntity 확장, Soft Delete 패턴, 레벨 시스템 로직
- **Moment 도메인**: `moment/domain/Moment.java`, `moment/service/moment/MomentService.java`
  - 정책 패턴(OnceADayPolicy), 도메인 로직 위치
- **Notification 도메인**: `notification/service/facade/NotificationFacadeService.java`
  - Facade 패턴, 이벤트 기반 비동기 처리

### 새 기능 추가 시 흐름
1. **도메인 엔티티** → BaseEntity 확장, @SQLDelete/@SQLRestriction 추가
2. **리포지토리** → JpaRepository 확장, 쿼리 메서드 정의
3. **도메인 서비스** → 비즈니스 로직 구현, @Transactional 관리
4. **Application/Facade 서비스** → 여러 도메인 조율 (필요 시)
5. **DTO** → record + 정적 팩토리 메서드
6. **컨트롤러** → 얇게 유지, 서비스 호출 + DTO 반환
7. **테스트** → 단위 테스트 + E2E 테스트
8. **마이그레이션** → Flyway SQL 스크립트

구체적인 구현은 기존 코드 참고 후 프로젝트 컨벤션을 따르세요.
</examples>

<anti_patterns>
## 아키텍처 원칙 위반 사례

### 1. 레이어 책임 위반
**문제**: 컨트롤러에 비즈니스 로직 배치
- 비즈니스 규칙 검증을 컨트롤러에서 수행
- 정책 로직을 프레젠테이션 레이어에 노출

**해결**: 비즈니스 로직은 서비스 레이어에 위치
- 컨트롤러는 요청/응답 변환과 서비스 호출만 담당
- 도메인 정책은 Policy 클래스 또는 도메인 서비스에 캡슐화

### 2. 의존성 주입 안티패턴
**문제**: 필드 주입 사용 (`@Autowired private ...`)
- 테스트 어려움, 불변성 보장 불가
- 순환 참조 발견 지연

**해결**: 생성자 주입 사용
- `@RequiredArgsConstructor`로 final 필드 주입
- 의존성 명시적 표현, 테스트 용이

### 3. 엔티티 노출
**문제**: API 응답으로 엔티티 직접 반환
- 내부 구조 노출, 순환 참조 위험
- API 스펙이 도메인 모델에 종속

**해결**: DTO 레이어 분리
- 응답은 항상 DTO로 변환
- 엔티티는 서비스 레이어 내부에서만 사용

### 4. Hard Delete
**문제**: 데이터베이스에서 직접 삭제 (DELETE 쿼리)
- 데이터 복구 불가, 감사 추적 어려움
- 외래 키 제약으로 인한 복잡성

**해결**: Soft Delete 패턴 사용
- @SQLDelete로 deletedAt 업데이트
- @SQLRestriction으로 조회 시 자동 필터링

### 5. 트랜잭션 경계 오류
**문제**: 컨트롤러나 리포지토리에 @Transactional
- 잘못된 트랜잭션 범위 설정
- 비즈니스 로직 단위와 불일치

**해결**: 서비스 레이어에서 트랜잭션 관리
- 클래스 레벨 readOnly=true, 쓰기 메서드에 @Transactional
- 비즈니스 유스케이스 단위로 트랜잭션 경계 설정
</anti_patterns>

---

## 구현 체크리스트

새 기능 구현 시 아키텍처 원칙 준수 확인:

### 도메인 & 인프라
- [ ] 엔티티가 `BaseEntity`를 확장하여 감사 기능 활용
- [ ] Soft Delete 패턴 적용 (`@SQLDelete`, `@SQLRestriction`)
- [ ] 리포지토리는 Spring Data JPA 메서드 네이밍 또는 `@Query` 사용

### 서비스 & 비즈니스 로직
- [ ] 비즈니스 로직이 서비스 레이어에 위치
- [ ] 트랜잭션 경계가 서비스 레이어에서 관리됨
- [ ] 비동기 작업은 이벤트 기반으로 트랜잭션 커밋 후 처리

### API & 인터페이스
- [ ] DTO 패턴 사용 (엔티티 직접 노출 금지)
- [ ] DTO 변환 로직이 DTO 내부에 캡슐화
- [ ] 에러 처리가 `ErrorCode` enum으로 일관되게 관리

### 테스트 & 품질
- [ ] 도메인 로직에 대한 단위 테스트 작성
- [ ] E2E 테스트에 `@Tag("e2e")` 적용
- [ ] 커밋 전 `./gradlew fastTest` 실행으로 빠른 피드백

### 데이터베이스
- [ ] DB 스키마 변경 시 Flyway 마이그레이션 생성
- [ ] 마이그레이션 파일 네이밍 컨벤션 준수

이 체크리스트는 아키텍처 일관성 유지를 위한 최소 기준입니다.
