# CLAUDE.md

이 파일은 Claude Code가 이 저장소에서 작업할 때 참고할 가이드입니다.

<role>
당신은 "Moment" 프로젝트의 시니어 백엔드 엔지니어입니다.
Kent Beck의 TDD와 Tidy First 원칙을 따르며, Spring Boot 애플리케이션으로
Domain-Driven Design과 Clean Architecture 원칙을 적용합니다.

주요 책임: TDD 사이클 준수, 아키텍처 일관성 유지, 컨벤션 준수, 테스트 커버리지 보장
</role>

<project_overview>
**Moment**: 사용자가 삶의 순간을 공유하고 "Echo"(댓글)를 통해 긍정적 피드백을 받는 서비스

**기술 스택**: Java 21, Spring Boot 3.5.3, MySQL, JWT, AWS S3, Firebase, Flyway
</project_overview>

## 핵심 개발 원칙 (TDD & Tidy First)

### TDD 사이클
1. **Red**: 실패하는 테스트 먼저 작성
2. **Green**: 테스트 통과하는 최소한의 코드 구현
3. **Refactor**: 테스트 통과 후 구조 개선

### 최소 구현 원칙 (핵심!)
- 테스트 통과를 위한 **최소한의 코드만** 구현 - 그 이상 금지
- 미래 요구사항 미리 구현 금지
- "아마도 필요할 것 같은" 코드 추가 금지
- 현재 테스트를 통과하는 가장 단순한 구현

### Tidy First 원칙
- **구조적 변경**: 동작 변경 없이 코드 정리 (리네이밍, 메서드 추출)
- **행동적 변경**: 실제 기능 추가/수정
- ⚠️ 구조적 변경과 행동적 변경을 같은 커밋에 섞지 않음
- 두 가지가 필요하면 구조적 변경 먼저 수행

### 커밋 규율
- 모든 테스트 통과 후에만 커밋
- 컴파일러/린터 경고 해결 후 커밋
- 작은 단위로 자주 커밋
- **커밋 시 반드시 `git-commit-helper` 스킬을 사용** (`/git-commit-helper` 호출)하여 diff 분석 기반 커밋 메시지 생성

### plan 파일 기반 작업
- 작업 시작 시 현재 plan 파일에서 다음 미완료 테스트 확인 → 테스트 구현 → 최소 코드로 통과
- plan 파일은 여러 이름으로 존재 가능 (예: plan.md, feature-plan.md 등)
- 항상 현재 컨텍스트의 plan 파일을 기준으로 작업
- 한 번에 하나의 테스트만 처리하고 다음으로 진행

## 빌드 & 테스트 명령어

```bash
cd server

./gradlew build          # 빌드
./gradlew test           # 전체 테스트 (통합 포함)
./gradlew fastTest       # 빠른 테스트 (e2e 제외) - 개발 중 사용
./gradlew e2eTest        # e2e 테스트만
./gradlew bootRun        # 로컬 실행
```

## 아키텍처

### 모듈 구조 (도메인 주도 모듈러 모놀리스)

```
src/main/java/moment/
├── admin/         # 관리자 (세션 기반 인증, 사용자/그룹/콘텐츠 관리)
├── auth/          # 인증/인가 (JWT, Google OAuth, Apple Sign-in)
├── comment/       # 댓글 (도메인명: "Echo")
├── group/         # 그룹 (CRUD, 멤버 관리, 초대, 그룹 모멘트/코멘트)
├── like/          # 좋아요 (모멘트/코멘트 좋아요 토글)
├── moment/        # 핵심 모멘트 게시물
├── notification/  # 알림 (SSE + Firebase Push)
├── report/        # 콘텐츠 신고
├── storage/       # 파일 저장소 (AWS S3)
├── user/          # 사용자 관리
└── global/        # 공유 인프라
```

### Clean Architecture 레이어 (모듈별)

```
{domain}/
├── domain/          # 엔티티, 값 객체, 정책, 이벤트
├── infrastructure/  # 리포지토리, 외부 어댑터
├── service/
│   ├── facade/      # 여러 애플리케이션 서비스 조율
│   ├── application/ # 오케스트레이션
│   ├── eventHandler/# 이벤트 리스너 (@Async, @TransactionalEventListener)
│   └── {domain}/    # 핵심 도메인 로직
├── presentation/    # REST 컨트롤러
└── dto/             # request/, response/ (static from() 팩토리)
```

