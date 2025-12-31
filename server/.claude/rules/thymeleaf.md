# 관리자 페이지 개발 가이드라인 (Modern Design)

## 기술 스택

- 프론트엔드: Thymeleaf (SSR) + Tailwind CSS + Lucide Icons
- CSS 프레임워크: Tailwind CSS 3.4+ (CDN 또는 빌드 도구)
- 아이콘: Lucide Icons (또는 Feather Icons)
- 파일 경로: `server/src/main/resources/templates/admin`

---

## 디자인 원칙

### 모던 Admin 페이지의 3가지 스타일 지침

Admin 페이지는 다음 3가지 스타일 중 **하나를 선택하여 전체 페이지에 일관되게 적용**합니다.
각 스타일은 서로 다른 톤의 사용자 경험을 제공하며, 프로젝트의 특성에 따라 선택할 수 있습니다.

#### **Style 1: Soft Glass & Light Mode (권장)**
- **설명**: 밝고 세련된 분위기, 글래스모르피즘으로 현대적 감각 표현
- **주요 특징**: 투명한 배경, 부드러운 그림자, 넓은 여백
- **색상 팔레트**:
    - 배경: `#FFFFFF` (흰색)
    - 카드 배경: `#F8FAFC` (아이스 블루)
    - 텍스트: `#1F2937` (다크 그레이)
    - 강조: `#6366F1` (인디고)
    - 보조: `#10B981` (에메랄드)
- **Tailwind 클래스**:
```
  bg-white
  bg-slate-50
  text-gray-800
  text-gray-600 (서브텍스트)
  from-indigo-500 (강조 그라디언트)
```

#### **Style 2: Dark Mode Professional**
- **설명**: 기술 중심, 야간 작업 최적화, 높은 대비로 가독성 우수
- **주요 특징**: 다크톤 배경, 네온 포인트 컬러, 세련된 테두리
- **색상 팔레트**:
    - 배경: `#0F172A` (딥 네이비)
    - 카드 배경: `#1E293B` (슬레이트)
    - 텍스트: `#F1F5F9` (화이트)
    - 강조: `#10B981` (에메랄드)
    - 보조: `#8B5CF6` (바이올렛)
- **Tailwind 클래스**:
```
  bg-slate-900
  bg-slate-800 (카드)
  text-slate-50
  text-slate-400 (서브텍스트)
  border-slate-700
```

#### **Style 3: Clean & Minimal**
- **설명**: 가장 단순하고 깔끔한 형태, 정보 전달에 집중
- **주요 특징**: 단색 기조, 최소 장식, 명확한 계층
- **색상 팔레트**:
    - 배경: `#F9FAFB` (라이트 그레이)
    - 카드 배경: `#FFFFFF` (흰색)
    - 텍스트: `#111827` (거의 검은색)
    - 강조: `#3B82F6` (블루)
    - 보조: `#6B7280` (그레이)
- **Tailwind 클래스**:
```
  bg-gray-50
  bg-white (카드)
  text-gray-900
  text-gray-600 (서브텍스트)
  border-gray-200
```

---

## UI 일관성 (Tailwind CSS 기반)

### 1. 레이아웃 & 구조

#### 페이지 템플릿 상속
- 모든 페이지는 **반드시 `admin/layout.html`** 을 확장하여 사용합니다.
- Thymeleaf 확장 방식:
```html
  <!DOCTYPE html>
  <html xmlns:th="http://www.thymeleaf.org"
        xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
        layout:decorate="~{admin/layout.html}">
  <head>
    <title>페이지 제목</title>
  </head>
  <body>
    <main layout:fragment="content">
      <!-- 페이지별 콘텐츠 -->
    </main>
  </body>
  </html>
```

