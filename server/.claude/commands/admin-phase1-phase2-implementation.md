# Admin 기능 구현 계획: Phase 1 & Phase 2 상세 실행 계획

## 📋 개요

Moment 프로젝트에 Thymeleaf 기반의 관리자(Admin) 기능을 추가합니다.
- **구현 범위**: Phase 1 (기반 설정) + Phase 2 (Admin 인증 시스템 + 관리자 등록)
- **인증 방식**: 세션 기반 (Stateful)
- **Admin 계정**: 별도 엔티티 (User와 독립)
- **로그인 ID**: 이메일 (username 필드 없음)
- **관리자 추가**: 상위 관리자가 웹 페이지에서 이메일 입력하여 등록
- **기술 스택**: Thymeleaf (SSR) + Tailwind CSS 3.4+ + Lucide Icons

## 🔍 현재 코드베이스 상태

### ✅ 확인된 정보
- **Spring Boot**: 3.5.3 (Java 21)
- **Thymeleaf**: 미설치 (추가 필요)
- **최신 Flyway 버전**: V22 → 다음 버전은 V23
- **ErrorCode 패턴**: `{prefix}-{number}` (G, U, T, E, C, M, N, V)
- **Soft Delete 패턴**: User 엔티티에 적용 중 (`@SQLDelete` + `@SQLRestriction`)
- **BaseEntity**: `createdAt` 자동 설정 (`@CreatedDate`)
- **WebConfig**: 현재 `LoginUserArgumentResolver` 등록됨 (JWT 처리)

### 📂 기존 도메인 구조
```
moment/
├── auth/           # JWT 인증
├── user/           # 사용자 관리
├── moment/         # 모멘트 게시물
├── comment/        # 댓글
├── notification/   # 알림
├── report/         # 신고
└── global/         # 공유 인프라
```

---

## 🚀 Phase 1: 기반 설정 (Foundation)

### 1-1. Thymeleaf 의존성 추가

**파일**: `server/build.gradle`

**위치**: `dependencies` 블록 내부

**추가할 내용**:
```gradle
// Thymeleaf for Admin pages
implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0'
```

**작업 후**: IntelliJ IDEA에서 Gradle 리로드 (⌘⇧I 또는 우측 Gradle 패널에서 Reload)

---

### 1-2. 디렉토리 구조 생성

**생성할 디렉토리**:
```
server/src/main/resources/
├── templates/admin/           # Thymeleaf 템플릿
│   └── users/                 # 사용자 관리 페이지
└── static/admin/              # 정적 리소스 (선택)
    └── css/                   # 커스텀 CSS (필요 시)
```

**명령어**:
```bash
cd server/src/main/resources
mkdir -p templates/admin/users
mkdir -p static/admin/css
```

---

### 1-3. Admin 모듈 패키지 생성

**생성할 패키지 구조**:
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
├── dto/
│   └── request/
│       └── AdminLoginRequest.java
└── config/
    └── AdminInitializer.java
```

**명령어**:
```bash
cd server/src/main/java/moment
mkdir -p admin/domain
mkdir -p admin/infrastructure
mkdir -p admin/service/admin
mkdir -p admin/service/application
mkdir -p admin/presentation
mkdir -p admin/dto/request
mkdir -p admin/config
```

---

## 🔐 Phase 2: Admin 인증 시스템

### 2-1. Flyway 마이그레이션 파일 작성

**파일**: `server/src/main/resources/db/migration/mysql/V23__create_admin_table__mysql.sql`

**내용**:
```sql
CREATE TABLE admins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,  -- BCrypt 해시
    name VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    INDEX idx_email (email),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**검증**: 애플리케이션 시작 시 Flyway가 자동 실행 → `admins` 테이블 생성 확인

**주의**: email이 로그인 ID로 사용됨 (username 필드 없음)

---

### 2-2. ErrorCode enum 확장

**파일**: `server/src/main/java/moment/global/exception/ErrorCode.java`

**위치**: 마지막 에러 코드 다음 (세미콜론 앞)

**추가할 에러 코드**:
```java
// Admin errors (A-xxx)
ADMIN_LOGIN_FAILED("A-001", "관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
ADMIN_NOT_FOUND("A-002", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
ADMIN_UNAUTHORIZED("A-003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),
ADMIN_EMAIL_CONFLICT("A-004", "이미 등록된 관리자 이메일입니다.", HttpStatus.CONFLICT),
```

