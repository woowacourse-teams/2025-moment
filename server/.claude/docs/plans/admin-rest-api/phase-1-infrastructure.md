# Phase 1: 인프라 설정

## 목표
Admin REST API를 위한 기반 인프라 구축 (예외 처리, 응답 DTO, 인터셉터 수정)

---

## 작업 목록

### 1.1 AdminErrorCode 생성

**파일**: `server/src/main/java/moment/admin/global/exception/AdminErrorCode.java`

**구현 내용**:
```java
package moment.admin.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode {
    // 인증/인가
    LOGIN_FAILED("A-001", "관리자 로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    NOT_FOUND("A-002", "존재하지 않는 관리자입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("A-003", "관리자 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 비즈니스 규칙
    DUPLICATE_EMAIL("A-004", "이미 등록된 관리자 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_INPUT("A-005", "유효하지 않은 입력 정보입니다.", HttpStatus.BAD_REQUEST),
    CANNOT_BLOCK_SELF("A-006", "자기 자신을 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CANNOT_BLOCK_LAST_SUPER_ADMIN("A-007", "마지막 SUPER_ADMIN은 차단할 수 없습니다.", HttpStatus.BAD_REQUEST),

    // 세션
    SESSION_NOT_FOUND("A-008", "세션을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_EXPIRED("A-009", "세션이 만료되었습니다. 다시 로그인해 주세요.", HttpStatus.UNAUTHORIZED),

    // 서버 오류
    INTERNAL_SERVER_ERROR("A-500", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
```

---

### 1.2 AdminException 생성

**파일**: `server/src/main/java/moment/admin/global/exception/AdminException.java`

**구현 내용**:
```java
package moment.admin.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AdminException extends RuntimeException {
    private final AdminErrorCode errorCode;

    public AdminException(AdminErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }
}
```

---

### 1.3 AdminErrorResponse 생성

**파일**: `server/src/main/java/moment/admin/dto/response/AdminErrorResponse.java`

**구현 내용**:
```java
package moment.admin.dto.response;

import moment.admin.global.exception.AdminErrorCode;

public record AdminErrorResponse(
    String code,
    String message,
    int status
) {
    public static AdminErrorResponse from(AdminErrorCode errorCode) {
        return new AdminErrorResponse(
            errorCode.getCode(),
            errorCode.getMessage(),
            errorCode.getStatus().value()
        );
    }
}
```

---

### 1.4 AdminSuccessResponse 생성

**파일**: `server/src/main/java/moment/admin/dto/response/AdminSuccessResponse.java`

**구현 내용**:
```java
package moment.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

@Schema(description = "Admin API 성공 응답 DTO")
public record AdminSuccessResponse<T>(
    @Schema(description = "상태 코드", example = "200")
    int status,
    @Schema(description = "응답 데이터")
    T data
) {
    public static <T> AdminSuccessResponse<T> of(HttpStatus httpStatus, T data) {
        return new AdminSuccessResponse<>(httpStatus.value(), data);
    }
}
```

---

### 1.5 AdminApiExceptionHandler 생성

**파일**: `server/src/main/java/moment/admin/presentation/api/AdminApiExceptionHandler.java`

**구현 내용**:
```java
package moment.admin.presentation.api;

import lombok.extern.slf4j.Slf4j;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.global.exception.AdminErrorCode;
import moment.admin.global.exception.AdminException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice(basePackages = "moment.admin.presentation.api")
public class AdminApiExceptionHandler {

    @ExceptionHandler(AdminException.class)
    public ResponseEntity<AdminErrorResponse> handleAdminException(AdminException e) {
        AdminErrorCode errorCode = e.getErrorCode();
        log.warn("Admin API Exception: code={}, message={}",
                 errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity
            .status(e.getStatus())
            .body(AdminErrorResponse.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AdminErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getBindingResult().getAllErrors());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AdminErrorResponse.from(AdminErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<AdminErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch: {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AdminErrorResponse.from(AdminErrorCode.INVALID_INPUT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AdminErrorResponse> handleException(Exception e) {
        log.error("Unexpected error in Admin API", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(AdminErrorResponse.from(AdminErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

---

### 1.6 AdminAuthInterceptor 수정

**파일**: `server/src/main/java/moment/admin/global/interceptor/AdminAuthInterceptor.java`

**변경 사항**:
- `/api/admin/` 경로 요청 시 JSON 에러 응답 반환
- 기존 SSR 경로는 리다이렉트 유지

**수정 내용**:
```java
// 기존 import에 추가
import com.fasterxml.jackson.databind.ObjectMapper;
import moment.admin.dto.response.AdminErrorResponse;
import moment.admin.global.exception.AdminErrorCode;
import org.springframework.http.MediaType;