#### 전체 레이아웃 구조
```html
<body class="bg-white dark:bg-slate-900">
  <!-- 사이드바: 고정, 너비 250px -->
  <aside class="fixed left-0 top-0 h-screen w-64 bg-slate-900 dark:bg-slate-950 shadow-lg">
    <!-- 사이드바 콘텐츠 -->
  </aside>

  <!-- 메인 영역: 사이드바 제외 -->
  <div class="ml-64">
    <!-- 헤더: 상단 고정 -->
    <header class="sticky top-0 bg-white dark:bg-slate-800 shadow-sm z-40">
      <!-- 헤더 콘텐츠 -->
    </header>

    <!-- 페이지 콘텐츠 -->
    <main class="p-6 lg:p-8">
      <!-- 각 페이지 콘텐츠 -->
    </main>
  </div>
</body>
```

#### 컨테이너 & 섹션
- 메인 콘텐츠는 `max-w-7xl mx-auto` 로 최대 너비를 제한합니다.
- 주요 섹션은 `section` 태그로 감싸고 `mb-6` 또는 `mb-8` 로 구분합니다.

### 2. 카드 컴포넌트 (Core Design Element)

카드는 Admin 페이지의 **핵심 시각적 요소**입니다. 모든 데이터 그룹, 폼, 통계는 카드로 표현합니다.

#### 기본 카드 스타일
```html
<!-- Soft Glass 스타일 (권장) -->
<div class="rounded-2xl bg-slate-50 p-6 shadow-sm border border-slate-200/50 hover:shadow-md transition-shadow">
  <!-- 카드 콘텐츠 -->
</div>

<!-- Dark Mode 스타일 -->
<div class="rounded-2xl bg-slate-800 p-6 shadow-sm border border-slate-700/50 hover:shadow-lg transition-shadow">
  <!-- 카드 콘텐츠 -->
</div>

<!-- Clean & Minimal 스타일 -->
<div class="rounded-lg bg-white p-6 shadow-sm border border-gray-200 hover:shadow-md transition-shadow">
  <!-- 카드 콘텐츠 -->
</div>
```

#### 카드 헤더 (제목)
```html
<div class="border-b border-slate-200 pb-4 mb-4">
  <h2 class="text-2xl font-bold text-gray-900">카드 제목</h2>
  <p class="text-sm text-gray-500 mt-1">부제목 또는 설명</p>
</div>
```

#### 호버 효과가 있는 상호작용 카드
```html
<div class="rounded-2xl bg-slate-50 p-6 shadow-sm border border-slate-200/50 
            cursor-pointer transition-all duration-200 
            hover:shadow-md hover:border-indigo-300/30 hover:bg-slate-100">
  <!-- 클릭 가능한 카드 콘텐츠 -->
</div>
```

### 3. 색상 및 버튼 정책

#### 버튼 스타일 정의
```html
<!-- Primary Button (등록, 저장, 검색) -->
<button class="px-4 py-2.5 rounded-lg bg-indigo-600 text-white font-medium 
               hover:bg-indigo-700 active:scale-95 transition-all duration-150">
  <i class="lucide lucide-plus mr-2 inline"></i> 등록
</button>

<!-- Secondary Button (취소, 뒤로가기) -->
<button class="px-4 py-2.5 rounded-lg bg-slate-200 text-gray-800 font-medium 
               hover:bg-slate-300 active:scale-95 transition-all duration-150">
  <i class="lucide lucide-x mr-2 inline"></i> 취소
</button>

<!-- Danger Button (삭제, 차단) -->
<button class="px-4 py-2.5 rounded-lg bg-red-600 text-white font-medium 
               hover:bg-red-700 active:scale-95 transition-all duration-150">
  <i class="lucide lucide-trash-2 mr-2 inline"></i> 삭제
</button>

<!-- Small Button (테이블 행동) -->
<button class="px-2 py-1.5 rounded text-sm bg-blue-500 text-white 
               hover:bg-blue-600 transition-colors">
  수정
</button>
```