**주의**: 마지막 항목 뒤 세미콜론(`;`) 유지

---

### 2-3. Admin 엔티티 구현

**파일**: `server/src/main/java/moment/admin/domain/Admin.java`

**핵심 패턴** (User 엔티티와 동일):
- `@Entity(name = "admins")`
- `@SQLDelete(sql = "UPDATE admins SET deleted_at = NOW() WHERE id = ?")`
- `@SQLRestriction("deleted_at IS NULL")`
- `extends BaseEntity`
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`

**필드**:
```java
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, unique = true)
private String email;  // 로그인 ID로 사용

@Column(nullable = false)
private String password;  // BCrypt 해시

@Column(nullable = false, length = 100)
private String name;  // 관리자 이름

private LocalDateTime deletedAt;
```

**생성자**:
```java
public Admin(String email, String name, String password) {
    validateEmail(email);
    validateName(name);
    this.email = email;
    this.name = name;
    this.password = password;
}
```

**검증 메서드**: `email` null/empty 체크, `name` null/empty 체크

---

### 2-4. AdminRepository 인터페이스

**파일**: `server/src/main/java/moment/admin/infrastructure/AdminRepository.java`

**내용**:
```java
package moment.admin.infrastructure;

import moment.admin.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

### 2-5. AdminService 구현 (도메인 로직)

**파일**: `server/src/main/java/moment/admin/service/admin/AdminService.java`

**핵심 메서드**:
1. `authenticateAdmin(email, password)`: 로그인 검증
   - `findByEmail()` → Admin 조회
   - `passwordEncoder.matches()` → 비밀번호 검증
   - 실패 시 `MomentException(ErrorCode.ADMIN_LOGIN_FAILED)` 던지기

2. `getAdminById(id)`: 세션용 관리자 조회
   - `findById().orElseThrow()` → Admin 반환
   - 없으면 `ErrorCode.ADMIN_NOT_FOUND`

3. `createAdmin(email, name, password)`: 관리자 계정 생성
   - 중복 확인: `existsByEmail()`
   - 비밀번호 해시: `passwordEncoder.encode(password)`
   - `new Admin(email, name, hashedPassword)` → `adminRepository.save()`

4. `existsByEmail(email)`: 중복 체크

**애노테이션**:
- `@Service`
- `@RequiredArgsConstructor`
- `@Transactional(readOnly = true)` (클래스 레벨)
- `@Transactional` (쓰기 메서드: `createAdmin`)

**의존성**: `AdminRepository`, `PasswordEncoder` (기존 BCrypt 빈 재사용)

---

### 2-6. AdminAuthInterceptor 구현

**파일**: `server/src/main/java/moment/admin/infrastructure/AdminAuthInterceptor.java`

