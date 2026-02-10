# Testing Requirements

## Mock Usage Policy

Service tests use real objects and real database connections. Repository and internal Service classes are never mocked.

Mocking Repository hides JPQL syntax errors and incorrect query results that only surface at runtime in production. Similarly, mocking internal services skips real orchestration logic and event flows, giving false confidence that the code works correctly.

### What to use real objects for

| Component | Approach | Reason |
|-----------|----------|--------|
| Repository | Real H2 DB via `@DataJpaTest` or `@SpringBootTest` | Verifies actual JPQL and query method behavior |
| Internal services | Real beans with `@Autowired` | Validates orchestration and transaction boundaries |
| PasswordEncoder | Real `BCryptPasswordEncoder` | Confirms encryption/matching works end-to-end |
| ApplicationEventPublisher | Real event flow | Ensures events fire and handlers execute correctly |

### What to mock (external dependencies only)

| Component | Annotation | Reason |
|-----------|-----------|--------|
| FirebaseMessaging | `@MockitoBean` | External push notification service |
| S3Client | `@MockitoBean` | External file storage service |
| External HTTP clients | `@MockitoBean` | Third-party API calls |

## Required Test Layers

Every feature requires tests at 4 layers. Each layer catches different categories of bugs:

### 1. Domain Unit Test

Pure business logic validation without Spring context. Fastest feedback loop.

```java
@Tag(TestTags.UNIT)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentTest {

    @Test
    void 모멘트_내용_길이가_200자가_넘는_경우_예외가_발생한다() {
        User user = UserFixture.createUser();
        String longContent = "=".repeat(201);

        assertThatThrownBy(() -> new Moment(longContent, user))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

### 2. Repository Integration Test

Verifies custom queries work against a real database. Without this layer, JPQL syntax errors and incorrect query logic go undetected until deployment.

Required test targets:
- Custom `@Query` methods (JPQL and native)
- Complex method-naming queries (multi-condition, ordering)
- Soft delete filtering (`@SQLRestriction` behavior)
- Cursor-based pagination queries

```java
@Tag(TestTags.INTEGRATION)
@DataJpaTest
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentRepositoryTest {

    @Autowired
    MomentRepository momentRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void 커서_기반_페이지네이션으로_모멘트를_조회한다() {
        // given
        User momenter = userRepository.save(UserFixture.createUser());
        Moment moment1 = momentRepository.save(new Moment("first", momenter));
        Moment moment2 = momentRepository.save(new Moment("second", momenter));

        // when
        List<Moment> result = momentRepository.findMyMomentFirstPage(
                momenter, PageRequest.of(0, 2));

        // then
        assertThat(result).hasSize(2);
    }
}
```

### 3. Service Integration Test

Tests service orchestration with real Repository and internal Service beans. Only external APIs are mocked.

```java
@Tag(TestTags.INTEGRATION)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Transactional
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentServiceTest {

    @Autowired
    MomentService momentService;

    @Autowired
    UserRepository userRepository;

    @MockitoBean
    FirebaseMessaging firebaseMessaging;  // external API only

    private User momenter;

    @BeforeEach
    void setUp() {
        momenter = userRepository.save(UserFixture.createUser());
    }

    @Test
    void 모멘트를_생성한다() {
        // given
        String content = "hello!";

        // when
        Moment moment = momentService.create(content, momenter);

        // then
        assertThat(moment.getContent()).isEqualTo(content);
    }
}
```

### 4. E2E Test

Full HTTP flow verification with real server.

```java
@Tag(TestTags.E2E)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayNameGeneration(ReplaceUnderscores.class)
class MomentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.clean();
    }

    @Test
    void 기본_모멘트를_등록한다() {
        // given, when, then ...
    }
}
```

## Repository Test Rules

Use `@DataJpaTest` with `@ActiveProfiles("test")` for all repository tests. This loads only JPA-related components and uses the H2 in-memory database.

Test targets (repository tests are required for these):
- Every `@Query` annotated method
- Method-naming queries with 2+ conditions (e.g., `findByEmailAndProviderType`)
- Soft delete behavior (verify `@SQLRestriction` filters deleted records)
- Pagination queries (cursor-based and offset-based)
- `@Modifying` bulk update/delete queries

## Service Test Rules

Use `@SpringBootTest(webEnvironment = WebEnvironment.NONE)` with `@Transactional` for service tests. This loads the full application context without starting a web server.

- Inject services and repositories with `@Autowired` (real beans)
- Use `@MockitoBean` only for external API clients (Firebase, S3, external HTTP)
- Set up test data through real repositories in `@BeforeEach`
- Each test should be independent and not rely on data from other tests

## Test-Driven Development

Workflow:

1. Write test first (RED)
2. Run test - it should FAIL
3. Write minimal implementation (GREEN)
4. Run test - it should PASS
5. Refactor (IMPROVE)
6. Verify coverage (80%+)

## Minimum Test Coverage: 80%

## Troubleshooting Test Failures

1. Use **tdd-guide** agent
2. Check test isolation
3. Verify test data setup is correct
4. Fix implementation, not tests (unless tests are wrong)

## Agent Support

- **tdd-guide** - Use PROACTIVELY for new features, enforces write-tests-first
