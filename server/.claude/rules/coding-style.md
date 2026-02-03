# Coding Style

## Immutability (CRITICAL)

ALWAYS use immutable DTOs. Use Java `record` for all request/response DTOs.

### Request DTOs

모든 Request DTO에는 `@Schema` (Swagger) + Jakarta Validation 어노테이션을 함께 사용:

```java
// WRONG: Mutable class DTO
public class UserCreateRequest {
    private String email;
    public void setEmail(String email) { this.email = email; }
}

// CORRECT: Immutable record DTO with @Schema
@Schema(description = "회원가입 요청")
public record UserCreateRequest(
    @Schema(description = "사용자 이메일(아이디)", example = "mimi@icloud.com")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "EMAIL_INVALID")
    @Size(max = 255, message = "이메일은 최대 {max}자를 초과할 수 없습니다.")
    String email,

    @Schema(description = "사용자 비밀번호", example = "hipopo12!")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$", message = "PASSWORD_INVALID")
    String password,

    @Schema(description = "비밀번호 확인", example = "hipopo12!")
    String rePassword,

    @Schema(description = "사용자 닉네임", example = "mimi")
    @NotBlank(message = "NICKNAME_INVALID")
    String nickname
) {}
```

### Response DTOs

Response DTO에는 `@Schema` + 정적 팩토리 메서드 (`from()` 또는 `of()`):

```java
// 단일 엔티티 변환: from()
@Schema(description = "유저 프로필 응답")
public record UserProfileResponse(
    @Schema(description = "사용자 닉네임", example = "mimi")
    String nickname
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(user.getNickname());
    }
}

// 복합 엔티티 변환 또는 오버로딩 필요 시: of()
@Schema(description = "모멘트 응답")
public record MomentCreateResponse(
    @Schema(description = "모멘트 id", example = "1")
    Long id,
    @Schema(description = "모멘트 작성자", example = "1")
    Long momenterId,
    @Schema(description = "모멘트 작성 시간", example = "2025-07-14T16:24:34Z")
    LocalDateTime createdAt,
    @Schema(description = "모멘트 내용", example = "야근 힘들어용")
    String content,
    @Schema(description = "모멘트 이미지", example = "1")
    Long imageId
) {
    public static MomentCreateResponse of(Moment moment) {
        return new MomentCreateResponse(moment.getId(), moment.getMomenter().getId(),
            moment.getCreatedAt(), moment.getContent(), null);
    }

    public static MomentCreateResponse of(Moment moment, MomentImage momentImage) {
        return new MomentCreateResponse(moment.getId(), moment.getMomenter().getId(),
            moment.getCreatedAt(), moment.getContent(), momentImage.getId());
    }
}
```

**팩토리 메서드 규칙**:
- `from()`: 단일 엔티티 → DTO 변환 (1:1 매핑)
- `of()`: 복합 엔티티 변환 또는 오버로딩이 필요한 경우

## File Organization

Domain-driven modular monolith - organize by feature/domain, not by type:

```
{domain}/
├── domain/          # 엔티티, 값 객체, 정책, 이벤트
├── infrastructure/  # 리포지토리, 외부 어댑터
├── service/
│   ├── facade/      # 여러 애플리케이션 서비스 조율 + 이벤트 발행
│   ├── application/ # 도메인 서비스와 외부 시스템 조율
│   ├── eventHandler/# 이벤트 리스너 (@Async, @TransactionalEventListener)
│   └── {domain}/    # 핵심 도메인 비즈니스 로직
├── presentation/    # REST 컨트롤러
└── dto/
    ├── request/     # Request DTOs (records + @Schema)
    ├── response/    # Response DTOs (records + @Schema + from()/of())
    └── {Event}.java # 도메인 이벤트 (record)
```

Guidelines:
- High cohesion, low coupling per module
- Keep classes focused and single-responsibility
- Extract policies/strategies from large domain classes
- Controllers should be thin - delegate to services
- 이벤트 record는 `dto/` 패키지 직접 하위에 위치

## Naming Conventions

### Classes

| Layer | Pattern | Example |
|-------|---------|---------|
| Entity | `{Name}` (singular) | `User`, `Moment`, `Comment` |
| Controller | `{Resource}Controller` | `UserController`, `MomentController` |
| Domain Service | `{Domain}Service` | `UserService`, `MomentService` |
| Application Service | `{Domain}ApplicationService` | `MomentApplicationService` |
| Facade Service | `{Domain}{Action}FacadeService` | `CommentCreateFacadeService`, `MomentCreateFacadeService` |
| Event Handler | `{Domain}EventHandler` | `NotificationEventHandler` |
| Repository | `{Entity}Repository` | `UserRepository`, `MomentRepository` |
| Request DTO | `{Resource}{Action}Request` | `UserCreateRequest`, `MomentCreateRequest` |
| Response DTO | `{Resource}{Purpose}Response` | `UserProfileResponse`, `MomentCreateResponse` |
| Domain Event | `{Domain}{Action}Event` | `CommentCreateEvent`, `GroupJoinRequestEvent` |
| Exception | `MomentException` (singleton) | `throw new MomentException(ErrorCode.XXX)` |
| Test | `{Class}Test` | `UserServiceTest`, `MomentServiceTest` |
| Test Fixture | `{Entity}Fixture` | `UserFixture`, `MomentFixture` |
| Test Helper | `{Entity}{Purpose}Helper` | `MomentCreatedAtHelper` |