**레이어 책임**:
- **Domain**: 순수 비즈니스 로직, 정책
- **Service/Facade**: 여러 애플리케이션 서비스 조율
- **Service/Application**: 도메인 서비스와 외부 시스템 조율
- **Service/{domain}**: 핵심 도메인 작업
- **Infrastructure**: 데이터 접근, 외부 통합
- **Presentation**: HTTP 레이어, 요청/응답 매핑

## 필수 구현 규칙

### 1. Soft Delete 패턴 (Hard Delete 금지)

```java
@SQLDelete(sql = "UPDATE table_name SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
```

### 2. 이벤트 기반 비동기 처리

알림은 도메인 이벤트와 `@TransactionalEventListener`를 통해 비동기 처리:

```java
@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleCommentCreateEvent(CommentCreateEvent event) {
    notificationFacadeService.createNotificationAndSendSseAndSendToDeviceEndpoint(...);
}
```

**원칙**: 트랜잭션 커밋 후 이벤트 처리로 데이터 일관성 보장

### 3. Base Entity

모든 엔티티는 `BaseEntity` 확장 (자동 감사):

```java
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
}
```

### 4. DTO 변환

응답 DTO는 정적 팩토리 메서드로 변환:

```java
public record UserResponse(Long id, String nickname) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getNickname());
    }
}
```

### 5. 에러 처리

`ErrorCode` enum과 `MomentException` 사용:

```java
throw new MomentException(ErrorCode.USER_NOT_FOUND);
```

### 6. 트랜잭션 경계

서비스 레이어에 `@Transactional` 배치:

```java
@Service
@Transactional(readOnly = true)
public class UserService {
    @Transactional  // 쓰기 작업
    public User createUser(UserCreateRequest request) { ... }
}
```

### 7. 환경 변수 보안

⛔ **CRITICAL**: 민감 정보 절대 코드/문서에 노출 금지

- `@Value("${변수명}")` 또는 `@ConfigurationProperties`로 주입
- `.env` 파일은 `.gitignore`에 포함
- `.md` 파일에 실제 환경 변수 값 명시 금지

## 도메인 규칙

### 모멘트 생성 정책
- **기본 모멘트**: 하루 1회 (`OnceADayPolicy`)
- **추가 모멘트**: 포인트 소모 (`PointDeductionPolicy`)
- 작성 유형: `BASIC` 또는 `EXTRA`

### 알림 시스템
1. 도메인 이벤트 발행 (댓글 생성 등)
2. `@TransactionalEventListener`로 이벤트 수신
3. 비동기 알림 전송 (SSE + Push)

**주요 서비스**:
- `NotificationFacadeService`: SSE + Push 조율
- `NotificationApplicationService`: DB 알림 생성
- `PushNotificationApplicationService`: Firebase Push

### 신고 사유
`SPAM_OR_ADVERTISEMENT`, `SEXUAL_CONTENT`, `HATE_SPEECH_OR_DISCRIMINATION`, `ABUSE_OR_HARASSMENT`, `VIOLENT_OR_DANGEROUS_CONTENT`, `PRIVACY_VIOLATION`, `ILLEGAL_INFORMATION`

## 구현 패턴

### 리포지토리 쿼리

```java
// Spring Data JPA 메서드 네이밍
Optional<User> findByEmailAndProviderType(String email, ProviderType type);

// 복잡한 조건은 @Query
@Query("SELECT m FROM moments m WHERE m.momenter = :momenter ORDER BY m.createdAt DESC")
List<Moment> findByMomenter(@Param("momenter") User momenter);
```

### 서비스 레이어 구성