#### 버튼 그룹
```html
<div class="flex gap-3 mt-6">
  <button class="px-6 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition-colors">
    저장
  </button>
  <button class="px-6 py-2.5 rounded-lg bg-slate-200 text-gray-800 hover:bg-slate-300 transition-colors">
    취소
  </button>
</div>
```

### 4. 폼 요소 (Form Components)

#### Input Field
```html
<div class="mb-4">
  <label class="block text-sm font-medium text-gray-700 mb-2">
    이메일 <span class="text-red-600">*</span>
  </label>
  <input type="email" 
         class="w-full px-4 py-2.5 rounded-lg border border-slate-300 
                bg-white text-gray-900 placeholder-gray-500
                focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent
                transition-all duration-150"
         placeholder="user@example.com"
         th:field="*{email}" />
  <p class="text-xs text-gray-500 mt-1">새 관리자 로그인용 이메일</p>
</div>
```

#### Thymeleaf Form Object Binding
```html
<form th:object="${adminForm}" th:action="@{/admin/admins/create}" method="post" class="space-y-6">
  <!-- 이메일 필드 -->
  <div class="mb-4">
    <label th:for="email" class="block text-sm font-medium text-gray-700 mb-2">
      이메일 <span class="text-red-600">*</span>
    </label>
    <input type="email" 
           th:field="*{email}"
           class="w-full px-4 py-2.5 rounded-lg border border-slate-300 bg-white
                  focus:outline-none focus:ring-2 focus:ring-indigo-500"
           placeholder="admin@example.com" />
    <!-- 에러 메시지 -->
    <p th:if="${#fields.hasErrors('email')}" 
       th:errors="*{email}" 
       class="text-xs text-red-600 mt-1"></p>
  </div>

  <!-- 다른 필드들... -->

  <!-- 버튼 그룹 -->
  <div class="flex gap-3 pt-4 border-t border-slate-200">
    <button type="submit" class="px-6 py-2.5 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700">
      등록
    </button>
    <a href="@{/admin/users}" class="px-6 py-2.5 rounded-lg bg-slate-200 text-gray-800 hover:bg-slate-300">
      취소
    </a>
  </div>
</form>
```

### 5. 테이블 (Data List)

#### 기본 테이블 스타일
```html
<div class="overflow-x-auto rounded-lg border border-slate-200">
  <table class="w-full text-sm text-left text-gray-700">
    <!-- 테이블 헤더 -->
    <thead class="bg-slate-100 border-b border-slate-200">
      <tr>
        <th class="px-6 py-3 font-semibold text-gray-900">ID</th>
        <th class="px-6 py-3 font-semibold text-gray-900">이름</th>
        <th class="px-6 py-3 font-semibold text-gray-900">이메일</th>
        <th class="px-6 py-3 font-semibold text-gray-900">작업</th>
      </tr>
    </thead>
    <!-- 테이블 바디 -->
    <tbody>
      <tr th:each="admin : ${admins}" 
          class="border-b border-slate-100 hover:bg-slate-50 transition-colors">
        <td class="px-6 py-3" th:text="${admin.id}">1</td>
        <td class="px-6 py-3" th:text="${admin.name}">홍길동</td>
        <td class="px-6 py-3" th:text="${admin.email}">admin@example.com</td>
        <td class="px-6 py-3">
          <a href="#" class="text-indigo-600 hover:text-indigo-800 text-sm font-medium">수정</a>
          <span class="text-gray-300 mx-2">|</span>
          <a href="#" class="text-red-600 hover:text-red-800 text-sm font-medium">삭제</a>
        </td>
      </tr>
    </tbody>
  </table>
</div>

<!-- 빈 상태 -->
<div th:if="${#lists.isEmpty(admins)}" class="text-center py-12">
  <i class="lucide lucide-inbox text-gray-400 w-12 h-12 mx-auto mb-3"></i>
  <p class="text-gray-500">표시할 데이터가 없습니다.</p>
</div>
```

### 6. 네비게이션 & 사이드바