**내용**:
```java
package moment.admin.infrastructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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

**역할**: 모든 `/admin/**` 요청에서 세션 확인 → 없으면 로그인 페이지 리다이렉트

---

### 2-7. WebConfig 업데이트

**파일**: `server/src/main/java/moment/global/config/WebConfig.java`

**추가할 코드**:
```java
// 필드 추가
private final AdminAuthInterceptor adminAuthInterceptor;

// 메서드 추가 (또는 기존 메서드가 있으면 수정)
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin/login", "/admin/api/login");
}
```

**주의**:
- `@RequiredArgsConstructor`로 `adminAuthInterceptor` 자동 주입
- 로그인 페이지와 로그인 API는 인터셉터에서 제외

---

### 2-8. AdminLoginRequest DTO

**파일**: `server/src/main/java/moment/admin/dto/request/AdminLoginRequest.java`

**내용**:
```java
package moment.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,

    @NotBlank(message = "비밀번호를 입력해주세요")
    String password
) {}
```

---

### 2-9. AdminAuthController 구현

**파일**: `server/src/main/java/moment/admin/presentation/AdminAuthController.java`

**주의**: `@Controller` 사용 (NOT `@RestController`)

**엔드포인트**:

1. **GET `/admin/login`**: 로그인 페이지 렌더링
   ```java
   @GetMapping("/admin/login")
   public String loginPage(@RequestParam(required = false) String error, Model model) {
       if (error != null) {
           model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
       }
       return "admin/login";
   }
   ```

2. **POST `/admin/api/login`**: 로그인 처리
   ```java
   @PostMapping("/admin/api/login")
   public String login(@Valid @ModelAttribute AdminLoginRequest request,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {
       try {
           Admin admin = adminService.authenticateAdmin(
               request.email(),
               request.password()
           );
           session.setAttribute(AdminAuthInterceptor.ADMIN_SESSION_KEY, admin.getId());
           return "redirect:/admin/users";
       } catch (MomentException e) {
           redirectAttributes.addAttribute("error", "true");
           return "redirect:/admin/login";
       }
   }
   ```

3. **POST `/admin/logout`**: 로그아웃 처리
   ```java
   @PostMapping("/admin/logout")
   public String logout(HttpSession session) {
       session.invalidate();
       return "redirect:/admin/login";
   }
   ```

**의존성**: `AdminService`

---

### 2-10. 템플릿: 기본 레이아웃

**파일**: `server/src/main/resources/templates/admin/layout.html`

**구성 요소**:
1. **DOCTYPE & HTML 헤더**:
   ```html
   <!DOCTYPE html>
   <html xmlns:th="http://www.thymeleaf.org"
         xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout">
   ```

2. **CDN 링크**:
   - Tailwind CSS 3.4+ (CDN)
   - Lucide Icons (웹 컴포넌트)

3. **고정 좌측 사이드바** (다크 테마, 250px 너비):
   - 로고: "Moment Admin"
   - "사용자 관리" 링크 (`/admin/users`)
   - "관리자 등록" 링크 (`/admin/admins/create`)
   - 로그아웃 버튼 (`POST /admin/logout`)
   - 핵심 클래스: `fixed left-0 top-0 h-screen w-64 bg-slate-900 text-white`

4. **메인 영역** (사이드바 여백 확보):
   - 클래스: `ml-64`
   - Sticky 헤더: `sticky top-0 bg-white shadow-sm z-40`

5. **컨텐츠 영역**:
   ```html
   <main class="p-6 lg:p-8">
       <div layout:fragment="content">
           <!-- 페이지별 콘텐츠 -->
       </div>
   </main>
   ```

6. **스크립트 영역**:
   ```html
   <div layout:fragment="scripts">
       <!-- 페이지별 JS -->
   </div>
   ```

**디자인 스타일**: Soft Glass & Light Mode (권장) - `.claude/rules/thymeleaf.md` 준수
- 카드: `rounded-2xl bg-slate-50 p-6 shadow-sm border border-slate-200/50`
- 간격: `p-6`, `mb-4`, `mb-6`, `gap-3`
- 버튼: `px-6 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700`

---

### 2-11. 템플릿: 로그인 페이지

**파일**: `server/src/main/resources/templates/admin/login.html`

**특징**:
- **독립 페이지** (layout 상속 안 함)
- Tailwind CSS 스타일
- 중앙 정렬 카드 레이아웃

**디자인**:
- 배경: `bg-slate-50`
- 카드: `rounded-2xl bg-white shadow-sm border border-slate-200/50`
- 입력 필드: `px-4 py-2.5 rounded-lg border border-slate-300 focus:ring-2 focus:ring-indigo-500`
- 버튼: `px-6 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 w-full`

**구조**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Moment Admin - 로그인</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-50">
    <div class="min-h-screen flex items-center justify-center p-4">
        <div class="w-full max-w-md">
            <div class="rounded-2xl bg-white shadow-sm border border-slate-200/50 p-8">
                <!-- 헤더 -->
                <div class="text-center mb-6">
                    <h1 class="text-3xl font-bold text-gray-900">Moment Admin</h1>
                    <p class="text-sm text-gray-500 mt-2">관리자 로그인</p>
                </div>

                <!-- 에러 메시지 -->
                <div th:if="${error}" class="mb-4 p-4 rounded-lg bg-red-50 border border-red-200 text-red-700">
                    아이디 또는 비밀번호가 일치하지 않습니다.
                </div>

                <!-- 로그인 폼 -->
                <form th:action="@{/admin/api/login}" method="post" class="space-y-4">
                    <div>
                        <label for="email" class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
                        <input type="email" id="email" name="email" required
                               class="w-full px-4 py-2.5 rounded-lg border border-slate-300 bg-white
                                      focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent
                                      transition-all duration-150"
                               placeholder="admin@moment.com">
                    </div>
                    <div>
                        <label for="password" class="block text-sm font-medium text-gray-700 mb-2">비밀번호</label>
                        <input type="password" id="password" name="password" required
                               class="w-full px-4 py-2.5 rounded-lg border border-slate-300 bg-white
                                      focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent
                                      transition-all duration-150">
                    </div>
                    <button type="submit"
                            class="w-full px-6 py-2.5 rounded-lg bg-indigo-600 text-white font-medium
                                   hover:bg-indigo-700 active:scale-95 transition-all duration-150">
                        로그인
                    </button>
                </form>
            </div>
        </div>
    </div>
</body>
</html>
```

---

### 2-12. 환경 변수 설정 (Admin 초기 계정 및 세션 타임아웃)

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

**보안 원칙**:
- ⛔ 초기 관리자 정보를 절대 코드에 하드코딩하지 않음
- ✅ 환경 변수로 관리하여 리포지토리 노출 방지
- ✅ 프로덕션 배포 전 강력한 비밀번호로 변경 필수

---

### 2-13. AdminInitializer (초기 관리자 계정)

**파일**: `server/src/main/java/moment/admin/config/AdminInitializer.java`

**내용**:
```java
package moment.admin.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moment.admin.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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

**역할**: 애플리케이션 시작 시 환경 변수 기반 기본 관리자 계정 자동 생성

---

### 2-13. 관리자 등록 기능 추가 (Phase 2 확장)

상위 관리자가 웹 페이지에서 새로운 관리자를 등록할 수 있는 기능을 추가합니다.

#### 2-13-1. AdminCreateRequest DTO

**파일**: `server/src/main/java/moment/admin/dto/request/AdminCreateRequest.java`

**내용**:
```java
package moment.admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "유효한 이메일 형식이어야 합니다")
    String email,

    @NotBlank(message = "이름을 입력해주세요")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    String name,

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    String password
) {}
```

#### 2-13-2. AdminManagementController

**파일**: `server/src/main/java/moment/admin/presentation/AdminManagementController.java`

**주의**: `@Controller` 사용 (뷰 반환)

**엔드포인트**:

1. **GET `/admin/admins/create`**: 관리자 등록 페이지 렌더링
   ```java
   @GetMapping("/admin/admins/create")
   public String createAdminPage(Model model) {
       model.addAttribute("request", new AdminCreateRequest("", "", ""));
       return "admin/admins/create";
   }
   ```

2. **POST `/admin/admins`**: 관리자 등록 처리
   ```java
   @PostMapping("/admin/admins")
   public String createAdmin(@Valid @ModelAttribute("request") AdminCreateRequest request,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
       if (bindingResult.hasErrors()) {
           model.addAttribute("error", "입력 정보를 확인해주세요.");
           return "admin/admins/create";
       }

       try {
           adminService.createAdmin(request.email(), request.name(), request.password());
           redirectAttributes.addFlashAttribute("message", "관리자가 성공적으로 등록되었습니다.");
           return "redirect:/admin/users";
       } catch (MomentException e) {
           if (e.getErrorCode() == ErrorCode.ADMIN_EMAIL_CONFLICT) {
               model.addAttribute("error", "이미 등록된 이메일입니다.");
           } else {
               model.addAttribute("error", "관리자 등록에 실패했습니다.");
           }
           return "admin/admins/create";
       }
   }
   ```

**의존성**: `AdminService`

#### 2-13-3. 템플릿: 관리자 등록 페이지

**파일**: `server/src/main/resources/templates/admin/admins/create.html`

**구조**:
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{admin/layout}">
<head>
    <title>관리자 등록</title>
</head>
<body>
    <div layout:fragment="content">
        <div class="container-fluid py-4">
            <div class="row justify-content-center">
                <div class="col-md-6">
                    <div class="card shadow-sm">
                        <div class="card-header">
                            <h4 class="mb-0">새 관리자 등록</h4>
                        </div>
                        <div class="card-body p-4">
                            <!-- 에러 메시지 -->
                            <div th:if="${error}" class="alert alert-danger" role="alert">
                                <span th:text="${error}"></span>
                            </div>

                            <!-- 관리자 등록 폼 -->
                            <form th:action="@{/admin/admins}" th:object="${request}" method="post">
                                <div class="mb-3">
                                    <label for="email" class="form-label">이메일 *</label>
                                    <input type="email" class="form-control"
                                           id="email" th:field="*{email}" required>
                                    <div class="form-text">새 관리자가 로그인 시 사용할 이메일입니다.</div>
                                </div>
                                <div class="mb-3">
                                    <label for="name" class="form-label">이름 *</label>
                                    <input type="text" class="form-control"
                                           id="name" th:field="*{name}" required>
                                </div>
                                <div class="mb-3">
                                    <label for="password" class="form-label">초기 비밀번호 *</label>
                                    <input type="password" class="form-control"
                                           id="password" th:field="*{password}" required>
                                    <div class="form-text">최소 8자 이상이어야 합니다.</div>
                                </div>
                                <div class="d-flex gap-2">
                                    <button type="submit" class="btn btn-primary">
                                        <i class="bi bi-plus-circle me-1"></i>
                                        등록
                                    </button>
                                    <a th:href="@{/admin/users}" class="btn btn-secondary">
                                        취소
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

#### 2-13-4. 레이아웃 네비게이션 업데이트

**파일**: `server/src/main/resources/templates/admin/layout.html`

**사이드바에 추가**:
```html
<!-- 기존 사용자 관리 링크 -->
<li class="nav-item">
    <a class="nav-link" th:href="@{/admin/users}">
        <i class="bi bi-people me-2"></i>
        사용자 관리
    </a>
</li>

<!-- 새로 추가: 관리자 등록 링크 -->
<li class="nav-item">
    <a class="nav-link" th:href="@{/admin/admins/create}">
        <i class="bi bi-person-plus me-2"></i>
        관리자 등록
    </a>
</li>
```

---

## 📝 구현 순서 (단계별 실행)

### Step 1: 의존성 및 구조 준비
1. `build.gradle` 수정 (Thymeleaf 의존성)
2. Gradle 리로드
3. 디렉토리 생성 (templates, static, admin 패키지)

### Step 2: 데이터베이스 & 도메인
4. Flyway 마이그레이션 파일 작성 (V23)
5. `ErrorCode` enum에 A-xxx 에러 추가
6. `Admin` 엔티티 구현
7. `AdminRepository` 인터페이스 작성

### Step 3: 비즈니스 로직
8. `AdminService` 구현 (인증, 생성, 조회)
9. `AdminLoginRequest` DTO 작성

### Step 4: 인증 인프라
10. `AdminAuthInterceptor` 구현
11. `WebConfig` 업데이트 (인터셉터 등록)

### Step 5: 컨트롤러
12. `AdminAuthController` 구현 (로그인, 로그아웃)

### Step 6: 템플릿
13. `layout.html` 작성 (기본 레이아웃 + 사이드바)
14. `login.html` 작성 (로그인 페이지)

### Step 7: 초기화
15. `AdminInitializer` 작성 (초기 계정 생성)

### Step 8: 관리자 등록 기능
16. `AdminCreateRequest` DTO 작성
17. `AdminManagementController` 구현 (관리자 등록)
18. `admin/admins/create.html` 템플릿 작성
19. `layout.html` 사이드바 업데이트 (관리자 등록 링크 추가)

### Step 9: 테스트 & 검증
20. 애플리케이션 실행 → 테이블 생성 확인
21. `/admin/login` 접속 → 로그인 페이지 확인
22. `admin@moment.com / admin123` 로그인 테스트
23. 세션 인증 확인 (`/admin/users` 접근 시 리다이렉트)
24. `/admin/admins/create` 접속 → 새 관리자 등록 테스트

---

## ✅ 완료 체크리스트

### Phase 1: Foundation
- [ ] build.gradle에 Thymeleaf 의존성 추가
- [ ] Gradle 리로드 완료
- [ ] templates/admin/ 디렉토리 생성
- [ ] static/admin/ 디렉토리 생성
- [ ] admin 패키지 구조 생성

### Phase 2: Authentication
- [ ] V23 Flyway 마이그레이션 파일 작성 (email 기반)
- [ ] ErrorCode에 A-001 ~ A-004 추가
- [ ] Admin 엔티티 구현 (email, name, password)
- [ ] AdminRepository 인터페이스 작성
- [ ] AdminService 구현 (4개 메서드)
- [ ] AdminLoginRequest DTO 작성 (email, password)
- [ ] AdminAuthInterceptor 구현
- [ ] WebConfig에 인터셉터 등록
- [ ] AdminAuthController 구현 (3개 엔드포인트)
- [ ] layout.html 템플릿 작성
- [ ] login.html 템플릿 작성 (이메일 기반)
- [ ] AdminInitializer 작성 (admin@moment.com)
- [ ] AdminCreateRequest DTO 작성
- [ ] AdminManagementController 구현 (관리자 등록)
- [ ] admin/admins/create.html 템플릿 작성
- [ ] layout.html 사이드바 업데이트
- [ ] 애플리케이션 실행 및 로그인 테스트
- [ ] 새 관리자 등록 테스트

---

## 🎯 핵심 파일 경로 요약

### 생성할 파일 (15개)

#### Backend (11개)
1. `server/src/main/resources/db/migration/mysql/V23__create_admin_table__mysql.sql`
2. `server/src/main/java/moment/admin/domain/Admin.java`
3. `server/src/main/java/moment/admin/infrastructure/AdminRepository.java`
4. `server/src/main/java/moment/admin/infrastructure/AdminAuthInterceptor.java`
5. `server/src/main/java/moment/admin/service/admin/AdminService.java`
6. `server/src/main/java/moment/admin/dto/request/AdminLoginRequest.java`
7. `server/src/main/java/moment/admin/dto/request/AdminCreateRequest.java`
8. `server/src/main/java/moment/admin/presentation/AdminAuthController.java`
9. `server/src/main/java/moment/admin/presentation/AdminManagementController.java`
10. `server/src/main/java/moment/admin/config/AdminInitializer.java`

#### Frontend (4개)
11. `server/src/main/resources/templates/admin/layout.html`
12. `server/src/main/resources/templates/admin/login.html`
13. `server/src/main/resources/templates/admin/admins/create.html`

### 수정할 파일 (3개)
1. `server/build.gradle` - Thymeleaf 의존성 추가
2. `server/src/main/java/moment/global/exception/ErrorCode.java` - A-001 ~ A-004 에러 코드 추가
3. `server/src/main/java/moment/global/config/WebConfig.java` - AdminAuthInterceptor 등록

---

## 🔒 보안 고려사항

1. **세션 타임아웃**: 환경 변수(`ADMIN_SESSION_TIMEOUT`)로 관리
2. **비밀번호 해싱**: BCrypt (기존 `PasswordEncoder` 빈 재사용)
3. **XSS 방지**: Thymeleaf 자동 이스케이프
4. **CSRF**: Spring Security 없이 세션 기반 인증 사용 (단순화)
5. **초기 관리자 정보**: 절대 코드에 하드코딩하지 않고 환경 변수로 관리
6. **프로덕션 배포**: 강력한 비밀번호로 변경 필수

---

## ☁️ CloudFront 설정 (배포 환경)

### 현재 상태
- **SSL Offloading**: CloudFront에서 HTTPS 종료
- **라우팅**: `/api/*` 경로만 백엔드 서버로 터널링

### 필요한 변경사항

#### 1. CloudFront Behavior 추가

**새 Behavior 패턴**: `/admin/*`

**설정**:
```yaml
Path Pattern: /admin/*
Origin: Backend Server (ALB/EC2)
Viewer Protocol Policy: Redirect HTTP to HTTPS
Allowed HTTP Methods: GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE
Cache Policy: Managed-CachingDisabled (또는 커스텀)
Origin Request Policy: AllViewer (쿠키, 헤더 전달 필수)
```

**중요 설정**:
- ✅ **쿠키 전달**: `JSESSIONID` 쿠키를 Origin으로 전달 (세션 인증 필수)
- ✅ **헤더 전달**: `Host`, `User-Agent`, `Referer` 등
- ✅ **캐싱 비활성화**: Admin 페이지는 동적 콘텐츠이므로 캐싱 안 함

#### 2. Origin Request Policy (커스텀 정책 권장)

**Forward Cookies**: `All` 또는 `JSESSIONID` (세션 쿠키)
**Forward Headers**: `Host`, `CloudFront-Viewer-Country` (선택)
**Query Strings**: `All` (페이징, 검색 파라미터 전달)

#### 3. Cache Policy

**TTL**: `0` (캐싱 비활성화)
- Admin 페이지는 로그인 상태에 따라 다른 콘텐츠를 보여주므로 캐싱하면 안 됨

#### 4. 정적 리소스 처리 (선택 사항)

만약 `/admin/static/*` 경로를 별도로 처리하고 싶다면:

**Behavior Pattern**: `/admin/static/*`
**Cache Policy**: `CachingOptimized` (정적 리소스는 캐싱 가능)
**TTL**: `86400` (1일)

### CloudFront Distribution 예시

**Behaviors 우선순위**:
1. `/admin/static/*` → 캐싱 활성화 (정적 리소스)
2. `/admin/*` → 캐싱 비활성화 (동적 페이지)
3. `/api/*` → 캐싱 비활성화 (API)
4. `Default (*)` → S3 또는 Frontend

### Terraform 예시 (참고)

```hcl
resource "aws_cloudfront_distribution" "moment" {
  # 기존 설정...

  # Admin 페이지 Behavior
  ordered_cache_behavior {
    path_pattern     = "/admin/*"
    target_origin_id = "backend-server"

    allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods   = ["GET", "HEAD"]

    forwarded_values {
      query_string = true
      cookies {
        forward = "all"  # 세션 쿠키 전달 필수
      }
      headers = ["Host", "CloudFront-Forwarded-Proto"]
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 0
    max_ttl                = 0
  }

  # 기존 API Behavior
  ordered_cache_behavior {
    path_pattern     = "/api/*"
    target_origin_id = "backend-server"
    # ... 기존 설정
  }
}
```

### 테스트 방법

1. **CloudFront 설정 후 캐시 무효화**:
   ```bash
   aws cloudfront create-invalidation \
     --distribution-id YOUR_DISTRIBUTION_ID \
     --paths "/admin/*"
   ```

2. **브라우저에서 접속**:
   ```
   https://yourdomain.com/admin/login
   ```

3. **세션 쿠키 확인**:
   - 개발자 도구 → Application → Cookies
   - `JSESSIONID` 쿠키가 생성되는지 확인

4. **CloudFront 헤더 확인** (디버깅):
   ```
   curl -I https://yourdomain.com/admin/login
   ```

### 주의사항

1. **쿠키 전달 필수**: 세션 기반 인증이므로 `JSESSIONID` 쿠키가 Origin까지 전달되어야 함
2. **캐싱 비활성화**: Admin 페이지는 사용자별 콘텐츠이므로 캐싱하면 안 됨
3. **HTTPS 전용**: 관리자 페이지는 반드시 HTTPS로만 접근
4. **IP 화이트리스트** (선택): AWS WAF로 관리자 IP만 허용 가능

---

## 📚 참고 문서

- `.claude/rules/thymeleaf.md` - UI/UX 가이드라인
- `CLAUDE.md` - 프로젝트 아키텍처 원칙
- `admin-implementation.md` - 전체 구현 계획

---

## 📝 주요 변경사항 요약

### 관리자 구조 설계
- **로그인 ID**: 이메일 사용 (username 필드 제거)
- **필드**: `id`, `email`, `password`, `name`, `deletedAt`, `createdAt`
- **초기 계정**: `admin@moment.com / admin123` (CommandLineRunner로 자동 생성)

### 관리자 등록 흐름
1. 초기 관리자: 애플리케이션 시작 시 자동 생성
2. 추가 관리자: 기존 관리자가 `/admin/admins/create` 페이지에서 이메일 입력하여 등록
3. 등록 시 이메일 중복 검증, 비밀번호 BCrypt 해싱

### 구현 범위
- ✅ Phase 1: Thymeleaf 설정, 디렉토리 구조
- ✅ Phase 2: 인증 시스템 (로그인, 로그아웃, 세션 관리)
- ✅ Phase 2 확장: 관리자 등록 기능

---

이 계획은 기존 Moment 프로젝트의 Clean Architecture와 코딩 컨벤션을 완전히 준수하며, 이메일 기반 인증과 웹 기반 관리자 등록 기능을 제공합니다. Phase 3 (사용자 관리 기능)으로 확장 가능한 견고한 기반을 제공합니다.