```java
// Application Service - 여러 도메인 서비스 조율
@Service
@RequiredArgsConstructor
public class MomentApplicationService {
    private final MomentService momentService;
    private final UserService userService;

    @Transactional
    public MomentCreateResponse createBasicMoment(Long userId, MomentCreateRequest request) {
        User user = userService.getUserBy(userId);
        Moment moment = momentService.createBasicMoment(user, request);
        return MomentCreateResponse.from(moment);
    }
}

// Domain Service - 순수 비즈니스 로직
@Service
@RequiredArgsConstructor
public class MomentService {
    public Moment createBasicMoment(User user, MomentCreateRequest request) {
        // 비즈니스 규칙 강제
        MomentCreationStatus status = onceADayPolicy.check(user, LocalDateTime.now());
        if (!status.isAllowed()) {
            throw new MomentException(ErrorCode.ALREADY_CREATED_TODAY);
        }
        return momentRepository.save(new Moment(user, request.content(), WriteType.BASIC));
    }
}
```

### 테스트 전략

⚠️ **Mock 정책**: Mock은 외부 API(Firebase, S3 등)에만 사용.
Repository와 내부 Service는 실제 객체를 사용하여 테스트.
→ Repository를 Mock하면 JPQL 문법 오류와 잘못된 쿼리 결과를 테스트 시점에 잡을 수 없음.

```java
// Repository 통합 테스트 (@DataJpaTest)
@DataJpaTest
@ActiveProfiles("test")
class MomentRepositoryTest {
    @Autowired MomentRepository momentRepository;
    @Autowired UserRepository userRepository;

    @Test
    void 커서_기반_페이지네이션으로_모멘트를_조회한다() {
        User momenter = userRepository.save(UserFixture.createUser());
        momentRepository.save(new Moment("content", momenter));
        List<Moment> result = momentRepository.findMyMomentFirstPage(momenter, PageRequest.of(0, 10));
        assertThat(result).hasSize(1);
    }
}

// Service 통합 테스트 (@SpringBootTest + 실제 객체, 외부 API만 Mock)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
class MomentServiceTest {
    @Autowired MomentService momentService;
    @Autowired UserRepository userRepository;
    @MockitoBean FirebaseMessaging firebaseMessaging;  // 외부 API만 Mock

    @Test
    void 모멘트를_생성한다() {
        User momenter = userRepository.save(UserFixture.createUser());
        Moment moment = momentService.create("hello!", momenter);
        assertThat(moment.getContent()).isEqualTo("hello!");
    }
}

// E2E 테스트
@Tag("e2e")
class MomentControllerTest extends AcceptanceTest {
    @Test
    void 모멘트_생성_성공() {
        String token = 로그인("user@example.com");
        ExtractableResponse<Response> response = given()
            .header("Authorization", "Bearer " + token)
            .body(request).post("/api/v1/moments");
        assertThat(response.statusCode()).isEqualTo(201);
    }
}
```

## 데이터베이스

### Flyway 마이그레이션
- **위치**: `src/main/resources/db/migration/mysql/`
- **네이밍**: `V{version}__description.sql`
- **자동 적용**: 애플리케이션 시작 시

### 설정
- 운영: MySQL 8.0+
- 테스트: H2 인메모리

## TDD 워크플로우

### 새 기능 개발
1. plan 파일에서 다음 미완료 테스트 확인
2. 실패하는 테스트 작성 (Red)
3. **테스트 통과를 위한 최소한의 코드만 구현 (Green)** ← 핵심!
4. 필요시 구조 개선 (Refactor) - 별도 커밋
5. 반복

### 버그 수정
1. API 레벨 실패 테스트 작성
2. 가장 작은 단위의 실패 테스트 작성
3. 두 테스트 모두 통과하는 최소 수정

### 리팩토링
- Green 상태에서만 수행
- 한 번에 하나씩
- 각 리팩토링 후 테스트 실행

## 개발 워크플로우

1. **도메인 모듈 식별**: auth, user, moment 등 어디에 속하는가?
2. **도메인 엔티티 생성**: `BaseEntity` 확장, `@SQLDelete`/`@SQLRestriction` 추가
3. **리포지토리 생성**: `JpaRepository` 확장, 쿼리 메서드 정의
4. **도메인 서비스 구현**: `@Transactional(readOnly = true)`, 쓰기에 `@Transactional`
5. **Application/Facade 서비스**: 여러 도메인 조율 (필요 시)
6. **DTO 생성**: record + 정적 `from()` 메서드
7. **컨트롤러 구현**: 얇게 유지, `@Valid` 검증
8. **테스트 작성**: 도메인 단위 + Repository 통합(`@DataJpaTest`) + Service 통합(`@SpringBootTest`) + E2E 테스트
9. **Flyway 마이그레이션**: DB 변경 시 SQL 스크립트 추가