#### 사이드바 구조
```html
<aside class="fixed left-0 top-0 h-screen w-64 bg-slate-900 text-white flex flex-col shadow-lg">
  <!-- 로고 영역 -->
  <div class="h-16 flex items-center px-6 border-b border-slate-800">
    <h1 class="text-xl font-bold">Moment Admin</h1>
  </div>

  <!-- 네비게이션 메뉴 -->
  <nav class="flex-1 overflow-y-auto py-4">
    <a href="@{/admin/users}" 
       class="px-6 py-3 flex items-center gap-3 text-slate-300 hover:text-white 
              hover:bg-slate-800 transition-colors">
      <i class="lucide lucide-users w-5 h-5"></i>
      <span>사용자 관리</span>
    </a>
    <a href="@{/admin/admins/create}" 
       class="px-6 py-3 flex items-center gap-3 text-slate-300 hover:text-white 
              hover:bg-slate-800 transition-colors">
      <i class="lucide lucide-user-plus w-5 h-5"></i>
      <span>관리자 등록</span>
    </a>
  </nav>

  <!-- 푸터 영역 (로그아웃) -->
  <div class="px-6 py-4 border-t border-slate-800">
    <form th:action="@{/logout}" method="post">
      <button type="submit" 
              class="w-full px-4 py-2 rounded-lg bg-red-600 text-white 
                     hover:bg-red-700 transition-colors text-sm font-medium flex items-center justify-center gap-2">
        <i class="lucide lucide-log-out w-4 h-4"></i>
        로그아웃
      </button>
    </form>
  </div>
</aside>
```

#### 현재 페이지 강조
```html
<!-- JavaScript 또는 Thymeleaf 로직으로 현재 URL 확인 -->
<a th:href="@{/admin/users}" 
   th:class="'px-6 py-3 flex items-center gap-3 transition-colors ' + 
            (${#request.requestURI.contains('/users')} ? 
             'bg-indigo-600 text-white' : 'text-slate-300 hover:text-white hover:bg-slate-800')">
  <i class="lucide lucide-users w-5 h-5"></i>
  <span>사용자 관리</span>
</a>
```

---

## Thymeleaf 코딩 컨벤션

### 0. 컨트롤러 경로 규칙 (SSR 환경)

#### `@Controller` vs `@RestController`
- **SSR (Thymeleaf) 환경에서는 반드시 `@Controller`를 사용**합니다.
- `@RestController`는 JSON/XML을 반환하는 REST API 전용입니다.
- Admin 페이지는 Thymeleaf 뷰를 렌더링하므로 `@Controller`를 사용해야 합니다.

```java
// ✅ 올바른 방법: SSR 환경
@Controller
@RequiredArgsConstructor
public class AdminAuthController {
    @GetMapping("/admin/login")
    public String loginPage(Model model) {
        return "admin/login";  // 뷰 이름 반환
    }

    @PostMapping("/admin/login")
    public String login(@Valid @ModelAttribute AdminLoginRequest request) {
        // 폼 처리 후 리다이렉트
        return "redirect:/admin/dashboard";
    }
}

// ❌ 잘못된 방법: SSR 환경에서 RestController 사용
@RestController  // JSON을 반환하므로 SSR에 부적합
public class AdminAuthController {
    @PostMapping("/admin/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // ...
    }
}
```

#### 경로 네이밍 규칙
1. **SSR 폼 처리는 같은 경로에 GET/POST 매핑**
   - GET: 폼 페이지 표시
   - POST: 폼 제출 처리
   - 예: `/admin/login` (GET & POST 모두)

2. **`/api/` 경로는 REST API 전용으로 예약**
   - SSR 폼 처리에 `/api/` 경로를 사용하지 않습니다.
   - `/admin/api/login` ❌ → `/admin/login` ✅

3. **일관된 경로 패턴 유지**
   - 리소스별로 resource-oriented URL 적용
   - 예: `/admin/accounts/new` (GET & POST)
   - **`/new` 사용** (명사) - `/create` (동사) 사용 지양

