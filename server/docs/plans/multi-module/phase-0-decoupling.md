# Phase 0: 사전 작업 (모놀리스 내 결합도 해소)

> Created: 2026-02-11
> Status: PLANNED
> 예상 작업 파일 수: ~10개
> 검증: 각 단계 후 `./gradlew test` 통과 확인

## 목적

모듈 분리 전에 **단일 모듈 상태에서** admin↔api 간 결합을 끊는다.
Phase 0 의 모든 변경은 현재 모놀리스에서 테스트를 통과해야 하며,
이후 Phase에서의 코드 이동을 최소 마찰로 진행하게 만든다.

---

## 0-1. V35 H2 테스트 마이그레이션 위치 확인

### 현재 상태

- MySQL: `src/main/resources/db/migration/mysql/V35__create_admin_group_logs.sql` (존재)
- H2: `src/main/resources/db/migration/h2/V35__create_admin_group_logs__h2.sql` (존재, 그러나 main/resources에 위치)
- 다른 H2 마이그레이션은 모두 `src/test/resources/db/migration/h2/`에 위치

### 작업

V35 H2 파일이 `main/resources`에 있으므로 테스트 classpath에서 접근 가능하지만, 일관성을 위해 위치를 확인만 한다. Phase 2에서 common 모듈로 이동할 때 정리.

### 검증

```bash
./gradlew test  # 현재 상태에서 admin 관련 테스트 통과 확인
```

---

## 0-2. AdminFixture dead code 정리

### 현재 상태

- 파일: `src/test/java/moment/fixture/AdminFixture.java`
- grep 결과: **AdminFixture를 import하는 테스트 클래스 없음** (자기 자신만 매칭)
- Admin 테스트들은 직접 `new Admin(...)` 등으로 생성 중

### 작업

AdminFixture는 dead code가 아닌 것으로 판단 — admin 테스트에서 향후 사용 가능성이 있으므로 **유지**.
단, DTO 메서드(`createAdminCreateRequest`, `createAdminCreateRequestByEmail`, `createAdminLoginRequest`)를 별도 파일로 분리.

### 변경 파일

1. **새 파일 생성**: `src/test/java/moment/fixture/AdminRequestFixture.java`

```java
package moment.fixture;

import moment.admin.dto.request.AdminCreateRequest;
import moment.admin.dto.request.AdminLoginRequest;
import java.util.UUID;

public class AdminRequestFixture {

    public static AdminCreateRequest createAdminCreateRequest() {
        return new AdminCreateRequest(getEmail(), getName(), "password123!@#");
    }

    public static AdminCreateRequest createAdminCreateRequestByEmail(String email) {
        return new AdminCreateRequest(email, getName(), "password123!@#");
    }

    public static AdminLoginRequest createAdminLoginRequest(String email, String password) {
        return new AdminLoginRequest(email, password);
    }

    private static String getEmail() {
        return String.format("%s@admin.com", UUID.randomUUID());
    }

    private static String getName() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
```

2. **수정**: `src/test/java/moment/fixture/AdminFixture.java`
   - `createAdminCreateRequest()`, `createAdminCreateRequestByEmail()`, `createAdminLoginRequest()` 메서드 삭제
   - `AdminCreateRequest`, `AdminLoginRequest` import 삭제
   - 엔티티 생성 메서드만 유지 (`createAdmin`, `createSuperAdmin`, `createAdminBy*`, `createAdminsByAmount`)

### 검증

```bash
./gradlew test  # AdminFixture 변경 후 테스트 통과 확인
```

---

## 0-3. AdminGroupApiController: SuccessResponse → AdminSuccessResponse 교체

### 현재 상태

- 파일: `src/main/java/moment/admin/presentation/api/AdminGroupApiController.java`
- `moment.global.dto.response.SuccessResponse` import 사용 중
- 18개 메서드에서 `SuccessResponse.of(...)` 호출

### 작업

모든 반환 타입과 호출을 `AdminSuccessResponse`로 변경.

### 변경 내용

```
import moment.global.dto.response.SuccessResponse;
→
import moment.admin.dto.response.AdminSuccessResponse;
```

메서드별 교체 (18곳):