### Methods

| Purpose | Pattern | Example |
|---------|---------|---------|
| Get (throws if not found) | `getBy{Property}()` | `getUserBy(Long id)` |
| Find (returns Optional) | `findBy{Property}()` | `findByEmail(String email)` |
| Find multiple | `findAllBy{Property}()` | `findAllByIdIn(List<Long> ids)` |
| Check existence | `existsBy{Property}()` | `existsByNickname(String nickname)` |
| Create | `create{Entity}()` | `createBasicMoment()` |
| Update | `update{Property}()` | `updateNickname()` |
| Delete | `delete{Entity}()` | `deleteMoment()` |

### Test Methods

Use Korean descriptive names with underscore separation:

```java
@DisplayNameGeneration(ReplaceUnderscores.class)
class UserServiceTest {
    @Test
    void 일반_회원가입_유저를_추가한다() { ... }

    @Test
    void 중복된_닉네임이_존재하는_경우_유저를_추가할_수_없다() { ... }
}
```

### API Endpoints

- Base path: `/api/v2/{resources}` (plural)
- Nested resources: `/api/v2/moments/{id}/comments`
- Example paths: `/api/v2/users/signup`, `/api/v2/moments/writable/basic`

## Error Handling

ALWAYS use `MomentException` with `ErrorCode` enum. NEVER throw raw exceptions in service layer:

```java
// WRONG: Raw exception
throw new RuntimeException("User not found");

// CORRECT: Domain exception with error code
throw new MomentException(ErrorCode.USER_NOT_FOUND);
```

ErrorCode format: `{PREFIX}-{NUMBER}` with Korean user-friendly messages:

```java
USER_NOT_FOUND("U-002", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND),
ALREADY_CREATED_TODAY("M-001", "오늘 이미 모멘트를 작성했습니다.", HttpStatus.CONFLICT),
```

Domain entity constructor validation uses `IllegalArgumentException`:

```java
private void validateEmail(String email) {
    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("email이 null이거나 빈 값이어서는 안 됩니다.");
    }
}
```

## Input Validation

ALWAYS validate at two layers:

### 1. DTO Layer (Jakarta Validation)

```java
public record UserCreateRequest(
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
             message = "EMAIL_INVALID")
    @NotBlank(message = "EMAIL_INVALID")
    String email,

    @Pattern(regexp = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[a-zA-Z\\d!@#$%^&*()]{8,16}$",
             message = "PASSWORD_INVALID")
    @NotBlank(message = "PASSWORD_INVALID")
    String password
) {}
```

### 2. Domain Layer (Constructor Validation)

```java
public User(String email, String password, String nickname) {
    validate(email, password, nickname);
    this.email = email;
    this.password = password;
    this.nickname = nickname;
}
```

Controllers must use `@Valid`:

```java
@PostMapping
public ResponseEntity<SuccessResponse<CreateResponse>> create(
    @Valid @RequestBody CreateRequest request) { ... }
```

## Logging

Use `@Slf4j` with Logstash structured logging:

```java
@Slf4j
public class GlobalExceptionHandler {
    log.warn("Handled MomentException",
        kv("errorCode", errorCode.name()),
        kv("status", exception.getStatus()),
        kv("errorMessage", exception.getMessage())
    );
}
```

Rules:
- Use `log.error()` for unrecoverable errors
- Use `log.warn()` for handled exceptions and validation failures
- Use `log.info()` for significant business events
- Use `log.debug()` for repository operations and method calls
- NEVER log sensitive data (passwords, tokens, personal info)

## Dependency Injection

ALWAYS use constructor injection via Lombok:

```java
// WRONG: Field injection
@Autowired
private UserRepository userRepository;

// CORRECT: Constructor injection
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
}
```

## Transaction Management

Place `@Transactional` at service layer only:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default: read-only
public class UserService {
    @Transactional  // Override for write operations
    public User createUser(...) { ... }

    public User getUserBy(Long id) { ... }  // Inherits readOnly = true
}
```

## Code Formatting

- Indentation: 4 spaces (not tabs)
- Opening brace on same line: `public void method() {`
- No wildcard imports - each class explicitly imported
- Annotations on separate lines
- Record components: one per line for multiple fields

## Code Quality Checklist

Before marking work complete:
- [ ] Code is readable with clear naming (classes, methods, variables)
- [ ] Methods are focused and single-responsibility
- [ ] DTOs are immutable records with static `from()`/`of()` factory methods
- [ ] No deep nesting (>4 levels) - extract methods instead
- [ ] Error handling uses `MomentException` + `ErrorCode`
- [ ] No `System.out.println` - use `@Slf4j` logging
- [ ] No hardcoded values - use `@Value` or constants
- [ ] Constructor injection only (no `@Autowired` field injection)
- [ ] `@Transactional` boundaries are at service layer
- [ ] Soft delete pattern applied to new entities
- [ ] Test methods use Korean descriptive names