```java
// ✅ 올바른 경로 패턴
@Controller
public class AdminManagementController {
    // 새 리소스 등록 폼: /new 사용 (명사)
    @GetMapping("/admin/accounts/new")
    public String newAccountPage(Model model) {
        model.addAttribute("request", new AdminCreateRequest("", "", ""));
        return "admin/accounts/new";
    }

    @PostMapping("/admin/accounts/new")  // 같은 경로 사용 (GET과 일치)
    public String createAccount(@Valid @ModelAttribute("request") AdminCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/accounts/new";  // 에러 시 같은 경로 유지
        }
        adminService.create(request);
        return "redirect:/admin/accounts";  // 성공 시 목록으로
    }
}

// ❌ 잘못된 경로 패턴
@Controller
public class AdminManagementController {
    @GetMapping("/admin/accounts/new")
    public String newAccountPage(Model model) {
        return "admin/accounts/new";
    }

    @PostMapping("/admin/api/accounts")  // ❌ /api/ 사용 금지 & 경로 불일치
    public String createAccount(@Valid @ModelAttribute AdminCreateRequest request) {
        return "redirect:/admin/accounts";
    }
}

// ⚠️ 피해야 할 패턴 (동사 사용)
@Controller
public class AdminManagementController {
    @GetMapping("/admin/accounts/create")  // create는 동사
    @PostMapping("/admin/accounts/create")
    // /new (명사)를 사용하는 것이 더 resource-oriented
}
```

#### 인터셉터 제외 경로 설정
- GET과 POST가 같은 경로를 사용하므로, 인터셉터 제외 경로는 **하나만 지정**하면 됩니다.

```java
// ✅ 올바른 방법: 같은 경로에 GET/POST 매핑
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin/login");  // GET, POST 모두 제외됨
}

// ❌ 잘못된 방법: 불필요하게 두 경로를 제외
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/admin/**")
            .excludePathPatterns("/admin/login", "/admin/api/login");  // 중복
}
```

#### 반환 타입 규칙
- **뷰 이름 반환**: `return "admin/login";`
- **리다이렉트**: `return "redirect:/admin/dashboard";`
- **포워드**: `return "forward:/admin/error";` (드물게 사용)

```java
@Controller
public class AdminController {
    // 뷰 렌더링
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", stats);
        return "admin/dashboard";  // templates/admin/dashboard.html
    }

    // PRG 패턴 (Post-Redirect-Get)
    @PostMapping("/admin/accounts/new")
    public String createAccount(@Valid @ModelAttribute AdminCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/accounts/new";  // 에러 시 같은 폼 유지
        }
        adminService.create(request);
        redirectAttributes.addFlashAttribute("message", "등록 성공");
        return "redirect:/admin/accounts";  // 리다이렉트로 중복 제출 방지
    }
}
```

---

### 1. 폼 처리

#### DTO 바인딩 (Model Object)
```java
// Controller
model.addAttribute("adminForm", new AdminCreateRequest());
```
```html
<!-- Thymeleaf Template -->
<form th:object="${adminForm}" th:action="@{/admin/admins/create}" method="post">
  <input th:field="*{email}" />
  <input th:field="*{name}" />
  <input th:field="*{password}" />
</form>
```

#### 에러 처리
```html
<div class="mb-4">
  <label class="block text-sm font-medium text-gray-700 mb-2">이메일</label>
  <input th:field="*{email}" class="w-full px-4 py-2.5 rounded-lg border" />
  <div th:if="${#fields.hasErrors('email')}" class="text-red-600 text-xs mt-1">
    <p th:each="err : ${#fields.errors('email')}" th:text="${err}"></p>
  </div>
</div>
```