| 메서드 | 변경 전 | 변경 후 |
|--------|---------|---------|
| `getGroupStats` | `SuccessResponse<AdminGroupStatsResponse>` | `AdminSuccessResponse<AdminGroupStatsResponse>` |
| `getGroupList` | `SuccessResponse<AdminGroupListResponse>` | `AdminSuccessResponse<AdminGroupListResponse>` |
| `getGroupDetail` | `SuccessResponse<AdminGroupDetailResponse>` | `AdminSuccessResponse<AdminGroupDetailResponse>` |
| `getApprovedMembers` | `SuccessResponse<AdminGroupMemberListResponse>` | `AdminSuccessResponse<AdminGroupMemberListResponse>` |
| `getPendingMembers` | `SuccessResponse<AdminGroupMemberListResponse>` | `AdminSuccessResponse<AdminGroupMemberListResponse>` |
| `updateGroup` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `deleteGroup` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `restoreGroup` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `approveMember` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `rejectMember` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `kickMember` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `transferOwnership` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `getInviteLink` | `SuccessResponse<AdminGroupInviteLinkResponse>` | `AdminSuccessResponse<AdminGroupInviteLinkResponse>` |
| `getGroupLogs` | `SuccessResponse<AdminGroupLogListResponse>` | `AdminSuccessResponse<AdminGroupLogListResponse>` |
| `getMoments` | `SuccessResponse<AdminMomentListResponse>` | `AdminSuccessResponse<AdminMomentListResponse>` |
| `deleteMoment` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |
| `getComments` | `SuccessResponse<AdminCommentListResponse>` | `AdminSuccessResponse<AdminCommentListResponse>` |
| `deleteComment` | `SuccessResponse<Void>` | `AdminSuccessResponse<Void>` |

### 검증

```bash
./gradlew test  # admin E2E 테스트에서 응답 구조 변경 없는지 확인
```

---

## 0-4. WebConfig 분리 (CORS 정책 차별화 포함)

### 현재 상태

- 파일: `src/main/java/moment/global/config/WebConfig.java`
- `AuthService` (api용)와 `AdminAuthInterceptor` (admin용)를 모두 의존
- CORS: `allowedOriginPatterns("*")` + `allowCredentials(true)` — CSRF 취약

### 작업

WebConfig를 삭제하고 ApiWebConfig + AdminWebConfig 2개로 분리.

### 삭제

- `src/main/java/moment/global/config/WebConfig.java`

### 생성 1: `src/main/java/moment/global/config/ApiWebConfig.java`

```java
package moment.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import moment.auth.application.AuthService;
import moment.auth.presentation.LoginUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ApiWebConfig implements WebMvcConfigurer {

    private final AuthService authService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(authService));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

> **Note**: CORS 도메인 제한은 멀티모듈 분리 후 별도 이슈로 처리.
> 현재는 기존과 동일한 `allowedOriginPatterns("*")` 유지하여 기능 변경 없이 구조만 분리.

### 생성 2: `src/main/java/moment/admin/global/config/AdminWebConfig.java`

```java
package moment.admin.global.config;

import lombok.RequiredArgsConstructor;
import moment.admin.global.interceptor.AdminAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminWebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/admin/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

### 검증

```bash
./gradlew test  # CORS + Interceptor + ArgumentResolver 동작 검증
```

---

## 0-5. SwaggerConfig 분리

### 현재 상태

- 파일: `src/main/java/moment/global/config/SwaggerConfig.java`
- API 태그(1~5번)와 Admin 태그(6번)가 하나의 설정에 혼재

### 삭제

- `src/main/java/moment/global/config/SwaggerConfig.java`

### 생성 1: `src/main/java/moment/global/config/ApiSwaggerConfig.java`

```java
package moment.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://dev.connectingmoment.com", description = "개발 서버"),
                @Server(url = "https://connectingmoment.com", description = "운영 서버")
        }
)
public class ApiSwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("모멘트 API 문서")
                        .version("v1")
                        .description("모멘트 REST API 명세입니다.")
                )
                .tags(List.of(
                        new Tag().name("Auth API").description("인증/인가 관련 API 명세"),
                        new Tag().name("User API").description("사용자 관련 API 명세"),
                        new Tag().name("MyPage API").description("마이페이지 API 명세"),
                        new Tag().name("Moment API").description("모멘트 관련 API 명세"),
                        new Tag().name("Comment API").description("Comment 관련 API 명세"),
                        new Tag().name("Report API").description("신고 관련 API 명세"),
                        new Tag().name("Notification API").description("알림 관련 API 명세"),
                        new Tag().name("Push Notification API").description("푸시 알림 관련 API 명세"),
                        new Tag().name("Group API").description("그룹 관리 관련 API 명세"),
                        new Tag().name("Group Invite API").description("그룹 초대 관련 API 명세"),
                        new Tag().name("Group Member API").description("그룹 멤버 관리 관련 API 명세"),
                        new Tag().name("Group Member Approval API").description("그룹 멤버 승인/강퇴/소유권 이전 관련 API 명세"),
                        new Tag().name("Group Moment API").description("그룹 모멘트 관련 API 명세"),
                        new Tag().name("Group Comment API").description("그룹 코멘트 관련 API 명세"),
                        new Tag().name("Storage API").description("S3 저장소 API 명세"),
                        new Tag().name("Block API").description("사용자 차단 관련 API 명세")
                ));
    }
}
```

### 생성 2: `src/main/java/moment/admin/global/config/AdminSwaggerConfig.java`

