# Common Patterns

## API Response Format

User API and Admin API use **separate response wrappers** with identical structure but different types.

### User API (`SuccessResponse` / `ErrorResponse`)

```java
public record SuccessResponse<T>(int status, T data) {
    public static <T> SuccessResponse<T> of(HttpStatus httpStatus, T data) {
        return new SuccessResponse<>(httpStatus.value(), data);
    }
}

public record ErrorResponse(String code, String message, int status) {
    public static ErrorResponse from(ErrorCode errorCode) { ... }
}
```

### Admin API (`AdminSuccessResponse` / `AdminErrorResponse`)

```java
public record AdminSuccessResponse<T>(int status, T data) {
    public static <T> AdminSuccessResponse<T> of(HttpStatus httpStatus, T data) {
        return new AdminSuccessResponse<>(httpStatus.value(), data);
    }
}

public record AdminErrorResponse(String code, String message, int status) {
    public static AdminErrorResponse from(AdminErrorCode errorCode) { ... }
}
```

### Error Code Separation

| Module | Exception | ErrorCode Enum | Prefix |
|--------|-----------|---------------|--------|
| User | `MomentException` | `ErrorCode` | `U-*`, `T-*`, `M-*`, `C-*`, `N-*`, `G-*` |
| Admin | `AdminException` | `AdminErrorCode` | `A-*`, `AG-*`, `AM-*`, `AC-*` |

Admin exception handler is scoped via `@RestControllerAdvice(basePackages = "moment.admin.presentation.api")`.

### Controller usage

```java
// User API
HttpStatus status = HttpStatus.CREATED;
return ResponseEntity.status(status).body(SuccessResponse.of(status, response));

// Admin API
HttpStatus status = HttpStatus.OK;
return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
```

## Authentication Pattern

User API and Admin API use **different authentication mechanisms**.

### User API: JWT Cookie + Custom ArgumentResolver

```java
// Custom annotation
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticationPrincipal {
    boolean required() default true;
}

// Authentication record (resolved from JWT)
public record Authentication(Long id) {
    public static Authentication from(Long id) {
        return new Authentication(id);
    }
}

// ArgumentResolver extracts JWT from "accessToken" cookie
@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthService authService;

    @Override
    public Object resolveArgument(...) {
        String accessToken = extractFromCookie(request, "accessToken");
        return authService.getAuthenticationByToken(accessToken);
    }
}
```

**Controller usage:**
```java
@PostMapping
public ResponseEntity<SuccessResponse<MomentCreateResponse>> createBasicMoment(
        @Valid @RequestBody MomentCreateRequest request,
        @AuthenticationPrincipal Authentication authentication
) {
    MomentCreateResponse response = facade.createBasicMoment(request, authentication.id());
    HttpStatus status = HttpStatus.CREATED;
    return ResponseEntity.status(status).body(SuccessResponse.of(status, response));
}
```

### Admin API: Session-based + Interceptor

```java
// Interceptor validates session and role
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {
    private final AdminSessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, ...) {
        HttpSession session = request.getSession(false);
        if (session == null) throw new AdminException(AdminErrorCode.UNAUTHORIZED);

        sessionManager.validateAuthorized(session);

        // Verify session is still active in DB
        if (!sessionManager.isSessionActiveInDb(session.getId())) {
            sessionManager.invalidate(session);
            throw new AdminException(AdminErrorCode.SESSION_EXPIRED);
        }

        // SUPER_ADMIN-only path check
        AdminRole role = sessionManager.getRole(session);
        if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
            throw new AdminException(AdminErrorCode.FORBIDDEN);
        }
        return true;
    }
}

// Registered in WebConfig
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns("/api/admin/**")
        .excludePathPatterns("/api/admin/auth/login");
}
```

**Controller usage (session injected directly):**
```java
@PostMapping("/login")
public ResponseEntity<AdminSuccessResponse<AdminLoginResponse>> login(
        @Valid @RequestBody AdminLoginRequest request,
        HttpSession session, HttpServletRequest httpRequest) {
    Admin admin = adminService.authenticateAdmin(request.email(), request.password());
    httpRequest.changeSessionId(); // session fixation prevention
    sessionManager.registerSession(session, admin.getId(), admin.getRole(), ...);
    ...
}
```

| | User API | Admin API |
|---|----------|-----------|
| Mechanism | Stateless JWT cookie | DB-persisted HTTP session |
| Injection | `@AuthenticationPrincipal` ArgumentResolver | `HttpSession` parameter / Interceptor |
| Validation | Token signature + expiry | Session existence + DB active check |
| Authorization | N/A | Role-based (`SUPER_ADMIN` paths) |

## Repository Pattern

### Standard Spring Data JPA Repository

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // Simple query methods via naming convention
    boolean existsByNickname(String nickname);
    Optional<User> findByEmailAndProviderType(String email, ProviderType providerType);
    List<User> findAllByIdIn(List<Long> ids);

    // Complex queries with @Query (JPQL)
    @Query("""
        SELECT m FROM moments m
        JOIN FETCH m.momenter
        WHERE m.momenter = :momenter
        ORDER BY m.createdAt DESC, m.id DESC
        """)
    List<Moment> findMyMomentFirstPage(@Param("momenter") User momenter, Pageable pageable);

    // Soft delete via native query
    @Modifying
    @Query(value = "UPDATE moments SET deleted_at = NOW() WHERE group_id = :groupId AND deleted_at IS NULL",
           nativeQuery = true)
    void softDeleteByGroupId(@Param("groupId") Long groupId);
}
```

### Pagination: Two Strategies

The project uses **cursor-based pagination** for user-facing feeds and **offset/limit pagination** for admin dashboards.

#### User API: Cursor-based Pagination

Optimized for infinite scroll feeds. Uses composite cursor (`createdAt_id`) to avoid offset drift.

```java
// Entities implement Cursorable for cursor support
public interface Cursorable {
    LocalDateTime getCreatedAt();
    Long getId();
}