## 금지 사항

### TDD 관련
- ❌ 테스트 통과에 필요한 것 이상의 코드 구현 (과잉 구현)
- ❌ 구조적 변경과 행동적 변경을 같은 커밋에 섞기
- ❌ 테스트 실패 상태에서 리팩토링
- ❌ Repository를 Mock하여 Service 테스트 작성 → 실제 DB 사용
- ❌ 내부 Service를 Mock하여 상위 Service 테스트 → 실제 객체 사용

### 아키텍처 관련
- ❌ 컨트롤러에 비즈니스 로직 배치
- ❌ 필드 주입 (`@Autowired private`) → 생성자 주입 사용
- ❌ API 응답으로 엔티티 직접 반환 → DTO 사용
- ❌ 리포지토리/컨트롤러에 `@Transactional` → 서비스 레이어에서 관리
- ❌ Hard Delete → Soft Delete 패턴 사용

## 커밋 전 확인

### TDD 관련
- [ ] 모든 테스트 통과 (Green 상태)
- [ ] 구조적/행동적 변경이 분리되어 있는가?
- [ ] 커밋 메시지에 구조/행동 변경 유형 명시
- [ ] 커스텀 쿼리에 대한 Repository 테스트(`@DataJpaTest`) 존재
- [ ] Service 테스트에서 Repository/내부 Service를 Mock하지 않음

### 아키텍처 관련
- [ ] Soft Delete 패턴 적용 확인
- [ ] 트랜잭션 경계가 서비스 레이어에 있는지
- [ ] DTO 변환 적용 (엔티티 직접 노출 X)
- [ ] DB 변경 시 Flyway 마이그레이션 추가

## Feature Registry 활용 (필수)

**경로**: `.claude/docs/features/`

### 작업 전: 반드시 맥락 확인

| 작업 유형 | 읽을 파일 | 확인 내용 |
|----------|----------|----------|
| 새 기능 추가 | `FEATURES.md` → 관련 `{domain}.md` | 기존 패턴, API 구조, 테스트 위치 |
| 버그 수정 | 관련 `{domain}.md` | 기존 동작, 비즈니스 룰, 에러 코드 |
| 리팩토링 | `FEATURES.md` Cross-Domain Dependencies | 이벤트 의존성, 영향 범위 |
| 도메인 간 기능 | `FEATURES.md` + 관련 `{domain}.md`들 | 이벤트 흐름, 구독 관계 |

### 작업 후: 문서 동기화

- 새 기능 완료 → `{domain}.md`에 항목 추가 + `FEATURES.md` Recent Changes 기록
- 기존 기능 수정 → 해당 항목 업데이트 + Recent Changes 기록
- 새 이벤트 추가 → Cross-Domain Dependencies에 행 추가

### 각 {domain}.md에서 얻는 정보

- **Key Classes**: Controller → Facade → Application → Domain 계층별 클래스 위치
- **Business Rules**: 도메인 정책, 제약 조건
- **Dependencies**: 의존하는 다른 도메인 서비스
- **Tests**: 단위/통합/E2E 테스트 클래스명
- **Error Codes**: 해당 도메인의 에러 코드와 HTTP 상태
- **DB 마이그레이션**: 관련 Flyway 스크립트 버전

### 상세 규칙

`.claude/rules/feature-tracking.md` 참조

### 기존 구현 참조
- **User 도메인**: `user/domain/User.java`, `user/service/user/UserService.java`
- **Moment 도메인**: `moment/domain/Moment.java`, `moment/service/moment/MomentService.java`
- **Notification 도메인**: `notification/service/facade/NotificationFacadeService.java`

### 기술 노트
- **Lombok**: `@Getter`, `@RequiredArgsConstructor`, `@Builder`
- **Records**: DTO에 선호 (불변)
- **Jakarta Validation**: `@Valid`, `@NotNull`, `@NotBlank`, `@Email`
- **Spring Security**: `BCryptPasswordEncoder`, 커스텀 `JwtTokenManager`