```java
package moment.admin.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminSwaggerConfig {

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("모멘트 Admin API 문서")
                        .version("v1")
                        .description("모멘트 관리자 REST API 명세입니다.")
                )
                .tags(List.of(
                        new Tag().name("Admin Auth API").description("관리자 인증 API"),
                        new Tag().name("Admin Account API").description("관리자 계정 관리 API (SUPER_ADMIN 전용)"),
                        new Tag().name("Admin User API").description("관리자용 사용자 관리 API"),
                        new Tag().name("Admin Group API").description("관리자용 그룹 관리 API"),
                        new Tag().name("Admin Session API").description("관리자 세션 관리 API (SUPER_ADMIN 전용)")
                ));
    }
}
```

> **주의**: 현재 모놀리스에서는 두 OpenAPI Bean이 충돌할 수 있다.
> `@Primary` 또는 Bean 이름 분리로 해결. 모듈 분리 후에는 각 모듈에 하나의 OpenAPI만 존재하므로 충돌 없음.
> **임시 해결**: 모놀리스 단계에서는 `openAPI`와 `adminOpenAPI`로 Bean 이름 분리.

### 검증

```bash
./gradlew test
# + 수동 확인: http://localhost:8080/swagger-ui.html 접근하여 태그 목록 정상 출력
```

---

## 0-6. 테스트 픽스쳐에서 DTO 의존 메서드 분리

### UserFixture 분리

**현재 상태**: `src/test/java/moment/fixture/UserFixture.java`에 엔티티 + DTO 메서드 혼재

**DTO 의존 메서드 (api 전용)**:
- `createUserCreateRequest()` → `UserCreateRequest` (DTO)
- `createUserCreateRequestByEmail(String)` → `UserCreateRequest`
- `createUserCreateRequestByNickname(String)` → `UserCreateRequest`
- `createUserCreateRequestByPassword(String, String)` → `UserCreateRequest`

**생성**: `src/test/java/moment/fixture/UserRequestFixture.java`

```java
package moment.fixture;

import moment.user.dto.request.UserCreateRequest;
import java.util.UUID;

public class UserRequestFixture {

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(getEmail(), "password123!@#", "password123!@#", getNickname());
    }

    public static UserCreateRequest createUserCreateRequestByEmail(String email) {
        return new UserCreateRequest(email, "password123!@#", "password123!@#", getNickname());
    }

    public static UserCreateRequest createUserCreateRequestByNickname(String nickname) {
        return new UserCreateRequest(getEmail(), "password", "password", nickname);
    }

    public static UserCreateRequest createUserCreateRequestByPassword(String password, String checkedPassword) {
        return new UserCreateRequest(getEmail(), password, checkedPassword, getNickname());
    }

    private static String getEmail() {
        return String.format("%s@email.com", UUID.randomUUID());
    }

    private static String getNickname() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
```

**수정**: `src/test/java/moment/fixture/UserFixture.java`
- 위 4개 메서드 삭제
- `UserCreateRequest` import 삭제
- 엔티티 생성 메서드만 유지

**수정**: UserRequestFixture를 사용하도록 기존 테스트 코드 수정
- `UserFixture.createUserCreateRequest()` → `UserRequestFixture.createUserCreateRequest()`
- grep으로 사용처 찾기:

```bash
grep -r "UserFixture.createUserCreateRequest" src/test/ --include="*.java" -l
```

각 사용처의 import를 `UserRequestFixture`로 변경.

### 검증

```bash
./gradlew test  # 모든 테스트 통과 확인
```

---

## 0-7. Phase 0 완료 검증

```bash
./gradlew test  # 전체 테스트 통과 확인
git add -A && git commit -m "refactor: Phase 0 - 모듈 분리 사전 작업 (결합도 해소)"
git tag phase-0-complete
```

### Phase 0 변경 파일 체크리스트

| 작업 | 파일 | 변경 유형 |
|------|------|-----------|
| 0-2 | `fixture/AdminRequestFixture.java` | 새 파일 |
| 0-2 | `fixture/AdminFixture.java` | DTO 메서드 삭제 |
| 0-3 | `admin/presentation/api/AdminGroupApiController.java` | SuccessResponse → AdminSuccessResponse |
| 0-4 | `global/config/WebConfig.java` | **삭제** |
| 0-4 | `global/config/ApiWebConfig.java` | 새 파일 |
| 0-4 | `admin/global/config/AdminWebConfig.java` | 새 파일 |
| 0-5 | `global/config/SwaggerConfig.java` | **삭제** |
| 0-5 | `global/config/ApiSwaggerConfig.java` | 새 파일 |
| 0-5 | `admin/global/config/AdminSwaggerConfig.java` | 새 파일 |
| 0-6 | `fixture/UserRequestFixture.java` | 새 파일 |
| 0-6 | `fixture/UserFixture.java` | DTO 메서드 삭제 |
| 0-6 | 기존 테스트 파일들 | import 변경 |