### 2. 날짜 & 시간 포맷
```html
<!-- 날짜 표시 -->
<span th:text="${#temporals.format(admin.createdAt, 'yyyy-MM-dd HH:mm')}">2024-01-15 14:30</span>

<!-- 동적 날짜 포맷 -->
<span th:text="${admin.updatedAt != null ? 
                 #temporals.format(admin.updatedAt, 'MM월 dd일') : 
                 '수정 안함'}">01월 15일</span>
```

### 3. 조건부 렌더링
```html
<!-- 데이터 존재 여부 -->
<div th:if="${not #lists.isEmpty(admins)}">
  <!-- 목록 표시 -->
</div>

<!-- 데이터 없음 -->
<div th:if="${#lists.isEmpty(admins)}" class="text-center py-12">
  <p class="text-gray-500">표시할 데이터가 없습니다.</p>
</div>

<!-- 권한 확인 -->
<button th:if="${user.role == 'ADMIN'}" class="...">관리자 기능</button>
```

### 4. 반복문 (Loop)
```html
<tr th:each="admin : ${admins}" th:with="isLast=${adminStat.last}">
  <td th:text="${adminStat.count}"></td> <!-- 1부터 시작하는 인덱스 -->
  <td th:text="${admin.name}"></td>
  <td th:class="${isLast ? 'border-b-0' : 'border-b'}"></td>
</tr>
```

### 5. URL 처리 (Context Path)
```html
<!-- 링크 생성 (권장) -->
<a th:href="@{/admin/users}">사용자 목록</a>
<a th:href="@{/admin/admins/{id}/edit(id=${admin.id})}">수정</a>

<!-- 폼 액션 -->
<form th:action="@{/admin/admins/create}" method="post">
  <!-- 폼 콘텐츠 -->
</form>

<!-- 이미지 경로 -->
<img th:src="@{/images/logo.png}" alt="로고" />
```

### 6. 프래그먼트 & 재사용 컴포넌트

#### 헤더 프래그먼트 (`fragments/header.html`)
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head></head>
<body>
  <header th:fragment="main" class="sticky top-0 bg-white shadow-sm z-40">
    <div class="h-16 flex items-center justify-between px-8 border-b border-slate-200">
      <h1 class="text-2xl font-bold text-gray-900" th:text="${pageTitle}">페이지 제목</h1>
      <div class="text-gray-600 text-sm" th:text="'로그인: ' + ${#authentication.name}"></div>
    </div>
  </header>
</body>
</html>
```

#### 페이지에서 사용
```html
<header th:replace="~{fragments/header :: main}" th:with="pageTitle='사용자 관리'"></header>
```

---

## 간격 시스템 (Spacing System)

Tailwind CSS의 8px 단위 간격 시스템을 따릅니다.

### 페이지 레벨
```
py-8      = 32px (페이지 상단/하단 여백)
px-6      = 24px (좌우 여백)
```

### 섹션 레벨
```
mb-8      = 32px (섹션 간 간격)
```

### 카드 레벨
```
p-6       = 24px (카드 내부 패딩)
rounded-2xl = 16px (카드 둥근 모서리)
```

### 폼 필드
```
mb-4      = 16px (필드 간 간격)
gap-3     = 12px (내부 요소 간 간격)
```

### 사용 예시
```html
<main class="py-8 px-6">
  <section class="mb-8">
    <div class="rounded-2xl bg-white p-6 shadow-sm">
      <div class="mb-4">
        <label class="block mb-2 text-sm font-medium">레이블</label>
        <input class="w-full px-4 py-2.5 rounded-lg border" />
      </div>
    </div>
  </section>
</main>
```

---

## 반응형 디자인 (Responsive Design)

### 화면 크기별 레이아웃
```html
<!-- 모바일: 사이드바 숨김, 풀 너비 -->
<div class="ml-0 md:ml-64">
  <!-- md (768px) 이상에서 사이드바 여백 추가 -->
</div>

<!-- 그리드 반응형 -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
  <div class="rounded-2xl bg-white p-6">카드 1</div>
  <div class="rounded-2xl bg-white p-6">카드 2</div>
