# Admin 기능 구현 계획 (Thymeleaf SSR)

> **구현 방식**: 별도 Admin 엔티티 + 세션 기반 인증

이 문서는 Moment 프로젝트에 Thymeleaf 기반의 관리자(Admin) 기능을 추가하기 위한 단계별 실행 계획을 담고 있습니다.

---

## 📋 개요

Moment 프로젝트에 **별도의 Admin 시스템**을 추가합니다. 기존 User 도메인과 완전히 독립된 Admin 계정 시스템을 구축하고, Thymeleaf + Tailwind CSS 기반의 모던한 관리자 페이지를 제공합니다.

**기술 스택**: Thymeleaf (SSR) + Tailwind CSS 3.4+ + Lucide Icons

**우선순위**: 사용자 관리 > 콘텐츠 관리 > 신고 관리

---

## 🔍 현재 상태 분석

### ✅ 존재하는 것
- JWT 기반 사용자 인증 시스템
- Clean Architecture 구조 (domain/infrastructure/service/presentation/dto)
- Soft Delete 패턴 (@SQLDelete, @SQLRestriction)
- User, Moment, Comment, Report 도메인
- BCryptPasswordEncoder
- Flyway 마이그레이션

### ❌ 없는 것
- **Thymeleaf 의존성 및 설정** (완전히 미설치)
- **Role/권한 시스템** (User에 role 필드 없음)
- **Admin 관련 코드** (엔티티, 서비스, 컨트롤러 전무)
- **templates/ 디렉토리**
- **@Controller 타입 컨트롤러** (모두 @RestController)

---

## 🚀 구현 단계

### Phase 1: 기반 설정 (Foundation)

#### 1-1. Thymeleaf 의존성 추가
**파일**: `server/build.gradle`

```gradle
dependencies {
    // 기존 dependencies...
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'

#### 1-2. 디렉토리 구조 생성
```
server/src/main/resources/
├── templates/admin/
│   ├── layout.html       # 기본 레이아웃 (navbar, sidebar)
│   ├── login.html        # 로그인 페이지
│   └── users/
│       ├── list.html     # 사용자 목록
│       └── detail.html   # 사용자 상세
└── static/admin/
    └── css/
        └── admin-custom.css (필요 시)
```

#### 1-3. Admin 모듈 패키지 생성
```
server/src/main/java/moment/admin/
├── domain/
│   └── Admin.java
├── infrastructure/
│   ├── AdminRepository.java
│   └── AdminAuthInterceptor.java
├── service/
│   ├── admin/
│   │   └── AdminService.java
│   └── application/
│       └── AdminUserApplicationService.java
├── presentation/
│   ├── AdminAuthController.java
│   └── AdminUserController.java
└── dto/
    └── request/
        └── AdminLoginRequest.java
```

---

### Phase 2: Admin 인증 시스템

#### 2-1. 데이터베이스 스키마 (Flyway)
**파일**: `server/src/main/resources/db/migration/mysql/V23__create_admin_table__mysql.sql`

```sql
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### 2-2. Admin 엔티티
**파일**: `server/src/main/java/moment/admin/domain/Admin.java`

**핵심 패턴**:
- `@Entity(name = "admins")`
- `@SQLDelete(sql = "UPDATE admins SET deleted_at = NOW() WHERE id = ?")`
- `@SQLRestriction("deleted_at IS NULL")`
- `extends BaseEntity`
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- 생성자에서 검증 로직 수행 (User 패턴과 동일)

**필드**:
- `id`, `username`, `password`, `name`, `email`, `deletedAt`

#### 2-3. 에러 코드 추가
**파일**: `server/src/main/java/moment/global/exception/ErrorCode.java`

```java
// Admin errors (A-xxx) - 기존 코드 끝에 추가
ADMIN_LOGIN_FAILED("A-001", "관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
ADMIN_NOT_FOUND("A-002", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
ADMIN_UNAUTHORIZED("A-003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),
ADMIN_USERNAME_CONFLICT("A-004", "이미 사용 중인 관리자 아이디입니다.", HttpStatus.CONFLICT),
ADMIN_EMAIL_CONFLICT("A-005", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
```