// preHandle 메서드 내 수정
// 세션이 없는 경우 처리
if (session == null) {
    log.warn("No session found");
    return handleUnauthorized(request, response, "No session found");
}

// 인증 실패 시 처리
try {
    sessionManager.validateAuthorized(session);
} catch (MomentException e) {
    log.warn("Admin unauthorized");
    return handleUnauthorized(request, response, "Unauthorized");
}

// DB 세션 비활성화 시 처리
if (!isActiveInDb) {
    log.warn("Session invalidated in database: sessionId={}", sessionId);
    sessionManager.invalidate(session);
    return handleUnauthorized(request, response, "SESSION_INVALIDATED");
}

// SUPER_ADMIN 권한 체크 실패 시
if (isSuperAdminOnlyPath(requestURI) && role != AdminRole.SUPER_ADMIN) {
    log.warn("Access denied to SUPER_ADMIN only path: uri={}", requestURI);
    return handleForbidden(request, response);
}

// 새로운 private 메서드 추가
private boolean handleUnauthorized(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String reason) throws Exception {
    String requestURI = request.getRequestURI();

    if (requestURI.startsWith("/api/admin/")) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.UNAUTHORIZED);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }

    // SSR 요청은 리다이렉트
    response.sendRedirect("/admin/login?error=" + reason);
    return false;
}

private boolean handleForbidden(HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
    String requestURI = request.getRequestURI();

    if (requestURI.startsWith("/api/admin/")) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        AdminErrorResponse errorResponse = AdminErrorResponse.from(AdminErrorCode.UNAUTHORIZED);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }

    response.sendRedirect("/admin/error/forbidden");
    return false;
}
```

---

### 1.7 SessionConfig 수정

**파일**: `server/src/main/java/moment/admin/global/config/SessionConfig.java`

**변경 사항**:
- `/api/admin/**` 경로에 인터셉터 적용
- `/api/admin/auth/login` 경로 제외

**수정 내용** (addInterceptors 메서드):
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/admin/**", "/api/admin/**")
            .excludePathPatterns(
                "/admin/login",
                "/admin/error/**",
                "/api/admin/auth/login"  // 추가
            );
}
```

---

### 1.8 새 DTO 생성

#### AdminLoginResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminLoginResponse.java`

```java
package moment.admin.dto.response;

import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminLoginResponse(
    Long id,
    String email,
    String name,
    AdminRole role
) {
    public static AdminLoginResponse from(Admin admin) {
        return new AdminLoginResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole()
        );
    }
}
```

#### AdminMeResponse

**파일**: `server/src/main/java/moment/admin/dto/response/AdminMeResponse.java`

```java
package moment.admin.dto.response;

import java.time.LocalDateTime;
import moment.admin.domain.Admin;
import moment.admin.domain.AdminRole;

public record AdminMeResponse(
    Long id,
    String email,
    String name,
    AdminRole role,
    LocalDateTime createdAt
) {
    public static AdminMeResponse from(Admin admin) {
        return new AdminMeResponse(
            admin.getId(),
            admin.getEmail(),
            admin.getName(),
            admin.getRole(),
            admin.getCreatedAt()
        );
    }
}
```

---

## 검증 방법

1. 애플리케이션 빌드 확인
```bash
./gradlew build
```

2. 인터셉터 동작 테스트
- 미인증 상태에서 `/api/admin/auth/me` 호출 시 401 JSON 응답 확인

---

## 완료 조건

- [ ] AdminErrorCode.java 생성
- [ ] AdminException.java 생성
- [ ] AdminErrorResponse.java 생성
- [ ] AdminSuccessResponse.java 생성
- [ ] AdminApiExceptionHandler.java 생성
- [ ] AdminAuthInterceptor.java 수정
- [ ] SessionConfig.java 수정
- [ ] AdminLoginResponse.java 생성
- [ ] AdminMeResponse.java 생성
- [ ] 빌드 성공