// Cursor format: "{createdAt}_{id}" (e.g. "2025-07-14T16:24:34_42")
public record Cursor(String cursor) {
    public boolean isFirstPage() { return cursor == null || cursor.isBlank(); }
    public LocalDateTime dateTime() { return LocalDateTime.parse(cursor.split("_")[0]); }
    public Long id() { return Long.valueOf(cursor.split("_")[1]); }
}

// PageSize fetches size+1 to detect next page
public record PageSize(int size) {
    public PageRequest getPageRequest() { return PageRequest.of(0, size + 1); }
    public boolean hasNextPage(int targetSize) { return targetSize > size; }
}
```

**Repository query:**
```java
// First page: no cursor condition
List<Moment> findMyMomentFirstPage(@Param("momenter") User momenter, Pageable pageable);

// Next page: composite cursor condition (createdAt + id)
@Query("""
    SELECT m FROM moments m
    WHERE m.momenter = :momenter
      AND (m.createdAt < :cursorTime
           OR (m.createdAt = :cursorTime AND m.id < :cursorId))
    ORDER BY m.createdAt DESC, m.id DESC
    """)
List<Moment> findMyMomentsNextPage(...);
```

**Service usage:**
```java
public List<Moment> getMomentsBy(User momenter, Cursor cursor, PageSize pageSize) {
    PageRequest pageable = pageSize.getPageRequest();
    if (cursor.isFirstPage()) {
        return momentRepository.findMyMomentFirstPage(momenter, pageable);
    }
    return momentRepository.findMyMomentsNextPage(
        momenter, cursor.dateTime(), cursor.id(), pageable);
}
```

#### Admin API: Offset/Limit Pagination

Standard page-based navigation for admin dashboards with total count.

**Service:**
```java
public Page<User> getAllUsers(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return userRepository.findAllIncludingDeleted(pageable);
}
```

**Response DTO wraps Spring `Page` metadata:**
```java
public record AdminGroupListResponse(
    List<AdminGroupSummary> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public static AdminGroupListResponse from(Page<AdminGroupSummary> pageResult) {
        return new AdminGroupListResponse(
            pageResult.getContent(), pageResult.getNumber(),
            pageResult.getSize(), pageResult.getTotalElements(),
            pageResult.getTotalPages());
    }
}
```

**Controller (page/size as request params):**
```java
@GetMapping
public ResponseEntity<AdminSuccessResponse<Page<AdminUserListResponse>>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size) {
    Page<User> users = adminUserService.getAllUsers(page, size);
    Page<AdminUserListResponse> response = users.map(AdminUserListResponse::from);
    HttpStatus status = HttpStatus.OK;
    return ResponseEntity.status(status).body(AdminSuccessResponse.of(status, response));
}
```

| | User API (Cursor) | Admin API (Offset/Limit) |
|---|-------------------|--------------------------|
| Use case | Infinite scroll feeds | Dashboard tables with page numbers |
| Params | `cursor` (string) | `page` (int), `size` (int) |
| Next page detection | Fetch `size+1` rows | `Page.getTotalPages()` |
| Response metadata | `nextCursor` (nullable) | `page`, `size`, `totalElements`, `totalPages` |
| Offset drift | Immune | Possible on concurrent writes |

## Domain Event Pattern

When implementing cross-domain communication:

1. Define event as an immutable record with `of()` factory
2. Publish from Facade Service via `ApplicationEventPublisher`
3. Handle asynchronously with `@Async` + `@TransactionalEventListener(AFTER_COMMIT)`

```java
// 1. Event definition (record under dto/ package)
public record CommentCreateEvent(Long momentId, Long momenterId) {
    public static CommentCreateEvent of(Moment moment) {
        return new CommentCreateEvent(moment.getId(), moment.getMomenterId());
    }
}

// 2. Publishing from Facade Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentCreateFacadeService {
    private final CommentApplicationService commentApplicationService;
    private final MomentApplicationService momentApplicationService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public CommentCreateResponse createComment(CommentCreateRequest request, Long userId) {
        commentApplicationService.validateCreateComment(request, userId);
        Moment moment = momentApplicationService.getMomentBy(request.momentId());
        CommentCreateResponse created = commentApplicationService.createComment(request, userId);
        publisher.publishEvent(CommentCreateEvent.of(moment));
        return created;
    }
}

// 3. Async event handling (fires after transaction commit)
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
    private final NotificationFacadeService notificationFacadeService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreateEvent(CommentCreateEvent event) {
        notificationFacadeService.createNotificationAndSendSseAndPush(
            event.momenterId(), event.momentId(),
            NotificationType.NEW_COMMENT_ON_MOMENT, TargetType.MOMENT,
            null, PushNotificationMessage.REPLY_TO_MOMENT);
    }
}
```

**Rules:**
- Always combine `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)`
- Skip self-notification (e.g. commenting on own moment)
- Log event receipt at `info` level with key identifiers