**주의**: 마지막 항목 뒤에 세미콜론(`;`) 유지

#### 2-4. AdminRepository
**파일**: `server/src/main/java/moment/admin/infrastructure/AdminRepository.java`

```java
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

#### 2-5. AdminService (인증 로직)
**파일**: `server/src/main/java/moment/admin/service/admin/AdminService.java`

**핵심 메서드**:
- `authenticateAdmin(username, password)`: 로그인 검증
- `getAdminById(id)`: 세션용 관리자 조회
- `createAdmin(...)`: 관리자 계정 생성 (초기 설정용)
- `existsByUsername(username)`: 중복 확인

**패턴**:
- `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`
- `PasswordEncoder` 의존성 주입 (기존 BCrypt 사용)
- 에러는 `MomentException(ErrorCode.XXX)` 던지기

#### 2-6. 세션 기반 인증 인터셉터
**파일**: `server/src/main/java/moment/admin/infrastructure/AdminAuthInterceptor.java`

**세션 기반을 선택한 이유**:
- ✅ SSR(Thymeleaf)과 자연스러운 통합
- ✅ 즉시 로그아웃 가능 (JWT는 토큰 만료까지 유효)
- ✅ Refresh Token 로직 불필요
- ✅ 구현이 간단하고 유지보수 용이
- ✅ 관리자는 소수이므로 Stateful 부담 적음

```java
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    public static final String ADMIN_SESSION_KEY = "ADMIN_ID";

    @Override
    public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(ADMIN_SESSION_KEY) == null) {
            response.sendRedirect("/admin/login");
            return false;
        }

        return true;
    }
}
```

#### 2-7. WebConfig 업데이트
**파일**: `server/src/main/java/moment/global/config/WebConfig.java`

**추가사항**:
- `AdminAuthInterceptor` 의존성 주입
- `addInterceptors()` 메서드 오버라이드
- `/admin/**` 경로 등록 (단, `/admin/login`, `/admin/api/login` 제외)

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin/login", "/admin/api/login");
}
```

#### 2-8. AdminAuthController (로그인/로그아웃)
**파일**: `server/src/main/java/moment/admin/presentation/AdminAuthController.java`

**주의**: `@Controller` 사용 (NOT `@RestController`)

**엔드포인트**:
- `GET /admin/login`: 로그인 페이지 렌더링
- `POST /admin/api/login`: 로그인 처리 → 세션 생성 → 리다이렉트
- `POST /admin/logout`: 세션 무효화 → 로그인 페이지 리다이렉트

#### 2-9. 템플릿: 기본 레이아웃
**파일**: `server/src/main/resources/templates/admin/layout.html`

**구성요소**:
- Tailwind CSS 3.4+ CDN
- Lucide Icons (웹 컴포넌트)
- 고정 좌측 사이드바 (다크 테마, 250px 너비)
  - "Moment Admin" 로고
  - 사용자 관리 링크
  - 로그아웃 버튼
- 메인 영역 (`ml-64` 클래스로 사이드바 여백 확보)
  - Sticky 상단 헤더
  - `layout:fragment="content"` 영역
  - `layout:fragment="scripts"` 영역

**디자인 스타일**: Soft Glass & Light Mode (권장) - `.claude/rules/thymeleaf.md` 준수

**핵심 Tailwind 클래스**:
- 사이드바: `fixed left-0 top-0 h-screen w-64 bg-slate-900 text-white`
- 메인 영역: `ml-64`
- 헤더: `sticky top-0 bg-white shadow-sm z-40`
- 콘텐츠: `p-6 lg:p-8`

#### 2-10. 템플릿: 로그인 페이지
**파일**: `server/src/main/resources/templates/admin/login.html`

**기능**:
- 독립 페이지 (layout 상속 안 함)
- Tailwind CSS 스타일
- 중앙 정렬 카드 레이아웃
- 에러 메시지 표시 (`th:if="${error}"`)
- POST → `/admin/api/login`

**디자인**:
- 배경: `bg-slate-50`
- 카드: `rounded-2xl bg-white shadow-sm border border-slate-200/50`
- 입력 필드: `px-4 py-2.5 rounded-lg border focus:ring-2 focus:ring-indigo-500`
- 버튼: `px-6 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700`

---

### Phase 3: 사용자 관리 기능 (우선 구현)

#### 3-1. UserRepository 확장
**파일**: `server/src/main/java/moment/user/infrastructure/UserRepository.java`

**추가 메서드**:
```java
// 검색 (Spring Data JPA 네이밍)
Page<User> findByEmailContainingOrNicknameContaining(
    String email, String nickname, Pageable pageable);

// Soft Delete 복원 (Native Query)
@Modifying
@Query(value = "UPDATE users SET deleted_at = NULL WHERE id = :userId",
       nativeQuery = true)
void restoreDeletedUser(@Param("userId") Long userId);

// 삭제된 사용자 포함 조회 (Native Query)
@Query(value = "SELECT * FROM users WHERE id = :userId", nativeQuery = true)
Optional<User> findByIdIncludingDeleted(@Param("userId") Long userId);
```

#### 3-2. AdminUserApplicationService
**파일**: `server/src/main/java/moment/admin/service/application/AdminUserApplicationService.java`

**메서드**:
- `getAllUsers(Pageable)`: 전체 사용자 목록 (페이징)
- `searchUsers(keyword, Pageable)`: 이메일/닉네임 검색
- `getUserById(userId)`: 상세 조회
- `softDeleteUser(userId)`: `userRepository.delete(user)` 호출
- `restoreUser(userId)`: `userRepository.restoreDeletedUser(userId)` 호출

**패턴**: `@Transactional(readOnly = true)` 클래스 레벨, 쓰기 메서드에 `@Transactional`

#### 3-3. AdminUserController
**파일**: `server/src/main/java/moment/admin/presentation/AdminUserController.java`

**주의**: `@Controller` 사용 (뷰 반환)

**엔드포인트**:
- `GET /admin/users?page=0&size=20&keyword=`: 사용자 목록
- `GET /admin/users/{userId}`: 사용자 상세
- `POST /admin/users/{userId}/delete`: 소프트 삭제
- `POST /admin/users/{userId}/restore`: 복원

**패턴**:
- `RedirectAttributes`로 성공/에러 메시지 전달
- `HttpSession`에서 관리자 정보 추출
- Pageable 기본값: `page=0, size=20, sort=createdAt,desc`

#### 3-4. 템플릿: 사용자 목록
**파일**: `server/src/main/resources/templates/admin/users/list.html`

**기능**:
- `layout:decorate="~{admin/layout}"` 상속
- 검색 바 (Tailwind 스타일)
- 사용자 테이블 (Tailwind 기반)
- 페이지네이션 (Tailwind 스타일)
- 빈 상태 처리 (`th:if="${#lists.isEmpty(users.content)}"`)
- 성공/에러 알림 (Tailwind Alert)
- 날짜 포맷: `yyyy-MM-dd HH:mm`

**디자인 요소**:
- 검색 바: `flex gap-3 mb-6`
- 테이블: `overflow-x-auto rounded-lg border border-slate-200`
- 테이블 헤더: `bg-slate-100 border-b border-slate-200`
- 테이블 행: `border-b border-slate-100 hover:bg-slate-50 transition-colors`
- 작업 버튼: `text-indigo-600 hover:text-indigo-800 text-sm font-medium`

**테이블 컬럼**:
- ID, 이메일, 닉네임, 가입 유형, 레벨, 별조각, 가입일, 작업 버튼

#### 3-5. 템플릿: 사용자 상세
**파일**: `server/src/main/resources/templates/admin/users/detail.html`

**기능**:
- `layout:decorate="~{admin/layout}"` 상속
- 사용자 정보 카드 형태로 표시
- 삭제 버튼 (확인 대화상자, Danger 스타일)
- 복원 버튼 (deletedAt이 있을 경우만 표시, Success 스타일)
- 목록으로 돌아가기 버튼 (Secondary 스타일)

**디자인 요소**:
- 카드: `rounded-2xl bg-slate-50 p-6 shadow-sm border border-slate-200/50`
- 정보 테이블: `divide-y divide-slate-200`
- 버튼 그룹: `flex gap-3 mt-6`

---

### Phase 4: 초기 관리자 계정 생성

#### 4-1. 환경 변수 설정
**파일**: `.env`

다음 환경 변수를 `.env` 파일에 추가합니다:
- `ADMIN_INITIAL_EMAIL`: 초기 관리자 이메일
- `ADMIN_INITIAL_PASSWORD`: 초기 관리자 비밀번호
- `ADMIN_INITIAL_NAME`: 초기 관리자 이름
- `ADMIN_SESSION_TIMEOUT`: 관리자 세션 타임아웃 (예: `1h`, `3600s`)

**파일**: `src/main/resources/application-dev.yml`

```yaml
# 세션 타임아웃 설정
server:
  servlet:
    session:
      timeout: ${ADMIN_SESSION_TIMEOUT}

# 초기 관리자 계정 설정
admin:
  initial:
    email: ${ADMIN_INITIAL_EMAIL}
    password: ${ADMIN_INITIAL_PASSWORD}
    name: ${ADMIN_INITIAL_NAME}
```

#### 4-2. CommandLineRunner 구현
**파일**: `server/src/main/java/moment/admin/config/AdminInitializer.java`

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final AdminService adminService;

    @Value("${admin.initial.email}")
    private String initialEmail;

    @Value("${admin.initial.password}")
    private String initialPassword;

    @Value("${admin.initial.name}")
    private String initialName;

    @Override
    public void run(String... args) throws Exception {
        if (!adminService.existsByEmail(initialEmail)) {
            adminService.createAdmin(initialEmail, initialName, initialPassword);
            log.info("✅ 초기 관리자 계정 생성: {}", initialEmail);
            log.warn("⚠️  프로덕션 환경에서는 반드시 초기 비밀번호를 변경하세요!");
        }
    }
}
```

**보안 원칙**:
- ⛔ 초기 관리자 정보를 절대 코드에 하드코딩하지 않음
- ✅ 환경 변수로 관리하여 리포지토리 노출 방지
- ✅ 프로덕션 배포 전 강력한 비밀번호로 변경 필수
- ✅ 세션 타임아웃도 환경 변수로 관리하여 환경별 유연한 설정 가능

---

### Phase 5: 테스트

#### 5-1. 단위 테스트
**파일**: `server/src/test/java/moment/admin/service/admin/AdminServiceTest.java`

- `authenticateAdmin_성공()`
- `authenticateAdmin_잘못된_비밀번호_실패()`
- `createAdmin_성공()`
- `createAdmin_중복_아이디_실패()`

#### 5-2. E2E 테스트
**파일**: `server/src/test/java/moment/admin/presentation/AdminAuthControllerTest.java`

- `@Tag("e2e")` 추가
- 로그인 성공/실패
- 인증 없이 접근 시 리다이렉트
- 로그아웃 후 세션 무효화

---

## 🏗️ 아키텍처 결정 사항

### 1. 세션 기반 인증 vs JWT

#### 세션 기반 선택 이유

**Admin 페이지 특성**:
- Thymeleaf SSR (서버 사이드 렌더링)
- 페이지 이동마다 전체 리로드
- 관리자는 소수 (확장성 부담 적음)

**세션 기반의 장점**:
- ✅ **즉시 로그아웃**: 서버에서 세션 무효화 → 즉시 적용
- ✅ **간단한 구현**: Refresh Token 로직 불필요
- ✅ **SSR 친화적**: Spring 기본 세션 관리와 자연스럽게 통합
- ✅ **보안 강화**: 관리자 계정의 즉시 차단 가능

**JWT를 사용할 경우의 문제점**:
- ❌ 로그아웃 불가능 (토큰 만료 시까지 유효)
- ❌ Blacklist 관리 필요 → 결국 Stateful
- ❌ SSR에서 토큰 갱신 로직 복잡
- ❌ 모든 컨트롤러에서 토큰 검증 중복

### 2. 별도 Admin 엔티티 vs User에 Role 추가

#### 별도 Admin 엔티티 선택

**이유**:
- User와 완전히 분리된 인증 흐름
- 관리자 전용 필드 추가 용이
- User 도메인에 영향 없음

**장점**:
- ✅ User 도메인 불변성 유지
- ✅ Admin 전용 비즈니스 로직 분리
- ✅ 보안 경계 명확

**단점**:
- ❌ 두 개의 인증 시스템 유지
- ❌ 사용자를 관리자로 승격 시 데이터 마이그레이션 필요

**대안 (User에 Role 추가)**:
- User 엔티티에 `role` 필드 추가
- JWT 토큰에 Role 클레임 포함
- 단일 인증 시스템
- 단, User 도메인 복잡도 증가

### 3. Soft Delete 복원
- **방법**: Native Query로 `deleted_at = NULL` 업데이트
- **이유**: `@SQLRestriction`이 삭제된 레코드를 JPA에서 숨기므로 우회 필요
- **주의**: Admin 전용 기능, 신중히 사용

### 4. 기존 Repository 확장
- **방법**: UserRepository에 admin 전용 메서드 추가
- **이유**: DRY 원칙, 단일 진실 공급원
- **표시**: 주석으로 "// Admin-only method" 명시

---

## 🔮 향후 확장 계획 (구현 안 함, 아키텍처만)

### 콘텐츠 관리 (Moments/Comments)
- `AdminMomentController` 추가
- `admin/moments/list.html`, `admin/moments/detail.html`
- MomentRepository에 검색/복원 메서드 추가
- 사용자 상세 페이지에서 해당 사용자의 모멘트 링크

### 신고 관리
- `AdminReportController` 추가
- Report 엔티티에 `status` 필드 추가 (PENDING/RESOLVED/REJECTED)
- `admin/reports/list.html`, `admin/reports/detail.html`
- 신고 처리 액션 (승인 → 콘텐츠 삭제, 기각)

### 통계 대시보드
- 사용자 가입 추이
- 모멘트 작성 추이
- 신고 통계
- 활성 사용자 분석

---

## ✅ 구현 체크리스트

### Phase 1: Foundation
- [ ] build.gradle에 Thymeleaf 의존성 추가
- [ ] templates/admin/ 디렉토리 생성
- [ ] static/admin/ 디렉토리 생성
- [ ] admin 패키지 구조 생성

### Phase 2: Authentication
- [ ] V23 마이그레이션 파일 작성
- [ ] Admin 엔티티 구현 (Soft Delete 패턴)
- [ ] ErrorCode에 A-xxx 에러 추가
- [ ] AdminRepository 인터페이스 작성
- [ ] AdminService 구현 (인증 로직)
- [ ] AdminAuthInterceptor 구현
- [ ] WebConfig에 인터셉터 등록
- [ ] AdminAuthController 구현
- [ ] AdminLoginRequest DTO 작성
- [ ] layout.html 템플릿 작성
- [ ] login.html 템플릿 작성

### Phase 3: User Management
- [ ] UserRepository 확장 (검색, 복원 메서드)
- [ ] AdminUserApplicationService 구현
- [ ] AdminUserController 구현
- [ ] users/list.html 템플릿 작성
- [ ] users/detail.html 템플릿 작성

### Phase 4: Initial Setup
- [ ] AdminInitializer 작성 (개발용)

### Phase 5: Testing
- [ ] AdminService 단위 테스트
- [ ] AdminAuthController E2E 테스트
- [ ] `./gradlew fastTest` 실행
- [ ] `./gradlew test` 실행

---

## 📂 주요 파일 경로 요약

### 생성할 파일 (총 16개)

#### Backend (12개)
1. `server/src/main/resources/db/migration/mysql/V23__create_admin_table__mysql.sql`
2. `server/src/main/java/moment/admin/domain/Admin.java`
3. `server/src/main/java/moment/admin/infrastructure/AdminRepository.java`
4. `server/src/main/java/moment/admin/infrastructure/AdminAuthInterceptor.java`
5. `server/src/main/java/moment/admin/service/admin/AdminService.java`
6. `server/src/main/java/moment/admin/service/application/AdminUserApplicationService.java`
7. `server/src/main/java/moment/admin/presentation/AdminAuthController.java`
8. `server/src/main/java/moment/admin/presentation/AdminUserController.java`
9. `server/src/main/java/moment/admin/dto/request/AdminLoginRequest.java`
10. `server/src/main/java/moment/admin/config/AdminInitializer.java`
11. `server/src/test/java/moment/admin/service/admin/AdminServiceTest.java`
12. `server/src/test/java/moment/admin/presentation/AdminAuthControllerTest.java`

#### Frontend (4개)
13. `server/src/main/resources/templates/admin/layout.html`
14. `server/src/main/resources/templates/admin/login.html`
15. `server/src/main/resources/templates/admin/users/list.html`
16. `server/src/main/resources/templates/admin/users/detail.html`

### 수정할 파일 (4개)
1. `server/build.gradle` - Thymeleaf 의존성
2. `server/src/main/java/moment/global/exception/ErrorCode.java` - Admin 에러 코드
3. `server/src/main/java/moment/global/config/WebConfig.java` - 인터셉터 등록
4. `server/src/main/java/moment/user/infrastructure/UserRepository.java` - Admin 메서드 확장

---

## 🔒 보안 고려사항

1. **세션 보안**
   - HttpOnly 쿠키 사용
   - 세션 타임아웃: 환경 변수(`ADMIN_SESSION_TIMEOUT`)로 관리
   - 로그아웃 시 세션 완전 무효화

2. **비밀번호**
   - BCrypt 해싱 (기존 PasswordEncoder 재사용)
   - 초기 비밀번호는 즉시 변경 권장

3. **XSS 방지**
   - Thymeleaf 자동 이스케이프
   - Tailwind CSS 기본 보안 설정 사용

4. **접근 제어**
   - 인터셉터로 모든 `/admin/**` 경로 보호
   - 로그인 페이지만 예외 처리

---

## 🎯 실행 순서

1. **의존성 추가**: build.gradle → Gradle 리로드
2. **데이터베이스**: 마이그레이션 파일 작성
3. **도메인**: Admin 엔티티 → Repository
4. **서비스**: AdminService → AdminUserApplicationService
5. **인프라**: AdminAuthInterceptor → WebConfig 수정
6. **컨트롤러**: AdminAuthController → AdminUserController
7. **템플릿**: layout.html → login.html → users/*.html (Tailwind CSS + Lucide Icons)
8. **테스트**: 단위 테스트 → E2E 테스트
9. **초기화**: AdminInitializer 작성

---

## 📚 개발 원칙

1. **SSR 우선**: 관리자 페이지는 Thymeleaf의 `Model`을 통한 서버 사이드 렌더링을 우선함
2. **보안**: 모든 관리자 페이지는 반드시 세션 체크를 거쳐야 함
3. **UI 컨벤션**: `.claude/rules/thymeleaf.md`에 정의된 Tailwind CSS 디자인 가이드를 준수함 (Soft Glass & Light Mode 권장)
4. **Clean Architecture**: 기존 프로젝트의 레이어 구조와 패턴을 따름
5. **Soft Delete**: 모든 삭제는 Soft Delete로 처리
6. **모던 디자인**: Lucide Icons 사용, 부드러운 전환 효과, 반응형 레이아웃

---

이 계획은 기존 Moment 프로젝트의 Clean Architecture 원칙과 코딩 컨벤션을 완전히 준수하며, 향후 콘텐츠 관리 및 신고 관리로 확장 가능한 견고한 기반을 제공합니다.