</div>

<!-- 테이블 반응형 -->
<div class="overflow-x-auto">
  <table class="w-full text-sm">
    <!-- 모바일에서 자동 스크롤 -->
  </table>
</div>
```

---

## 접근성 (Accessibility)

### 색상 대비
- 텍스트와 배경의 명도 차이: 최소 4.5:1 이상
- 현재 스타일 자동 준수 (흰 배경 + 다크 텍스트 등)

### 포커스 표시
```html
<a href="#" class="text-indigo-600 hover:text-indigo-800 
                   focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2">
  링크
</a>

<button class="px-4 py-2 rounded-lg bg-indigo-600 text-white
               focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2">
  버튼
</button>
```

### 시맨틱 HTML
```html
<header>헤더</header>
<nav>네비게이션</nav>
<main>메인 콘텐츠</main>
<section>섹션</section>
<article>기사</article>
<aside>사이드바</aside>
<footer>푸터</footer>
```

### ARIA 레이블
```html
<!-- 스크린 리더용 라벨 -->
<label for="email" class="sr-only">이메일</label>
<input id="email" type="email" aria-label="이메일 주소" />

<!-- 버튼 설명 -->
<button aria-label="메뉴 열기" class="...">
  <i class="lucide lucide-menu"></i>
</button>
```

---

## 모던 디자인 체크리스트

새 페이지 작성 시 다음 항목을 확인하세요:

- [ ] `admin/layout.html` 상속 확인
- [ ] 선택한 스타일(Soft Glass/Dark/Minimal) 일관성 유지
- [ ] 모든 카드에 `rounded-2xl`, `shadow-sm` 적용
- [ ] 일관된 간격 사용 (`mb-4`, `mb-6`, `p-6` 등)
- [ ] 호버 효과 구현 (버튼, 링크, 카드)
- [ ] 부드러운 전환 효과 추가 (`transition-all`, `duration-150`)
- [ ] 반응형 클래스 적용 (`md:`, `lg:` 등)
- [ ] 접근성 고려 (색상 대비, 포커스 표시, ARIA 레이블)
- [ ] 폼 에러 메시지 표시
- [ ] 빈 상태(Empty State) 처리
- [ ] Thymeleaf URL 처리 (`@{...}`)
- [ ] Lucide Icons 적절히 활용

---

## Tailwind CSS 핵심 클래스 레퍼런스

### 색상 (Soft Glass 기준)
```
배경: bg-white, bg-slate-50, bg-gray-100
텍스트: text-gray-900, text-gray-600, text-gray-500
강조: bg-indigo-600, text-indigo-600, border-indigo-500
```

### 간격
```
p-4, p-6, p-8      (패딩)
m-4, m-6, m-8      (마진)
gap-2, gap-3, gap-4 (내부 간격)
mb-3, mb-4, mb-6   (하단 마진)
```

### 형태
```
rounded, rounded-lg, rounded-2xl    (둥근 모서리)
shadow-sm, shadow-md, shadow-lg     (그림자)
border, border-2                    (테두리)
```

### 상태
```
hover:bg-gray-100              (호버)
focus:ring-2 focus:ring-blue-500 (포커스)
active:scale-95                 (활성)
disabled:opacity-50             (비활성화)
```

### 반응형
```
md:      (768px 이상)
lg:      (1024px 이상)
xl:      (1280px 이상)
```

---

## 추가 참고 자료

- **Tailwind CSS 공식 문서**: https://tailwindcss.com/docs
- **Lucide Icons**: https://lucide.dev
- **Thymeleaf 공식 문서**: https://www.thymeleaf.org
- **색상 팔레트 선택**: https://tailwindcss.com/docs/customizing-colors

---

이 가이드라인을 따르면 **일관되고 모던한 Admin 페이지**를 구축할 수 있습니다.
각 섹션은 프로젝트의 요구사항에 따라 상황별로 적용하시기 바랍니다.
